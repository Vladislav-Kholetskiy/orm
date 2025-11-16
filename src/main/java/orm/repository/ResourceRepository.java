package orm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import orm.entity.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
}
