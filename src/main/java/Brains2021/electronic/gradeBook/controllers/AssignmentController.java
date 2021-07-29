package Brains2021.electronic.gradeBook.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

import Brains2021.electronic.gradeBook.dtos.in.CreateAssignmentDTO;
import Brains2021.electronic.gradeBook.dtos.out.GETAssignmentDTO;
import Brains2021.electronic.gradeBook.entites.AssignmentEntity;
import Brains2021.electronic.gradeBook.entites.TeacherSubjectEntity;
import Brains2021.electronic.gradeBook.entites.users.ParentEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentEntity;
import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;
import Brains2021.electronic.gradeBook.entites.users.UserEntity;
import Brains2021.electronic.gradeBook.repositories.AssignmentRepository;
import Brains2021.electronic.gradeBook.repositories.StudentGroupTakingASubjectRepository;
import Brains2021.electronic.gradeBook.repositories.StudentRepository;
import Brains2021.electronic.gradeBook.repositories.SubjectRepository;
import Brains2021.electronic.gradeBook.repositories.TeacherRepository;
import Brains2021.electronic.gradeBook.repositories.TeacherSubjectRepository;
import Brains2021.electronic.gradeBook.repositories.UserRepository;
import Brains2021.electronic.gradeBook.security.Views;
import Brains2021.electronic.gradeBook.services.assignment.AssignmentService;
import Brains2021.electronic.gradeBook.services.subject.SubjectService;
import Brains2021.electronic.gradeBook.services.user.UserService;
import Brains2021.electronic.gradeBook.utils.RESTError;
import Brains2021.electronic.gradeBook.utils.enums.ERole;
import Brains2021.electronic.gradeBook.utils.enums.ESubjectName;

@RestController
@RequestMapping(path = "/api/v1/assignments")
public class AssignmentController {

	@Autowired
	private AssignmentRepository assignmentRepo;

	@Autowired
	private AssignmentService assignmentService;

	@Autowired
	private SubjectService subjectService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private TeacherSubjectRepository teacherSubjectRepo;

	@Autowired
	private StudentGroupTakingASubjectRepository studentGroupTakingASubjectRepo;

	@Autowired
	private TeacherRepository teacherRepo;

	@Autowired
	SubjectRepository subjectRepo;

	@Autowired
	StudentRepository studentRepo;

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	/***************************************************************************************
	 * POST endpoint for teaching staff looking to create a new assignment
	 * -- postman code adm008 --
	 * 
	 * @param assignment
	 * @return if ok, new assignment
	 ***************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_HOMEROOM", "ROLE_HEADMASTER" })
	@JsonView(Views.Teacher.class)
	@RequestMapping(method = RequestMethod.POST, path = "/newAssignment")
	public ResponseEntity<?> postNewAssignment(@Valid @RequestBody CreateAssignmentDTO assignment) {

		logger.info("**POST ASSIGNMENT** Access to the endpoint successful.");

		// check to see if subject name is valid
		logger.info("**POST ASSIGNMENT** Attempt to find if subject name is allowed.");
		if (!subjectService.isSubjectInEnum(assignment.getSubject())) {
			logger.warn("**POST ASSIGNMENT** Subject name is allowed, must be a value from ESubjectName.");
			return new ResponseEntity<RESTError>(
					new RESTError(2000, "Subject name not allowed, check ESubjectName for details."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**POST ASSIGNMENT** Subject name is allowed.");

		// check if teacher is valid or admin posting 
		logger.info("**POST ASSIGNMENT** Attempting to find if teacher is teaching the subject.");
		Optional<TeacherSubjectEntity> teacherSubject = teacherSubjectRepo
				.findBySubjectAndTeacher(ESubjectName.valueOf(assignment.getSubject()), userService.whoAmI());
		if (teacherSubject.isEmpty()
				&& !userRepo.findByUsername(userService.whoAmI()).get().getRole().getName().equals(ERole.ROLE_ADMIN)) {
			logger.warn("**POST ASSIGNMENT** Teacher not teaching the subject and is not with admin role.");
			return new ResponseEntity<RESTError>(
					new RESTError(2050, "Teacher not teaching the subject and logged user not with admin role."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**POST ASSIGNMENT** User is a teacher teaching the subject or admin.");

		AssignmentEntity newAssignment = assignmentService.createAssignmentDTOtranslation(assignment);

		logger.info(
				"**POST ASSIGNMENT** Check if user is admin. If so, make new dummy teacher subject entity and assign properties");
		if (userService.amIAdmin() && teacherSubject.isEmpty()) {
			logger.info("**POST ASSIGNMENT** User is admin.");
			TeacherSubjectEntity adminTeacherSubject = new TeacherSubjectEntity(
					teacherRepo.findByUsername(userService.whoAmI()).get(),
					subjectRepo.findByName(ESubjectName.valueOf(assignment.getSubject())).get());
			newAssignment.setTeacherIssuing(adminTeacherSubject);
			teacherSubjectRepo.save(adminTeacherSubject);
			logger.info("**POST ASSIGNMENT** Admin teacher subject added and assigned to assignemnt.");
		}

		logger.info("**POST ASSIGNMENT** Attempting to save the changed assignment to database.");
		assignmentRepo.save(newAssignment);

		logger.info("**POST ASSIGNMENT** Assignment saved, invoking service for translation to output DTO.");
		return assignmentService.createdAssignmentDTOtranslation(newAssignment);

	}

	/***************************************************************************************
	 * PUT endpoint for teaching staff looking to link an assignment to a student
	 * -- postman code adm019 --
	 * 
	 * @param assignment
	 * @param student
	 * @return if ok, assignment linked to student
	 ***************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_HOMEROOM", "ROLE_HEADMASTER" })
	@JsonView(Views.Teacher.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/giveAssignmentToStudent")
	public ResponseEntity<?> assignToStudent(@RequestParam Long assignmentID, @RequestParam String studentUsername,
			@RequestParam("dueDate") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate dueDate) {

		Optional<AssignmentEntity> assignment = assignmentRepo.findById(assignmentID);

		// check if id is valid and active
		if (assignment.isEmpty()) {
			return new ResponseEntity<RESTError>(new RESTError(5002, "Not a valid assignment id, check and retry."),
					HttpStatus.BAD_REQUEST);
		}

		if (assignment.get().getDeleted() == 1) {
			return new ResponseEntity<RESTError>(new RESTError(5003, "Not an active assignment."),
					HttpStatus.BAD_REQUEST);
		}

		// check if teacher was the one who issued the assignment
		if (!assignment.get().getTeacherIssuing().getTeacher().getUsername().equals(userService.whoAmI())
				&& !userRepo.findByUsername(userService.whoAmI()).get().getRole().getName().equals(ERole.ROLE_ADMIN)) {
			return new ResponseEntity<RESTError>(
					new RESTError(5004, "Logged user didn't post the assignment with the give id."),
					HttpStatus.BAD_REQUEST);
		}

		Optional<UserEntity> user = userRepo.findByUsername(studentUsername);

		if (user.isEmpty()) {
			return new ResponseEntity<RESTError>(new RESTError(3003, "Student not in database."),
					HttpStatus.BAD_REQUEST);
		}

		if (user.get().getDeleted() == 1) {
			return new ResponseEntity<RESTError>(new RESTError(3004, "Not an active student."), HttpStatus.BAD_REQUEST);
		}

		if (!user.get().getRole().getName().equals(ERole.ROLE_STUDENT)) {
			return new ResponseEntity<RESTError>(new RESTError(3005, "User is not a student."), HttpStatus.BAD_REQUEST);
		}

		StudentEntity student = (StudentEntity) user.get();

		// check if student belongs to a group taking the subject taught by the teacher ---- FIX THIS ! -----
		if (studentGroupTakingASubjectRepo.findAllByTeacherSubject(assignment.get().getTeacherIssuing()).isEmpty()
				&& !userRepo.findByUsername(userService.whoAmI()).get().getRole().getName().equals(ERole.ROLE_ADMIN)) {
			return new ResponseEntity<RESTError>(
					new RESTError(5004, "Logged teacher not teaching the student group the student belongs to."),
					HttpStatus.BAD_REQUEST);
		}

		// check that student attends the study year corresponding with the one in assignment
		if (!assignment.get().getTeacherIssuing().getSubject().getYearOfSchooling()
				.equals(student.getBelongsToStudentGroup().getYear())) {

			return new ResponseEntity<RESTError>(
					new RESTError(3006, "Can't assign, check student's and assignment's study year."),
					HttpStatus.BAD_REQUEST);
		}

		// asign to student

		assignment.get().setAssignedTo(student);
		assignment.get().setDateAssigned(LocalDate.now());
		assignment.get().setDueDate(dueDate);
		assignmentRepo.save(assignment.get());

		return new ResponseEntity<String>("Assignment " + assignment.get().getType() + " in subject "
				+ assignment.get().getTeacherIssuing().getSubject().getName() + " given by "
				+ assignment.get().getTeacherIssuing().getTeacher().getUsername() + " asigned to student "
				+ studentUsername + " on " + assignment.get().getDateAssigned() + " with due date " + dueDate + ".",
				HttpStatus.OK);

	}

	/***************************************************************************************
	 * PUT endpoint for teaching staff looking to edit an assignment
	 * -- postman code adm021 --
	 * 
	 * @param assignmentID
	 * @param createAssignmentDTO
	 * @return if ok, edited assignment
	 ***************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_HOMEROOM", "ROLE_HEADMASTER" })
	@JsonView(Views.Teacher.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/changeAssignment/{assignmentID}")
	public ResponseEntity<?> changeAssignement(@Valid @RequestBody CreateAssignmentDTO assignment,
			@PathVariable Long assignmentID) {

		logger.info("**CHANGE ASSIGNMENT** Access to the endpoint successful.");

		logger.info("**CHANGE ASSIGNMENT** Attempt to change an active assignment.");
		// initial check for existance in db
		Optional<AssignmentEntity> ogAssignment = assignmentRepo.findById(assignmentID);
		if (ogAssignment.isEmpty() || ogAssignment.get().getDeleted() == 1) {
			logger.warn("**CHANGE ASSIGNMENT** Assignment not in database or deleted.");
			return new ResponseEntity<RESTError>(
					new RESTError(7533, "Assignment not found in database or is deleted, please provide a valid id."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**CHANGE ASSIGNMENT** Assignment accessed successfuly.");

		// check to see if subject name is valid
		logger.info("**CHANGE ASSIGNMENT** Attempt to find if subject name is allowed.");
		if (!subjectService.isSubjectInEnum(assignment.getSubject())) {
			logger.warn("**CHANGE ASSIGNMENT** Subject name is allowed, must be a value from ESubjectName.");
			return new ResponseEntity<RESTError>(
					new RESTError(2300, "Subject name not allowed, check ESubjectName for details."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**CHANGE ASSIGNMENT** Subject name is allowed.");

		// check to see if logged teacher is the one who issued the assignment
		logger.info("**CHANGE ASSIGNMENT** Attempt to find if assignment was created by logged teacher or admin.");
		if (!ogAssignment.get().getTeacherIssuing().getTeacher().getUsername().equals(userService.whoAmI())
				&& !userService.amIAdmin()) {
			logger.warn("**CHANGE ASSIGNMENT** Loogged user or admin not the one who issued the assignment.");
			return new ResponseEntity<RESTError>(
					new RESTError(2360, "Loogged user not the one who issued the assignment, nor is an admin."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**CHANGE ASSIGNMENT** Logged teacher issued the assignment, or is an admin.");

		logger.info("**CHANGE ASSIGNMENT** Attempting to save the changed assignment to database.");
		AssignmentEntity newAssignment = assignmentService.createAssignmentDTOtranslation(assignment);
		assignmentRepo.save(newAssignment);
		logger.info("**CHANGE ASSIGNMENT** Assignment saved, invoking service for translation to output DTO.");

		return assignmentService.createdAssignmentDTOtranslation(newAssignment);
	}

	/***************************************************************************************
	 * PUT endpoint for teaching staff looking to grade an assignment
	 * -- postman code adm020 --
	 * 
	 * @param assignmentID, grade
	 * @return if ok, give grade
	 ***************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_HOMEROOM", "ROLE_HEADMASTER" })
	@JsonView(Views.Teacher.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/gradeAssignment")
	public ResponseEntity<?> gradeAssignment(@RequestParam Long assignmentID, @RequestParam Integer grade) {

		logger.info("**GRADE ASSIGNMENT** Access to the endpoint successful.");
		// validate grade
		logger.info("**GRADE ASSIGNMENT** Perform validation on grade entry");
		if (grade != 1 && grade != 2 && grade != 3 && grade != 4 && grade != 5) {
			logger.warn("**GRADE ASSIGNMENT** Faulty grade entered.");
			return new ResponseEntity<RESTError>(new RESTError(5007, "Provide a valid grade between 1 and 5."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**GRADE ASSIGNMENT** Grade passed validation.");

		Optional<AssignmentEntity> assignmentForGrading = assignmentRepo.findById(assignmentID);

		logger.info("**GRADE ASSIGNMENT** Check if id is active and in database.");
		// check if id is valid and active
		if (assignmentForGrading.isEmpty() || assignmentForGrading.get().getDeleted() == 1) {
			logger.warn("**GRADE ASSIGNMENT** Assignment not valid or deleted.");
			return new ResponseEntity<RESTError>(new RESTError(5002, "Not a valid assignment id, check and retry."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**GRADE ASSIGNMENT** Assignment present and active.");

		logger.info("**GRADE ASSIGNMENT** Check if logged user is teacher that issued the assignment or admin.");
		if (!assignmentForGrading.get().getTeacherIssuing().getTeacher().getUsername().equals(userService.whoAmI())
				&& !userRepo.findByUsername(userService.whoAmI()).get().getRole().getName().equals(ERole.ROLE_ADMIN)) {
			logger.warn("**GRADE ASSIGNMENT** User not qualified to post the grade.");
			return new ResponseEntity<RESTError>(
					new RESTError(5002,
							"Looged teacher not responsible for this assignment. Please check assignment ID"),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**GRADE ASSIGNMENT** Permission are ok, continuing.");

		logger.info("**GRADE ASSIGNMENT** Check if assignment is given out.");
		if (assignmentForGrading.get().getAssignedTo() == null) {
			logger.warn("**GRADE ASSIGNMENT** Assignment not yet given out.");
			return new ResponseEntity<RESTError>(new RESTError(5009, "Assignment not yet given out."),
					HttpStatus.BAD_REQUEST);
		}

		logger.info("**GRADE ASSIGNMENT** Check if assignment is already graded.");
		if (assignmentForGrading.get().getGradeRecieved() != null) {
			logger.warn("**GRADE ASSIGNMENT** Assignment already graded.");
			return new ResponseEntity<RESTError>(new RESTError(5010, "Assignment already graded."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**GRADE ASSIGNMENT** All checks completed.");

		assignmentForGrading.get().setGradeRecieved(grade);
		assignmentForGrading.get().setDateCompleted(LocalDate.now());

		logger.info("**GRADE ASSIGNMENT** Saving assignment and sending out email to parents.");
		assignmentRepo.save(assignmentForGrading.get());

		assignmentService.sendEmailForGradedAssignemnt(assignmentForGrading.get());

		return new ResponseEntity<String>("Assignment " + assignmentForGrading.get().getType() + " in subject "
				+ assignmentForGrading.get().getTeacherIssuing().getSubject().getName() + " given by "
				+ assignmentForGrading.get().getTeacherIssuing().getTeacher().getName() + " "
				+ assignmentForGrading.get().getTeacherIssuing().getTeacher().getSurname() + " asigned to student "
				+ assignmentForGrading.get().getAssignedTo().getName() + " "
				+ assignmentForGrading.get().getAssignedTo().getSurname() + " on "
				+ assignmentForGrading.get().getDateAssigned() + " with due date "
				+ assignmentForGrading.get().getDueDate() + " has just been graded and recieved "
				+ assignmentForGrading.get().getGradeRecieved()
				+ ".\nAn email has been sent to the parent(s) as a notification.", HttpStatus.OK);
	}

	/***************************************************************************************
	 * PUT endpoint for headmaster, teacher issuing or admin looking to override a grade
	 * -- postman code adm022 --
	 * 
	 * @param assignmentID
	 * @return if ok, override grade
	 ***************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_HOMEROOM", "ROLE_HEADMASTER" })
	@JsonView(Views.Teacher.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/overrideGrade")
	public ResponseEntity<?> overrideGrade(@RequestParam Long assignmentID, @RequestParam Integer overridenGrade) {

		// validate grade
		logger.info("**OVERRIDE GRADE** Access to the endpoint successful.");

		logger.info("**OVERRIDE GRADE** Perform validation on grade entry");
		if (overridenGrade != 1 && overridenGrade != 2 && overridenGrade != 3 && overridenGrade != 4
				&& overridenGrade != 5) {
			logger.warn("**OVERRIDE GRADE** Faulty grade entered.");
			return new ResponseEntity<RESTError>(new RESTError(5007, "Provide a valid grade between 1 and 5."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**OVERRIDE GRADE** Grade passed validation.");

		Optional<AssignmentEntity> assignmentForGrading = assignmentRepo.findById(assignmentID);

		// check if id is valid and active
		logger.info("**OVERRIDE GRADE** Check if id is active and in database.");
		if (assignmentForGrading.isEmpty() || assignmentForGrading.get().getDeleted() == 1) {
			logger.warn("**OVERRIDE GRADE** Assignment not valid or deleted.");
			return new ResponseEntity<RESTError>(new RESTError(5002, "Not a valid assignment id, check and retry."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**OVERRIDE GRADE** Assignment present and active.");

		logger.info(
				"**OVERRIDE GRADE** Check if logged user is teacher that issued the assignment, headmaster or admin.");
		if (!assignmentForGrading.get().getTeacherIssuing().getTeacher().getUsername().equals(userService.whoAmI())
				&& !userService.amIAdmin() && !userService.amIHeadmaster()) {
			logger.warn("**OVERRIDE GRADE** User not qualified to override the grade.");
			return new ResponseEntity<RESTError>(new RESTError(5002,
					"Looged teacher doesn't have permissions to override the grade. Please check assignment ID"),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**OVERRIDE GRADE** Permission are ok, continuing.");

		logger.info("**OVERRIDE GRADE** Check if assignment is given out.");
		if (assignmentForGrading.get().getAssignedTo() == null) {
			logger.warn("**OVERRIDE GRADE** Assignment not yet given out.");
			return new ResponseEntity<RESTError>(new RESTError(5009, "Assignment not yet given out."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**OVERRIDE GRADE** Assignment given out.");

		logger.info("**OVERRIDE GRADE** Check if assignment is completed.");
		if (assignmentForGrading.get().getDateCompleted() == null) {
			logger.warn("**OVERRIDE GRADE** Assignment not completed, impossible to override.");
			return new ResponseEntity<RESTError>(
					new RESTError(5009, "Assignment still active, not possible to override."), HttpStatus.BAD_REQUEST);
		}
		logger.info("**OVERRIDE GRADE** All checks completed.");

		assignmentForGrading.get().setOverridenGrade(overridenGrade);

		logger.info("**OVERRIDE GRADE** Saving assignment and sending out email to parents.");
		assignmentRepo.save(assignmentForGrading.get());

		assignmentService.sendEmailForGradedAssignemnt(assignmentForGrading.get());

		return new ResponseEntity<String>(
				"Previously graded assignment " + assignmentForGrading.get().getType() + " in subject "
						+ assignmentForGrading.get().getTeacherIssuing().getSubject().getName() + " given by "
						+ assignmentForGrading.get().getTeacherIssuing().getTeacher().getName() + " "
						+ assignmentForGrading.get().getTeacherIssuing().getTeacher().getSurname()
						+ " asigned to student " + assignmentForGrading.get().getAssignedTo().getName() + " "
						+ assignmentForGrading.get().getAssignedTo().getSurname() + " on "
						+ assignmentForGrading.get().getDateAssigned() + ", originaly graded on "
						+ assignmentForGrading.get().getDateCompleted() + " with grade "
						+ assignmentForGrading.get().getGradeRecieved() + " recieved an overriden grade "
						+ assignmentForGrading.get().getGradeRecieved() + ".\nUser responsible for overriding: "
						+ userService.whoAmI() + ". \nAn email has been sent to the parent(s) as a notification.",
				HttpStatus.OK);
	}

	/***************************************************************************************
	 * PUT/DELETE endpoint for administrator looking to soft delete an assignment.
	 * -- postman code adm036 --
	 * 
	 * @param assignment id
	 * @return if ok set deleted to 1
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/deleteAssignment/{assignmentID}")
	public ResponseEntity<?> deleteAssignment(@PathVariable Long assignmentID) {

		logger.info("**DELETE ASSIGNMENT** Access to the endpoint successful.");

		logger.info("**DELETE ASSIGNMENT** Attempt to find an active assignment in database.");
		// initial check for existance in db
		Optional<AssignmentEntity> ogAssignment = assignmentRepo.findById(assignmentID);
		if (ogAssignment.isEmpty() || ogAssignment.get().getDeleted() == 1) {
			logger.warn("**DELETE ASSIGNMENT** Assignment not in database or deleted.");
			return new ResponseEntity<RESTError>(
					new RESTError(7530, "Assignment not found in database or is deleted, please provide a valid id."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**DELETE ASSIGNMENT** Attempt successful.");

		logger.info("**DELETE ASSIGNMENT** Attempt to find if assignment is given out.");
		// cehck if assignement is given to student
		if (ogAssignment.get().getAssignedTo() != null) {
			logger.warn("**DELETE ASSIGNMENT** Assignment given out, can't delete.");
			return new ResponseEntity<RESTError>(
					new RESTError(7531, "Assignment has been assigned to a student, can't delete."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**DELETE ASSIGNMENT** Attempt successful, not given out.");

		// set to deleted and save
		logger.info("**DELETE ASSIGNMENT** Attempt on editing deleted field and saving to db.");
		ogAssignment.get().setDeleted(1);
		assignmentRepo.save(ogAssignment.get());
		logger.info("**DELETE ASSIGNMENT** Attempt successful.");

		return new ResponseEntity<String>("Assignment with id " + assignmentID + " deleted.", HttpStatus.OK);
	}

	/***************************************************************************************
	 * PUT endpoint for administrator looking to restore a deleted assignment.
	 * -- postman code adm037 --
	 * 
	 * @param assignment id
	 * @return if ok set deleted to 0
	 **************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/admin/restoreAssignment/{assignmentID}")
	public ResponseEntity<?> restoreAssignment(@PathVariable Long assignmentID) {

		logger.info("**RESTORE ASSIGNMENT** Access to the endpoint successful.");

		logger.info("**RESTORE ASSIGNMENT** Attempt to find a deleted assignment in database.");
		// initial check for existance in db
		Optional<AssignmentEntity> ogAssignment = assignmentRepo.findById(assignmentID);
		if (ogAssignment.isEmpty() || ogAssignment.get().getDeleted() == 0) {
			logger.warn("**RESTORE ASSIGNMENT** Assignment not in database or active.");
			return new ResponseEntity<RESTError>(
					new RESTError(7532, "Assignment not found in database or is active, please provide a valid id."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**RESTORE ASSIGNMENT** Attempt successful.");

		// set to active and save
		logger.info("**RESTORE ASSIGNMENT** Attempt on editing deleted field and saving to db.");
		ogAssignment.get().setDeleted(0);
		assignmentRepo.save(ogAssignment.get());
		logger.info("**RESTORE ASSIGNMENT** Attempt successful.");

		return new ResponseEntity<String>("Assignment with id " + assignmentID + " restored.", HttpStatus.OK);
	}

	/***************************************************************************************
	 * GET endpoint for administrator looking to fetch all assignments.
	 * -- postman code adm046 --
	 * 
	 * @param 
	 * @return if ok list of all assignemnts in database
	 **************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/")
	public ResponseEntity<?> getAllAssignments() {

		logger.info("**GET ALL ASSIGNMENTS** Access to the endpoint successful.");

		logger.info("**GET ALL ASSIGNMENTS** Attempt to find assignments in database.");
		// initial check to see if there are any assignements at all
		if (assignmentRepo.findAll() == null) {
			logger.warn("**GET ALL ASSIGNMENTS** No assignments in database.");
			return new ResponseEntity<RESTError>(new RESTError(6532, "No assignments found in database."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**GET ALL ASSIGNMENTS** Attempt successful, assignemnts are present.");

		// fetch assignments and present to admin/headmaster
		logger.info("**GET ALL ASSIGNMENTS** Attempt to invoke service to translate assignemnts to DTOSs.");
		List<GETAssignmentDTO> ogAssignementsDTO = assignmentService
				.getAssignmentsDTOTranslation((List<AssignmentEntity>) assignmentRepo.findAll());
		logger.info("**GET ALL ASSIGNMENTS** Attempt successful, list retrieved. Exiting controller");

		return new ResponseEntity<List<GETAssignmentDTO>>(ogAssignementsDTO, HttpStatus.OK);
	}

	/***************************************************************************************
	 * GET endpoint for administrator looking to fetch an assignment by ID.
	 * -- postman code adm047 --
	 * 
	 * @param assignment id
	 * @return if ok assignemnts with given id
	 **************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/{assignmentID}")
	public ResponseEntity<?> getAssignmentByID(@PathVariable Long assignmentID) {

		logger.info("**GET ASSIGNMENT BY ID** Access to the endpoint successful.");

		logger.info("**GET ASSIGNMENT BY ID** Attempt to find a assignment in database.");
		Optional<AssignmentEntity> ogAssignment = assignmentRepo.findById(assignmentID);
		if (ogAssignment.isEmpty()) {
			logger.warn("**GET ASSIGNMENT BY ID** No assignment with given id in database.");
			return new ResponseEntity<RESTError>(new RESTError(6535, "No assignment with given id in database."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**GET ASSIGNMENT BY ID** Assignment found.");

		logger.info("**GET ASSIGNMENT BY ID** All done, output to DTO.");
		return new ResponseEntity<GETAssignmentDTO>(assignmentService.getAssignmentDTOTranslation(ogAssignment.get()),
				HttpStatus.OK);
	}

	/***************************************************************************************
	 * GET endpoint for administrator looking to fetch all assignments by student.
	 * -- postman code adm048 --
	 * 
	 * @param student id
	 * @return if ok list of assignments given to a student
	 **************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/student/{studentID}")
	public ResponseEntity<?> getAssignmentsByStudent(@PathVariable Long studentID) {

		logger.info("**GET ASSIGNMENTS BY STUDENT** Access to the endpoint successful.");

		logger.info("**GET ASSIGNMENTS BY STUDENT** Attempt to find the student in database.");
		Optional<StudentEntity> ogStudent = studentRepo.findById(studentID);
		if (ogStudent.isEmpty()) {
			logger.warn("**GET ASSIGNMENTS BY STUDENT** No student with given id in database.");
			return new ResponseEntity<RESTError>(new RESTError(6541, "No student with given id in database."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**GET ASSIGNMENTS BY STUDENT** Student found.");

		List<AssignmentEntity> ogAssignments = assignmentRepo.findByAssignedTo(ogStudent.get());
		logger.info("**GET ASSIGNMENTS BY STUDENT** Attempt to find if student has any assignments.");
		if (ogAssignments.isEmpty()) {
			logger.warn("**GET ASSIGNMENTS BY STUDENT** Student associated with no assignemnets.");
			return new ResponseEntity<RESTError>(new RESTError(6511, "Student associated with no assignmnets."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**GET ASSIGNMENTS BY STUDENT** Assignments found.");

		logger.info("**GET ASSIGNMENTS BY STUDENT** Attempt to invoke service to translate assignemnts to DTOSs.");
		List<GETAssignmentDTO> ogAssignementsDTO = assignmentService.getAssignmentsDTOTranslation(ogAssignments);
		logger.info("**GET ASSIGNMENTS BY STUDENT** Attempt successful, list retrieved. Exiting controller");

		return new ResponseEntity<List<GETAssignmentDTO>>(ogAssignementsDTO, HttpStatus.OK);
	}

	/***************************************************************************************************
	 * GET endpoint for administrator looking to fetch all assignments issued by a teacher on a subject.
	 * -- postman code adm049 --
	 * 
	 * @param teacher id
	 * @return if ok list of assignments issued by a teacher
	 ***************************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER", "ROLE_TEACHER", "ROLE_HOMEROOM" })
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/teacherSubject/{subjectTeacherID}")
	public ResponseEntity<?> getAssignmentsByTeacher(@PathVariable Long subjectTeacherID) {

		logger.info("**GET ASSIGNMENTS BY TEACHER-SUBJECT** Access to the endpoint successful.");

		logger.info(
				"**GET ASSIGNMENTS BY TEACHER-SUBJECT** Attempt to find the teacher-subject combination in database.");
		Optional<TeacherSubjectEntity> ogTeacherSubject = teacherSubjectRepo.findById(subjectTeacherID);
		if (ogTeacherSubject.isEmpty()) {
			logger.warn(
					"**GET ASSIGNMENTS BY TEACHER-SUBJECT** No teacher-subject combination with given id in database.");
			return new ResponseEntity<RESTError>(
					new RESTError(6541, "No teacher-subject combination with given id in database."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**GET ASSIGNMENTS BY TEACHER-SUBJECT** Teacher-subject found.");

		logger.info(
				"**GET ASSIGNMENTS BY TEACHER-SUBJECT** Attempt to check roles and allow only teacher who teach the subject, admin and headmaster.");
		if (!ogTeacherSubject.get().getTeacher().getUsername().equals(userService.whoAmI()) && !userService.amIAdmin()
				&& !userService.amIHeadmaster()) {
			logger.warn(
					"**GET ASSIGNMENTS BY TEACHER-SUBJECT** Role not adequate or teacher not from the teacher-subject combination.");
			return new ResponseEntity<RESTError>(
					new RESTError(6841, "Role not adequate or teacher not from the teacher-subject combination."),
					HttpStatus.BAD_REQUEST);
		}
		logger.info("**GET ASSIGNMENTS BY TEACHER-SUBJECT** Role cleared.");

		List<AssignmentEntity> ogAssignments = assignmentRepo.findByTeacherIssuing(ogTeacherSubject.get());
		logger.info(
				"**GET ASSIGNMENTS BY TEACHER-SUBJECT** Attempt to find if teacher-subject has given out any assignments.");
		if (ogAssignments.isEmpty()) {
			logger.warn("**GET ASSIGNMENTS BY TEACHER-SUBJECT** Teacher-subject associated with no assignemnets.");
			return new ResponseEntity<RESTError>(
					new RESTError(6511, "Teacher teaching a subject associated with no assignmnets."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**GET ASSIGNMENTS BY TEACHER-SUBJECT** Assignments found.");

		logger.info(
				"**GET ASSIGNMENTS BY TEACHER-SUBJECT** Attempt to invoke service to translate assignemnts to DTOSs.");
		List<GETAssignmentDTO> ogAssignementsDTO = assignmentService.getAssignmentsDTOTranslation(ogAssignments);
		logger.info("**GET ASSIGNMENTS BY TEACHER-SUBJECT** Attempt successful, list retrieved. Exiting controller");

		return new ResponseEntity<List<GETAssignmentDTO>>(ogAssignementsDTO, HttpStatus.OK);
	}

	/***************************************************************************************
	 * GET endpoint for student looking to fetch all associated assignments.
	 * -- postman code adm050 --
	 * 
	 * @param student id
	 * @return if ok list of assignments given to a student
	 **************************************************************************************/
	@Secured("ROLE_STUDENT")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/student")
	public ResponseEntity<?> getMyAssignments() {

		logger.info("**GET MY(STUDENT) ASSIGNMENTS** Access to the endpoint successful.");

		Optional<UserEntity> ogStudent = userRepo.findByUsername(userService.whoAmI());

		List<AssignmentEntity> ogAssignments = assignmentRepo.findByAssignedTo((StudentEntity) ogStudent.get());
		logger.info("**GET MY(STUDENT) ASSIGNMENTS** Attempt to find if student has any assignments.");
		if (ogAssignments.isEmpty()) {
			logger.warn("**GET MY(STUDENT) ASSIGNMENTS** Student associated with no assignemnets.");
			return new ResponseEntity<RESTError>(new RESTError(6511, "Student associated with no assignmnets."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**GET MY(STUDENT) ASSIGNMENTS** Assignments found.");

		logger.info("**GET MY(STUDENT) ASSIGNMENTS** Attempt to invoke service to translate assignemnts to DTOSs.");
		List<GETAssignmentDTO> ogAssignementsDTO = assignmentService.getAssignmentsDTOTranslation(ogAssignments);
		logger.info("**GET MY(STUDENT) ASSIGNMENTS** Attempt successful, list retrieved. Exiting controller");

		return new ResponseEntity<List<GETAssignmentDTO>>(ogAssignementsDTO, HttpStatus.OK);
	}

	/***************************************************************************************
	 * GET endpoint for parent looking to fetch all student associated assignments.
	 * -- postman code adm051 --
	 * 
	 * @param student id
	 * @return if ok list of assignments given to a student
	 **************************************************************************************/
	@Secured("ROLE_PARENT")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/parent/{student}")
	public ResponseEntity<?> getMyChildrensAssignments(@PathVariable String student) {

		logger.info("**GET MY(CHILDRENS) ASSIGNMENTS** Access to the endpoint successful.");

		Optional<UserEntity> ogParent = userRepo.findByUsername(userService.whoAmI());

		ParentEntity ogParentCast = (ParentEntity) ogParent.get();

		Optional<UserEntity> ogStudent = userRepo.findByUsername(student);

		StudentEntity ogStudentCast = (StudentEntity) ogStudent.get();

		logger.info("**GET MY(CHILDRENS) ASSIGNMENTS** Attempt to find if student and logged parent are related.");
		if (!userService.areWeRelated(ogParentCast, ogStudentCast)) {
			logger.warn("**GET MY(CHILDRENS) ASSIGNMENTS** No relation between users.");
			return new ResponseEntity<RESTError>(new RESTError(6511, "Student and logged user not related."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**GET MY(CHILDRENS) ASSIGNMENTS** Users related.");

		List<AssignmentEntity> ogAssignments = assignmentRepo.findByAssignedTo((StudentEntity) ogStudent.get());
		logger.info("**GET MY(CHILDRENS) ASSIGNMENTS** Attempt to find if student has any assignments.");
		if (ogAssignments.isEmpty()) {
			logger.warn("**GET MY(CHILDRENS) ASSIGNMENTS** Student associated with no assignemnets.");
			return new ResponseEntity<RESTError>(new RESTError(6511, "Student associated with no assignmnets."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**GET MY(CHILDRENS) ASSIGNMENTS** Assignments found.");

		logger.info("**GET MY(CHILDRENS) ASSIGNMENTS** Attempt to invoke service to translate assignemnts to DTOSs.");
		List<GETAssignmentDTO> ogAssignementsDTO = assignmentService.getAssignmentsDTOTranslation(ogAssignments);
		logger.info("**GET MY(CHILDRENS) ASSIGNMENTS** Attempt successful, list retrieved. Exiting controller");

		return new ResponseEntity<List<GETAssignmentDTO>>(ogAssignementsDTO, HttpStatus.OK);
	}

	/***************************************************************************************
	 * GET endpoint for admin looking to fetch all assignments with pagination.
	 * -- postman code adm057 --
	 * 
	 * 
	 * @return if ok list of assignments with options
	 **************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/search")
	public ResponseEntity<?> getAllAssignmentsPagable(@RequestParam(defaultValue = "0") Integer pageNo,
			@RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "id") String sortBy,
			@RequestParam String sortOrder) {

		logger.info("**ADMIN PAGINATED ASSIGNMENTS** Access to the endpoint successful.");

		return assignmentService.getAssignmentsPaginated(pageNo, pageSize, sortBy, sortOrder);

	}

	/***************************************************************************************
	 * GET endpoint for student looking to fetch all associated assignments with pagination.
	 * -- postman code adm059 --
	 * 
	 * @param student id
	 * @return if ok paginated list of assignments given to a student
	 **************************************************************************************/
	@Secured("ROLE_STUDENT")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/search/student")
	public ResponseEntity<?> getMyPaginatedAssignments(@RequestParam(defaultValue = "0") Integer pageNo,
			@RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "id") String sortBy,
			@RequestParam String sortOrder) {

		logger.info("**GET MY(STUDENT) PAGINATED ASSIGNMENTS** Access to the endpoint successful.");

		Optional<UserEntity> ogStudent = userRepo.findByUsername(userService.whoAmI());

		logger.info(
				"**GET MY(CHILDRENS) PAGINATED ASSIGNMENTS** Attempt to invoke service to translate assignemnts to DTOSs.");

		return assignmentService.getAssignmentsPaginatedForStudent(ogStudent.get().getId(), pageNo, pageSize, sortBy,
				sortOrder);
	}

	/**********************************************************************************************
	 * GET endpoint for parent looking to fetch all student associated assignments with pagination.
	 * -- postman code adm058 --
	 * 
	 * @param student id
	 * @return if ok paginated list of assignments given to a student
	 **********************************************************************************************/
	@Secured("ROLE_PARENT")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/search/parent/{student}")
	public ResponseEntity<?> getMyChildrensAssignments(@PathVariable String student,
			@RequestParam(defaultValue = "0") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize,
			@RequestParam(defaultValue = "id") String sortBy, @RequestParam String sortOrder) {

		logger.info("**GET MY(CHILDRENS) PAGINATED ASSIGNMENTS** Access to the endpoint successful.");

		Optional<UserEntity> ogParent = userRepo.findByUsername(userService.whoAmI());

		ParentEntity ogParentCast = (ParentEntity) ogParent.get();

		Optional<UserEntity> ogStudent = userRepo.findByUsername(student);

		StudentEntity ogStudentCast = (StudentEntity) ogStudent.get();

		logger.info(
				"**GET MY(CHILDRENS) PAGINATED ASSIGNMENTS** Attempt to find if student and logged parent are related.");
		if (!userService.areWeRelated(ogParentCast, ogStudentCast)) {
			logger.warn("**GET MY(CHILDRENS) PAGINATED ASSIGNMENTS** No relation between users.");
			return new ResponseEntity<RESTError>(new RESTError(6511, "Student and logged user not related."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**GET MY(CHILDRENS) PAGINATED ASSIGNMENTS** Users related.");
		logger.info(
				"**GET MY(CHILDRENS) PAGINATED ASSIGNMENTS** Attempt to invoke service to translate assignemnts to DTOSs.");

		return assignmentService.getAssignmentsPaginatedForStudent(ogStudent.get().getId(), pageNo, pageSize, sortBy,
				sortOrder);
	}

	/*******************************************************************************************************
	 * GET endpoint for homeroom teacher looking to fetch all for student group assignments with pagination.
	 * -- postman code adm060 --
	 * 
	 * @param
	 * @return if ok paginated list of assignments for students in homerooms student group
	 *******************************************************************************************************/
	@Secured("ROLE_HOMEROOM")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/search/homeroom")
	public ResponseEntity<?> getMyStudentGroupAssignments(@RequestParam(defaultValue = "0") Integer pageNo,
			@RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "id") String sortBy,
			@RequestParam String sortOrder) {

		logger.info("**GET STUDENT GROUP PAGINATED ASSIGNMENTS** Access to the endpoint successful.");

		Optional<UserEntity> ogUser = userRepo.findByUsername(userService.whoAmI());

		TeacherEntity ogHomeroomCast = (TeacherEntity) ogUser.get();

		logger.info(
				"**GET STUDENT GROUP PAGINATED ASSIGNMENTS** Attempt to invoke service to translate assignemnts to DTOSs.");

		return assignmentService.getAssignmentsPaginatedForHomeroom(ogHomeroomCast.getInChargeOf().getId(), pageNo,
				pageSize, sortBy, sortOrder);
	}

	/*******************************************************************************************************
	 * GET endpoint for teacher looking to fetch all assignments for he issued.
	 * -- postman code adm061 --
	 * 
	 * @param
	 * @return if ok paginated list of assignments issued by teacher
	 *******************************************************************************************************/
	@Secured("ROLE_HOMEROOM")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, path = "/search/teacher")
	public ResponseEntity<?> getMyTeacherAssignments(@RequestParam(defaultValue = "0") Integer pageNo,
			@RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "id") String sortBy,
			@RequestParam String sortOrder) {

		logger.info("**GET TEACHER PAGINATED ASSIGNMENTS** Access to the endpoint successful.");

		Optional<UserEntity> ogUser = userRepo.findByUsername(userService.whoAmI());

		TeacherEntity ogTeacherCast = (TeacherEntity) ogUser.get();

		logger.info(
				"**GET TEACHER PAGINATED ASSIGNMENTS** Attempt to invoke service to translate assignemnts to DTOSs.");

		return assignmentService.getAssignmentsPaginatedForTeacher(ogTeacherCast.getId(), pageNo, pageSize, sortBy,
				sortOrder);
	}
}
