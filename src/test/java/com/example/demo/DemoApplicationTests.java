package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jwt.secret=test-secret-with-at-least-thirty-two-bytes")
class DemoApplicationTests {

    @Test
    void contextLoads() {
    }

}
