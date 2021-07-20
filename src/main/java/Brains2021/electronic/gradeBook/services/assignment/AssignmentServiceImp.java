package Brains2021.electronic.gradeBook.services.assignment;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import Brains2021.electronic.gradeBook.dtos.in.CreateAssignmentDTO;
import Brains2021.electronic.gradeBook.dtos.out.CreatedAssignmentDTO;
import Brains2021.electronic.gradeBook.entites.AssignmentEntity;
import Brains2021.electronic.gradeBook.entites.TeacherSubjectEntity;
import Brains2021.electronic.gradeBook.repositories.TeacherSubjectRepository;
import Brains2021.electronic.gradeBook.services.user.UserService;
import Brains2021.electronic.gradeBook.utils.enums.EAssignmentType;
import Brains2021.electronic.gradeBook.utils.enums.ESubjectName;

@Service
public class AssignmentServiceImp implements AssignmentService {

	@Autowired
	private TeacherSubjectRepository teacherSubjectRepo;

	@Autowired
	private UserService userService;

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
		newAssignment.setDeleted(false);
		newAssignment.setDescription(assignment.getDescription());
		newAssignment.setSemester(assignment.getSemester());
		newAssignment.setStudyYear(assignment.getStudyYear());
		newAssignment.setTeacherIssuing(teachingTeacher.get());

		return newAssignment;
	}

	@Override
	public ResponseEntity<?> createdAssignmentDTOtranslation(AssignmentEntity assignment) {

		CreatedAssignmentDTO newAssignmentDTO = new CreatedAssignmentDTO();

		newAssignmentDTO.setDateCreated(assignment.getDateCreated());
		newAssignmentDTO.setDescription(assignment.getDescription());
		newAssignmentDTO.setSemester(assignment.getSemester());
		newAssignmentDTO.setStudyYear(assignment.getStudyYear());
		newAssignmentDTO.setSubject(assignment.getTeacherIssuing().getSubject().getName().toString());
		newAssignmentDTO.setTeacher(assignment.getTeacherIssuing().getTeacher().getUsername());
		newAssignmentDTO.setType(assignment.getType().toString());

		return new ResponseEntity<CreatedAssignmentDTO>(newAssignmentDTO, HttpStatus.OK);
	}

}
