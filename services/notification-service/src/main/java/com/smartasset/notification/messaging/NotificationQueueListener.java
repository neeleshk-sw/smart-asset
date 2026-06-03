package com.smartasset.notification.messaging;

import com.smartasset.notification.domain.Notification;
import com.smartasset.notification.service.NotificationService;
import com.smartasset.notification.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationQueueListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationQueueListener.class);
    private final NotificationService notificationService;

    public NotificationQueueListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consumeNotification(Notification notification) {
        log.info("Received notification from RabbitMQ: {} for {}", notification.getSubject(),
                notification.getRecipientEmail());
        try {
            // Processing logic (e.g., sending email)
            notificationService.send(notification);
        } catch (Exception e) {
            log.error("Failed to process notification: {}", e.getMessage());
        }
    }
}
