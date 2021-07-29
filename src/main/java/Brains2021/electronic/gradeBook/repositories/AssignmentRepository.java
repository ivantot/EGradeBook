package Brains2021.electronic.gradeBook.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import Brains2021.electronic.gradeBook.entites.AssignmentEntity;
import Brains2021.electronic.gradeBook.entites.StudentGroupEntity;
import Brains2021.electronic.gradeBook.entites.TeacherSubjectEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentEntity;
import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;

public interface AssignmentRepository extends PagingAndSortingRepository<AssignmentEntity, Long> {

	List<AssignmentEntity> findByTeacherIssuing(TeacherSubjectEntity teacherIssuing);

	List<AssignmentEntity> findByAssignedTo(StudentEntity student);

	Page<AssignmentEntity> findAllByAssignedTo(StudentEntity student, Pageable pageable);

	@Query(value = "SELECT a FROM AssignmentEntity a JOIN a.assignedTo b WHERE b.belongsToStudentGroup =:homeroomGroup")
	Page<AssignmentEntity> findAllByStudentGroup(@Param("homeroomGroup") StudentGroupEntity homeroomGroup,
			Pageable pageable);

	@Query(value = "SELECT a FROM AssignmentEntity a JOIN a.teacherIssuing b WHERE b.teacher =:teacher")
	Page<AssignmentEntity> findAllByTeacherIssuing(@Param("teacher") TeacherEntity teacher, Pageable pageable);
}
