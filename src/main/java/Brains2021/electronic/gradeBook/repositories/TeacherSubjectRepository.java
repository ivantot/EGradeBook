package Brains2021.electronic.gradeBook.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import Brains2021.electronic.gradeBook.entites.TeacherSubjectEntity;
import Brains2021.electronic.gradeBook.utils.enums.ESubjectName;

public interface TeacherSubjectRepository extends CrudRepository<TeacherSubjectEntity, Long> {

	@Query(value = "SELECT a FROM TeacherSubjectEntity a " + "LEFT JOIN FETCH a.teacher b "
			+ "LEFT JOIN FETCH a.subject c " + "WHERE b.username = :username and c.name = :subject")
	Optional<TeacherSubjectEntity> findBySubjectAndTeacher(@Param("subject") ESubjectName subject,
			@Param("username") String username);

	Optional<TeacherSubjectEntity> findByIdAndDeleted(Long id, Integer deleted);

}
