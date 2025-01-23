package be.anticair.anticairapi.keycloak.controller;

import be.anticair.anticairapi.keycloak.service.GroupService;
import jakarta.ws.rs.NotFoundException;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for managing user groups in Keycloak.
 * @author Zarzycki Alexis
 */
@RestController
@RequestMapping("/api/groups")
public class GroupController {

    /**
     * Service for performing group-related operations.
     * @author Zarzycki Alexis
     */
    private final GroupService groupService;

    /**
     * Constructor with dependency injection for the GroupService.
     *
     * @param groupService the service used to manage groups in Keycloak.
     * @author Zarzycki Alexis
     */
    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * Get all the groups in the keycloak realm.
     *
     * @return a ResponseEntity containing a success message.
     * @author Zarzycki Alexis
     */
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @GetMapping("/")
    public ResponseEntity<List<Map<String, Object>>> getGroups() {
        List<GroupRepresentation> groups = groupService.getGroup();

        // Transforming the groups to match the frontend format
        List<Map<String, Object>> formattedGroups = groups.stream()
                .map(group -> {
                    Map<String, Object> formattedGroup = new HashMap<>();
                    formattedGroup.put("name", group.getName());
                    return formattedGroup;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(formattedGroups);
    }

    /**
     * Get all the groups for a user.
     *
     * @param emailId   the email of the user to get the groups for.
     * @return a ResponseEntity containing a list of groups the user belongs to.
     * @author Zarzycki Alexis
     */
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @GetMapping("/{emailId}/groups")
    public ResponseEntity<List<Map<String, Object>>> getGroupsFromUser(@RequestParam String emailId) {
        // Fetch the groups the user is a member of (assuming groupService.getGroupsForUser fetches this)
        List<GroupRepresentation> userGroups = groupService.getGroupFromUser(emailId);

        // If the user has no groups, return an empty list
        if (userGroups == null || userGroups.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // Transforming the groups to match the frontend format
        List<Map<String, Object>> formattedGroups = userGroups.stream()
                .map(group -> {
                    Map<String, Object> formattedGroup = new HashMap<>();
                    formattedGroup.put("name", group.getName());
                    return formattedGroup;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(formattedGroups);
    }

    /**
     * Adds a user to a specified group in Keycloak.
     *
     * @param emailId   the email of the user to be added to the group.
     * @param groupName the name of the group to which the user will be added.
     * @return a ResponseEntity containing a success message.
     * @author Zarzycki Alexis
     */
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addGroup(
            @RequestParam String emailId,
            @RequestParam String groupName
    ) {
        groupService.addGroup(emailId, groupName);
        Map<String, String> responseMessage = new HashMap<>();
        responseMessage.put("message", emailId + " added to the group " + groupName);
        return ResponseEntity.ok(responseMessage);
    }

    /**
     * Removes a user from a specified group in Keycloak.
     *
     * @param emailId   the email of the user to be removed from the group.
     * @param groupName the name of the group from which the user will be removed.
     * @return a ResponseEntity containing a success message.
     * @author Zarzycki Alexis
     */
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @PostMapping("/remove")
    public ResponseEntity<Map<String, String>> removeGroup(
            @RequestParam String emailId,
            @RequestParam String groupName
    ) {
        groupService.removeGroup(emailId, groupName);
        Map<String, String> responseMessage = new HashMap<>();
        responseMessage.put("message", emailId + " removed from the group " + groupName);
        return ResponseEntity.ok(responseMessage);
    }

    /**
     * Handles exceptions when a user or group is not found in Keycloak.
     *
     * @param ex the NotFoundException thrown when the user or group is not found.
     * @return a ResponseEntity with a 404 status and an error message.
     * @author Zarzycki Alexis
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Handles general runtime exceptions.
     *
     * @param ex the RuntimeException thrown during processing.
     * @return a ResponseEntity with a 500 status and an error message.
     * @author Zarzycki Alexis
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
