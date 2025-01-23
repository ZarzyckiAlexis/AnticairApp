package be.anticair.anticairapi.keycloak.service;

import be.anticair.anticairapi.Class.PhotoAntiquity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * A Repository for the Photo Antiquity
 * */
@Repository
public interface PhotoAntiquityRepository extends JpaRepository<PhotoAntiquity, Integer> {
    @Transactional
    @Modifying
    @Query("DELETE FROM PhotoAntiquity p WHERE p.idAntiquity = ?1")
    void deleteByIdAntiquity(Integer idAntiquity);

    /**
     * Retrieves all photos associated with a specific antiquity ID.
     *
     * <p>This method queries the database to find all photos linked to the antiquity
     * identified by the given ID. Each photo is returned as a {@link PhotoAntiquity} object,
     * which contains information such as the file path and antiquity ID.</p>
     *
     * @param idAntiquity the ID of the antiquity whose associated photos are to be retrieved
     * @return a list of {@link PhotoAntiquity} objects representing the photos associated
     *         with the specified antiquity
     *
     * @author Neve Thierry
     * @see PhotoAntiquity
     */
    List<PhotoAntiquity> findByIdAntiquity(Integer idAntiquity);

    /**
     * Find the photo path from a Id Antiquity
     * @param idAntiquity the ID of the antiquity
     * @return A List of the path
     */
    @Query("SELECT p.pathPhoto FROM PhotoAntiquity p WHERE p.idAntiquity = ?1")
    List<String> findPathByIdAntiquity(Integer idAntiquity);
}
