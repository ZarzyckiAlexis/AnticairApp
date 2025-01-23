package be.anticair.anticairapi.keycloak.controller;


import be.anticair.anticairapi.enumeration.TypeOfMail;
import be.anticair.anticairapi.keycloak.service.EmailService;
import be.anticair.anticairapi.keycloak.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


/**
 * REST Controller to RGPD
 * @author Neve Thierry
 */
@RestController
@RequestMapping("/api/rgpd")
public class RGPDController {
    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Value("${spring.mail.username}")
    private String sender;


    /**
     * Endpoint to update a user's profile to comply with GDPR regulations.
     *
     * <p>This method processes a request to anonymize and deactivate a user's profile.
     * It first validates the provided email, sends a notification email to the user,
     * and then updates the user's profile in Keycloak. If an error occurs,
     * a descriptive error message is returned.</p>
     *
     * @param userDetails a map containing the user's details, where the key "email" is required
     *                    to identify the user
     * @return a {@link ResponseEntity} containing a JSON response with a success message
     *         or an error description
     *
     * @throws RuntimeException if an error occurs during the profile update or email sending
     *
     * @author Neve Thierry
     * @see UserService#updateRGPDUserProfile(Map)
     * @see EmailService#sendHtmlEmail(String, String, TypeOfMail, Map)
     * @see TypeOfMail
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String,String>> updateRGPD(@RequestBody Map<String, Object> userDetails) {
        Map<String, String> response = new HashMap<>();
        try {
            String email = (String) userDetails.get("email");
            if (email == null || email.isEmpty()) {

                response.put("error", "Email is required.");
                return ResponseEntity.badRequest().body(response);
            }
            Map<String,String> otherInformation = new HashMap<>();
            emailService.sendHtmlEmail(email,"info@anticairapp.sixela.be", TypeOfMail.DELETEUSERDATA, otherInformation);
            userService.updateRGPDUserProfile(userDetails);


            response.put("message", "User profile updated successfully.");
            return ResponseEntity.ok(response); // Return valid json
        } catch (Exception e) {
            response.put("error", "Error updating user profile: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
