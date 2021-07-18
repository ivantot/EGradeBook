package Brains2021.electronic.gradeBook.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import Brains2021.electronic.gradeBook.entites.StudentGroupEntity;

public interface StudentGroupRepository extends CrudRepository<StudentGroupEntity, Long> {

	Optional<StudentGroupEntity> findByYearAndYearIndex(String year, Integer yearIndex);
}
