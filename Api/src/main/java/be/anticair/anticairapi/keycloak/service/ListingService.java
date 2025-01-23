package be.anticair.anticairapi.keycloak.service;

import be.anticair.anticairapi.Class.ListingWithPhotosDto;
import be.anticair.anticairapi.Class.PhotoAntiquity;
import be.anticair.anticairapi.enumeration.TypeOfMail;
import jakarta.mail.MessagingException;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

import be.anticair.anticairapi.Class.Listing;
import be.anticair.anticairapi.enumeration.AntiquityState;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static be.anticair.anticairapi.enumeration.AntiquityState.*;

/**
 * Service for performing listing-related operations.
 *
 * @author Blommaert Youry, Neve Thierry
 */
@Service
public class ListingService {

    @Autowired
    private ListingRepository listingRepository;

    public ListingService() {
    }

    @Autowired
    private ListingRepository ListingRepository;
    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private PhotoAntiquityService photoAntiquityService;

    private AntiquityState antiquityState;

    public Optional<Listing> getAntiquityById(Long id) {
        return ListingRepository.findById(id);
    }

    /**
     * Create a new listing in the database.
     *
     * @param email The email of the user creating the listing.
     * @param newListing The listing to create.
     * @param photos The photos to associate with the listing.
     * @return The created listing.
     * @author Blommaert Youry
     */
    public Listing createListing(String email, Listing newListing, List<MultipartFile> photos) throws MessagingException, IOException {
        if(newListing.getPriceAntiquity() < 0) {
            throw new IllegalArgumentException("Price is negative");
        }
        List<UserRepresentation> users = userService.getUsersByEmail(email);


        if(users.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        UserRepresentation user = userService.getUsersByEmail(email).get(0);

        // Verify that the listing has a price, description, and title
        if (newListing.getPriceAntiquity() == 0 ||
                newListing.getDescriptionAntiquity() == null ||
                newListing.getTitleAntiquity() == null) {
            throw new NullPointerException("Price, description, and title are required");
        }

        List<UserRepresentation> usersAntiquarians = userService.getUsersByGroupName("Antiquarian");
        if(usersAntiquarians.isEmpty()) {
            throw new RuntimeException("Antiquarian not found");
        }

        UserRepresentation userAntiquarian;
        // Get a random antiquarian different from the author of the listing
        do {
            Random random = new Random();
            userAntiquarian = usersAntiquarians.get(random.nextInt(usersAntiquarians.size()));
        } while (email.equals(userAntiquarian.getEmail()));

        newListing.setMailSeller(email);
        newListing.setState(NEED_TO_BE_CHECKED.getState());  // Initialized to 0 (not yet verified)
        newListing.setIsDisplay(true);  // Initialized to true
        newListing.setMailAntiquarian(userAntiquarian.getEmail());

        // Save the listing if it has all the required fields
        Listing savedListing = ListingRepository.save(newListing);
        try {
            this.emailService.sendHtmlEmail(userAntiquarian.getEmail(),"info@anticairapp.sixela.be",TypeOfMail.NEWANTIQUITY,new HashMap<>());
        }catch (Exception e) {
            throw new RuntimeException("Error sending email");
        }
        // Save the photos
        if (photos != null && !photos.isEmpty()) {
            for (MultipartFile photo : photos) {
                try {
                    // Use the PhotoAntiquityService to save the photo
                    PhotoAntiquity photoAntiquity = photoAntiquityService.createPhotoAntiquity(savedListing, photo);
                } catch (IOException e) {
                    // Make sure to delete the listing if the photo fails to save
                    throw new RuntimeException("Failed to save photo", e);
                }
            }
        }

        return savedListing;
    }

    /**
     * Get all the listing accepted in the database.
     * @return The list of all the listings accepted.
     * @Author Blommaert Youry
     */
    public List<Listing> getAllListingsAccepted() {
        List<Listing> listings = new ArrayList<>();
        listings = ListingRepository.getAllAntiquityChecked();

        if(listings.isEmpty()) {
            throw new RuntimeException("No listings found");
        }

        return listings;
    }

    /**
     * Get all the listing in the database.
     * @return The list of all the listings.
     * @Author Blommaert Youry
     */
    public List<Listing> getAllListing() {
        List<Listing> listings = new ArrayList<>();
        listings = ListingRepository.findAll();

        if(listings.isEmpty()) {
            throw new RuntimeException("No listings found");
        }

        return listings;
    }



    /**
     * Function to reject the antiquity, apply the commission and send the mail
     *
     * @param otherInformation Map that containt the id of the antiquity adn the review
     * @return the rejected antiquity
     * @author Verly Noah
     */
    public Listing rejectAntiquity(Map<String,String> otherInformation){
        if(otherInformation==null || otherInformation.isEmpty() || otherInformation.get("id").isEmpty() || otherInformation.get("note_title").isEmpty() || otherInformation.get("note_description").isEmpty() || otherInformation.get("note_price").isEmpty() || otherInformation.get("note_photo").isEmpty()) return null;
        //Get the antiquity
        Optional<Listing> antiquity = getAntiquityById(Long.valueOf(otherInformation.get("id")));
        //If empty or the state of the antiquity is different of need to be checked and accepted bu modified, return null
        //Because there are the two only state that could be rejected
        if(antiquity.isEmpty() || (antiquity.get().getState()!= NEED_TO_BE_CHECKED.getState() && antiquity.get().getState()!= ACCEPTED_BUT_MODIFIED.getState())){return null;}
        //If the antiquity state is not "NEED_TO_BE_CHECKED", don't change
        if(antiquity.get().getState()== NEED_TO_BE_CHECKED.getState()) antiquity.get().setState(REJECTED.getState());

        otherInformation.put("title",antiquity.get().getTitleAntiquity());
        otherInformation.put("description",antiquity.get().getDescriptionAntiquity());
        otherInformation.put("price", antiquity.get().getPriceAntiquity().toString());

        otherInformation.put("note_title",otherInformation.get("note_title"));
        otherInformation.put("note_description",otherInformation.get("note_description"));
        otherInformation.put("note_price",otherInformation.get("note_price"));
        otherInformation.put("note_photo",otherInformation.get("note_photo"));
        //Save the changes
        antiquity = Optional.of(this.ListingRepository.save(antiquity.get()));
        try {
            //Send the mail
            this.emailService.sendHtmlEmail(antiquity.get().getMailSeller(),"info@anticairapp.sixela.be",TypeOfMail.REJECTIONOFANTIQUITY,otherInformation);
            //return the antiquity
            return antiquity.get();
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Function to accept the antiquity, apply the commission and send the mail
     *
     * @param otherInformation Map that containt the id of the antiquity
     * @return the accepted antiquity
     * @author Verly Noah
     */
    public Listing acceptAntiquity(Map<String,String> otherInformation){
        if(otherInformation==null || otherInformation.isEmpty() || otherInformation.get("id").isEmpty() ) return null;
        //Get the antiquity
        Optional<Listing> antiquity = getAntiquityById(Long.valueOf(otherInformation.get("id")));
        if(antiquity.isEmpty() || (antiquity.get().getState()!= NEED_TO_BE_CHECKED.getState() && antiquity.get().getState()!= ACCEPTED_BUT_MODIFIED.getState())){return null;}
        int initialState = antiquity.get().getState();
        antiquity.get().setState(ACCEPTED.getState());

        otherInformation.put("title",antiquity.get().getTitleAntiquity());
        otherInformation.put("description",antiquity.get().getDescriptionAntiquity());
        otherInformation.put("price", antiquity.get().getPriceAntiquity().toString());

        //Save the changes
        antiquity = Optional.of(this.ListingRepository.save(antiquity.get()));
        try {
            //Send the mail
            this.emailService.sendHtmlEmail(antiquity.get().getMailSeller(),"info@anticairapp.sixela.be",TypeOfMail.VALIDATIONOFANANTIQUITY,otherInformation);
            //If the antiquity has never been accepted, return the antiquity with the application the commission , else just return the antiquity
            return initialState == NEED_TO_BE_CHECKED.getState() ?  this.applyCommission(Integer.valueOf(otherInformation.get("id"))) :  antiquity.get();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Updates the details of an existing listing based on the provided ID.
     *
     * <p>This method searches for a listing by its ID and updates its properties (such as price,
     * description, title, etc.) with the values provided in the {@link Listing} object. If no listing
     * is found with the given ID, a {@link RuntimeException} is thrown.</p>
     *
     * @param id the ID of the listing to be updated
     * @param updatedListing the {@link Listing} object containing the new values to update the listing
     * @return the updated {@link Listing} object after saving it to the database
     * @throws RuntimeException if no listing is found with the given ID
     *
     * @author Neve Thierry
     */
    public Listing updateListing(Long id, Listing updatedListing) throws MessagingException, IOException {
        Listing listing = ListingRepository.getReferenceById(id);
        int newState;
        switch (listing.getState()) {
            case -1 :
                newState = NEED_TO_BE_CHECKED.getState();
                this.emailService.sendHtmlEmail(updatedListing.getMailAntiquarian(),"info@anticairapp.sixela.be",TypeOfMail.NEWANTIQUITY,new HashMap<>());
                break;
            case 1 :
                newState = ACCEPTED_BUT_MODIFIED.getState();
                this.emailService.sendHtmlEmail(updatedListing.getMailAntiquarian(),"info@anticairapp.sixela.be",TypeOfMail.NEWANTIQUITY,new HashMap<>());
                break;
            case 3 :
               return listing;
            default :
                newState = updatedListing.getState();
                break;
        }
        int finalNewState = newState;
        return ListingRepository.findById(id).map(antiquity -> {
            antiquity.setPriceAntiquity(updatedListing.getPriceAntiquity());
            antiquity.setDescriptionAntiquity(updatedListing.getDescriptionAntiquity());
            antiquity.setTitleAntiquity(updatedListing.getTitleAntiquity());
            antiquity.setMailSeller(updatedListing.getMailSeller());
            antiquity.setMailAntiquarian(updatedListing.getMailAntiquarian());
            antiquity.setState(finalNewState);
            antiquity.setIsDisplay(updatedListing.getIsDisplay());
            return ListingRepository.save(antiquity);
        }).orElseThrow(() -> new RuntimeException("Antiquity not found with id: " + id));
    }


    /**
     * Retrieves a listing by its ID along with its associated photos.
     *
     * <p>This method searches for a listing by its ID and retrieves all the photos
     * associated with that listing. If no listing is found with the given ID,
     * a {@link RuntimeException} is thrown. The method returns a {@link ListingWithPhotosDto}
     * object containing the listing details and the associated photos.</p>
     *
     * @param id the ID of the listing to retrieve
     * @return a {@link ListingWithPhotosDto} object containing the listing and its associated photos
     * @throws RuntimeException if no listing is found with the given ID
     *
     *
     * @author Neve Thierry
     * @see PhotoAntiquityService#findByIdAntiquity(Integer)
     * @see ListingWithPhotosDto
     */
    public ListingWithPhotosDto getListingById(Integer id) {
        // Get the listing
        Listing listing = ListingRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Listing not found with id " + id));

        // get the images associates with the antiquity
        List<PhotoAntiquity> photos = photoAntiquityService.findByIdAntiquity(id);

        // create and return the objects
        return new ListingWithPhotosDto(listing, photos);
    }

    /**
     *Function to apply the commission
     *
     * @param id the id of the antiquity
     * @return the antiquity with the commission
     * @author Verly Noah
     */
    public Listing applyCommission(Integer id){
        //Check if the id is valid
        if(id==null ||id<1) return null;
        //Get the antiquity with the id
        Optional<Listing> listing = ListingRepository.findById(Long.valueOf(id));
        //If there isn't antiquity with this id, return null
        if(listing.isEmpty()) {return null;}
        //Applied the commission
        listing.get().applyCommission();
        //Save the change
        return ListingRepository.save(listing.get());
    }

    /**
     * Allow to change the antiquarian of the antiquity
     *
     * @param antiquity the antiquity that we want to change the antiquarian
     * @param emailNewAntiquarian the email of the new antiquarian
     * @return a boolean, true if the change has been made, false, in case of a problem
     * @author Verly Noah
     */
    public boolean changeListingAntiquarian(Listing antiquity, String emailNewAntiquarian) throws MessagingException, IOException {
        //If th antiquity is null or the email is empty, return null
        if(antiquity==null || emailNewAntiquarian.isEmpty()) return false;
        //Check if the user exist
        if(userService.getUsersByEmail(emailNewAntiquarian).getFirst() == null) {return false;}
        //Check if his account is activated
        if(!this.userService.getUserStatus(emailNewAntiquarian)) return false;
        //Change the antiquarian of the antiquity
        antiquity.setMailAntiquarian(emailNewAntiquarian);
        //Save
        ListingRepository.save(antiquity);
       //Prepare a mail with the inforamtion of the antiquity to warn the antiquarian
        Map<String,String> otherInformation = new HashMap<>();
        otherInformation.put("title", antiquity.getTitleAntiquity());
        otherInformation.put("description", antiquity.getDescriptionAntiquity());
        otherInformation.put("price", antiquity.getPriceAntiquity().toString());
        //Send the mail
        this.emailService.sendHtmlEmail(emailNewAntiquarian, "info@anticairapp.sixela.be", TypeOfMail.REDISTRIBUTEANTIQUITYNEWANTIQUARIAN, otherInformation);
        return true;
    }

    /**
     * Allow to change an antiquity to sold and pay the antiquarian
     * @param listingId the id of the Antiquity
     * @author Zarzycki Alexis
     */
    public void markAsSold(Long listingId) throws MessagingException, IOException {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        listing.setState(AntiquityState.SOLD.getState());
        listingRepository.save(listing);
    }

    /**
     * Retrieves all antiquities associated with a specific antiquarian that have a state of 0 or 2.
     *
     * <p>This method queries the database to find all antiquities that match the provided antiquarian's email
     * and whose state is either 0 or 2. The resulting list contains only the antiquities that satisfy these criteria.</p>
     *
     * @param mailAntiquarian the email of the antiquarian whose antiquities are to be retrieved
     * @return a list of {@link Listing} objects that match the filtering criteria
     *
     * @author Neve Thierry
     * @see ListingRepository#findByStateInAndMailAntiquarian(List, String)
     */

    public List<Listing> getAntiquitiesByState(String mailAntiquarian) {
        return listingRepository.findByStateInAndMailAntiquarian(Arrays.asList(0, 2), mailAntiquarian);
    }

    /**
     * Retrieves a list of visible antiquities (isDisplay = true) associated with a seller by their email.
     *
     * This method filters the antiquities retrieved by checking their `isDisplay` field and returns only those
     * that are marked as visible (isDisplay = true).
     *
     * @param mailSeller The email address of the seller for whom the antiquities are retrieved.
     * @return A list of `Listing` containing the visible antiquities associated with the seller's email.
     *
     * @author Neve Thierry
     */
    public List<Listing> getAntiquitiesByMailSeller(String mailSeller) {
        return listingRepository.getAllAntiquityDisplayByMailSeller(mailSeller);
    }

    /**
     * Updates the 'isDisplay' field of a listing to false based on the provided ID.
     *
     * This method finds the listing by its ID, and if the listing exists, it sets the `isDisplay`
     * field to `false` and saves the updated listing.
     *
     * @param id The ID of the listing to update.
     * @return The updated `Listing` object with `isDisplay` set to `false`.
     * @throws RuntimeException If the listing with the provided ID is not found.
     *
     * @author Neve Thierry
     */
    public Listing updateIsDisplay(long id) {
        Listing listing = listingRepository.findById(id).orElseThrow(() -> new RuntimeException("Entity not found"));

            listing.setIsDisplay(false);
            return ListingRepository.save(listing);


    }
}


