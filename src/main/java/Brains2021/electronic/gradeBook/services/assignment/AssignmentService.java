package Brains2021.electronic.gradeBook.services.assignment;

import org.springframework.http.ResponseEntity;

import Brains2021.electronic.gradeBook.dtos.in.CreateAssignmentDTO;
import Brains2021.electronic.gradeBook.entites.AssignmentEntity;

public interface AssignmentService {

	public AssignmentEntity createAssignmentDTOtranslation(CreateAssignmentDTO assignment);

	public ResponseEntity<?> createdAssignmentDTOtranslation(AssignmentEntity assignment);

	public ResponseEntity<?> sendEmailForGradedAssignemnt(AssignmentEntity assignment);

}
