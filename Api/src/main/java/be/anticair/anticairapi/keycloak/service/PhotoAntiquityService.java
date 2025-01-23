package be.anticair.anticairapi.keycloak.service;

import be.anticair.anticairapi.Class.Listing;
import be.anticair.anticairapi.Class.PhotoAntiquity;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A Service for the Photo Antiquity
 */
@Service
public class PhotoAntiquityService {

    private PhotoAntiquityRepository photoAntiquityRepository;

    public PhotoAntiquityService(PhotoAntiquityRepository photoAntiquityRepository) {
        this.photoAntiquityRepository = photoAntiquityRepository;
    }

    /**
     * Updates the photos associated with a specific antiquity.
     *
     * <p>This method performs the following operations:
     * <ul>
     *   <li>Deletes all existing photos associated with the specified antiquity ID.</li>
     *   <li>Processes and saves the provided list of new photos to both the file system and the database.</li>
     * </ul>
     * If a file is empty or an error occurs during processing, an {@link IOException} is thrown.</p>
     *
     * @param antiquityId the ID of the antiquity whose photos are being updated
     * @param photos a list of {@link MultipartFile} objects representing the new photos to be associated with the antiquity
     * @throws IOException if an error occurs while processing or saving the files
     *
     * @author Neve Thierry
     * @see PhotoAntiquityRepository#deleteByIdAntiquity(Integer)
     * @see MultipartFile
     */
    @Transactional
    public void updatePhotos(Integer antiquityId, List<MultipartFile> photos) throws IOException {
        try {
            // Delete the old images
            photoAntiquityRepository.deleteByIdAntiquity(antiquityId);

            // Add the new images
            for (MultipartFile file : photos) {
                // Verify if the files isn't null
                if (file.isEmpty()) {
                    throw new IOException("The file is empty : " + file.getOriginalFilename());
                }

                // Save the file on the HD
                String filePath = saveFile(file);

                PhotoAntiquity photo = new PhotoAntiquity();
                photo.setPathPhoto(filePath);
                photo.setIdAntiquity(antiquityId);

                // Save on the database
                photoAntiquityRepository.save(photo);
            }
        } catch (IOException e) {
            
            System.err.println("Error with the update of the pictures : " + e.getMessage());
            throw new IOException("Error with the update of the pictures : " + e.getMessage(), e);
        }
    }

    /**
     * Create a new photo for an antiquity.
     *
     * @param antiquity The antiquity to associate the photo with.
     * @param photoFile The photo file to save.
     * @return The created photo.
     * @throws IOException If an error occurs while saving the photo.
     * @author Blommaert Youry
     */
    public PhotoAntiquity createPhotoAntiquity(Listing antiquity, MultipartFile photoFile) throws IOException {
        // Save the photo file
        String photoPath = saveFile(photoFile);

        // Create the PhotoAntiquity object
        PhotoAntiquity photoAntiquity = new PhotoAntiquity();
        photoAntiquity.setPathPhoto(photoPath);
        photoAntiquity.setIdAntiquity(antiquity.getIdAntiquity());

        // Sauvegarder dans la base de données
        return photoAntiquityRepository.save(photoAntiquity);
    }

    /**
     * Create a list of photos for an antiquity.
     *
     * @param antiquity The antiquity to associate the photos with.
     * @param photoFiles The photo files to save.
     * @return The list of created photos.
     * @throws IOException If an error occurs while saving the photos.
     * @author Blommaert Youry
     */
    public List<PhotoAntiquity> createPhotoAntiquities(Listing antiquity, List<MultipartFile> photoFiles) throws IOException {
        List<PhotoAntiquity> savedPhotos = new ArrayList<>();

        for (MultipartFile photoFile : photoFiles) {
            PhotoAntiquity savedPhoto = createPhotoAntiquity(antiquity, photoFile);
            savedPhotos.add(savedPhoto);
        }

        return savedPhotos;
    }

    /**
     * Save a file on the server.
     *
     * @param file The file to save.
     * @return The path to the saved file.
     * @throws IOException If an error occurs while saving the file.
     * @author Blommaert Youry, Neve Thierry
     */
    public String saveFile(MultipartFile file) throws IOException {
        // Define the directory where the files will be saved
        String directory = System.getProperty("user.dir") + "/uploads/";

        // Create the directory if it doesn't exist
        File directoryPath = new File(directory);
        if (!directoryPath.exists()) {
            directoryPath.mkdirs();  // Create the directory
        }

        if (file.isEmpty()) {
            throw new IOException("The file is empty!");
        }

        // Generate a unique name for the file (UUID)
        String extension = "";
        String originalFileName = file.getOriginalFilename();
        if (originalFileName != null) {
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
                extension = originalFileName.substring(dotIndex); // Extract the file extension
            }
        }

        String newFileName = UUID.randomUUID().toString() + extension; // Create a unique name

        // Create the file path
        String filePath = directory + newFileName;
        File dest = new File(filePath);

        // Save the file
        file.transferTo(dest);

        // Return the path to the saved file
        return "/uploads/" + dest.getName();
    }

    /**
     * Retrieves a list of photos associated with a specific antiquity.
     *
     * <p>This method queries the database to fetch all photos linked to the antiquity
     * identified by the provided ID. Each photo is represented as a {@link PhotoAntiquity} object.</p>
     *
     * @param id the ID of the antiquity whose photos are to be retrieved
     * @return a list of {@link PhotoAntiquity} objects representing the photos associated
     *         with the specified antiquity
     *
     * @author Neve Thierry
     * @see PhotoAntiquity
     * @see PhotoAntiquityRepository#findByIdAntiquity(Integer)
     */
    public List<PhotoAntiquity> findByIdAntiquity(Integer id) {
        // Récupérer les photos associées
        return photoAntiquityRepository.findByIdAntiquity(id);
    }

    public List<String> findPathByIdAntiquity(Integer id) {
        // Récupérer les photos associées
        return photoAntiquityRepository.findPathByIdAntiquity(id);
    }

}
