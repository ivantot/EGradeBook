package Brains2021.electronic.gradeBook.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;

import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;

public interface TeacherRepository extends PagingAndSortingRepository<TeacherEntity, Long> {

}
