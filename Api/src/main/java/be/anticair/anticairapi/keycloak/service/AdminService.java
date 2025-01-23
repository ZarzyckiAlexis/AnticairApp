package be.anticair.anticairapi.keycloak.service;

import jakarta.ws.rs.NotFoundException;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.keycloak.admin.client.Keycloak;

import java.util.*;

/**
 * Service to the admin
 * @author Dewver David
 */
@Service
public class AdminService {

    @Value("${keycloak.realm}")
    private String realm;

    private final Keycloak keycloak;

    @Autowired
    private UserService userService;

    public AdminService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    /**
     * Force a user to reset their password using Keycloak required actions.
     * @param userEmail The email of the user.
     */
    public void forcePasswordReset(String userEmail) {
        try {
            // Find the user by email
            List<UserRepresentation> users = userService.getUsersByEmail(userEmail);

            if (users.isEmpty()) {
                throw new NotFoundException("No user found with email: " + userEmail);
            }

            UserRepresentation user = users.get(0); // Retrieve the first user from the list

            // Add the UPDATE_PASSWORD action to the user's required actions
            List<String> requiredActions = user.getRequiredActions();
            if (requiredActions == null) {
                requiredActions = new ArrayList<>();
            }
            requiredActions.add(UserModel.RequiredAction.UPDATE_PASSWORD.name());
            user.setRequiredActions(requiredActions);

            // Get the user's ID
            String userId = user.getId();
            // Actually update the user in Keycloak

            keycloak.realm(realm).users().get(userId).update(user);

        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error while forcing password reset for user: " + userEmail, e);
        }
    }

}