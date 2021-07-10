package Brains2021.electronic.gradeBook.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;

import Brains2021.electronic.gradeBook.entites.users.StudentEntity;

public interface StudentRepository extends PagingAndSortingRepository<StudentEntity, Long> {

}
