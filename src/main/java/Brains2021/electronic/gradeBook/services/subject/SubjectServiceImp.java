package Brains2021.electronic.gradeBook.services.subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import Brains2021.electronic.gradeBook.dtos.in.CreateSubjectDTO;
import Brains2021.electronic.gradeBook.dtos.out.CreatedSubjectDTO;
import Brains2021.electronic.gradeBook.entites.SubjectEntity;
import Brains2021.electronic.gradeBook.repositories.SubjectRepository;
import Brains2021.electronic.gradeBook.utils.enums.ESubjectName;

@Service
public class SubjectServiceImp implements SubjectService {

	@Autowired
	private SubjectRepository subjectRepo;

	/**
	 * 
	 * check if ESubject enum contains the provided subject 
	 * 
	 */
	@Override
	public Boolean isSubjectInEnum(String subject) {
		ESubjectName[] allSubjects = ESubjectName.values();
		for (ESubjectName eSubjectName : allSubjects) {
			//do comparison
			if (subject.equals(eSubjectName.toString())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * service that takes an input DTO and translates to entity and populates remaining fields
	 * used for creating Subjects
	 * 
	 */
	@Override
	public SubjectEntity createSubjectDTOtranslation(CreateSubjectDTO subject) {

		// translate DTO to entity and save to db
		SubjectEntity newSubject = new SubjectEntity();
		newSubject.setName(ESubjectName.valueOf(subject.getName()));
		newSubject.setDescription(subject.getDescription());
		newSubject.setYearOfSchooling(subject.getYerofSchooling());
		newSubject.setWeeklyHoursRequired(subject.getWeeklyHoursRequired());
		newSubject.setDeleted(0);

		return subjectRepo.save(newSubject);
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for creating Subjects
	 * 
	 */
	@Override
	public ResponseEntity<?> createdSubjectDTOtranslation(SubjectEntity subject) {
		// translate entity to DTO 
		CreatedSubjectDTO newSubjectDTO = new CreatedSubjectDTO();
		newSubjectDTO.setDescription(subject.getDescription());
		newSubjectDTO.setName(subject.getName().toString());
		newSubjectDTO.setWeeklyHoursRequired(subject.getWeeklyHoursRequired());
		newSubjectDTO.setYerofSchooling(subject.getYearOfSchooling());

		return new ResponseEntity<CreatedSubjectDTO>(newSubjectDTO, HttpStatus.OK);
	}

}
