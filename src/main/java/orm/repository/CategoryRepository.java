package orm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import orm.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);
}
