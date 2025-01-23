package be.anticair.anticairapi.Class;


import be.anticair.anticairapi.enumeration.AntiquityState;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * Listing Class
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "antiquity")
public class Listing {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id_antiquity")
        private Integer idAntiquity;

        @Column(name = "price_antiquity")
        private Double priceAntiquity;

        @Column(name = "description_antiquity")
        private String descriptionAntiquity;

        @Column(name = "title_antiquity")
        private String titleAntiquity;

        @Column(name = "mail_antiquarian")
        private String mailAntiquarian;

        @Column(name = "state")
        private Integer state;

        @Column(name = "is_display")
        private Boolean isDisplay;

        @Column(name = "mail_seller")
        private String mailSeller;



        /**
         * Function to apply the commission
         * @Author Verly Noah
         */
        public void applyCommission(){
                this.priceAntiquity += this.priceAntiquity * 0.20;
        }
}
