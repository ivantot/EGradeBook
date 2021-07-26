package Brains2021.electronic.gradeBook.controllers;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.entites.StudentGroupEntity;
import Brains2021.electronic.gradeBook.entites.StudentGroupTakingASubjectEntity;
import Brains2021.electronic.gradeBook.entites.TeacherSubjectEntity;
import Brains2021.electronic.gradeBook.repositories.StudentGroupRepository;
import Brains2021.electronic.gradeBook.repositories.StudentGroupTakingASubjectRepository;
import Brains2021.electronic.gradeBook.repositories.TeacherSubjectRepository;
import Brains2021.electronic.gradeBook.security.Views;
import Brains2021.electronic.gradeBook.utils.RESTError;

@RestController
@RequestMapping(path = "/api/v1/studentGroupTakingASubject")
public class StudentGroupTakingASubjectController {

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	@Autowired
	private StudentGroupRepository studentGroupRepo;

	@Autowired
	private TeacherSubjectRepository teacherSubjectRepo;

	@Autowired
	private StudentGroupTakingASubjectRepository studentGroupTakingASubjectRepo;

	/*************************************************************************************************
	 * POST endpoint for posting teacher-subject combination to a particular student group
	 * -- postman code adm009 --
	 * 
	 * @param studentGroupID
	 * @param teacherSubjectID
	 * @param hoursRequired
	 * @return if ok, new link between teacher teaching a subject and a student group
	 *************************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Headmaster.class)
	@RequestMapping(method = RequestMethod.POST, path = "/admin/newStudentGroupTakingASubject")
	public ResponseEntity<?> postNewStudentGroupTakingASubject(@RequestParam Long studentGroupID,
			@RequestParam Long teacherSubjectID, @RequestParam Integer hoursRequired) {

		logger.info("**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Access to the endpoint successful.");

		logger.info("**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt to find the student group in database.");
		// check existance and deleted state of student group in db
		Optional<StudentGroupEntity> studentGroup = studentGroupRepo.findById(studentGroupID);
		if (studentGroup.isEmpty() || studentGroup.get().getDeleted() == 1) {
			logger.warn("**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Student Group not in database or deleted.");
			return new ResponseEntity<RESTError>(new RESTError(9000, "Student Group not in database."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Student Group found.");

		logger.info(
				"**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt to find the teacher subject combination in database.");
		// check existance and deleted state of teacher-subject combo group in db
		Optional<TeacherSubjectEntity> teacherSubject = teacherSubjectRepo.findById(teacherSubjectID);
		if (teacherSubject.isEmpty() || teacherSubject.get().getDeleted() == 1) {
			logger.warn(
					"**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Teacher subject combination not in database or deleted.");
			return new ResponseEntity<RESTError>(new RESTError(9001, "Teacher subject combination not in database."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Teacher subject combination found.");

		logger.info(
				"**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt to check the schooling year compatibility between subject and student group.");
		// check if student group is the same schooling year as subject from teacher-subject combo
		if (!studentGroup.get().getYear().equals(teacherSubject.get().getSubject().getYearOfSchooling())) {
			logger.warn(
					"**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Schooling years not compatible beteewen subject thaught and student group.");
			return new ResponseEntity<RESTError>(
					new RESTError(9002, "Schooling years not compatible beteewen subject thaught and student group."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Schooling years compatible.");

		logger.info(
				"**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt to check for existing relation between teacher-subject combination and student group.");
		// check if teacher-subject combo already related to student group
		if (studentGroupTakingASubjectRepo.findByStudentGroupAndTeacherSubject(studentGroup.get(), teacherSubject.get())
				.isPresent()) {
			logger.warn(
					"**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Relationship existing, subject already assigned to student group.");
			return new ResponseEntity<RESTError>(new RESTError(9005, "Subject already assigned to student group."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info(
				"**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** No relation between student group and teacher-subject combination found in db.");

		logger.info(
				"**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt to check if student-teacher combo has available hours to teach.");
		// check if hours required are less than hours allotted for teacher-subject combo
		if (teacherSubject.get().getWeeklyHoursAlloted() < hoursRequired) {
			logger.warn(
					"**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Teacher subject combination doesn't have available hours to teach this student group.");
			return new ResponseEntity<RESTError>(
					new RESTError(9003,
							"Teacher subject combination doesn't have available hours to teach this student group."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info(
				"**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Hours available are greater than hours required.");

		// assign a link between teacher-subject and student group
		logger.info(
				"**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempting to assign a new entity linking teacher-subject combination with student group.");
		StudentGroupTakingASubjectEntity studentGroupTakingASubject = new StudentGroupTakingASubjectEntity();
		studentGroupTakingASubject.setDeleted(0);
		studentGroupTakingASubject.setStudentGroup(studentGroup.get());
		studentGroupTakingASubject.setTeacherSubject(teacherSubject.get());
		studentGroupTakingASubject.setWeeklyHours(hoursRequired);
		logger.info("**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Entity created.");

		// reduce allotted hours in teacher-subject combo by hours required
		logger.info(
				"**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempting to reduce teacher-subject alloted hours for future use, than save to db");
		teacherSubject.get().setWeeklyHoursAlloted(teacherSubject.get().getWeeklyHoursAlloted() - hoursRequired);
		teacherSubjectRepo.save(teacherSubject.get());
		logger.info("**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt successful.");

		// save teacher-subject and save new link to repo
		logger.info(
				"**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempting to save new student group taking a subject to db");
		studentGroupTakingASubjectRepo.save(studentGroupTakingASubject);
		logger.info("**ASSIGN STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt successful.");

		return new ResponseEntity<String>("Student Group " + studentGroup.get().getYear() + "-"
				+ studentGroup.get().getYearIndex() + " got assigned a subject "
				+ teacherSubject.get().getSubject().getName().toString() + " taught by "
				+ teacherSubject.get().getTeacher().getName() + " " + teacherSubject.get().getTeacher().getSurname()
				+ ". Class will be held for " + studentGroupTakingASubject.getWeeklyHours() + " hour(s) a week.",
				HttpStatus.OK);
	}

	/**********************************************************************************************************
	 * PUT/DELETE endpoint for administrator looking to soft delete a student group - teacher subject relation.
	 * -- postman code adm032 --
	 * 
	 * @param studentGroupTakingASubjectID
	 * @return if ok, deleted set to 1
	 **********************************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Headmaster.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/deleteStudentGroupTakingASubject/{studentGroupTakingASubjectID}")
	public ResponseEntity<?> deleteStudentGroupTakingASubject(@PathVariable Long studentGroupTakingASubjectID) {

		logger.info("**DELETE STUDENT GROUP TO TEACHER-SUBJECT COMBO** Access to the endpoint successful.");

		logger.info("**DELETE STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt to find the combo in database.");
		// check existance and deleted state of student group taking a subject in db
		Optional<StudentGroupTakingASubjectEntity> studentGroupTakingASubject = studentGroupTakingASubjectRepo
				.findById(studentGroupTakingASubjectID);
		if (studentGroupTakingASubject.isEmpty() || studentGroupTakingASubject.get().getDeleted() == 1) {
			logger.warn(
					"**DELETE STUDENT GROUP TO TEACHER-SUBJECT COMBO** Student Group Taking A Subject combination not in database or deleted.");
			return new ResponseEntity<RESTError>(
					new RESTError(9000, "Student Group Taking A Subject combination not in database or deleted."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**RESTORE STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt successful.");

		logger.info(
				"**DELETE STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt on editing deleted field and saving to db.");
		studentGroupTakingASubject.get().setDeleted(1);
		studentGroupTakingASubjectRepo.save(studentGroupTakingASubject.get());
		logger.info("**DELETE STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt successful.");

		// update teacher's available hours
		logger.info(
				"**DELETE STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt to update teacher-subject alloted hours and saving to db.");
		TeacherSubjectEntity ogTeacherSubject = studentGroupTakingASubject.get().getTeacherSubject();
		ogTeacherSubject.setWeeklyHoursAlloted(
				ogTeacherSubject.getWeeklyHoursAlloted() + studentGroupTakingASubject.get().getWeeklyHours());
		teacherSubjectRepo.save(ogTeacherSubject);
		logger.info("**DELETE STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt successful.");

		return new ResponseEntity<String>(
				"Student Group " + studentGroupTakingASubject.get().getStudentGroup().getYear() + "-"
						+ studentGroupTakingASubject.get().getStudentGroup().getYearIndex()
						+ " no longer taking classes in "
						+ studentGroupTakingASubject.get().getTeacherSubject().getSubject().getName().toString()
						+ " taught by " + studentGroupTakingASubject.get().getTeacherSubject().getTeacher().getName()
						+ " " + studentGroupTakingASubject.get().getTeacherSubject().getTeacher().getSurname() + ".",
				HttpStatus.OK);
	}

	/**********************************************************************************************************
	 * PUT/DELETE endpoint for administrator looking to restore a student group - teacher subject relation.
	 * -- postman code adm033 --
	 * 
	 * @param studentGroupTakingASubjectID
	 * @return if ok, deleted set to 0
	 **********************************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Headmaster.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/restoreStudentGroupTakingASubject/{studentGroupTakingASubjectID}")
	public ResponseEntity<?> restoreStudentGroupTakingASubject(@PathVariable Long studentGroupTakingASubjectID) {

		logger.info("**RESTORE STUDENT GROUP TO TEACHER-SUBJECT COMBO** Access to the endpoint successful.");

		logger.info("**RESTORE STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt to find a deleted combo in database.");
		// check existance and deleted state of student group taking a subject in db
		Optional<StudentGroupTakingASubjectEntity> studentGroupTakingASubject = studentGroupTakingASubjectRepo
				.findById(studentGroupTakingASubjectID);
		if (studentGroupTakingASubject.isEmpty() || studentGroupTakingASubject.get().getDeleted() == 0) {
			logger.warn(
					"**RESTORE STUDENT GROUP TO TEACHER-SUBJECT COMBO** Student Group Taking A Subject combination not in database or already active.");
			return new ResponseEntity<RESTError>(
					new RESTError(9001,
							"Student Group Taking A Subject combination not in database or already active."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**RESTORE STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt successful.");

		logger.info(
				"**RESTORE STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt on editing deleted field and saving to db.");
		studentGroupTakingASubject.get().setDeleted(0);
		studentGroupTakingASubjectRepo.save(studentGroupTakingASubject.get());
		logger.info("**RESTORE STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt successful.");

		// update teacher's available hours
		logger.info(
				"**RESTORE STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt to update teacher-subject alloted hours and saving to db.");
		TeacherSubjectEntity ogTeacherSubject = studentGroupTakingASubject.get().getTeacherSubject();
		ogTeacherSubject.setWeeklyHoursAlloted(
				ogTeacherSubject.getWeeklyHoursAlloted() - studentGroupTakingASubject.get().getWeeklyHours());
		teacherSubjectRepo.save(ogTeacherSubject);
		logger.info("**RESTORE STUDENT GROUP TO TEACHER-SUBJECT COMBO** Attempt successful.");

		return new ResponseEntity<String>(
				"Student Group " + studentGroupTakingASubject.get().getStudentGroup().getYear() + "-"
						+ studentGroupTakingASubject.get().getStudentGroup().getYearIndex()
						+ " is once again taking classes in "
						+ studentGroupTakingASubject.get().getTeacherSubject().getSubject().getName().toString()
						+ " taught by " + studentGroupTakingASubject.get().getTeacherSubject().getTeacher().getName()
						+ " " + studentGroupTakingASubject.get().getTeacherSubject().getTeacher().getSurname() + ".",
				HttpStatus.OK);
	}
}
