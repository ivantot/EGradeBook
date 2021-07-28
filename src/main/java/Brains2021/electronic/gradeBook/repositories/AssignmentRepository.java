package Brains2021.electronic.gradeBook.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import Brains2021.electronic.gradeBook.entites.AssignmentEntity;
import Brains2021.electronic.gradeBook.entites.TeacherSubjectEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentEntity;

public interface AssignmentRepository extends CrudRepository<AssignmentEntity, Long> {

	List<AssignmentEntity> findByTeacherIssuing(TeacherSubjectEntity teacherIssuing);

	List<AssignmentEntity> findByAssignedTo(StudentEntity student);

}
