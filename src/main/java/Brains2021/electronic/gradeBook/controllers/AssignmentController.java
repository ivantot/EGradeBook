package Brains2021.electronic.gradeBook.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.dtos.in.CreateAssignmentDTO;
import Brains2021.electronic.gradeBook.entites.AssignmentEntity;
import Brains2021.electronic.gradeBook.repositories.AssignmentRepository;
import Brains2021.electronic.gradeBook.security.Views;
import Brains2021.electronic.gradeBook.services.assignment.AssignmentService;
import Brains2021.electronic.gradeBook.services.subject.SubjectService;
import Brains2021.electronic.gradeBook.utils.RESTError;

@RestController
@RequestMapping(path = "/api/v1/assignments")
public class AssignmentController {

	@Autowired
	private AssignmentRepository assignmentRepo;

	@Autowired
	private AssignmentService assignmentService;

	@Autowired
	private SubjectService subjectService;

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
		if (assignmentService.createAssignmentDTOtranslation(assignment) == null) {
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
	 * -- postman code adm017 --
	 * 
	 * @param assignment
	 * @param student
	 * @return if ok, assignment linked to student
	 ***************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_HOMEROOM", "ROLE_HEADMASTER" })
	@JsonView(Views.Teacher.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/assignToStudent")
	public ResponseEntity<?> assignToStudent(String assignment, String student) {

		// TODO check if teacher was the one who issued the assignment
		// TODO check if student belongs to a group taking the subject taught by the teacher
		// TODO asign to student

		return new ResponseEntity<String>(
				"Assignment " + "TODO" + " given by " + "TODO" + " asigned to student " + "TODO" + ".", HttpStatus.OK);
	}

}
