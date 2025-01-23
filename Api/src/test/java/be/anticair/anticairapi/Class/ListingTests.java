package be.anticair.anticairapi.Class;

import be.anticair.anticairapi.enumeration.AntiquityState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test that verify the Listing function's
 * @Author Verly Noah
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true"
})
public class ListingTests {

    /**
     * The antiquity that will be used for the test
     */
    private Listing listing;

    /**
     * The mail that will be use for the owner of the antiquity
     */
    private static final String mailSeller = "test-user@gmail.com";
    /**
     * The mail that will be use for the owner of the antiquity
     */
    private static final String mailAntiquarian = "test-antiquarian@gmail.com";

    /**
     * SetUp to create a antiquity
     */
    @BeforeEach
    @DisplayName("SetUp to create a antiquity")
    public void setUp(){
        listing = new Listing(1,100.0,"C'est Jésus","Statut",mailAntiquarian,0,true,mailSeller);
    }

    /**
     * Test to check the application of the commission
     * @Author Noah Verly
     */
    @Test
    @DisplayName("Test to check the application of the commission")
    public void testApplyCommission(){
        //delta 0,01, is used as tolerance for the floating point
        assertEquals(100.0, listing.getPriceAntiquity());
        listing.applyCommission();
        assertEquals(120.0, listing.getPriceAntiquity(), 0.01);
        listing.applyCommission();
        assertEquals(144.0, listing.getPriceAntiquity(), 0.01);
        listing.applyCommission();
        assertEquals(172.8, listing.getPriceAntiquity(), 0.01);
        listing.applyCommission();
        assertEquals(207.36, listing.getPriceAntiquity(), 0.01);
        listing.applyCommission();
        assertEquals(248.83, listing.getPriceAntiquity(), 0.01);
    }
}
