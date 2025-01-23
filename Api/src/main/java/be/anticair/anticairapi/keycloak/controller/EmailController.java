package be.anticair.anticairapi.keycloak.controller;

import be.anticair.anticairapi.keycloak.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {
//
//    private final EmailService emailService;
//
//
//    @Autowired
//    public EmailController(EmailService emailService) {
//        this.emailService = emailService;
//    }
//
//    @PostMapping("/send")
//
//    public ResponseEntity<String> sendMail(
//            @RequestParam("receiver") String receiver,
//            @RequestParam("sender") String sender,
//            @RequestParam("subject") String subject,
//            @RequestParam("typeOfMail") int typeOfMail
//            //,@RequestBody Map<String,String> otherInformation
//            ) throws MessagingException, IOException {
//
//        Map<String, String> otherInformation = new HashMap<>();
//        otherInformation.put("title", "Stone Mask");
//        otherInformation.put("price", "150.00");
//        otherInformation.put("description", "It's a mask");
//        otherInformation.put("note_title", "Is not a mask");
//        otherInformation.put("note_description", "Not accurate");
//        otherInformation.put("note_price", "Too expensive");
//
//        this.emailService.sendHtmlEmail(receiver,sender,subject,typeOfMail, otherInformation);
//        return ResponseEntity.ok("Nice");
//    }
}
