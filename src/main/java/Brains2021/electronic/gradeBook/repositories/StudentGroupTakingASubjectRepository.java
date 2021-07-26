package Brains2021.electronic.gradeBook.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import Brains2021.electronic.gradeBook.entites.StudentGroupEntity;
import Brains2021.electronic.gradeBook.entites.StudentGroupTakingASubjectEntity;
import Brains2021.electronic.gradeBook.entites.SubjectEntity;
import Brains2021.electronic.gradeBook.entites.TeacherSubjectEntity;

public interface StudentGroupTakingASubjectRepository extends CrudRepository<StudentGroupTakingASubjectEntity, Long> {

	Optional<StudentGroupTakingASubjectEntity> findByStudentGroupAndTeacherSubject(StudentGroupEntity studentGroup,
			TeacherSubjectEntity teacherSubject);

	List<StudentGroupTakingASubjectEntity> findAllByTeacherSubject(TeacherSubjectEntity teacherSubject);

	@Query(value = "SELECT s FROM StudentGroupTakingASubjectEntity s LEFT JOIN FETCH s.teacherSubject t WHERE t.subject = :subject")
	List<StudentGroupTakingASubjectEntity> findAllBySubject(@Param(value = "subject") SubjectEntity subject);

}
