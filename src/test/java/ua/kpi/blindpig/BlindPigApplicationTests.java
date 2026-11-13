package ua.kpi.blindpig;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Full context needs bean implementations from every domain (e.g. OrderService); enable after integration")
@SpringBootTest
class BlindPigApplicationTests {

    @Test
    void contextLoads() {
    }

}
