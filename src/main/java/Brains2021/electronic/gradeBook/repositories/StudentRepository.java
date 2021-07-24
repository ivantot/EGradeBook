package Brains2021.electronic.gradeBook.repositories;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import Brains2021.electronic.gradeBook.entites.users.StudentEntity;
import Brains2021.electronic.gradeBook.entites.users.UserEntity;

public interface StudentRepository extends PagingAndSortingRepository<StudentEntity, Long> {

	public Optional<UserEntity> findByStudentUniqueNumber(String studentUniqueNumber);

}
