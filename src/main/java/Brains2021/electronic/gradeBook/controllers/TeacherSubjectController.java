package Brains2021.electronic.gradeBook.controllers;

import java.util.List;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.dtos.in.CreateTeacherSubjectDTO;
import Brains2021.electronic.gradeBook.entites.StudentGroupEntity;
import Brains2021.electronic.gradeBook.entites.StudentGroupTakingASubjectEntity;
import Brains2021.electronic.gradeBook.entites.SubjectEntity;
import Brains2021.electronic.gradeBook.entites.TeacherSubjectEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentEntity;
import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;
import Brains2021.electronic.gradeBook.entites.users.UserEntity;
import Brains2021.electronic.gradeBook.repositories.StudentGroupRepository;
import Brains2021.electronic.gradeBook.repositories.StudentGroupTakingASubjectRepository;
import Brains2021.electronic.gradeBook.repositories.SubjectRepository;
import Brains2021.electronic.gradeBook.repositories.TeacherSubjectRepository;
import Brains2021.electronic.gradeBook.repositories.UserRepository;
import Brains2021.electronic.gradeBook.security.Views;
import Brains2021.electronic.gradeBook.services.subject.SubjectService;
import Brains2021.electronic.gradeBook.services.teacherSubject.TeacherSubjectService;
import Brains2021.electronic.gradeBook.utils.RESTError;
import Brains2021.electronic.gradeBook.utils.enums.ERole;
import Brains2021.electronic.gradeBook.utils.enums.ESubjectName;

@RestController
@RequestMapping(path = "/api/v1/teacherSubject")
public class TeacherSubjectController {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private SubjectService subjectService;

	@Autowired
	private SubjectRepository subjectRepo;

	@Autowired
	private TeacherSubjectRepository teacherSubjectRepo;

	@Autowired
	private StudentGroupRepository studentGroupRepository;

	@Autowired
	private TeacherSubjectService teacherSubjectService;

	@Autowired
	private StudentGroupTakingASubjectRepository studentGroupTakingASubjectRepo;

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	/***************************************************************************************
	 * POST endpoint for administrator looking to assign subject to teacher.
	 * -- postman code adm007 / hdm100 --
	 * 
	 * @param subject
	 * @param username
	 * @return if ok teacher will be assigned to teach a subject
	 **************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Headmaster.class)
	@RequestMapping(method = RequestMethod.POST, path = "/assign")
	public ResponseEntity<?> assignSubjectToTeacher(@RequestBody CreateTeacherSubjectDTO teacherSubject) {

		if (!subjectService.isSubjectInEnum(teacherSubject.getSubject())) {
			return new ResponseEntity<RESTError>(
					new RESTError(2000, "Subject name not allowed, check ESubjectName for details."),
					HttpStatus.BAD_REQUEST);
		}

		Optional<SubjectEntity> ogSubject = subjectRepo.findByNameAndYearOfSchooling(
				ESubjectName.valueOf(teacherSubject.getSubject()), teacherSubject.getYearOfSchooling());

		if (ogSubject.isEmpty()) {
			return new ResponseEntity<RESTError>(new RESTError(5030,
					"Subject not found in database, please provide a valid subject. Check if subject and schooling year combination is correct"),
					HttpStatus.NOT_FOUND);
		}

		if (ogSubject.get().getDeleted() == 1) {
			return new ResponseEntity<RESTError>(
					new RESTError(5031, "Not an active subject, please contact the administrator to reinstate."),
					HttpStatus.BAD_REQUEST);
		}

		Optional<UserEntity> ogUser = userRepo.findByUsername(teacherSubject.getUsername());

		if (ogUser.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1030, "Username not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		if (ogUser.get().getDeleted() == 1) {
			return new ResponseEntity<RESTError>(
					new RESTError(1031, "Not an active user, please contact the administrator to reinstate."),
					HttpStatus.BAD_REQUEST);
		}

		// check if user is a teacher

		if (!ogUser.get().getRole().getName().equals(ERole.ROLE_ADMIN)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_HEADMASTER)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_HOMEROOM)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_TEACHER)) {
			return new ResponseEntity<RESTError>(
					new RESTError(1060, "Not a teaching user, unable to assign a subject."), HttpStatus.BAD_REQUEST);
		}
		TeacherEntity ogTeacher = (TeacherEntity) ogUser.get();

		// check if teacher can handle new subject with hours he can take
		if (ogTeacher.getWeeklyHourCapacity() < teacherSubject.getWeeklyHoursAlloted()
				&& ogTeacher.getWeeklyHourCapacity() - teacherSubject.getWeeklyHoursAlloted() < 0) {
			return new ResponseEntity<RESTError>(new RESTError(1039,
					"Teacher not able to handle needed hours, look for other teachers or decrease alloted hours."),
					HttpStatus.BAD_REQUEST);
		}

		TeacherSubjectEntity newTeacherSubject = teacherSubjectService
				.createTeacherSubjectDTOtranslation(teacherSubject);

		if (teacherSubjectRepo.findByTeacherAndSubject(ogTeacher, ogSubject.get()).isPresent()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1539, "Combination teacher - subject already existing in database."),
					HttpStatus.BAD_REQUEST);
		}

		// reduce hours capacity for the teacher and update db
		ogTeacher.setWeeklyHourCapacity(ogTeacher.getWeeklyHourCapacity() - teacherSubject.getWeeklyHoursAlloted());
		userRepo.save(ogTeacher);

		teacherSubjectRepo.save(newTeacherSubject);
		// do magic and use service to translate to and fro DTO

		return teacherSubjectService.createdTeacherSubjectDTOtranslation(newTeacherSubject);
	}

	/*******************************************************************************************
	 * PUT endpoint for administrator or headmaster looking to link a student group to a subject
	 * -- postman code adm017 --
	 * 
	 * @param student group
	 * @param subject taught by a teacher
	 * @return if ok, student group linked to a subject
	 *******************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Headmaster.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/assignSubjectToStudentGroup")
	public ResponseEntity<?> assignToStudentGroup(@RequestParam Long studentGroupID, @RequestParam Long subjectTaughtID,
			@RequestParam Integer weeklyHours) {

		Optional<StudentGroupEntity> studentGroup = studentGroupRepository.findByIdAndDeleted(studentGroupID, 0);

		if (studentGroup.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(5040, "Active student group not found in database, please provide a valid id."),
					HttpStatus.NOT_FOUND);
		}

		Optional<TeacherSubjectEntity> teacherSubject = teacherSubjectRepo.findByIdAndDeleted(subjectTaughtID, 0);

		if (teacherSubject.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(5050, "Active subject taught not found in database, please provide a valid id."),
					HttpStatus.NOT_FOUND);
		}

		// connect the student group and teacher-subject combo 

		StudentGroupTakingASubjectEntity newStudentGroupTakingASubject = new StudentGroupTakingASubjectEntity();

		newStudentGroupTakingASubject.setDeleted(0);
		newStudentGroupTakingASubject.setStudentGroup(studentGroup.get());
		newStudentGroupTakingASubject.setTeacherSubject(teacherSubject.get());
		newStudentGroupTakingASubject.setWeeklyHours(weeklyHours);

		studentGroupTakingASubjectRepo.save(newStudentGroupTakingASubject);

		return new ResponseEntity<String>("Subject " + teacherSubject.get().getSubject().getName().toString()
				+ " taught by " + teacherSubject.get().getTeacher().getName() + " asigned to student group "
				+ studentGroup.get().getYear() + "-" + studentGroup.get().getYearIndex() + ".", HttpStatus.OK);
	}

	/*********************************************************************************************
	 * PUT/DELETE endpoint for administrator looking to soft delete a teacher subject combination.
	 * -- postman code adm039 --
	 * 
	 * @param teacherSubject id
	 * @return if ok set deleted to 1
	 *********************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/deleteTeacherSubject/{teacherSubjectID}")
	public ResponseEntity<?> deleteTeacherSubject(@PathVariable Long teacherSubjectID) {

		logger.info("**DELETE TEACHER SUBJECT COMBINATION** Access to the endpoint successful.");

		logger.info("**DELETE TEACHER SUBJECT COMBINATION** Attempt to find an active teacher subject combination in database.");
		// initial check for existance in db
		Optional<TeacherSubjectEntity> ogTeacherSubject = teacherSubjectRepo.findById(teacherSubjectID);
		if (ogTeacherSubject.isEmpty() || ogTeacherSubject.get().getDeleted() == 1) {
			logger.warn("**DELETE TEACHER SUBJECT COMBINATION** Teacher subject combination not in database or deleted.");
			return new ResponseEntity<RESTError>(
					new RESTError(7530,
							"Teacher subject combination not found in database or is deleted, please provide a valid id."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**DELETE TEACHER SUBJECT COMBINATION** Attempt successful.");

		logger.info("**DELETE TEACHER SUBJECT COMBINATION** Attempt to unlink students from student group, if any.");
		// do not allow if there are active assignemnts linked to this combinaton
		// unlink student groups and teacher subject
		List<StudentEntity> students = studentRepo.findByBelongsToStudentGroup(ogStudentGroup.get());

		if (!students.isEmpty()) {
			for (StudentEntity studentEntity : students) {
				studentEntity.setBelongsToStudentGroup(null);
			}
			studentRepo.saveAll(students);
			logger.info("**DELETE STUDENT GROUP** Attempt successful, students unlinked and saved to db.");
		}

		logger.info("**DELETE STUDENT GROUP** Attempt to unlink homeroom teacher from student group, if any.");
		// unlink homeroomTeacher
		Optional<TeacherEntity> homeroomTeacher = teacherRepo.findByInChargeOf(ogStudentGroup.get());

		if (homeroomTeacher.isPresent()) {
			homeroomTeacher.get().setInChargeOf(null);
			homeroomTeacher.get().setIsHomeroomTeacher(0);
			homeroomTeacher.get().setSalaryHomeroomBonus(0.00);
			teacherRepo.save(homeroomTeacher.get());
			logger.info("**DELETE STUDENT GROUP** Attempt successful, homeroom teacher unlinked and saved to db.");
		}

		// set to deleted and save
		logger.info("**DELETE STUDENT GROUP** Attempt on editing deleted field and saving to db.");
		ogStudentGroup.get().setDeleted(1);
		studentGroupRepo.save(ogStudentGroup.get());
		logger.info("**DELETE STUDENT GROUP** Attempt successful.");

		return new ResponseEntity<String>(
				"Student group with id " + studentGroupID + " deleted, students and homeroom teacher unlinked.",
				HttpStatus.OK);

	}

	/***************************************************************************************
	 * PUT endpoint for administrator looking to restore a deleted student group.
	 * -- postman code adm040 --
	 * 
	 * @param studentGroup id
	 * @return if ok set deleted to 0
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/restoreStudentGroup/{studentGroupID}")
	public ResponseEntity<?> restoreStudentGroup(@PathVariable Long studentGroupID) {

		logger.info("**RESTORE STUDENT GROUP** Access to the endpoint successful.");

		logger.info("**RESTORE STUDENT GROUP** Attempt to find a deleted student group in database.");
		// initial check for existance in db
		Optional<StudentGroupEntity> ogStudentGroup = studentGroupRepo.findById(studentGroupID);
		if (ogStudentGroup.isEmpty() || ogStudentGroup.get().getDeleted() == 0) {
			logger.warn("**RESTORE STUDENT GROUP** Student group not in database or active.");
			return new ResponseEntity<RESTError>(
					new RESTError(7531,
							"Student group not found in database or is deleted, please provide a valid id."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**RESTORE STUDENT GROUP** Attempt successful.");

		// set to active and save
		logger.info("**RESTORE STUDENT GROUP** Attempt on editing deleted field and saving to db.");
		ogStudentGroup.get().setDeleted(0);
		studentGroupRepo.save(ogStudentGroup.get());
		logger.info("**RESTORE STUDENT GROUP** Attempt successful.");

		return new ResponseEntity<String>("Assignment with id " + studentGroupID + " restored.", HttpStatus.OK);
	}

}
