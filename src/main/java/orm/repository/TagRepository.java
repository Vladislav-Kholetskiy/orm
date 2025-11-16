package orm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import orm.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

    boolean existsByName(String name);
}
