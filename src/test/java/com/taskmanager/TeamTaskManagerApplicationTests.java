package com.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "app.jwt.secret=test-secret-key-that-is-at-least-256-bits-long-for-testing",
    "app.jwt.expiration=86400000"
})
class TeamTaskManagerApplicationTests {
    @Test
    void contextLoads() {}
}
