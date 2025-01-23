package be.anticair.anticairapi.keycloak.service;

import be.anticair.anticairapi.enumeration.TypeOfMail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service to send email with html template
 * @author Verly Noah
 */
@Service
public class EmailService {
    /**
     * name of the directory with the template
     */
    private static final String URL_TEMPLATE_HTML = "/TemplateHTML/";
    /**
     * service to have information about the users
     */
    @Autowired
    @Lazy
    private UserService userService;
    /**
     * Allow tos send a mail
     */
    @Autowired
    private JavaMailSender mailSender;

    /**
     * Function to send a mail with html template
     * @param receiver the mail of the receiver
     * @param sender the mail of the sender
     * @param typeOfMail A enumeration with the type of mail a integer and the name of the template
     * @param otherInformation other information, who aren't shared with all template
     * @throws MessagingException error during the creation of the mail
     * @throws IOException error if the access of the template isn't a success
     * @author Verly Noah
     */
    public void sendHtmlEmail(String receiver, String sender, TypeOfMail typeOfMail, Map<String,String> otherInformation) throws MessagingException, IOException {
        if(receiver.isEmpty() || sender.isEmpty() ) return;
       try {
           System.out.println(sender +" "+ receiver);
           //Allow to create email
           MimeMessage message= this.setInformationMail(sender,receiver,typeOfMail.getSubject());

           String htmlTemplate = this.loadFilePath(typeOfMail.getTemplateHTMLName1());
           switch (typeOfMail.getTypeOfMail()) {
               case 1: //Validation of an antiquity, so notify the owner
                  htmlTemplate = this.replaceAntiquityInformation(htmlTemplate,otherInformation);
                   break;
               case 2: //Application of the commission, so notify the antiquarian
                   htmlTemplate = this.replaceAntiquityInformation(htmlTemplate,otherInformation);
                   double priceWithCommission = Double.parseDouble(otherInformation.get("price"));
                   double commissionDouble = priceWithCommission/1.20;
                   priceWithCommission -= commissionDouble;
                   String commissionString = Double.toString(priceWithCommission);
                   htmlTemplate = htmlTemplate.replace("${commission}", commissionString);
                   break;
               case 3: //Rejection of an antiquity, so notify the owner
                   htmlTemplate = this.replaceAntiquityInformation(htmlTemplate,otherInformation);
                   htmlTemplate = htmlTemplate.replace("${note_title}", otherInformation.get("note_title"));
                   htmlTemplate = htmlTemplate.replace("${note_description}", otherInformation.get("note_description"));
                   htmlTemplate = htmlTemplate.replace("${note_price}", otherInformation.get("note_price"));
                   htmlTemplate = htmlTemplate.replace("${note_photo}", otherInformation.get("note_photo"));
                   break;
               case 4: //Warning the initial antiquarian that one of his antiquity has been redistributed
                   break;
               case 5: //Warning the new antiquarian that he get a new antiquity
                   htmlTemplate = this.replaceAntiquityInformation(htmlTemplate,otherInformation);
                   break;
               case 6: // Warning the user that is account status has been changed
                   htmlTemplate = htmlTemplate.replace("${account_newstatus}", otherInformation.get("account_newstatus"));
                   break;
               case 7: // Warning the user that they received a payment
                   htmlTemplate = this.replaceAntiquityInformation(htmlTemplate, otherInformation);

                   // Get the price with commission
                   double priceWithCommission2 = Double.parseDouble(otherInformation.get("price"));

                   // Calculate the commission (20%)
                   double commissionDouble2 = priceWithCommission2 * 0.20;

                   // Format commission to two decimal places
                   DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);  // Force dot separator
                   DecimalFormat df = new DecimalFormat("#.00", symbols);  // Always two decimal places
                   String commissionString2 = df.format(commissionDouble2);

                   // Replace the commission placeholder in the HTML template
                   htmlTemplate = htmlTemplate.replace("${commission}", commissionString2);
                   break;
                case 10:
                   break;
               case 8:
                   break;
               default: // Just in case
                  return;

           }
           //Shared information
           htmlTemplate = this.replaceSharedInformation(htmlTemplate,receiver);

           //Set the content of the mail
           message.setContent(htmlTemplate, "text/html; charset=utf-8");
           //Send it
           mailSender.send(message);
       }catch (MessagingException e){
           throw new MessagingException("Error while sending email : " + e.getMessage());
       }

    }

    /**
     * Allow to load the base information of the mail
     * @param sender Email of the sender
     * @param receiver Email of the receiver
     * @param subject Subject of the email
     * @return return the based email
     * @throws MessagingException error during the creation of the mail
     * @author Verly Noah
     */
    private MimeMessage setInformationMail(String sender, String receiver, String subject) throws MessagingException {

        try{
            //Allow to create email
            MimeMessage message = mailSender.createMimeMessage();
            //To set from who
            message.setFrom(new InternetAddress(sender));
            //To set the receiver
            message.setRecipients(MimeMessage.RecipientType.TO, receiver);
            //To the subject
            message.setSubject(subject);
            return message;
        }catch (MessagingException e){
            throw new MessagingException("Error while setting email's information : " + e.getMessage());
        }
    }

    /**
     * Allow to load the base information of the mail
     * @param fileName name of the file with the template
     * @return return the based email
     * @throws IOException error if the access of the template isn't a success
     * @author Verly Noah
     */
    private String loadFilePath(String fileName) throws IOException {
        try{
            // Constructing the absolute path to the HTML template
            String filePath = System.getProperty("user.dir") + URL_TEMPLATE_HTML + fileName;

            // read the html template
            return new String(Files.readAllBytes(Paths.get(filePath)));
        }catch (IOException e){
            throw new IOException(e.getMessage());
        }

    }

    /**
     * Allow put the shared information to each mail
     * @param htmlTemplate name of the file with the template
     * @param receiver Email of the receiver
     * @return return the based email
     * @author Verly Noah
     */
    private String replaceSharedInformation(String htmlTemplate, String receiver){
        //Get information about the receiver
        List<UserRepresentation> users = userService.getUsersByEmail(receiver);
        //Replace the name
        htmlTemplate = htmlTemplate.replace("${receiver_name}", users.getFirst().getLastName()+" "+users.getFirst().getFirstName());

        //Replace the year
        String currentYear = String.valueOf(LocalDate.now().getYear());
        htmlTemplate = htmlTemplate.replace("${current_year}", currentYear);
        return htmlTemplate;
    }

    /**
     * Allow to repalce all the antiquity's information
     * @param htmlTemplate the template html
     * @param otherInformation the antiquity's information
     * @return the template with the antiquity's information
     * @author Verly Noah
     */
    private String replaceAntiquityInformation(String htmlTemplate, Map<String,String> otherInformation){
        htmlTemplate = htmlTemplate.replace("${title}", otherInformation.get("title"));
        htmlTemplate = htmlTemplate.replace("${description}", otherInformation.get("description"));
        htmlTemplate = htmlTemplate.replace("${price}", otherInformation.get("price"));
        return htmlTemplate;
    }



}
