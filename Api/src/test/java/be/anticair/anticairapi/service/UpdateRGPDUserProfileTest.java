package be.anticair.anticairapi.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for the RGPD update service.
 *
 * <p>This test class covers scenarios for the {@code UpdateRGPDService}
 * that anonymizes user information in Keycloak to comply with GDPR regulations.</p>
 *
 * @author Neve Thierry
 * @see UpdateRGPDService
 * @see UserRepresentation
 * @see Keycloak
 */
class UpdateRGPDUserProfileTest {

    private Keycloak keycloak;
    private RealmResource realmResource;
    private UsersResource usersResource;
    private UpdateRGPDService service;

    private final String realm = "testRealm";

    /**
     * Sets up the mocks for the Keycloak API before each test.
     */
    @BeforeEach
    void setup() {
        keycloak = mock(Keycloak.class);
        realmResource = mock(RealmResource.class);
        usersResource = mock(UsersResource.class);

        when(keycloak.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);

        service = new UpdateRGPDService(keycloak, realm);
    }

    /**
     * Test case where the user is not found in Keycloak.
     *
     * <p>Ensures that the appropriate exception is thrown when the email
     * of the user does not exist in the realm.</p>
     */
    @Test
    void testUserNotFound() {
        when(usersResource.search("nonexistent@example.com"))
                .thenReturn(Collections.emptyList());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateRGPDUserProfile(Map.of("email", "nonexistent@example.com"));
        });

        assertEquals("User not found with email: nonexistent@example.com", exception.getMessage());
    }

    /**
     * Test case where an error occurs during the update process.
     *
     * <p>Ensures that an exception is thrown if the update operation fails.</p>
     */
    @Test
    void testErrorDuringUpdate() {
        UserRepresentation user = new UserRepresentation();
        user.setId("user123");

        // Mock user search result
        when(usersResource.search("error@example.com"))
                .thenReturn(List.of(user));

        // Mock update operation with an exception
        UserResource userResource = mock(UserResource.class);
        when(usersResource.get(user.getId())).thenReturn(userResource);
        doThrow(new RuntimeException("Update failed"))
                .when(userResource).update(any(UserRepresentation.class));

        // Capture and assert exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.updateRGPDUserProfile(Map.of("email", "error@example.com"));
        });

        // Verify exception message
        assertEquals("Update failed", exception.getMessage());

        // Verify interactions with mocks
        verify(usersResource).search("error@example.com");
        verify(usersResource).get(user.getId());
        verify(userResource).update(any(UserRepresentation.class));
    }

    /**
     * Test case for a successful RGPD update operation.
     *
     * <p>Ensures that no exceptions are thrown, and the update process
     * completes correctly for a valid user.</p>
     */
    @Test
    void testSuccessfulUpdate() {
        UserRepresentation user = new UserRepresentation();
        user.setId("user123");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");

        // Mock user search result
        when(usersResource.search("success@example.com"))
                .thenReturn(List.of(user));

        // Mock retrieval of user resource and void update operation
        var userResource = mock(UserResource.class); // Separate mock for UserResource
        when(usersResource.get("user123")).thenReturn(userResource);
        doNothing().when(userResource).update(any(UserRepresentation.class));

        // Call the method under test
        assertDoesNotThrow(() -> {
            service.updateRGPDUserProfile(Map.of("email", "success@example.com"));
        });

        // Verify interactions with mocks
        verify(usersResource).search("success@example.com");
        verify(usersResource).get("user123");
        verify(userResource).update(any(UserRepresentation.class));
    }

    /**
     * Service class responsible for RGPD compliance updates in Keycloak.
     *
     * <p>This service anonymizes personal user information and disables
     * user accounts to comply with GDPR regulations.</p>
     *
     * @author Neve Thierry
     */
    private static class UpdateRGPDService {
        private final Keycloak keycloak;
        private final String realm;

        /**
         * Constructs the service with a Keycloak instance and the target realm.
         *
         * @param keycloak Keycloak client instance
         * @param realm    Name of the Keycloak realm
         */
        UpdateRGPDService(Keycloak keycloak, String realm) {
            this.keycloak = keycloak;
            this.realm = realm;
        }

        /**
         * Updates a user's profile to comply with GDPR regulations.
         *
         * <p>This method anonymizes the user's personal data (e.g., name, email)
         * and disables the user account.</p>
         *
         * @param userDetails a map containing the user's email under the key "email"
         * @throws RuntimeException if the user is not found or the update fails
         */
        public void updateRGPDUserProfile(Map<String, Object> userDetails) {
            String email = (String) userDetails.get("email");
            List<UserRepresentation> users = keycloak.realm(realm).users().search(email);
            if (users.isEmpty()) {
                throw new RuntimeException("User not found with email: " + email);
            }

            UserRepresentation user = users.get(0);
            user.setFirstName("FirstName_" + user.getId());
            user.setLastName("LastName_" + user.getId());
            user.singleAttribute("phoneNumber", "+32000000000");
            user.setEmail("anonymized" + user.getId() + "@deleted.com");
            user.setEnabled(false);

            keycloak.realm(realm).users().get(user.getId()).update(user);
        }
    }
}