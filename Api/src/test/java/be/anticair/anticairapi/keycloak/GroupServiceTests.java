package be.anticair.anticairapi.keycloak;

import be.anticair.anticairapi.keycloak.service.GroupService;
import be.anticair.anticairapi.keycloak.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import jakarta.ws.rs.NotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that verify the GroupService function's
 * @Author Zarzycki Alexis
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true"
})
public class GroupServiceTests {

    @Autowired
    private GroupService groupService;

    @Autowired
    private Keycloak keycloak;

    @Autowired
    private UserService userService;

    private static final String TEST_USER_EMAIL = "test-user@gmail.com";

    /**
     * Testing the getGroupsByName
     * @Author Zarzycki Alexis
     */
    @Test
    @DisplayName("Test getGroupsByName")
    public void testGetGroupsByName() {
        // Attempt to retrieve groups
        List<GroupRepresentation> groups = groupService.getGroupsByName("Admin");

        assertNotNull(groups);
        assertFalse(groups.isEmpty());
        assertEquals("Admin", groups.getFirst().getName());
    }

    /**
     * Testing Adding and Removing a group to a user
     * @Author Zarzycki Alexis
     */
    @Test
    @DisplayName("Test addGroup and removeGroup")
    public void testAddAndRemoveGroup() {
        String groupName = "Admin";

        try {
            // First, get the user
            List<UserRepresentation> users = userService.getUsersByEmail(TEST_USER_EMAIL);
            assertFalse(users.isEmpty(), "User must exist in Keycloak");

            // Add group
            groupService.addGroup(TEST_USER_EMAIL, groupName);

            // Verify group membership
            List<GroupRepresentation> userGroups = keycloak.realm("anticairapp")
                    .users()
                    .get(users.getFirst().getId())
                    .groups();

            assertTrue(
                    userGroups.stream().anyMatch(g -> g.getName().equals(groupName)),
                    "User should be in the group after addGroup"
            );

            // Remove group
            groupService.removeGroup(TEST_USER_EMAIL, groupName);

            // Verify group removal
            userGroups = keycloak.realm("anticairapp")
                    .users()
                    .get(users.getFirst().getId())
                    .groups();

            assertFalse(
                    userGroups.stream().anyMatch(g -> g.getName().equals(groupName)),
                    "User should not be in the group after removeGroup"
            );

        } catch (Exception e) {
            fail("Exception occurred during group management: " + e.getMessage());
        }
    }

    /**
     * Testing Adding a group to a nonExistent User
     * @Author Zarzycki Alexis
     */
    @Test
    @DisplayName("Test addGroup with non-existent user")
    public void testAddGroup_NonExistentUser() {
        // Use an email that definitely doesn't exist
        String nonExistentEmail = "nonexistent_user_987654@anticairapp.be";

        assertThrows(NotFoundException.class, () -> {
            groupService.addGroup(nonExistentEmail, "Admin");
        });
    }

    /**
     * Testing Adding a user to a nonExistent Group
     * @Author Zarzycki Alexis
     */
    @Test
    @DisplayName("Test addGroup with non-existent group")
    public void testAddGroup_NonExistentGroup() {
        // Use a non-existent group
        assertThrows(NotFoundException.class, () -> {
            groupService.addGroup(TEST_USER_EMAIL, "non_existent_group_xyz");
        });
    }

    /**
     * Testing Removing a group from a nonExistent User
     * @Author Zarzycki Alexis
     */
    @Test
    @DisplayName("Test removeGroup with non-existent user")
    public void testRemovingroup_NonExistentUser() {
        // Use an email that definitely doesn't exist
        String nonExistentEmail = "nonexistent_user_987654@anticairapp.be";

        assertThrows(NotFoundException.class, () -> {
            groupService.removeGroup(nonExistentEmail, "Admin");
        });
    }

    /**
     * Testing Remove a user from a nonExistent Group
     * @Author Zarzycki Alexis
     */
    @Test
    @DisplayName("Test removeGroup with non-existent group")
    public void testRemoveGroup_NonExistentGroup() {
        // Use a non-existent group
        assertThrows(NotFoundException.class, () -> {
            groupService.removeGroup(TEST_USER_EMAIL, "non_existent_group_xyz");
        });
    }
}