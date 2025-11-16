package orm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import orm.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_IdAndReadFalse(Long userId);
}