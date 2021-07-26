package Brains2021.electronic.gradeBook.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import Brains2021.electronic.gradeBook.entites.users.ParentEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentParentEntity;

public interface StudentParentRepository extends CrudRepository<StudentParentEntity, Long> {

	List<StudentParentEntity> findByStudent(StudentEntity student);

	Optional<StudentParentEntity> findByStudentAndParentAndDeleted(StudentEntity student, ParentEntity parent,
			Integer deleted);

}
