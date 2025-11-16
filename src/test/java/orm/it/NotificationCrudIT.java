package orm.it;

import orm.OrmApplication;
import orm.entity.Notification;
import orm.entity.User;
import orm.model.NotificationType;
import orm.model.Role;
import orm.repository.NotificationRepository;
import orm.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OrmApplication.class)
@Transactional
@Rollback
class NotificationCrudIT {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("CRUD для уведомлений")
    void notificationCrudFlow() {
        // Пользователь
        User user = userRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    User u = new User();
                    u.setName("Notif User");
                    u.setEmail("notif.user@example.com");
                    u.setPassword("pwd");
                    u.setRole(Role.STUDENT);
                    u.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(u);
                });

        // CREATE
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.GENERAL);
        notification.setMessage("Добро пожаловать на платформу!");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.isRead()).isFalse();

        // READ
        Notification persisted = notificationRepository.findById(saved.getId()).orElseThrow();
        assertThat(persisted.getMessage()).contains("Добро пожаловать");
        assertThat(persisted.getUser().getId()).isEqualTo(user.getId());

        // UPDATE (пометить прочитанным)
        persisted.setRead(true);
        notificationRepository.save(persisted);

        Notification updated = notificationRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.isRead()).isTrue();

        // DELETE
        notificationRepository.delete(updated);

        List<Notification> all = notificationRepository.findAll();
        assertThat(all.stream().noneMatch(n -> n.getId().equals(saved.getId()))).isTrue();
    }
}
