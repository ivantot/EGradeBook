package Brains2021.electronic.gradeBook.repositories;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import Brains2021.electronic.gradeBook.entites.StudentGroupEntity;
import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;

public interface TeacherRepository extends PagingAndSortingRepository<TeacherEntity, Long> {

	Optional<TeacherEntity> findByInChargeOf(StudentGroupEntity studentGroup);

}
