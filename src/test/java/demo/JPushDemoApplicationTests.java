package demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = { "jpush.masterSecret=000000000000000000000000", "jpush.appKey=000000000000000000000000" })
class JPushDemoApplicationTests {

    @Test
    void contextLoads() {
    }

}
