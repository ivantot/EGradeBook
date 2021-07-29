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

	/******************************************************************************************************
	 * POST endpoint for administrator looking to create new subject, meant to be accessed by IT specialist
	 * -- postman code adm005 --
	 * 
	 * @param subject
	 * @return if ok, new subject
	 ******************************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.POST, path = "/admin/newSubject")
	public ResponseEntity<?> postNewSubject(@Valid @RequestBody CreateSubjectDTO subject) {

		// check if subject name allowed

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

	/**********************************************************************************************************
	 * PUT/DELETE endpoint for administrator looking to soft delete a subject.
	 * -- postman code adm034 --
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
	 * PUT/DELETE endpoint for administrator looking to restore a subject.
	 * -- postman code adm035 --
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
	 * GET endpoint for administrator looking to fetch all subjects.
	 * -- postman code adm052 --
	 * 
	 * @param 
	 * @return if ok list of all assignemnts in database
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/")
	public ResponseEntity<?> getAllSubjects() {

		logger.info("**GET ALL SUBJECTS** Access to the endpoint successful.");

		logger.info("**GET ALL SUBJECTS** Attempt to find subjects in database.");
		// initial check to see if there are any assignements at all
		if (subjectRepo.findAll() == null) {
			logger.warn("**GET ALL SUBJECTS** No subjects in database.");
			return new ResponseEntity<RESTError>(new RESTError(1530, "No subjects found in database."),
					HttpStatus.NOT_FOUND);
		}

		logger.info("**GET ALL ASSIGNMENTS** Attempt successful, list retrieved. Exiting controller");

		return new ResponseEntity<List<SubjectEntity>>((List<SubjectEntity>) subjectRepo.findAll(), HttpStatus.OK);
	}

}