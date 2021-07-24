package Brains2021.electronic.gradeBook.controllers;

import java.time.LocalDate;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.dtos.in.CreateAssignmentDTO;
import Brains2021.electronic.gradeBook.entites.AssignmentEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentEntity;
import Brains2021.electronic.gradeBook.entites.users.UserEntity;
import Brains2021.electronic.gradeBook.repositories.AssignmentRepository;
import Brains2021.electronic.gradeBook.repositories.TeacherSubjectRepository;
import Brains2021.electronic.gradeBook.repositories.UserRepository;
import Brains2021.electronic.gradeBook.security.Views;
import Brains2021.electronic.gradeBook.services.assignment.AssignmentService;
import Brains2021.electronic.gradeBook.services.subject.SubjectService;
import Brains2021.electronic.gradeBook.services.user.UserService;
import Brains2021.electronic.gradeBook.utils.RESTError;
import Brains2021.electronic.gradeBook.utils.enums.ERole;

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
	public ResponseEntity<?> postNewAssignemnt(@Valid @RequestBody CreateAssignmentDTO assignment) {

		if (!subjectService.isSubjectInEnum(assignment.getSubject())) {
			return new ResponseEntity<RESTError>(
					new RESTError(2000, "Subject name not allowed, check ESubjectName for details."),
					HttpStatus.BAD_REQUEST);
		}

		// invoke a service to verify that the teacher with credentials used to log in teaches the subject from the assignment
		if (assignmentService.createAssignmentDTOtranslation(assignment) == null
				&& !userRepo.findByUsername(userService.whoAmI()).get().getRole().getName().equals(ERole.ROLE_ADMIN)) {
			return new ResponseEntity<RESTError>(new RESTError(5001,
					"Logged teacher not teaching the subject posted in the assignment, please verify the subject."),
					HttpStatus.BAD_REQUEST);
		}

		AssignmentEntity newAssignment = assignmentService.createAssignmentDTOtranslation(assignment);
		assignmentRepo.save(newAssignment);

		return assignmentService.createdAssignmentDTOtranslation(newAssignment);
	}

	/***************************************************************************************
	 * PUT endpoint for teaching staff looking to link and assignment to a student
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
		if (!student.getBelongsToStudentGroup().getSubjectsTaken()
				.contains(teacherSubjectRepo.findBySubjectAndTeacher(
						assignment.get().getTeacherIssuing().getSubject().getName(), userService.whoAmI()))
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
	 * PUT endpoint for teaching staff looking to grade an assignment
	 * -- postman code adm020 --
	 * 
	 * @param assignmentID
	 * @return if ok, give grade to assignment
	 ***************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_HOMEROOM", "ROLE_HEADMASTER" })
	@JsonView(Views.Teacher.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/gradeAssignment")
	public ResponseEntity<?> gradeAssignment(@RequestParam Long assignmentID, @RequestParam Integer grade) {

		// validate grade
		if (grade != 1 && grade != 2 && grade != 3 && grade != 4 && grade != 5) {
			return new ResponseEntity<RESTError>(new RESTError(5007, "Provide a valid grade between 1 and 5."),
					HttpStatus.BAD_REQUEST);
		}

		Optional<AssignmentEntity> assignmentForGrading = assignmentRepo.findById(assignmentID);

		// check if id is valid and active
		if (assignmentForGrading.isEmpty()) {
			return new ResponseEntity<RESTError>(new RESTError(5002, "Not a valid assignment id, check and retry."),
					HttpStatus.BAD_REQUEST);
		}

		assignmentForGrading.get().setGradeRecieved(grade);
		assignmentForGrading.get().setDateCompleted(LocalDate.now());

		assignmentRepo.save(assignmentForGrading.get());

		assignmentService.sendEmailForGradedAssignemnt(assignmentForGrading.get());

		return new ResponseEntity<String>("Assignment " + assignmentForGrading.get().getType() + " in subject "
				+ assignmentForGrading.get().getTeacherIssuing().getSubject().getName() + " given by "
				+ assignmentForGrading.get().getTeacherIssuing().getTeacher().getName()
				+ assignmentForGrading.get().getTeacherIssuing().getTeacher().getSurname() + " asigned to student "
				+ assignmentForGrading.get().getAssignedTo().getName()
				+ assignmentForGrading.get().getAssignedTo().getSurname() + " on "
				+ assignmentForGrading.get().getDateAssigned() + " with due date "
				+ assignmentForGrading.get().getDueDate() + " has just been graded and recieved "
				+ assignmentForGrading.get().getGradeRecieved()
				+ ". And email has been sent to the parent(s) as a notification.", HttpStatus.OK);
	}

}
