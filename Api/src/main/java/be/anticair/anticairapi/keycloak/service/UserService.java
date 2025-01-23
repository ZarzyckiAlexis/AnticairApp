package be.anticair.anticairapi.keycloak.service;

import be.anticair.anticairapi.Class.Listing;
import be.anticair.anticairapi.enumeration.TypeOfMail;
import jakarta.mail.MessagingException;
import jakarta.ws.rs.NotFoundException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Service that manage user's
 * @author Zarzycki Alexis
 **/
@Service
public class UserService {

    /**
     * The repository of antiquity who all the crud
     */
    @Autowired
    @Lazy
    private ListingRepository listingRepository;

    /**
     * The service of antiquity
     */
    @Autowired
    @Lazy
    private ListingService listingService;

    @Autowired

    @Lazy

    private EmailService emailService;

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${spring.mail.username}")
    private String sender;

    @Autowired
    public UserService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    /**
     * Retrieves a user from his email
     * @param userEmail The Email of the user to find
     * @return List of the users finded
     * @author Zarzycki Alexis
     */
    public List<UserRepresentation> getUsersByEmail(String userEmail) {
        try {

            // Search for the user in the realm by email
            List<UserRepresentation> users = keycloak.realm(realm).users().search(userEmail);

            // Check if the list is empty
            if (users.isEmpty()) {
                throw new NotFoundException("No users found with email: " + userEmail);
            }

            return users;
        } catch (Exception e) {
            // Debug log for exception
            throw new NotFoundException("No users found with email: " + userEmail, e);
        }
    }

    /**
     * Retrieves all users from the realm
     * @return List of all users
     * @author Blommaert Youry
     */
    public List<UserRepresentation> getAllUsers() {
        try{
            List<UserRepresentation> users = keycloak.realm(realm).users().list();

            if(users.isEmpty()){
                throw new NotFoundException("No users found");
            }

            return users;

        } catch(Exception e){
            throw new NotFoundException("Error while retrieving all users", e);
        }
    }

    /**
     * Retrieves all users without group assignments from the realm
     * @return List of users without group memberships
     * @author Blommaert Youry
     */
    public List<UserRepresentation> getUsersWithoutGroups() {
        try {
            // Get all users from the realm
            List<UserRepresentation> allUsers = keycloak.realm(realm).users().list();

            // Filter out users without groups
            List<UserRepresentation> usersWithoutGroups = allUsers.stream()
                    .filter(user -> {
                        // Get the groups of the user
                        List<GroupRepresentation> userGroups = keycloak.realm(realm).users()
                                .get(user.getId())
                                .groups();

                        return userGroups.isEmpty();
                    })
                    .collect(Collectors.toList());

            if(usersWithoutGroups.isEmpty()){
                throw new NotFoundException("No users without groups found");
            }

            return usersWithoutGroups;

        } catch(Exception e){
            throw new NotFoundException("Error while retrieving users without groups", e);
        }
    }

    /**
     * Retrieves the number of user from the realm
     * @return the number of user
     * @author Verly Noah
     */
    public int getNumberOfUsers() {
       return this.getAllUsers().size();
    }

    /**
     * Retrieves all users from the realm that are in the specified group.
     *
     * @param groupName
     * @return List of all users in the group specified
     * @author Blommaert Youry
     */
    public List<UserRepresentation> getUsersByGroupName(String groupName) {
        List<UserRepresentation> users = new ArrayList<>();
        try {
            if (groupName == null || groupName.isEmpty()) {
                throw new IllegalArgumentException("Group name must not be null or empty");
            }

            GroupRepresentation group = keycloak.realm(realm).groups().groups().stream()
                    .filter(g -> g.getName().equals(groupName))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Group not found: " + groupName));

            users = keycloak.realm(realm).groups().group(group.getId()).members();

            if (users.isEmpty()) {
                throw new NotFoundException("No users found in the group: " + groupName);
            }
        } catch (NotFoundException e) {
            System.err.println("Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            throw new RuntimeException("Error while retrieving users in the group: " + groupName, e);
        }
        return users;
    }

    /**
     * Disable a user from their Email
     * @param userEmail The email of the user to disable
     * @return boolean True if it has been disabled
     * @author Zarzycki Alexis
     */
    public boolean disableUser(String userEmail) {
        try {
            // Get the users with the email to desactivate
            List<UserRepresentation> users = keycloak.realm(realm).users().search(userEmail);

            // Check if any users are found
            if (users.isEmpty()) {
                throw new NotFoundException("No users found with email: " + userEmail);
            }

            // We get the first user in the list
            UserRepresentation user = users.getFirst();

            // Get the user's ID
            String userId = user.getId();

            if(keycloak.realm(realm).users().get(userId).groups().stream()
                    .anyMatch(group -> group.getName().equals("Antiquarian"))){
                String result = this.redistributeAntiquity(userEmail);
                if(!result.equals("Antiquity's antiquarian changed")){
                    return false;
                }
            }

            // Check if the user is an admin
            boolean isAdmin = keycloak.realm(realm).users().get(userId).groups().stream()
                    .anyMatch(group -> group.getName().equals("Admin"));

            // If the user is an admin, don't allow disabling
            if (isAdmin) {
                throw new RuntimeException("Cannot disable an admin user.");
            }

            // Desactivate the user
            user.setEnabled(false);

            // Actually update the user in Keycloak
            keycloak.realm(realm).users().get(userId).update(user);

            // Creating the map to store user's status
            Map<String, String> otherInformation = new HashMap<>();
            otherInformation.put("account_newstatus", "disabled");
            emailService.sendHtmlEmail(userEmail, "info@anticairapp.sixela.be", TypeOfMail.ENABLEORDISABLEUSER, otherInformation);
            return true;
        } catch (NotFoundException e) {
            throw new NotFoundException("No users found with email: " + userEmail);
        } catch (Exception e) {
            throw new RuntimeException("Error while disabling user with email: " + userEmail, e);
        }
    }

    /**
     * Enable a user from their Email
     * @param userEmail The email of the user to enable
     * @return boolean True if it has been enabled
     * @author Zarzycki Alexis
     */
    public boolean enableUser(String userEmail) {
        try {
            // Get the users with the email to activate
            List<UserRepresentation> users = keycloak.realm(realm).users().search(userEmail);

            // Check if any users are found
            if (users.isEmpty()) {
                throw new NotFoundException("No users found with email: " + userEmail);
            }

            // We get the first user in the list
            UserRepresentation user = users.getFirst();

            // Get the user's ID
            String userId = user.getId();

            // Enable the user
            user.setEnabled(true);

            // Actually update the user in Keycloak
            keycloak.realm(realm).users().get(userId).update(user);

            // Creating the map to store user's status
            Map<String, String> otherInformation = new HashMap<>();
            otherInformation.put("account_newstatus", "enabled");
            this.emailService.sendHtmlEmail(userEmail, sender, TypeOfMail.ENABLEORDISABLEUSER, otherInformation);
            return true;
        } catch (NotFoundException e) {
            throw new NotFoundException("No users found with email: " + userEmail);
        } catch (Exception e) {
            throw new RuntimeException("Error while enabling user with email: " + userEmail, e);
        }
    }

    /**
     * Get the status of a user
     * @param userEmail the email of the user to get the status
     * @return boolean true if it is enabled, false if not
     * @author Zarzycki Alexis
     */
    public boolean getUserStatus(String userEmail) {
        try {
            // Get the users with the email to activate
            List<UserRepresentation> users = keycloak.realm(realm).users().search(userEmail);

            // Check if any users are found
            if (users.isEmpty()) {
                throw new NotFoundException("No users found with email: " + userEmail);
            }

            // We get the first user in the list
            UserRepresentation user = users.getFirst();

            // Return the status of the user
            return user.isEnabled();
        } catch (NotFoundException e) {
            throw new NotFoundException("No users found with the email: " + userEmail);
        } catch (Exception e) {
            throw new RuntimeException("Error while getting the status of the user with email: " + userEmail, e);
        }
    }

    /**
     * Fonction to redistribute Antiquity
     * @param userEmail the email of the user to get the status
     * @return string, to know what was happened
     * @author Verly Noah
     */
    public String redistributeAntiquity(String userEmail) throws MessagingException, IOException {
    //Get the new antiquarian
        if(userEmail == null || userEmail.isEmpty()){ return "No email address provided"; }
        //Get all the antiquarian
        List<UserRepresentation> allAntiquarian = this.getUsersByGroupName("Antiquarian");
        //Check if there is atleast 1 antiquarian
        if(allAntiquarian.isEmpty()){return "No antiquarian found";}
        //Check if the only antiquarian isn't the antiquarian that we want change
        if(allAntiquarian.size() == 1 && allAntiquarian.getFirst().getId().equals(userEmail)){return "No other antiquarian found";}
        //Select the new antiquarian of the antiquity
        int randomUser = getRandom.apply(allAntiquarian.size());
        //Check if the selectionned antiquarain if not the same that the remplaced
        while(userEmail.equals(allAntiquarian.get(randomUser).getEmail())){
            randomUser = getRandom.apply(allAntiquarian.size());
        }
    //Change all the antiquarian's antiquity
        List<Listing> listings = this.listingRepository.getAllAntiquityNotCheckedFromAnAntiquarian(userEmail);
        if(listings.isEmpty()){ return "Antiquity's antiquarian changed";}
        Map<String,String> otherInformation = new HashMap<>();
        for(Listing listing : listings){
            if(!this.listingService.changeListingAntiquarian(listing, allAntiquarian.get(randomUser).getEmail())){
                return "Error while changing antiquarian";
            }
            otherInformation.put("title",listing.getTitleAntiquity());
            otherInformation.put("description",listing.getDescriptionAntiquity());
            otherInformation.put("price", listing.getPriceAntiquity().toString());
            this.emailService.sendHtmlEmail(allAntiquarian.get(randomUser).getEmail(), sender, TypeOfMail.REDISTRIBUTEANTIQUITYNEWANTIQUARIAN,otherInformation);
        }

        otherInformation.clear();
        this.emailService.sendHtmlEmail(userEmail, sender, TypeOfMail.REDISTRIBUTEANTIQUITYINITANTIQUARIAN,otherInformation);

        return "Antiquity's antiquarian changed";

    }

    /**
     * Lambda expression to get a random number between 1 and a max
     * @author Verly Noah
     */
   private Function<Integer,Integer> getRandom = max ->  (int) (Math.random() * max);

    /**
     * Fonction to update user profile
     * @param userDetails the details of user.
     *  @author Dewever David
     */
    public void updateUserProfile(Map<String, Object> userDetails) {
        String email = (String) userDetails.get("email");

        List<UserRepresentation> users = keycloak.realm(realm).users().search(email);
        if (users.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        UserRepresentation user = users.get(0);
        user.setFirstName((String) userDetails.get("firstName"));
        user.setLastName((String) userDetails.get("lastName"));
        user.singleAttribute("phoneNumber", (String) userDetails.get("phoneNumber"));

        // Mise à jour sur Keycloak
        keycloak.realm(realm).users().get(user.getId()).update(user);
    }


    /**
     * Updates a user's profile in Keycloak to comply with GDPR regulations.
     *
     * <p>This method anonymizes the user's personal data such as first name, last name,
     * email address, and phone number, and disables the user account. The updates are
     * performed directly in the Keycloak user management system based on the provided
     * email address.</p>
     *
     * @param userDetails a map containing the user's details, including the key "email"
     *                    which specifies the email of the user to be updated
     * @throws RuntimeException if the user is not found in Keycloak or if an error occurs during the update
     *
     * @author Neve Thierry
     * @see UserRepresentation
     * @see org.keycloak.admin.client.Keycloak
     */
    public void updateRGPDUserProfile(Map<String, Object> userDetails) {
        String email = (String) userDetails.get("email");
        try{List<UserRepresentation> users = keycloak.realm(realm).users().search(email);
            if (users.isEmpty()) {
                throw new RuntimeException("User not found with email: " + email);
            }

            UserRepresentation user = users.get(0);
            user.setFirstName("FirstName_"+ user.getId());
            user.setLastName("LastName_"+ user.getId());
            user.singleAttribute("phoneNumber", "+32000000000");
            user.setEmail("anonymized"+user.getId()+"@deleted.com");
            user.setEnabled(false);

            // Mise à jour sur Keycloak
            keycloak.realm(realm).users().get(user.getId()).update(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the balance of a user
     * @param userEmail the email of the user to get the balance
     * @return double the amount of the balance
     * @author Zarzycki Alexis
     */
    public double getUserBalance(String userEmail) {
        try {
            // Get the users with the email
            List<UserRepresentation> users = keycloak.realm(realm).users().search(userEmail);

            // Check if any users are found
            if (users.isEmpty()) {
                throw new NotFoundException("No users found with email: " + userEmail);
            }

            // We get the first user in the list
            UserRepresentation user = users.get(0);

            // Retrieve the "balance" attribute from user attributes
            Map<String, List<String>> attributes = user.getAttributes();

            if (attributes != null && attributes.containsKey("balance")) {
                List<String> balanceValues = attributes.get("balance");

                if (balanceValues != null && !balanceValues.isEmpty()) {
                    // Convert balance to double for proper calculation
                    return Double.parseDouble(balanceValues.get(0));
                }
            }
            // If no "balance" attribute is found, return a default value
            return 0;
        } catch (NotFoundException e) {
            throw new NotFoundException("No users found with the email: " + userEmail, e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid balance value for user with email: " + userEmail, e);
        } catch (Exception e) {
            throw new RuntimeException("Error while getting the balance of the user with email: " + userEmail, e);
        }
    }

    /**
     * Add a specified amount to a user's balance.
     * @param userEmail the email of the user to update the balance.
     * @param amount the amount to add to the user's balance.
     * @author Zarzycki Alexis
     */
    public void addToUserBalance(String userEmail, double amount) {
        try {
            // Check the amount
            if (amount < 0) {
                throw new IllegalArgumentException("Amount must be non-negative.");
            }

            // Get the users with the email
            List<UserRepresentation> users = keycloak.realm(realm).users().search(userEmail);

            // Check if any users are found
            if (users.isEmpty()) {
                throw new NotFoundException("No users found with email: " + userEmail);
            }

            // Get the first user in the list
            UserRepresentation user = users.get(0);

            // Retrieve the current "balance" attribute
            Map<String, List<String>> attributes = user.getAttributes();
            double currentBalance = getUserBalance(userEmail);

            // Calculate the new balance
            double newBalance = currentBalance + amount;

            // Format the new balance to ensure it's in the correct format (with two decimal places)
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);  // Force dot separator
            DecimalFormat df = new DecimalFormat("#.00", symbols);  // Always two decimal places
            String formattedBalance = df.format(newBalance);

            // Update the user's attributes with the new balance
            if (attributes == null) {
                attributes = new HashMap<>();
            }
            attributes.put("balance", Collections.singletonList(formattedBalance));
            user.setAttributes(attributes);

            // Update the user in Keycloak
            keycloak.realm(realm).users().get(user.getId()).update(user);
        } catch (NotFoundException e) {
            throw new NotFoundException("No users found with the email: " + userEmail, e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid balance value for user with email: " + userEmail, e);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Error while updating the balance of the user with email: " + userEmail, e);
        }
    }

}
