package be.anticair.anticairapi.keycloak.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import be.anticair.anticairapi.keycloak.service.AdminService;

import java.util.HashMap;
import java.util.Map;


/**
 * REST Controller to force the password reset
 * @author Dewever David
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    /**
     * Admin service
     */
    @Autowired
    private AdminService adminService;

    /**
     * Function to reset a password
     *
     * @param userEmail the email of the user whose password will be reset
     * @return a message with the result
     * @author Dewever David
     */
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @PostMapping("/force-password-reset/{userEmail}")
    public ResponseEntity<?> forcePasswordReset(@PathVariable String userEmail) {
        adminService.forcePasswordReset(userEmail);
        Map<String, String> responseMessage = new HashMap<>();
        responseMessage.put("message", "Password has been forced to be reset successfully");
        return ResponseEntity.ok(responseMessage);
    }


}