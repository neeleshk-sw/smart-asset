package com.smartasset.notification.service;

import com.smartasset.notification.domain.Notification;
import com.smartasset.notification.repository.NotificationRepository;
import com.smartasset.common.exception.ResourceNotFoundException;
import com.smartasset.notification.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final RabbitTemplate rabbitTemplate;

    public NotificationService(NotificationRepository notificationRepository, RabbitTemplate rabbitTemplate) {
        this.notificationRepository = notificationRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    public Notification findById(UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
    }

    public Notification send(Notification notification) {
        // Publish to RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                notification);

        notification.setStatus(Notification.NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        Notification saved = notificationRepository.save(notification);
        log.info("Sent notification {} to {} via RabbitMQ", saved.getId(), saved.getRecipientEmail());
        return saved;
    }

    public Notification retry(UUID id) {
        Notification notification = findById(id);
        notification.setRetryCount(notification.getRetryCount() + 1);
        notification.setStatus(Notification.NotificationStatus.RETRYING);
        return notificationRepository.save(notification);
    }
}
