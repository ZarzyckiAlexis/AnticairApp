package be.anticair.anticairapi.keycloak.service;

import jakarta.ws.rs.NotFoundException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service that manage a user's groups
 *
 * @author Zarzycki Alexis
 **/
@Service
public class GroupService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Autowired
    private UserService userService;

    @Autowired
    public GroupService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    /**
     * Adds a user to a specific group in Keycloak.
     *
     * @param userEmail The email ID of the user
     * @param groupName The name of the group in the Keycloak Realm
     * @author Zarzycki Alexis
     */
    public void addGroup(String userEmail, String groupName) {
        try {
            // Look for the group in the realm
            List<GroupRepresentation> groups = getGroupsByName(groupName);

            // Find user by email
            List<UserRepresentation> users = userService.getUsersByEmail(userEmail);

            if (users.isEmpty()) {
                throw new NotFoundException("User not found: " + userEmail);
            }

            UserRepresentation user = users.getFirst(); // Get the first user

            keycloak.realm(realm).users().get(user.getId()).joinGroup(groups.getFirst().getId());

        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new NotFoundException("Error while adding user: " + userEmail + " to group: " + groupName);
        }
    }
    
    /**
     * Removes a user from a specific group in Keycloak.
     *
     * @param userEmail The email ID of the user
     * @param groupName The name of the group in the Keycloak Realm
     * @author Zarzycki Alexis
     */
    public void removeGroup(String userEmail, String groupName) {
        try {
            // Look for the group in the realm
            List<GroupRepresentation> groups = getGroupsByName(groupName);

            // Find user by email
            List<UserRepresentation> users = userService.getUsersByEmail(userEmail);

            if (users.isEmpty()) {
                throw new NotFoundException("User not found: " + userEmail);
            }

            UserRepresentation user = users.getFirst(); // Get the first user

            // Get the user's ID
            String userId = user.getId();

            if((keycloak.realm(realm).users().get(userId).groups().stream()
                    .anyMatch(group -> group.getName().equals("Antiquarian"))) && groupName.equalsIgnoreCase("Antiquarian")) {
                String result = this.userService.redistributeAntiquity(userEmail);
                if(!result.equals("Antiquity's antiquarian changed")){
                    return;
                }
            }

            keycloak.realm(realm).users().get(user.getId()).leaveGroup(groups.getFirst().getId());

        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new NotFoundException("Error while removing group: " + groupName + " to user: " + userEmail, e);
        }
    }

    /**
     * Check if a user belongs to a specific group in Keycloak.
     *
     * @param userEmail The email of the user to check.
     * @param groupName The name of the group to verify membership.
     * @return true if the user is a member of the group, false otherwise.
     * @author Zarzycki Alexis
     */
    private boolean isUserInGroup(String userEmail, String groupName) {
        try {
            // Look for the group in the realm
            List<GroupRepresentation> groups = getGroupsByName(groupName);

            // Find user by email
            List<UserRepresentation> users = userService.getUsersByEmail(userEmail);

            UserRepresentation user = users.getFirst(); // Get the first user

            // Retrieve the user's group memberships
            List<GroupRepresentation> userGroups = keycloak.realm(realm)
                    .users()
                    .get(user.getId())
                    .groups();

            // Check if the group is in the user's memberships
            return userGroups.stream().anyMatch(g -> g.getName().equals(groupName));

        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new NotFoundException("Error while checking group membership for user: " + userEmail + " in group: " + groupName, e);
        }
    }

    /**
     * Retrieves the list of groups matching the provided name in the Keycloak realm.
     *
     * @param groupName The name of the group to search for.
     * @return A list of GroupRepresentation objects matching the name.
     * @throws NotFoundException if no groups are found with the given name.
     * @author Zarzycki Alexis
     */
    public List<GroupRepresentation> getGroupsByName(String groupName) {
        try {
            // Look for the group in the realm
            List<GroupRepresentation> groups = keycloak.realm(realm)
                    .groups()
                    .groups()
                    .stream()
                    .filter(g -> g.getName().equals(groupName))
                    .collect(Collectors.toList());

            if (groups.isEmpty()) {
                throw new NotFoundException("No groups found with name: " + groupName);
            }

            return groups;
        } catch (Exception e) {
            throw new NotFoundException("Error while retrieving groups with name: " + groupName, e);
        }
    }

    /**
     * Retrieves the list of groups inside the keycloak realm.
     *
     * @return A list of GroupRepresentation objects
     * @throws NotFoundException if no groups are found with the given name.
     * @author Zarzycki Alexis
     */
    public List<GroupRepresentation> getGroup() {
        try {
            // Look for the group in the realm
            List<GroupRepresentation> groups = new ArrayList<>(keycloak.realm(realm)
                    .groups()
                    .groups());

            if (groups.isEmpty()) {
                throw new NotFoundException("No groups found ");
            }

            return groups;
        } catch (Exception e) {
            throw new NotFoundException("Error while retrieving groups");
        }
    }

    /**
     * Retrieves the list of groups of a user.
     * @param emailId the email of the user
     * @return A list of GroupRepresentation objects
     * @throws NotFoundException if the user is not found.
     * @author Zarzycki Alexis
     */
    public List<GroupRepresentation> getGroupFromUser(String emailId) {
        try {
            // Get the user by emailId
            UserRepresentation user = keycloak.realm(realm).users().search(emailId).stream().findFirst()
                    .orElseThrow(() -> new NotFoundException("User not found"));

            // Fetch the groups that the user is a member of
            List<GroupRepresentation> groups = keycloak.realm(realm)
                    .users().get(user.getId()).groups();

            // If no groups found, return an empty list
            if (groups == null || groups.isEmpty()) {
                return Collections.emptyList();
            }

            return groups;
        } catch (Exception e) {
            throw new RuntimeException("Error while retrieving groups for the user", e);
        }
    }

}
