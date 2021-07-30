package Brains2021.electronic.gradeBook.services.subject;

import java.util.Optional;

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
		if (subject.getName() != null) {
			newSubject.setName(ESubjectName.valueOf(subject.getName()));
		}
		if (subject.getDescription() != null) {
			newSubject.setDescription(subject.getDescription());
		}
		if (subject.getYearOfSchooling() != null) {
			newSubject.setYearOfSchooling(subject.getYearOfSchooling());
		}
		if (subject.getWeeklyHoursRequired() != null) {
			newSubject.setWeeklyHoursRequired(subject.getWeeklyHoursRequired());
		}
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
		if (subject.getDescription() != null) {
			newSubjectDTO.setDescription(subject.getDescription());
		}
		if (subject.getName() != null) {
			newSubjectDTO.setName(subject.getName().toString());
		}
		if (subject.getWeeklyHoursRequired() != null) {
			newSubjectDTO.setWeeklyHoursRequired(subject.getWeeklyHoursRequired());
		}
		if (subject.getYearOfSchooling() != null) {
			newSubjectDTO.setYerofSchooling(subject.getYearOfSchooling());
		}
		logger.info(
				"**POST NEW SUBJECT** Translation complete, exiting service and returning to endpoint. All actions complete, subject created.\n"
						+ newSubjectDTO.toString());

		return new ResponseEntity<CreatedSubjectDTO>(newSubjectDTO, HttpStatus.OK);
	}

	/**
	 * 
	 * service that takes an input DTO and translates to entity and populates remaining fields
	 * used for updating Subjects
	 * 
	 */
	@Override
	public SubjectEntity updateSubjectDTOtranslation(CreateSubjectDTO subject, Long ogSubjectID) {

		logger.info("**POST NEW SUBJECT** Entered service for DTO translation to entity.");
		// translate DTO to entity and save to db
		logger.info("**POST NEW SUBJECT** Translating started.");
		Optional<SubjectEntity> ogSubject = subjectRepo.findById(ogSubjectID);
		if (subject.getName() != null) {
			ogSubject.get().setName(ESubjectName.valueOf(subject.getName()));
		}
		if (subject.getDescription() != null) {
			ogSubject.get().setDescription(subject.getDescription());
		}
		if (subject.getYearOfSchooling() != null) {
			ogSubject.get().setYearOfSchooling(subject.getYearOfSchooling());
		}
		if (subject.getWeeklyHoursRequired() != null) {
			ogSubject.get().setWeeklyHoursRequired(subject.getWeeklyHoursRequired());
		}
		ogSubject.get().setDeleted(0);

		logger.info("**POST NEW SUBJECT** Translation complete, returning to controller");
		return ogSubject.get();
	}

}
