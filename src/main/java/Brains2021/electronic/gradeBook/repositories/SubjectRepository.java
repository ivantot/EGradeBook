package Brains2021.electronic.gradeBook.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import Brains2021.electronic.gradeBook.entites.SubjectEntity;
import Brains2021.electronic.gradeBook.utils.enums.ESubjectName;

public interface SubjectRepository extends CrudRepository<SubjectEntity, Long> {

	Optional<SubjectEntity> findByNameAndYearOfSchooling(ESubjectName subjectName, String yearOfSchooling);

	Optional<SubjectEntity> findByName(ESubjectName subjectName);

	List<SubjectEntity> findAllByName(ESubjectName subjectName);

}
