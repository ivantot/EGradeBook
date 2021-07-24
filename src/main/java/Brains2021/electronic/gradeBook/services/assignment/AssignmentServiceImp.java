package Brains2021.electronic.gradeBook.services.assignment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import Brains2021.electronic.gradeBook.dtos.EmailObjectDTO;
import Brains2021.electronic.gradeBook.dtos.in.CreateAssignmentDTO;
import Brains2021.electronic.gradeBook.dtos.out.CreatedAssignmentDTO;
import Brains2021.electronic.gradeBook.entites.AssignmentEntity;
import Brains2021.electronic.gradeBook.entites.TeacherSubjectEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentParentEntity;
import Brains2021.electronic.gradeBook.repositories.StudentParentRepository;
import Brains2021.electronic.gradeBook.repositories.TeacherSubjectRepository;
import Brains2021.electronic.gradeBook.services.email.EmailService;
import Brains2021.electronic.gradeBook.services.user.UserService;
import Brains2021.electronic.gradeBook.utils.RESTError;
import Brains2021.electronic.gradeBook.utils.enums.EAssignmentType;
import Brains2021.electronic.gradeBook.utils.enums.ESubjectName;

@Service
public class AssignmentServiceImp implements AssignmentService {

	@Autowired
	private TeacherSubjectRepository teacherSubjectRepo;

	@Autowired
	private UserService userService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private StudentParentRepository studentParentRepo;

	@Override
	public AssignmentEntity createAssignmentDTOtranslation(CreateAssignmentDTO assignment) {

		// check to find if teacher teaches a subject, this allows only for a logged teacher to post assignments for subject he/she teaches

		Optional<TeacherSubjectEntity> teachingTeacher = teacherSubjectRepo
				.findBySubjectAndTeacher(ESubjectName.valueOf(assignment.getSubject()), userService.whoAmI());

		if (teachingTeacher.isEmpty()) {
			return null;
		}

		AssignmentEntity newAssignment = new AssignmentEntity();

		newAssignment.setDateCreated(LocalDate.now());
		newAssignment.setType(EAssignmentType.valueOf(assignment.getType()));
		newAssignment.setDeleted(0);
		newAssignment.setDescription(assignment.getDescription());
		newAssignment.setSemester(assignment.getSemester());
		newAssignment.setTeacherIssuing(teachingTeacher.get());

		return newAssignment;
	}

	@Override
	public ResponseEntity<?> createdAssignmentDTOtranslation(AssignmentEntity assignment) {

		CreatedAssignmentDTO newAssignmentDTO = new CreatedAssignmentDTO();

		newAssignmentDTO.setDateCreated(assignment.getDateCreated());
		newAssignmentDTO.setDescription(assignment.getDescription());
		newAssignmentDTO.setSemester(assignment.getSemester());
		newAssignmentDTO.setSubject(assignment.getTeacherIssuing().getSubject().getName().toString());
		newAssignmentDTO.setTeacher(assignment.getTeacherIssuing().getTeacher().getUsername());
		newAssignmentDTO.setType(assignment.getType().toString());

		return new ResponseEntity<CreatedAssignmentDTO>(newAssignmentDTO, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> sendEmailForGradedAssignemnt(AssignmentEntity assignment) {

		// Send email		
		EmailObjectDTO object = new EmailObjectDTO();

		List<StudentParentEntity> parents = studentParentRepo.findByStudent(assignment.getAssignedTo());
		List<String> parentEmails = new ArrayList<>();

		for (StudentParentEntity parent : parents) {
			parentEmails.add(parent.getParent().getEmail());
		}

		if (parentEmails.isEmpty()) {
			return new ResponseEntity<RESTError>(new RESTError(8002, "No parent associated with this student :(."),
					HttpStatus.BAD_REQUEST);
		}

		if (parentEmails.size() == 1) {
			object.setTo(parentEmails.get(0));
		}

		if (parentEmails.size() > 1) {
			object.setTo(parentEmails.get(0));
			object.setCc(parentEmails.get(1));
		}

		object.setSubject("New grade posted for " + assignment.getAssignedTo().getName() + ".");
		object.setStudentName(assignment.getAssignedTo().getName());
		object.setStudentLastName(assignment.getAssignedTo().getSurname());
		object.setTeacherName(assignment.getTeacherIssuing().getTeacher().getName());
		object.setTeacherLastName(assignment.getTeacherIssuing().getTeacher().getSurname());
		object.setDate(assignment.getDateCompleted());
		object.setGrade(assignment.getGradeRecieved().toString());
		object.setGradedSubject(assignment.getTeacherIssuing().getSubject().getName().toString());
		object.setAssignment(assignment.getType().toString());
		try {
			emailService.sendTemplateMessage(object);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
