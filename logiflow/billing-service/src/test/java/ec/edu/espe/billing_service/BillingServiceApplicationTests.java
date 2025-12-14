package ec.edu.espe.billing_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class BillingServiceApplicationTests {

	@Test
	void contextLoads() {
	}
    @Test
    void main_noLanzaExcepcion() {
        assertDoesNotThrow(() ->
                BillingServiceApplication.main(new String[]{}));
    }

}
