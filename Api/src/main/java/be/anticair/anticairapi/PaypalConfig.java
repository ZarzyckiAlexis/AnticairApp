package be.anticair.anticairapi;

import be.anticair.anticairapi.Class.Listing;
import be.anticair.anticairapi.Class.ListingWithPhotosDto;
import be.anticair.anticairapi.enumeration.TypeOfMail;
import be.anticair.anticairapi.keycloak.service.EmailService;
import be.anticair.anticairapi.keycloak.service.ListingRepository;
import be.anticair.anticairapi.keycloak.service.ListingService;
import be.anticair.anticairapi.keycloak.service.UserService;
import com.paypal.api.payments.*;
import com.paypal.api.payments.Currency;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.api.payments.Invoice;
import com.paypal.api.payments.InvoiceItem;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import org.keycloak.admin.client.Keycloak;

/**
 * Configuration for Paypal
 */
@Service
public class PaypalConfig {

    private final Keycloak keycloak;
    private final UserService userService;
    private final ListingService listingService;
    private final EmailService emailService;

    /**
     * Paypal Configuration
     * @param keycloak the keycloak
     * @param userService the user service
     * @param listingService the listing service
     * @param emailService the email service
     */
    @Autowired
    public PaypalConfig(Keycloak keycloak, UserService userService, ListingService listingService, EmailService emailService) {
        this.keycloak = keycloak;
        this.userService = userService;
        this.listingService = listingService;
        this.emailService = emailService;
    }

    // PayPal client ID, configured in application properties
    @Value("${paypal.client.id}")
    private String clientId;

    // PayPal client secret, configured in application properties
    @Value("${paypal.client.secret}")
    private String clientSecret;

    // PayPal mode ("sandbox" for test purposes or "live" for production)
    @Value("${paypal.mode}")
    private String mode;

    // API Context: Responsible for authenticating and managing PayPal API requests
    private APIContext apiContext;

    @Autowired
    private ListingRepository listingRepository;

    /**
     * Lazy loads and initializes the APIContext object.
     * Ensures that the mode is either "sandbox" or "live" to avoid PayPal API errors.
     *
     * @return APIContext instance
     * @Author Zarzycki Alexis
     */
    private APIContext getApiContext() {
        if (this.apiContext == null) {
            // Validate the mode before creating the APIContext
            if (!"sandbox".equalsIgnoreCase(mode) && !"live".equalsIgnoreCase(mode)) {
                throw new IllegalArgumentException("Invalid PayPal mode: The mode must be either 'sandbox' or 'live'.");
            }

            // Initialize the API context with the client ID, secret, and mode
            this.apiContext = new APIContext(clientId, clientSecret, mode);
        }
        return this.apiContext;
    }

    /**
     * Creates a PayPal Payment object with specific details such as price, currency, payment method, etc.
     * The payment object is sent to PayPal for processing.
     *
     * @param total       The total amount to be paid
     * @param currency    The currency (e.g., USD, EUR)
     * @param method      Payment method (must be "paypal")
     * @param intent      Payment intent (e.g., "sale" or "authorize")
     * @param description A description of the payment/message to the payer
     * @param successUrl  The callback URL for successful payment
     * @param cancelUrl   The callback URL for canceled payment
     * @param listingId   Custom ID to track the associated transaction (e.g., a product or order ID)
     * @return A Payment object created via PayPal APIs
     * @throws PayPalRESTException In case of communication error with PayPal
     * @Author Zarzycki Alexis
     */
    public Payment createPayment(Double total, String currency, String method, String intent,
                                 String description, String successUrl, String cancelUrl, Integer listingId) throws PayPalRESTException {

        // Set the payment amount and format it to 2 decimal places for PayPal
        Amount amount = new Amount();
        amount.setCurrency(currency);

        // Force format to US standards to prevent PayPal issues
        amount.setTotal(String.format(java.util.Locale.US, "%.2f", total));

        // Configure transaction details
        Transaction transaction = new Transaction();
        transaction.setDescription(description); // Transaction description
        transaction.setCustom(String.valueOf(listingId)); // Set a custom ID to track listing details
        transaction.setAmount(amount); // Associate amount information with the transaction

        // Configure the payment object
        Payment payment = new Payment();
        payment.setIntent(intent); // Defines the intent of the payment (e.g., sale)
        payment.setPayer(new Payer().setPaymentMethod(method)); // Sets the payer and payment method
        payment.setTransactions(List.of(transaction)); // Assign transaction details to the payment

        // Set redirection URLs for success and cancel scenarios
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl); // URL to redirect if the payment is canceled
        redirectUrls.setReturnUrl(successUrl); // URL to redirect after successful payment

        payment.setRedirectUrls(redirectUrls);

        // Sends the created payment object to PayPal for processing
        return payment.create(getApiContext());
    }

    /**
     * Executes a previously approved PayPal payment using payment ID and payer ID.
     * This completes the payment process.
     *
     * @param paymentId The ID of the previously created payment
     * @param payerId   The ID of the payer who approved the payment
     * @return The Payment object containing the finalized payment details
     * @throws PayPalRESTException In case of communication or execution error
     * @Author Zarzycki Alexis
     */
    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        // Create a new payment object and set its ID
        Payment payment = new Payment();
        payment.setId(paymentId); // Associate the payment ID with the payment object

        // Create a PaymentExecution object and set the payer ID
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);

        // Execute the payment using the current API context
        return payment.execute(getApiContext(), paymentExecution);
    }

    /**
     * Creates an invoice with the given details and sends it to the specified customer email address.
     *
     * @param payer The payer
     * @param itemName The name of the item being invoiced.
     * @param itemPrice The price of the item in the specified currency.
     * @param currency The currency code for the item price.
     * @param quantity The quantity of the item.
     * @param listingId the id of the antiquity
     * @return The created and sent Invoice object.
     * @throws PayPalRESTException If an error occurs while creating or sending the invoice.
     * @Author Zarzycki Alexis
     */
    public Invoice createAndSendInvoice(Payer payer, String itemName, Double itemPrice, String currency, int quantity, Long listingId) throws PayPalRESTException, MessagingException, IOException {

        String customId = generateCustomId(listingId);
        Invoice existingInvoice = checkExistingInvoice(customId);
        if (existingInvoice != null) {

            if ("PAID".equalsIgnoreCase(existingInvoice.getStatus())) {
                return existingInvoice;
            }

            return existingInvoice;
        }

        Invoice invoice = new Invoice();

        // Configure merchant details
        MerchantInfo merchantInfo = new MerchantInfo()
                .setEmail("seller@anticairapp.sixela.be")
                .setFirstName("Alexis")
                .setLastName("Zarzycki")
                .setBusinessName("Anticair'App")
                .setPhone(new Phone().setNationalNumber("472734415").setCountryCode("BE"));

        // Configure the address
        InvoiceAddress merchantAddress = new InvoiceAddress();
        merchantAddress.setLine1("Rue Trieu Kaisin 136");
        merchantAddress.setCity("Montignies-Sur-Sambre");
        merchantAddress.setPostalCode("6061");
        merchantAddress.setCountryCode("BE");
        merchantInfo.setAddress(merchantAddress);

        invoice.setMerchantInfo(merchantInfo);

        // Validate and format item price
        if (itemPrice < 0 || itemPrice > 999999.99) {
            throw new IllegalArgumentException("Invalid item price: The value must be non-negative and up to six digits with two decimals.");
        }

        // Validate quantity
        if (quantity <= 0) {
            throw new IllegalArgumentException("Invalid quantity: The value must be greater than 0.");
        }

        // Format unit price and total amount to 2 decimals using Locale.US
        String formattedUnitPrice = String.format(Locale.US, "%.2f", itemPrice);
        String formattedTotalAmount = String.format(Locale.US, "%.2f", itemPrice * quantity);

        // Ensure total amount is valid
        if (Double.parseDouble(formattedTotalAmount) > 999999.99) {
            throw new IllegalArgumentException("Invalid total amount: Total must not exceed 999999.99.");
        }

        // Configure invoice items
        List<InvoiceItem> items = new ArrayList<>();
        InvoiceItem item = new InvoiceItem()
                .setName(itemName)
                .setQuantity(quantity)
                .setUnitPrice(new Currency()
                        .setCurrency(currency)
                        .setValue(formattedUnitPrice));
        items.add(item);
        invoice.setItems(items);

        // Configure total amount
        invoice.setTotalAmount(new Currency()
                .setCurrency(currency)
                .setValue(formattedTotalAmount));

        invoice.setNote(customId);

        BillingInfo billing = new BillingInfo()
                .setEmail(payer.getPayerInfo().getEmail())
                .setFirstName(payer.getPayerInfo().getFirstName())
                .setLastName(payer.getPayerInfo().getLastName());

        List<BillingInfo> billingInfoList = new ArrayList<>();
        billingInfoList.add(billing);
        invoice.setBillingInfo(billingInfoList);

        // Create the invoice on PayPal
        Invoice createdInvoice = invoice.create(getApiContext());

        // Send the invoice to the customer
        createdInvoice.send(getApiContext());

        // Mark the invoice as paid
        PaymentDetail paymentDetail = new PaymentDetail()
                .setMethod("PAYPAL") // Payment method is PayPal
                .setAmount(new Currency()
                        .setCurrency(currency)
                        .setValue(formattedTotalAmount)); // Total amount of the invoice

        // Ensure invoice is marked as paid only after sending
        createdInvoice.recordPayment(getApiContext(), paymentDetail);

        ListingWithPhotosDto listing = listingService.getListingById(listingId.intValue());

        // When the antiquity has been sold, we pay the antiquarian for the commission
        // We get the base price
        double basePrice = listing.getPriceAntiquity() / 1.2;
        double commissionToPay = listing.getPriceAntiquity() - basePrice;
        // When the antiquity has been sold, we pay the seller for the payment
        double sellerToPay = basePrice;

        // Format the commission to two decimal places before adding to the balance
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#.00", symbols);
        String formattedCommission = df.format(commissionToPay);
        String formattedSeller = df.format(sellerToPay);

        // Add the commission to the antiquarian's balance
        userService.addToUserBalance(listing.getMailAntiquarian(), Double.parseDouble(formattedCommission));
        // Add the commission to the seller's balance
        userService.addToUserBalance(listing.getMailSeller(), Double.parseDouble(formattedSeller));

        // We send the email to inform the antiquarian
        Map<String,String> otherInformation = new HashMap<>();
        otherInformation.put("title", listing.getTitleAntiquity());
        otherInformation.put("description", listing.getDescriptionAntiquity());
        otherInformation.put("price", listing.getPriceAntiquity().toString());
        emailService.sendHtmlEmail(listing.getMailAntiquarian(), "info@anticairapp.sixela.be", TypeOfMail.PAYMENTOFCOMMISSION, otherInformation);

        return createdInvoice;
    }

    /**
     * Checks for an existing invoice based on the provided custom ID.
     *
     * @param customId the custom identifier associated with the invoice to be searched
     * @return the matching Invoice object if found, or null if no matching invoice exists
     * @throws PayPalRESTException if an error occurs while retrieving the invoices
     * @Author Zarzycki Alexis
     */
    private Invoice checkExistingInvoice(String customId) throws PayPalRESTException {
        List<Invoice> invoices = Invoice.getAll(getApiContext()).getInvoices();

        for (Invoice invoice : invoices) {
            if (customId.equals(invoice.getNote())) {
                return invoice;
            }
        }
        return null;
    }

    /**
     * Generates a custom identifier string for a given listing ID.
     *
     * @param listingId the ID of the listing for which the custom identifier is to be generated.
     *                  Must be a non-null, positive number.
     * @return a custom identifier string in the format "Antiquity-listingId".
     * @throws IllegalArgumentException if the provided listingId is null or not a positive number.
     * @Author Zarzycki Alexis
     */
    public String generateCustomId(Long listingId) {
        if (listingId == null) {
            throw new IllegalArgumentException("Listing ID cannot be null");
        }

        if (listingId <= 0) {
            throw new IllegalArgumentException("Invalid listing ID: Must be a positive number.");
        }

        return String.format("Antiquity-%d", listingId);
    }
}