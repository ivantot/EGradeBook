package Brains2021.electronic.gradeBook.controllers;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.dtos.in.CreateSubjectDTO;
import Brains2021.electronic.gradeBook.entites.StudentGroupTakingASubjectEntity;
import Brains2021.electronic.gradeBook.entites.SubjectEntity;
import Brains2021.electronic.gradeBook.entites.TeacherSubjectEntity;
import Brains2021.electronic.gradeBook.repositories.StudentGroupTakingASubjectRepository;
import Brains2021.electronic.gradeBook.repositories.SubjectRepository;
import Brains2021.electronic.gradeBook.repositories.TeacherSubjectRepository;
import Brains2021.electronic.gradeBook.security.Views;
import Brains2021.electronic.gradeBook.services.subject.SubjectService;
import Brains2021.electronic.gradeBook.utils.RESTError;
import Brains2021.electronic.gradeBook.utils.enums.ESubjectName;

@RestController
@RequestMapping(path = "/api/v1/subjects")
public class SubjectController {

	@Autowired
	private SubjectRepository subjectRepo;

	@Autowired
	private SubjectService subjectService;

	@Autowired
	private TeacherSubjectRepository teacherSubjectRepo;

	@Autowired
	private StudentGroupTakingASubjectRepository studentGroupTakingASubjectRepo;

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	/*****************************************************************************************************************************
	 * POST endpoint for administrator looking to create new subject, meant to be accessed by IT specialist -- postman code 005 --
	 * 
	 * @param subject
	 * @return if ok, new subject
	 *****************************************************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.POST, path = "/admin/newSubject")
	public ResponseEntity<?> postNewSubject(@Valid @RequestBody CreateSubjectDTO subject) {

		logger.info("**POST NEW SUBJECT** Endpoint for posting a new subject entered successfuly.");

		logger.info("**POST NEW SUBJECT** Attempt to see if subject name is valid.");
		if (!subjectService.isSubjectInEnum(subject.getName())) {
			logger.warn("**POST NEW SUBJECT** Subject name invalid, ESubjectName for details.");
			return new ResponseEntity<RESTError>(
					new RESTError(2000, "Subject name not allowed, check ESubjectName for details."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**POST NEW SUBJECT** Subject name is valid.");

		// check db for subject-schooling year

		logger.info("**POST NEW SUBJECT** Attempt to see if subject and schooling year combination exists in db.");
		if (subjectRepo
				.findByNameAndYearOfSchooling(ESubjectName.valueOf(subject.getName()), subject.getYearOfSchooling())
				.isPresent()) {
			logger.warn("**POST NEW SUBJECT** Subject and schooling year combination exists in db.");
			return new ResponseEntity<RESTError>(
					new RESTError(2001, "Subject for a given year of schooling already in the database."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**POST NEW SUBJECT** No subject and schooling year combination present in db.");

		logger.info("**POST NEW TEACHER** Attempting to translate input DTO to Entity.");
		return subjectService.createdSubjectDTOtranslation(subjectService.createSubjectDTOtranslation(subject));

	}

	/*****************************************************************************************************************************
	 * PUT endpoint for administrator looking to edit a subject, meant to be accessed by IT specialist -- postman code 042 --
	 * 
	 * @param subjectID, subjectDTO
	 * @return if ok, updated subject
	 *****************************************************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/putSubject/{subjectID}")
	public ResponseEntity<?> putSubject(@Valid @RequestBody CreateSubjectDTO subject, @PathVariable Long subjectID) {

		logger.info("**CHANGE SUBJECT** Endpoint for posting a new subject entered successfuly.");

		// check if subject is in database
		Optional<SubjectEntity> ogSubject = subjectRepo.findById(subjectID);
		if (ogSubject.isEmpty()) {
			logger.info("**CHANGE SUBJECT** Attempt to find a subject in database.");
			// initial check to see if there is a subject with given id
			if (ogSubject.isEmpty()) {
				logger.warn("**CHANGE SUBJECT** No subject with given id in database.");
				return new ResponseEntity<RESTError>(new RESTError(1530, "No subject with given id in database."),
						HttpStatus.NOT_FOUND);
			}
		}

		// validate that subject name is acceptable
		logger.info("**CHANGE SUBJECT** Attempt to see if subject name is valid.");
		if (!subjectService.isSubjectInEnum(subject.getName())) {
			logger.warn("**CHANGE SUBJECT** Subject name invalid, ESubjectName for details.");
			return new ResponseEntity<RESTError>(
					new RESTError(2000, "Subject name not allowed, check ESubjectName for details."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**CHANGE SUBJECT** Subject name is valid.");

		// check for schooling year - subject combination in database

		SubjectEntity updSubject = subjectService.updateSubjectDTOtranslation(subject, subjectID);

		logger.info("**CHANGE SUBJECT** Attempt to see if subject and schooling year combination exists in db.");
		// first, see if changed year and new value for year are the same, if so break out, else check for collision with database subjects
		if (!ogSubject.get().getYearOfSchooling().equals(updSubject.getYearOfSchooling()) && subjectRepo
				.findByNameAndYearOfSchooling((updSubject.getName()), subject.getYearOfSchooling()).isPresent()) {
			logger.warn("**CHANGE SUBJECT** Subject and schooling year combination exists in db.");
			return new ResponseEntity<RESTError>(
					new RESTError(2001, "Subject for a given year of schooling already in the database."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**CHANGE SUBJECT** No subject and schooling year combination present in db.");
		subjectRepo.save(updSubject);

		// update subject

		logger.info("**CHANGE SUBJECT** Attempting to do all translations.");
		return subjectService.createdSubjectDTOtranslation(updSubject);
	}

	/**********************************************************************************************************
	 * PUT/DELETE endpoint for administrator looking to soft delete a subject. -- postman code 034 --
	 * 
	 * @param subjectID
	 * @return if ok, deleted set to 1
	 **********************************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/deleteSubject/{subjectID}")
	public ResponseEntity<?> deleteSubject(@PathVariable Long subjectID) {

		logger.info("**DELETE SUBJECT** Access to the endpoint successful.");

		logger.info("**DELETE SUBJECT** Attempt to find the subject in database.");
		// check existance and deleted state of subject in db
		Optional<SubjectEntity> subject = subjectRepo.findById(subjectID);
		if (subject.isEmpty() || subject.get().getDeleted() == 1) {
			logger.warn("**DELETE SUBJECT** Subject not in database or deleted.");
			return new ResponseEntity<RESTError>(new RESTError(9000, "Subject not in database or deleted."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**DELETE SUBJECT** Attempt on editing deleted field and saving to db.");
		subject.get().setDeleted(1);
		subjectRepo.save(subject.get());
		logger.info("**DELETE SUBJECT** Attempt successful.");

		// update teachers and subjects, set deleted to 1 where subject appears
		logger.info("**DELETE SUBJECT** Attempt to update teacher-subject where subject appears and save to db.");
		List<TeacherSubjectEntity> ogTeacherSubjects = teacherSubjectRepo.findAllBySubject(subject.get());
		for (TeacherSubjectEntity teacherSubjectEntity : ogTeacherSubjects) {
			teacherSubjectEntity.setDeleted(1);
		}
		teacherSubjectRepo.saveAll(ogTeacherSubjects);

		// update teachers and subjects and student gropus, set deleted to 1 where subject appears
		logger.info(
				"**DELETE SUBJECT** Attempt to update student group and teacher-subject where subject appears and save to db.");
		List<StudentGroupTakingASubjectEntity> ogStudentGroupsTakingASubject = studentGroupTakingASubjectRepo
				.findAllBySubject(subject.get());
		for (StudentGroupTakingASubjectEntity studentGroupTakingASubjectEntity : ogStudentGroupsTakingASubject) {
			studentGroupTakingASubjectEntity.setDeleted(1);
		}
		studentGroupTakingASubjectRepo.saveAll(ogStudentGroupsTakingASubject);

		logger.info("**DELETE SUBJECT** Attempt successful.");

		return new ResponseEntity<String>("Subject " + subject.get().getName().toString()
				+ " and all related teacher subject relations deleted from the database.", HttpStatus.OK);
	}

	/**********************************************************************************************************
	 * PUT/DELETE endpoint for administrator looking to restore a subject. -- postman code 035 --
	 * 
	 * @param subjectID
	 * @return if ok, deleted set to 0
	 **********************************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/restoreSubject/{subjectID}")
	public ResponseEntity<?> restoreSubject(@PathVariable Long subjectID) {

		logger.info("**RESTORE SUBJECT** Access to the endpoint successful.");

		logger.info("**RESTORE SUBJECT** Attempt to find a deleted subject in database.");
		// check existance and deleted state of subject in db
		Optional<SubjectEntity> subject = subjectRepo.findById(subjectID);
		if (subject.isEmpty() || subject.get().getDeleted() == 0) {
			logger.warn("**RESTORE SUBJECT** Subject not in database or active.");
			return new ResponseEntity<RESTError>(new RESTError(9000, "Subject not in database or active."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**RESTORE SUBJECT** Attempt on editing deleted field and saving to db.");
		subject.get().setDeleted(0);
		subjectRepo.save(subject.get());
		logger.info("**RESTORE SUBJECT** Attempt successful.");

		// teachers and subjects not updated, meant to be created from scratch

		return new ResponseEntity<String>("Subject " + subject.get().getName().toString() + " reinstated in db.",
				HttpStatus.OK);
	}

	/***************************************************************************************
	 * GET endpoint for administrator looking to fetch all subjects. -- postman code 052 --
	 * 
	 * @param 
	 * @return if ok list of all subjects in database
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/")
	public ResponseEntity<?> getAllSubjects() {

		logger.info("**GET ALL SUBJECTS** Access to the endpoint successful.");

		logger.info("**GET ALL SUBJECTS** Attempt to find subjects in database.");
		// initial check to see if there are any subjects at all
		if (subjectRepo.findAll() == null) {
			logger.warn("**GET ALL SUBJECTS** No subjects in database.");
			return new ResponseEntity<RESTError>(new RESTError(1530, "No subjects found in database."),
					HttpStatus.NOT_FOUND);
		}

		logger.info("**GET ALL SUBJECTS** Attempt successful, list retrieved. Exiting controller");

		return new ResponseEntity<List<SubjectEntity>>((List<SubjectEntity>) subjectRepo.findAll(), HttpStatus.OK);
	}

	/***************************************************************************************
	 * GET endpoint for administrator looking to fetch subject by ID. -- postman code 072 --
	 * 
	 * @param 
	 * @return if ok subject by id
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/{subjectID}")
	public ResponseEntity<?> getSubjectByID(@PathVariable Long subjectID) {

		logger.info("**GET SUBJECT BY ID** Access to the endpoint successful.");

		Optional<SubjectEntity> ogSubject = subjectRepo.findById(subjectID);
		logger.info("**GET SUBJECT BY ID** Attempt to find a subject in database.");
		// initial check to see if there is a subject with given id
		if (ogSubject.isEmpty()) {
			logger.warn("**GET SUBJECT BY ID** No subject with given id in database.");
			return new ResponseEntity<RESTError>(new RESTError(1530, "No subject with given id in database."),
					HttpStatus.NOT_FOUND);
		}

		logger.info("**GET SUBJECT BY ID** Attempt successful, subject retrieved. Exiting controller");

		return new ResponseEntity<SubjectEntity>(ogSubject.get(), HttpStatus.OK);
	}

}