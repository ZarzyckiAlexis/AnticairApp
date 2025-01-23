package be.anticair.anticairapi.keycloak.controller;

import be.anticair.anticairapi.keycloak.service.UserService;
import jakarta.mail.MessagingException;
import org.apache.catalina.User;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * REST Controller for managing users in Keycloak.
 * @author Blommaert Youry
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    /**
     * Service for performing users-related operations.
     * @author Blommaert Youry
     */
    private final UserService userService;

    /**
     * Constructor with dependency injection for the UserService.
     *
     * @param userService the service used to manage users in Keycloak.
     * @author Blommaert Youry
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Constructor with dependency injection for the UserService.
     *
     * @param userDetails the service to edit user details.
     * @author Dewever David
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> updateUserProfile(
            @RequestBody Map<String, Object> userDetails) {
        Map<String, String> response = new HashMap<>();
        try {
            userService.updateUserProfile(userDetails);
            response.put("message", "User profile updated successfully.");
            return ResponseEntity.ok(response); // Return valid json
        } catch (Exception e) {
            response.put("error", "Error updating user profile: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    /**
     * Get all users from the database.
     *
     * @return a ResponseEntity containing a list of all users.
     * @author Blommaert Youry
     */
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @GetMapping("/list")
    public ResponseEntity<List<UserRepresentation>> listUsers() {
        List<UserRepresentation> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get all users without groups from the database.
     *
     * @return a ResponseEntity containing a list of all users without groups.
     * @author Blommaert Youry
     */
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @GetMapping("/list/users")
    public ResponseEntity<List<UserRepresentation>> listUsersWithoutGroups() {
        List<UserRepresentation> users = userService.getUsersWithoutGroups();
        return ResponseEntity.ok(users);
    }

    /**
     * Get the number of users from the database
     *
     * @return a ResponseEntity containing the number of users
     * @author Verly Noah
     */
    @GetMapping("/nbrUsers")
    public ResponseEntity<Integer> numberUsers() {
        int nbrUser = userService.getNumberOfUsers();
        return ResponseEntity.ok(nbrUser);
    }

    /**
     * Get all users from a specific group.
     * @return ResponseEntity containing a list of all users in the group specified.
     * @author Blommaert Youry
     */
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @GetMapping("/list/admin")
    public ResponseEntity<List<UserRepresentation>> listAdmins() {
        List<UserRepresentation> admins = userService.getUsersByGroupName("Admin");
        return ResponseEntity.ok(admins);
    }

    /**
     * Get all users from a specific group
     * @return ResponseEntity containing a list of all users in the antiquarian group specified.
     * @author Zarzycki Alexis
     */
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @GetMapping("/list/antiquarian")
    public ResponseEntity<List<UserRepresentation>> listAntiquarian() {
        List<UserRepresentation> antiquarian = userService.getUsersByGroupName("Antiquarian");
        return ResponseEntity.ok(antiquarian);
    }

    /**
     * Desactivate a user
     * @return ResponseEntity containing a Json
     * @author Zarzycki Alexis
     */
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @PostMapping("/desactivate")
    public ResponseEntity<Map<String,String>> desactivateUser(
            @RequestParam String emailId
    ){
        userService.disableUser(emailId);
        Map<String, String> responseMessage = new HashMap<>();
        responseMessage.put("message", "User disabled successfully");
        return ResponseEntity.ok(responseMessage);
    }


    /**
     * Activate a user
     * @return ResponseEntity containing a Json
     * @author Zarzycki Alexis
     */
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @PostMapping("/activate")
    public ResponseEntity<Map<String,String>> activateUser(
            @RequestParam String emailId
    ){
        userService.enableUser(emailId);
        Map<String, String> responseMessage = new HashMap<>();
        responseMessage.put("message", "User enabled successfully");
        return ResponseEntity.ok(responseMessage);
    }

    /**
     * Get the status of a user
     * @return ResponseEntity containing a Json
     * @author Zarzycki Alexis
     */
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getUserStatus(
            @RequestParam String emailId
    ){
        String value = String.valueOf(userService.getUserStatus(emailId));
        Map<String, String> responseMessage = new HashMap<>();
        responseMessage.put("message", value);
        return ResponseEntity.ok(responseMessage);
    }

    /**
     * Redistribute the antiquity of an antiquarian
     * @return ResponseEntity containing a Json
     * @author Verly Noah
     */
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @PutMapping("/redistributeAntiquity")
    public ResponseEntity<Map<String, String>> redistributeAntiquity(
            @RequestParam String emailId
    ) throws MessagingException, IOException {
        String value = String.valueOf(userService.redistributeAntiquity(emailId));
        Map<String, String> responseMessage = new HashMap<>();
        responseMessage.put("message", value);
        if(Objects.equals(responseMessage.get("message"), "Antiquity's antiquarian changed")){
            return ResponseEntity.ok(responseMessage);
        }
        return ResponseEntity.badRequest().body(responseMessage);
    }

}
