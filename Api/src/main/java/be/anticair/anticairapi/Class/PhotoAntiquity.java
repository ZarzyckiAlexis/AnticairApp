package be.anticair.anticairapi.Class;

import jakarta.persistence.*;
import lombok.*;

/**
 * Class Antiquity Photo
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "Photo_Antiquity")
public class PhotoAntiquity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_photo", updatable = false)
    private Integer idPhoto;

    @Column(name = "path_photo", unique = true, nullable = false)
    private String pathPhoto;

    @Column(name = "id_antiquity", nullable = false)
    private Integer idAntiquity;
}
