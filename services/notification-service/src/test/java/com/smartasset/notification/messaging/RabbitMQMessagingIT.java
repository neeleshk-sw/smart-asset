package com.smartasset.notification.messaging;

import com.smartasset.notification.domain.Notification;
import com.smartasset.notification.repository.NotificationRepository;
import com.smartasset.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
public class RabbitMQMessagingIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("smartasset")
            .withUsername("user")
            .withPassword("password")
            .withInitScript("init-db.sql");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.12-management");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void sendNotification_ShouldStoreAndPublishToRabbitMQ() {
        Notification notification = new Notification();
        notification.setRecipientEmail("test@example.com");
        notification.setRecipientId(UUID.randomUUID());
        notification.setSubject("Test Title");
        notification.setContent("Test Message");
        notification.setType(Notification.NotificationType.EMAIL);
        notification.setStatus(Notification.NotificationStatus.PENDING);

        Notification sent = notificationService.send(notification);

        assertThat(sent.getId()).isNotNull();
        assertThat(sent.getStatus()).isEqualTo(Notification.NotificationStatus.SENT);
        assertThat(sent.getSentAt()).isNotNull();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Notification stored = notificationRepository.findById(sent.getId()).orElse(null);
            assertThat(stored).isNotNull();
            assertThat(stored.getStatus()).isEqualTo(Notification.NotificationStatus.SENT);
        });
    }
}
