package be.anticair.anticairapi.keycloak.controller;

import be.anticair.anticairapi.Class.PhotoAntiquity;
import be.anticair.anticairapi.keycloak.service.PhotoAntiquityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller to the photos of antiquities
 * @author Dewever David
 */
@RestController
@RequestMapping("/api/photoAntiquity")
public class PhotoAntiquityController {
    @Autowired
    private PhotoAntiquityService photoAntiquityService;

    /**
     * Get all the photos of an antiquity.
     *
     * @param id The id of the antiquity to get the photos of.
     * @return The list of photos of the antiquity.
     * @author Blommmaert Youry
     */
    @GetMapping("/{id}")
    public ResponseEntity<List<PhotoAntiquity>> getPhotosByAntiquityId(@PathVariable Long id) {
        return ResponseEntity.ok(photoAntiquityService.findByIdAntiquity(Math.toIntExact(id)));
    }

    /**
     * Get the list of the antiquity photo's name
     *
     * @param idAntiquity The id of the antiquity to get the photos of.
     * @return  the list of the antiquity photo's name
     * @author Blommmaert Youry
     */
    @GetMapping("/images/{id}")
    public ResponseEntity<List<String>> getPathPhotosByAntiquityId(@PathVariable("id") Long idAntiquity) {
        return ResponseEntity.ok(photoAntiquityService.findPathByIdAntiquity(Math.toIntExact(idAntiquity)));
    }
}
