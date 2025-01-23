package be.anticair.anticairapi.keycloak.controller;


import be.anticair.anticairapi.Class.Listing;
import be.anticair.anticairapi.Class.ListingWithPhotosDto;
import be.anticair.anticairapi.Class.PhotoAntiquity;
import be.anticair.anticairapi.PaypalConfig;
import be.anticair.anticairapi.enumeration.AntiquityState;
import be.anticair.anticairapi.keycloak.service.ListingService;
import be.anticair.anticairapi.keycloak.service.PhotoAntiquityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.api.payments.Invoice;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * REST Controller for managing listing in the database.
 * @author Blommaert Youry, Neve Thierry, Zarzycki Alexis
 */
@RestController
@RequestMapping("/api/listing")
public class ListingController {
    @Autowired
    private ListingService listingService;
    @Autowired
    private PhotoAntiquityService photoAntiquityService;
    @Autowired
    private PaypalConfig paypalConfig;


    /**
     * Updates an existing antiquity and its associated photos.
     *
     * <p>This endpoint allows updating an antiquity's details (such as price, description, title, etc.)
     * and optionally uploading new photos. The antiquity details are provided as a JSON string,
     * which is deserialized into a {@link Listing} object. The photos are provided as a list of
     * {@link MultipartFile} objects. If photos are provided, they are updated alongside the antiquity details.</p>
     *
     * @param id the ID of the antiquity to update
     * @param antiquityJson a JSON string representing the new details of the antiquity
     * @param images a list of {@link MultipartFile} objects representing the new photos to associate with the antiquity (optional)
     * @return a {@link ResponseEntity} containing a message indicating success or failure
     *
     * @author Neve Thierry
     * @see ListingService#updateListing(Long, Listing)
     * @see PhotoAntiquityService#updatePhotos(Integer, List)
     * @see Listing
     * @see MultipartFile
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String,String>> updateAntiquityWithPhotos(@PathVariable Integer id, @RequestParam("antiquity") String antiquityJson, @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        try {
            // Deserialize the antiquity JSON into a Listing object
            ObjectMapper objectMapper = new ObjectMapper();
            Listing antiquity = objectMapper.readValue(antiquityJson, Listing.class);

            // Call the Listing service and the Images service to update the antiquity
            if(antiquity != null){
                listingService.updateListing(Long.valueOf(id), antiquity);
            }
            if(images!=null){
                photoAntiquityService.updatePhotos(id, images);
            }

            Map<String, String> responseMessage = new HashMap<>();
            responseMessage.put("message", "Antiquity updated successfully");
            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            Map<String, String> responseMessage = new HashMap<>();
            responseMessage.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(responseMessage);
        }
    }


    /**
     * Create a new listing in the database.
     *
     * @param email The email of the user creating the listing.
     * @param title The title of the listing.
     * @param description The description of the listing.
     * @param price The price of the listing.
     * @param photos The images associated with the listing.
     * @return ResponseEntity indicating the creation status.
     * @author Blommaert Youry
     */
    @PostMapping("/create")
    public ResponseEntity<?> createListing(
            @RequestParam("email") String email,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos) {

        try {
            // Create a new Listing object
            Listing newListing = new Listing();
            newListing.setTitleAntiquity(title);
            newListing.setDescriptionAntiquity(description);
            newListing.setPriceAntiquity(price);

            // call the Listing service to create the listing
            Listing createdListing = listingService.createListing(email, newListing, photos);

            // Return the created listing
            return ResponseEntity.status(HttpStatus.CREATED).body(createdListing);

        } catch (IllegalArgumentException e) {
            // Manage missing required fields
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RuntimeException e) {
            // Manage generic exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());

        } catch (Exception e) {
            // Manage unexpected exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    /**
     * Retrieves a listing by its ID along with its associated photos.
     *
     * <p>This endpoint retrieves a listing and its associated photos using the given ID.
     * If the listing is found, it returns a {@link ListingWithPhotosDto} object in the response.
     * If no listing is found with the provided ID, it returns a {@link HttpStatus#NOT_FOUND} response.</p>
     *
     * @param id the ID of the listing to retrieve
     * @return a {@link ResponseEntity} containing a {@link ListingWithPhotosDto} object if found,
     *         or a {@link HttpStatus#NOT_FOUND} status if the listing is not found
     *
     * @author Neve Thierry
     * @see ListingService#getListingById(Integer)
     * @see ListingWithPhotosDto
     */
    @GetMapping("/{id}")
    public ResponseEntity<ListingWithPhotosDto> getListingById(@PathVariable Integer id) {
        try {
            ListingWithPhotosDto response = listingService.getListingById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    /**
     * Get all listings in the database.
     *
     * @return ResponseEntity containing a list of all listings.
     * @author Blommaert Youry
     */
    @GetMapping("/checked")
    public ResponseEntity<List<Listing>> getAllListingsChecked() {
        try {
            List<Listing> response = listingService.getAllListingsAccepted();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Reject an antiquity and send mails.
     *
     * @param otherInformation map which containt the review and the id of the antiquity
     * @author Verly Noah
     * @return ResponseEntity containing a Map <String, String>, with the message to know the result
     */
    @PreAuthorize("hasAuthority('ROLE_Antiquarian')")
    @PutMapping("/rejectAntiquity")
    public ResponseEntity<Map<String,String>> rejectAntiquity(@RequestBody Map<String, String> otherInformation) {
        Listing rejectedAntiquity= this.listingService.rejectAntiquity(otherInformation);
        Map<String, String> responseMessage = new HashMap<>();
        if(rejectedAntiquity != null){
            responseMessage.put("message", "Antiquity has been rejected");
            return ResponseEntity.ok(responseMessage);
        }
        responseMessage.put("message", "Error while rejecting antiquity");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMessage);
    }

    /**
     * Accept an antiquity,  send mails adn apply commission.
     *
     * @param otherInformation map which containt the id of the antiquity
     * @author Verly Noah
     * @return ResponseEntity containing a Map <String, String>, with the message to know the result
     */
    @PreAuthorize("hasAuthority('ROLE_Antiquarian')")
    @PutMapping("/acceptAntiquity")
    public ResponseEntity<Map<String,String>> acceptAntiquity(@RequestBody Map<String, String> otherInformation) {
        Listing acceptAntiquity= this.listingService.acceptAntiquity(otherInformation);
        Map<String, String> responseMessage = new HashMap<>();
        if(acceptAntiquity != null){
            responseMessage.put("message", "Antiquity has been accepted");
            return ResponseEntity.ok(responseMessage);
        }
        responseMessage.put("message", "Error while rejecting antiquity");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMessage);
    }


    /**
     * Handles the purchase process for a specific listing by its ID.
     * This method creates a payment authorization request using PayPal
     * and returns a payment approval URL for the user.
     *
     * @param id The unique identifier of the listing to be purchased.
     * @return A ResponseEntity containing the payment approval URL if successful,
     *         or an error message and HTTP status code if the operation fails.
     * @author Zarzycki Alexis
     */
    @PostMapping("/{id}/buy")
    public ResponseEntity<?> buyListing(@PathVariable Integer id) {
        Map<String, String> responseMessage = new HashMap<>();
        try {
            ListingWithPhotosDto listing = listingService.getListingById(id);
            if (listing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Listing not found");
            }

            if(listing.getState() == AntiquityState.SOLD.getState()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Listing already sold");
            }

            Payment payment = paypalConfig.createPayment(
                    listing.getPriceAntiquity(),
                    "EUR",
                    "paypal",
                    "Sale",
                    "Buying an antiquity : " + listing.getTitleAntiquity(),
                    "http://localhost:4200/payment/success",
                    "http://localhost:4200/payment/error",
                    listing.getIdAntiquity()
            );
            return ResponseEntity.ok(payment.getLinks().stream()
                    .filter(link -> "approval_url".equals(link.getRel()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No approval URL generated"))
                    .getHref());
        } catch (Exception e) {
            responseMessage.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMessage);
        }
    }

    /**
     * Executes a payment transaction using PayPal API. This method verifies the payment status,
     * retrieves custom metadata related to the transaction, and updates the associated listing state
     * to "sold" if the payment is approved.
     *
     * @param paymentId the unique identifier of the PayPal payment
     * @param payerId the unique identifier of the payer provided by PayPal
     * @return a ResponseEntity containing a success message with the listingId if the payment is
     *         successfully processed, or an error message if the payment fails or encounters an exception
     * @author Zarzycki Alexis
     */
    @GetMapping("/payment/execute")
    public ResponseEntity<?> executePayment(@RequestParam("paymentId") String paymentId,
                                            @RequestParam("PayerID") String payerId) {
        Map<String, String> responseMessage = new HashMap<>();
        try {
            Payment payment = paypalConfig.executePayment(paymentId, payerId);

            // Check the payment state
            if ("approved".equals(payment.getState())) {
                String listingIdCustomField = payment.getTransactions()
                        .get(0)
                        .getCustom();

                if (listingIdCustomField == null || listingIdCustomField.isEmpty()) {
                    throw new IllegalArgumentException("Listing ID missing in payment metadata.");
                }
                Transaction transaction = payment.getTransactions().get(0);
                Payer payer = payment.getPayer();
                String invoiceDescription = transaction.getDescription();
                Double amount = Double.valueOf(transaction.getAmount().getTotal());
                String currency = transaction.getAmount().getCurrency();
                int quantity = 1;
                Long listingId = Long.valueOf(listingIdCustomField);

                Invoice invoice = paypalConfig.createAndSendInvoice(
                        payer,
                        invoiceDescription,
                        amount,
                        currency,
                        quantity,
                        listingId
                );

                if (invoice == null) {
                    responseMessage.put("message", "Failed to create or send the invoice.");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMessage);
                }

                listingService.markAsSold(listingId);
                responseMessage.put("message", "Payment successful and listing marked as sold");
                responseMessage.put("listingId", listingId.toString());
                responseMessage.put("invoiceNumber", invoice.getId());
                return ResponseEntity.ok(responseMessage);
            }
            responseMessage.put("message", "Payment not approved");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage);
        } catch (PayPalRESTException e) {
            responseMessage.put("message", "PayPal error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMessage);
        } catch (Exception e) {
            responseMessage.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMessage);
        }
    }

    /**
     * Endpoint to retrieve all antiquities associated with a specific antiquarian that have a state of 0 or 1.
     *
     * <p>This method processes the antiquities linked to the provided antiquarian's email, retrieves the associated photos,
     * and maps the data to a DTO that includes the antiquities' details along with their photo URLs.</p>
     *
     * @param mailAntiquarian the email of the antiquarian whose antiquities are to be retrieved
     * @return a {@link ResponseEntity} containing a list of {@link ListingWithPhotosDto},
     *         where each DTO includes antiquity details and a list of associated photo URLs
     *
     * @author Neve Thierry
     * @see ListingWithPhotosDto
     * @see PhotoAntiquity
     * @see ListingService#getAntiquitiesByState(String)
     */
    @PreAuthorize("hasAuthority('ROLE_Antiquarian')")
    @GetMapping("/by-state")
    public ResponseEntity<List<ListingWithPhotosDto>> getAntiquitiesByState(@RequestParam String mailAntiquarian) {
        List<ListingWithPhotosDto> antiquitiesWithPhoto = new ArrayList<>();
        List<Listing> antiquities = listingService.getAntiquitiesByState(mailAntiquarian);
        for(Listing antiquitie : antiquities) {
            List<String> urlPhoto = new ArrayList<>();
            List<PhotoAntiquity> photo = photoAntiquityService.findByIdAntiquity(antiquitie.getIdAntiquity());
            ListingWithPhotosDto antiquityWithPhoto = new ListingWithPhotosDto();
            for(PhotoAntiquity photoAntiquity : photo) {
               urlPhoto.add(photoAntiquity.getPathPhoto()) ;
            }
            antiquityWithPhoto.setState(antiquitie.getState());
            antiquityWithPhoto.setDescriptionAntiquity(antiquitie.getDescriptionAntiquity());
            antiquityWithPhoto.setPriceAntiquity(antiquitie.getPriceAntiquity());
            antiquityWithPhoto.setTitleAntiquity(antiquitie.getTitleAntiquity());
            antiquityWithPhoto.setIdAntiquity(antiquitie.getIdAntiquity());
            antiquityWithPhoto.setMailAntiquarian(antiquitie.getMailAntiquarian());
            antiquityWithPhoto.setIsDisplay(antiquitie.getIsDisplay());
            antiquityWithPhoto.setMailSeller(antiquitie.getMailSeller());
            antiquityWithPhoto.setPhotos(urlPhoto);
            antiquitiesWithPhoto.add(antiquityWithPhoto);
        }
        return ResponseEntity.ok(antiquitiesWithPhoto);
    }

    /**
     * Retrieves a list of antiquities with their associated photos for a specific seller's email.
     *
     * This method handles a GET request to fetch all antiquities related to a given seller's email.
     * It includes the details of the antiquities along with the URLs of their associated photos.
     *
     * @param mailSeller The email address of the seller whose antiquities are being retrieved.
     * @return A ResponseEntity containing a list of `ListingWithPhotosDto` objects, each representing an antiquity
     *         with its details and associated photo URLs.
     *
     * @author Neve Thierry
     */
    @GetMapping("/byMailSeller")
    public ResponseEntity<List<ListingWithPhotosDto>> getAntiquitiesByMailSeller(@RequestParam String mailSeller) {
        List<ListingWithPhotosDto> antiquitiesWithPhoto = new ArrayList<>();
        List<Listing> antiquities = listingService.getAntiquitiesByMailSeller(mailSeller);
        for(Listing antiquitie : antiquities) {
            List<String> urlPhoto = new ArrayList<>();
            List<PhotoAntiquity> photo = photoAntiquityService.findByIdAntiquity(antiquitie.getIdAntiquity());
            ListingWithPhotosDto antiquityWithPhoto = new ListingWithPhotosDto();
            for(PhotoAntiquity photoAntiquity : photo) {
                urlPhoto.add(photoAntiquity.getPathPhoto()) ;
            }
            antiquityWithPhoto.setState(antiquitie.getState());
            antiquityWithPhoto.setDescriptionAntiquity(antiquitie.getDescriptionAntiquity());
            antiquityWithPhoto.setPriceAntiquity(antiquitie.getPriceAntiquity());
            antiquityWithPhoto.setTitleAntiquity(antiquitie.getTitleAntiquity());
            antiquityWithPhoto.setIdAntiquity(antiquitie.getIdAntiquity());
            antiquityWithPhoto.setMailAntiquarian(antiquitie.getMailAntiquarian());
            antiquityWithPhoto.setIsDisplay(antiquitie.getIsDisplay());
            antiquityWithPhoto.setMailSeller(antiquitie.getMailSeller());
            antiquityWithPhoto.setPhotos(urlPhoto);
            antiquitiesWithPhoto.add(antiquityWithPhoto);
        }
        return ResponseEntity.ok(antiquitiesWithPhoto);
    }

    /**
     * Updates the 'isDisplay' field of a listing to false based on the provided ID.
     *
     * This method handles a PUT request to update the `isDisplay` field of a listing. It calls the service
     * layer to perform the update and returns the updated listing in the response body.
     *
     * @param id The ID of the listing to update.
     * @return A ResponseEntity containing the updated `Listing` object with the `isDisplay` field set to `false`.
     *
     * @author Neve Thierry
     */
    @PutMapping("/isDisplay/{id}")
    public ResponseEntity<Listing> updateIsDisplay(@PathVariable Long id){
        Listing listing = listingService.updateIsDisplay(id);
        return ResponseEntity.ok(listing);
    }
}
