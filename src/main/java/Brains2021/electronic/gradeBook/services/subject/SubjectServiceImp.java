package Brains2021.electronic.gradeBook.services.subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

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

		logger.info("**POST NEW SUBJECT** Entered service for DTO translation to entity.");
		// translate DTO to entity and save to db
		logger.info("**POST NEW SUBJECT** Translating started.");
		SubjectEntity newSubject = new SubjectEntity();
		newSubject.setName(ESubjectName.valueOf(subject.getName()));
		newSubject.setDescription(subject.getDescription());
		newSubject.setYearOfSchooling(subject.getYearOfSchooling());
		newSubject.setWeeklyHoursRequired(subject.getWeeklyHoursRequired());
		newSubject.setDeleted(0);

		logger.info(
				"**POST NEW SUBJECT** Translation complete, saving entity to db and redirecting to service for output DTO translation.");
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

		logger.info("**POST NEW SUBJECT** Entered service for Entity translation to DTO.");
		// translate entity to DTO
		logger.info("**POST NEW SUBJECT** Translating started.");
		CreatedSubjectDTO newSubjectDTO = new CreatedSubjectDTO();
		newSubjectDTO.setDescription(subject.getDescription());
		newSubjectDTO.setName(subject.getName().toString());
		newSubjectDTO.setWeeklyHoursRequired(subject.getWeeklyHoursRequired());
		newSubjectDTO.setYerofSchooling(subject.getYearOfSchooling());
		logger.info(
				"**POST NEW SUBJECT** Translation complete, exiting service and returning to endpoint. All actions complete, subject created.\n"
						+ newSubjectDTO.toString());

		return new ResponseEntity<CreatedSubjectDTO>(newSubjectDTO, HttpStatus.OK);
	}

}
