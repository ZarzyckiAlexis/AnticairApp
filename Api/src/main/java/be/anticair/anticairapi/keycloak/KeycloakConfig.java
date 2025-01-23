package be.anticair.anticairapi.keycloak;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Configuration for Keycloak server connection
 * @author Zarzycki Alexis
 */
@Configuration
public class KeycloakConfig {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakConfig.class);

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    private Keycloak keycloak;

    /**
     * Creates a Keycloak client for administrative operations
     * @return Configured Keycloak client
     * @author Zarzycki Alexis
     */
    @Bean
    public Keycloak keycloakClient() {
        try {
            keycloak = KeycloakBuilder.builder()
                    .serverUrl(authServerUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .build();

            // Verify connection by trying to get realm
            keycloak.realm(realm).toRepresentation();

            logger.info("Successfully connected to Keycloak realm: {}", realm);
            return keycloak;
        } catch (Exception e) {
            logger.error("Failed to create Keycloak client", e);
            throw new RuntimeException("Could not create Keycloak client", e);
        }
    }
}