package orm.it;

import orm.entity.User;
import orm.model.Role;
import orm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserCrudIT {

    @Autowired
    private UserRepository userRepository;

    @Test
    void createAndReadUser() {
        User user = User.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("secret")
                .role(Role.STUDENT)
                .build();

        User saved = userRepository.save(user);

        User found = userRepository.findById(saved.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        assertThat(found.getId()).isNotNull();
        assertThat(found.getName()).isEqualTo("John Doe");
        assertThat(found.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(found.getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    void updateUserRole() {
        User user = User.builder()
                .name("Teacher")
                .email("teacher@example.com")
                .password("secret")
                .role(Role.STUDENT)
                .build();

        User saved = userRepository.save(user);

        saved.setRole(Role.TEACHER);
        User updated = userRepository.save(saved);

        assertThat(updated.getRole()).isEqualTo(Role.TEACHER);
    }

    @Test
    void deleteUser() {
        User user = User.builder()
                .name("To Delete")
                .email("delete.me@example.com")
                .password("secret")
                .role(Role.STUDENT)
                .build();

        User saved = userRepository.save(user);
        Long id = saved.getId();

        userRepository.deleteById(id);

        assertThat(userRepository.findById(id)).isEmpty();
    }

    @Test
    void listUsers() {
        List<User> all = userRepository.findAll();
        assertThat(all).isNotNull(); // просто проверяем, что запрос работает
    }
}
