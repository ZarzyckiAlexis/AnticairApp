package be.anticair.anticairapi.Class;

import lombok.*;


import java.util.List;

/**
 * Listing Class with Photos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListingWithPhotosDto {

    private Integer idAntiquity;
    private Double priceAntiquity;
    private String descriptionAntiquity;
    private String titleAntiquity;
    private String mailSeller;
    private Integer state;
    private Boolean isDisplay;
    private String mailAntiquarian;
    private List<String> photos;

    public ListingWithPhotosDto(Listing listing, List<PhotoAntiquity> photos) {
        this.idAntiquity = listing.getIdAntiquity();
        this.priceAntiquity = listing.getPriceAntiquity();
        this.descriptionAntiquity = listing.getDescriptionAntiquity();
        this.titleAntiquity = listing.getTitleAntiquity();
        this.mailSeller = listing.getMailSeller();
        this.state = listing.getState();
        this.isDisplay = listing.getIsDisplay();
        this.mailAntiquarian = listing.getMailAntiquarian();
        this.photos = photos.stream()
                .map(PhotoAntiquity::getPathPhoto)
                .toList();
    }

}

