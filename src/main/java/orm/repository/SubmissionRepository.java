package orm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import orm.entity.Submission;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByStudent_Id(Long studentId);

    List<Submission> findByAssignment_Id(Long assignmentId);

    Optional<Submission> findByAssignment_IdAndStudent_Id(Long assignmentId, Long studentId);

    boolean existsByAssignment_IdAndStudent_Id(Long assignmentId, Long studentId);
}
