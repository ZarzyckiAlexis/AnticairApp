package be.anticair.anticairapi.service;

import be.anticair.anticairapi.Class.Listing;
import be.anticair.anticairapi.Class.ListingWithPhotosDto;
import be.anticair.anticairapi.enumeration.AntiquityState;
import be.anticair.anticairapi.enumeration.TypeOfMail;
import be.anticair.anticairapi.keycloak.service.EmailService;
import be.anticair.anticairapi.keycloak.service.ListingRepository;
import be.anticair.anticairapi.keycloak.service.ListingService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static be.anticair.anticairapi.enumeration.AntiquityState.ACCEPTED_BUT_MODIFIED;
import static be.anticair.anticairapi.enumeration.AntiquityState.NEED_TO_BE_CHECKED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;


/**
 * Test that verify the Listing service function's
 * @author Verly Noah
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true"
})
public class ListingServiceTests {

    /**
     * The service
     */
    @Autowired
    private ListingService listingService;
    /**
     * The repository
     */
    @Autowired
    private ListingRepository listingRepository;

    /**
     * The antiquity that will be used for the tests
     */
    private Listing listing;
    @Autowired
    private EmailService emailService;

    /**
     * The mail that will be use for the owner of the antiquity
     */
    private static final String TEST_SELLER_EMAIL = "test-user@gmail.com";
    /**
     * The mail that will be use for the owner of the antiquity
     */
    private static final String TEST_ANTIQUARIAN_EMAIL = "test-antiquarian@gmail.com";
    /**
     * The mail that will be use for the new owner of the antiquity
     */
    private static final String TEST_NEW_ANTIQUARIAN_EMAIL = "test-antiquarian2@gmail.com";

    /**
     * Function which allow to delete a antiquity
     * @param listing, the listing that will be deleted
     * @author Verly Noah
     */
    public void cleanListing(Listing listing){
        this.listingRepository.delete(listing);
    }
    /**
     * Test to check if the createListing service work
     * @author Blommaert Youry
     */
    @Test
    @DisplayName("Create listing success")
    public void testCreateListing_Success() throws MessagingException, IOException {
        listing = new Listing();
        listing.setPriceAntiquity(100.0);
        listing.setDescriptionAntiquity("A description");
        listing.setTitleAntiquity("Pandora's box");

        Listing createdListing = listingService.createListing(TEST_SELLER_EMAIL, listing, new ArrayList<>());

        assertNotNull(createdListing);
        assertNotNull(createdListing.getMailSeller());
        assertEquals(TEST_SELLER_EMAIL, createdListing.getMailSeller());
        assertTrue(listingRepository.findById((long)createdListing.getIdAntiquity()).isPresent());
        this.cleanListing(createdListing);
    }

    /**
     * Test to check if the createListing service work with a price less than 0
     * @author Blommaert Youry
     */
    @Test
    @DisplayName("Create listing with price less than 0")
    public void testCreateListing_PriceLessThanZero() {
        listing = new Listing();
        listing.setPriceAntiquity(-1.0);
        listing.setDescriptionAntiquity("A description");
        listing.setTitleAntiquity("A title");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            listingService.createListing(TEST_SELLER_EMAIL, listing, Collections.emptyList());
        });

        assertEquals("Price is negative", exception.getMessage());
    }

    /**
     * Test to check if the createListing service work with a user not in the database
     * @author Blommaert Youry
     */
    @Test
    @DisplayName("Create listing with user not found")
    public void testCreateListing_UserNotFound() {
        listing = new Listing();
        listing.setPriceAntiquity(100.0);
        listing.setDescriptionAntiquity("A description");
        listing.setTitleAntiquity("A title");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            listingService.createListing("nonexistent@example.com", listing, Collections.emptyList());
        });

        assertEquals("No users found with email: nonexistent@example.com", exception.getMessage());
    }

    /**
     * Test to check if the createListing service work with missing required fields
     * @author Blommaert Youry
     */
    @Test
    @DisplayName("Create listing with missing required fields")
    public void testCreateListing_MissingRequiredFields() {
        listing = new Listing();
        listing.setPriceAntiquity(0.0);
        listing.setDescriptionAntiquity(null);
        listing.setTitleAntiquity(null);

        Exception exception = assertThrows(NullPointerException.class, () -> {
            listingService.createListing(TEST_SELLER_EMAIL, listing, Collections.emptyList());
        });

        assertEquals("Price, description, and title are required", exception.getMessage());
    }

    /**
     * Test to check if getAllAntiquities works if the database contains listings.
     * @author Blommaert Youry
     */
    @Test
    @DisplayName("Get all antiquities success")
    public void testGetAllAntiquities() {
        this.listing = new Listing(0,100.0,"A description","Pandora's box",TEST_ANTIQUARIAN_EMAIL,0,false,TEST_SELLER_EMAIL);
        Listing listingPost = listingRepository.save(this.listing);

        List<Listing> listings = listingRepository.findAll();

        assertFalse(listings.isEmpty());
        this.cleanListing(listingPost);
    }


    /**
     * Test to check if the applyCommission service work with normal values
     * @author Verly Noah
     */
    @Test
    @DisplayName("Apply commission success")
    public void applyCommission() throws MessagingException, IOException {
        //Creation of an antiquity
        this.listing = new Listing(0,100.0,"A description","Pandora's box",TEST_ANTIQUARIAN_EMAIL,0,false,TEST_SELLER_EMAIL);
        //The photos for the antiquity
        List<MultipartFile> photos = new ArrayList<>();
        //Add the antiquity in the database
        Listing listingAdded = this.listingService.createListing(TEST_SELLER_EMAIL,listing,photos);
        //Apply the commission
        Listing listingCommissionAplied = listingService.applyCommission(listingAdded.getIdAntiquity());
        //Check if the antiquity return is right
        assertEquals(120.0,listingCommissionAplied.getPriceAntiquity());
        //Get the modified antiquity from the database
        ListingWithPhotosDto listingWithPhotosDto = this.listingService.getListingById(listingCommissionAplied.getIdAntiquity());
        //Check if the antiquity return is right
        assertEquals(120.0,listingWithPhotosDto.getPriceAntiquity());
        this.cleanListing(listingCommissionAplied);

    }

    /**
     * Test to check if the applyCommission service return null, if the id is null or under 1
     * @author Verly Noah
     */
    @Test
    @DisplayName("Apply commission null")
    public void applyCommissionNull(){
        assertNull(this.listingService.applyCommission(null));
        assertNull(this.listingService.applyCommission(0));
        assertNull(this.listingService.applyCommission(-1));

    }

    /**
     * Test to check if the changeListing antiquarian work
     * @author Verly Noah
     */
    @Test
    @DisplayName("Change antiquarian success")
    public void ChangeAntiquarianTestFromListingService() throws MessagingException, IOException {
        for (int i = 0; i < 10; i++) {
            this.listing = new Listing(0,100.0,"A description","Pandora's box",TEST_ANTIQUARIAN_EMAIL,0,false,TEST_SELLER_EMAIL);
            this.listingRepository.save(this.listing);
        }
        List<Listing> listingList = this.listingRepository.getAllAntiquityNotCheckedFromAnAntiquarian(TEST_ANTIQUARIAN_EMAIL);
        for (Listing listing : listingList) {
            this.listingService.changeListingAntiquarian(listing,TEST_NEW_ANTIQUARIAN_EMAIL);
        }
        assertEquals(0,this.listingRepository.getAllAntiquityNotCheckedFromAnAntiquarian(TEST_ANTIQUARIAN_EMAIL).size());
        for (Listing listing : listingList) {
            this.cleanListing(listing);
        }

    }

    /**
     * Test to reject an antiquity
     * @author Verly Noah
     */
    @Test
    @DisplayName("Reject antiquarian success")
    public void rejectAntiquarianTestFromListingService() throws MessagingException, IOException {
        this.listing = new Listing(0,100.0,"A description","Pandora's box",TEST_ANTIQUARIAN_EMAIL,0,false,TEST_SELLER_EMAIL);
        this.listing = this.listingRepository.save(this.listing);
        Map<String,String> otherInformation = new HashMap<>();
        otherInformation.put("title",listing.getTitleAntiquity());
        otherInformation.put("description",listing.getDescriptionAntiquity());
        otherInformation.put("price",listing.getPriceAntiquity().toString());
        otherInformation.put("id",listing.getIdAntiquity().toString());
        otherInformation.put("note_title","test");
        otherInformation.put("note_description","test");
        otherInformation.put("note_price","test");
        otherInformation.put("note_photo","test");
        this.listing = this.listingService.rejectAntiquity(otherInformation);
        assertEquals(AntiquityState.REJECTED.getState(), this.listing.getState());
        this.cleanListing(listing);
    }

    /**
     * Test to accept an antiquity
     * @author Verly Noah
     */
    @Test
    @DisplayName("Accept antiquarian success")
    public void acceptAntiquarianTestFromListingService() throws MessagingException, IOException {
        this.listing = new Listing(0,100.0,"A description","Pandora's box",TEST_ANTIQUARIAN_EMAIL, NEED_TO_BE_CHECKED.getState(), false,TEST_SELLER_EMAIL);
        this.listing = this.listingRepository.save(this.listing);
        Map<String,String> otherInformation = new HashMap<>();
        otherInformation.put("title",listing.getTitleAntiquity());
        otherInformation.put("description",listing.getDescriptionAntiquity());
        otherInformation.put("price",listing.getPriceAntiquity().toString());
        otherInformation.put("id",listing.getIdAntiquity().toString());
        this.listing = this.listingService.acceptAntiquity(otherInformation);
        assertEquals(AntiquityState.ACCEPTED.getState(), this.listing.getState());
        this.cleanListing(listing);
    }

    /**
     * Test the successful update of the `isDisplay` field to `false`.
     *
     * This test verifies that the method `updateIsDisplay` correctly sets the
     * `isDisplay` field of an existing listing to `false` and persists the changes.
     *
     * @author Neve Thierry
     */
    @Test
    @DisplayName("Successfully update the isDisplay field to false")
    void testUpdateIsDisplay_Success() {
        // Create a new listing entity
        Listing listing = new Listing(0, 100.0, "A description", "Pandora's box", TEST_ANTIQUARIAN_EMAIL, 0, false, TEST_SELLER_EMAIL);
        listing = this.listingRepository.save(listing);

        // Call the method to be tested
        Listing updatedListing = listingService.updateIsDisplay(listing.getIdAntiquity());

        // Validate the results
        assertFalse(updatedListing.getIsDisplay());
        this.cleanListing(updatedListing); // Clean up the database
    }

    /**
     * Test the handling of a non-existent entity during the update process.
     *
     * This test ensures that when attempting to update the `isDisplay` field of a
     * non-existent listing, a `RuntimeException` is thrown with the appropriate message.
     *
     * @author Neve Thierry
     */
    @Test
    @DisplayName("Throw exception when listing entity is not found")
    void testUpdateIsDisplay_EntityNotFound() {
        // Use a non-existent listing ID
        long nonExistentListingId = 999L;

        // Verify the exception is thrown when trying to update a non-existent entity
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> listingService.updateIsDisplay(nonExistentListingId)
        );

        // Validate the exception message
        assertEquals("Entity not found", exception.getMessage());


    }
    /**
     * Test to accept an antiquity but with no id
     * @author Verly Noah
     */
    @Test
    public void acceptAntiquarianTestFromListingServiceNull() {
        Map<String,String> otherInformation = new HashMap<>();
        assertNull(this.listingService.acceptAntiquity(otherInformation));
        assertNull(this.listingService.acceptAntiquity(null));
        otherInformation.put("id","");
        assertNull(this.listingService.acceptAntiquity(otherInformation));
    }

    /**
     *
     * Test to accept an antiquity but with no id
     * @author Verly Noah
     */
    @Test
    public void rejectAntiquarianTestFromListingServiceNull() {
        Map<String,String> otherInformation = new HashMap<>();
        assertNull(this.listingService.rejectAntiquity(otherInformation));
        assertNull(this.listingService.rejectAntiquity(null));
        otherInformation.put("id","");
        assertNull(this.listingService.rejectAntiquity(otherInformation));
        otherInformation.put("note_title","");
        otherInformation.put("note_description","");
        otherInformation.put("note_price","");
        otherInformation.put("note_photo","");
        otherInformation.put("id","-1");
        assertNull(this.listingService.rejectAntiquity(otherInformation));
    }

    /**
     * Test for successfully updating a listing with state -1.
     * Verifies that the price, description, and state are updated correctly.
     *
     * @throws MessagingException if an email-related error occurs
     * @throws IOException if an input/output error occurs
     * @author Neve Thierry
     */
    @Test
    @Transactional
    @DisplayName("Successfully update a listing with state -1")
    void testUpdateListing_StateMinusOne() throws MessagingException, IOException {
        // Arrange
        Listing listing = new Listing(0, 100.0, "Old description", "Pandora's box", TEST_ANTIQUARIAN_EMAIL, -1, true, TEST_SELLER_EMAIL);
        listing = this.listingRepository.save(listing);

        Listing updatedListing = new Listing(0, 120.0, "Updated description", "Updated title", TEST_ANTIQUARIAN_EMAIL, -1, true, TEST_SELLER_EMAIL);

        // Act
        Listing result = listingService.updateListing(Long.valueOf(listing.getIdAntiquity()), updatedListing);

        // Assert
        assertNotNull(result, "The updated listing should not be null");
        assertEquals(120.0, result.getPriceAntiquity(), "The price should be updated");
        assertEquals("Updated description", result.getDescriptionAntiquity(), "The description should be updated");
        assertEquals(NEED_TO_BE_CHECKED.getState(), result.getState(), "The state should be updated to NEED_TO_BE_CHECKED");

        // Clean up
        this.cleanListing(result);
    }

    /**
     * Test for successfully updating a listing with state 1.
     * Ensures that the price, description, and state are updated correctly to reflect a modification.
     *
     * @throws MessagingException if an email-related error occurs
     * @throws IOException if an input/output error occurs
     *
     * @author Neve Thierry
     */
    @Test
    @Transactional
    @DisplayName("Successfully update a listing with state 1")
    void testUpdateListing_StateOne() throws MessagingException, IOException {
        // Arrange
        Listing listing = new Listing(0, 150.0, "Old description", "Zeus Statue", TEST_ANTIQUARIAN_EMAIL, 1, true, TEST_SELLER_EMAIL);
        listing = this.listingRepository.save(listing);

        Listing updatedListing = new Listing(0, 180.0, "New description", "New title", TEST_ANTIQUARIAN_EMAIL, 1, true, TEST_SELLER_EMAIL);

        // Act
        Listing result = listingService.updateListing(Long.valueOf(listing.getIdAntiquity()), updatedListing);

        // Assert
        assertNotNull(result, "The updated listing should not be null");
        assertEquals(180.0, result.getPriceAntiquity(), "The price should be updated");
        assertEquals("New description", result.getDescriptionAntiquity(), "The description should be updated");
        assertEquals(ACCEPTED_BUT_MODIFIED.getState(), result.getState(), "The state should be updated to ACCEPTED_BUT_MODIFIED");

        // Clean up
        this.cleanListing(result);
    }

    /**
     * Test to ensure no changes are made when the listing's state is 3.
     * Verifies that the listing remains unchanged for immutable states.
     *
     * @throws MessagingException if an email-related error occurs
     * @throws IOException if an input/output error occurs
     *
     * @author Neve Thierry
     */
    @Test
    @Transactional
    @DisplayName("Return the same listing when state is 3")
    void testUpdateListing_StateThree() throws MessagingException, IOException {
        // Arrange
        Listing listing = new Listing(0, 200.0, "Old description", "Athena Shield", TEST_ANTIQUARIAN_EMAIL, 3, true, TEST_SELLER_EMAIL);
        listing = this.listingRepository.save(listing);

        Listing updatedListing = new Listing(0, 220.0, "New description", "New title", TEST_ANTIQUARIAN_EMAIL, 3, true, TEST_SELLER_EMAIL);

        // Act
        Listing result = listingService.updateListing(Long.valueOf(listing.getIdAntiquity()), updatedListing);

        // Assert
        assertNotNull(result, "The updated listing should not be null");
        assertEquals(200.0, result.getPriceAntiquity(), "The price should not be updated");
        assertEquals("Old description", result.getDescriptionAntiquity(), "The description should not be updated");
        assertEquals("Athena Shield", result.getTitleAntiquity(), "The title should not be updated");
        assertEquals(3, result.getState(), "The state should remain the same");

        // Clean up
        this.cleanListing(result);
    }

    /**
     * Test for successfully updating a listing with a default state (0).
     * Ensures that the price, description, and title are updated correctly for new listings.
     *
     * @throws MessagingException if an email-related error occurs
     * @throws IOException if an input/output error occurs
     *
     * @author Neve Thierry
     */
    @Test
    @Transactional
    @DisplayName("Successfully update a listing with default state")
    void testUpdateListing_DefaultState() throws MessagingException, IOException {
        // Arrange
        Listing listing = new Listing(0, 250.0, "Old description", "Hermes Helmet", TEST_ANTIQUARIAN_EMAIL, 0, true, TEST_SELLER_EMAIL);
        listing = this.listingRepository.save(listing);

        Listing updatedListing = new Listing(0, 280.0, "Updated description", "Updated title", TEST_ANTIQUARIAN_EMAIL, 0, true, TEST_SELLER_EMAIL);

        // Act
        Listing result = listingService.updateListing(Long.valueOf(listing.getIdAntiquity()), updatedListing);

        // Assert
        assertNotNull(result, "The updated listing should not be null");
        assertEquals(280.0, result.getPriceAntiquity(), "The price should be updated");
        assertEquals("Updated description", result.getDescriptionAntiquity(), "The description should be updated");
        assertEquals("Updated title", result.getTitleAntiquity(), "The title should be updated");

        // Clean up
        this.cleanListing(result);
    }

    /**
     * Test for successfully retrieving listings with state 0 or 2 for a given antiquarian email.
     * Ensures that only the listings with the specified states are returned.
     *
     * @see ListingService#getAntiquitiesByState(String)
     *
     * @author Neve Thierry
     */
    @Test
    @Transactional
    @DisplayName("Successfully retrieve listings with state 0 or 2 for a specific antiquarian")
    void testGetAntiquitiesByState_Success() {
        // Arrange
        String mailAntiquarian = TEST_ANTIQUARIAN_EMAIL;

        Listing listing1 = new Listing(0, 100.0, "Description 1", "Title 1", mailAntiquarian, 0, true, TEST_SELLER_EMAIL);
        Listing listing2 = new Listing(0, 150.0, "Description 2", "Title 2", mailAntiquarian, 2, true, TEST_SELLER_EMAIL);
        Listing listing3 = new Listing(0, 200.0, "Description 3", "Title 3", mailAntiquarian, 1, true, TEST_SELLER_EMAIL);

        this.listingRepository.save(listing1);
        this.listingRepository.save(listing2);
        this.listingRepository.save(listing3);

        // Act
        List<Listing> result = listingService.getAntiquitiesByState(mailAntiquarian);

        // Assert
        assertNotNull(result, "The result list should not be null");
        assertEquals(2, result.size(), "The result list should contain only listings with state 0 or 2");
        assertTrue(result.stream().allMatch(listing -> Arrays.asList(0, 2).contains(listing.getState())),
                "All retrieved listings should have a state of 0 or 2");

        // Clean up
        this.cleanListing(listing1);
        this.cleanListing(listing2);
        this.cleanListing(listing3);
    }

    /**
     * Test for retrieving listings when no matching entries exist for the given antiquarian email.
     * Ensures that an empty list is returned when no results match the query criteria.
     *
     * @see ListingService#getAntiquitiesByState(String)
     *
     * @author Neve Thierry
     */
    @Test
    @Transactional
    @DisplayName("Return an empty list when no matching listings are found for a specific antiquarian")
    void testGetAntiquitiesByState_NoResults() {
        // Arrange
        String mailAntiquarian = TEST_ANTIQUARIAN_EMAIL;

        Listing listing1 = new Listing(0, 100.0, "Description 1", "Title 1", mailAntiquarian, 1, true, TEST_SELLER_EMAIL);
        Listing listing2 = new Listing(0, 150.0, "Description 2", "Title 2", mailAntiquarian, 3, true, TEST_SELLER_EMAIL);

        this.listingRepository.save(listing1);
        this.listingRepository.save(listing2);

        // Act
        List<Listing> result = listingService.getAntiquitiesByState(mailAntiquarian);

        // Assert
        assertNotNull(result, "The result list should not be null");
        assertTrue(result.isEmpty(), "The result list should be empty when no matching listings are found");

        // Clean up
        this.cleanListing(listing1);
        this.cleanListing(listing2);
    }

    /**
     * Test for retrieving listings with a nonexistent antiquarian email.
     * Ensures that an empty list is returned when the email provided does not exist in the database.
     *
     * @see ListingService#getAntiquitiesByState(String)
     *
     * @author Neve Thierry
     */
    @Test
    @Transactional
    @DisplayName("Return an empty list when the antiquarian email does not exist")
    void testGetAntiquitiesByState_NonexistentEmail() {
        // Arrange
        String nonexistentEmail = "nonexistent@anticairapp.com";

        // Act
        List<Listing> result = listingService.getAntiquitiesByState(nonexistentEmail);

        // Assert
        assertNotNull(result, "The result list should not be null");
        assertTrue(result.isEmpty(), "The result list should be empty when the antiquarian email does not exist");
    }
}

