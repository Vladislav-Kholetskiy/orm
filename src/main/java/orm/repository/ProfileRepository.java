package orm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import orm.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
