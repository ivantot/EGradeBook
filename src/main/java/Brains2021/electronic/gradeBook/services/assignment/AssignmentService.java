package Brains2021.electronic.gradeBook.services.assignment;

import java.util.List;

import org.springframework.http.ResponseEntity;

import Brains2021.electronic.gradeBook.dtos.in.CreateAssignmentDTO;
import Brains2021.electronic.gradeBook.dtos.out.GETAssignmentDTO;
import Brains2021.electronic.gradeBook.entites.AssignmentEntity;

public interface AssignmentService {

	public AssignmentEntity createAssignmentDTOtranslation(CreateAssignmentDTO assignment);

	public ResponseEntity<?> createdAssignmentDTOtranslation(AssignmentEntity assignment);

	public ResponseEntity<?> sendEmailForGradedAssignemnt(AssignmentEntity assignment);

	public List<GETAssignmentDTO> getAssignmentsDTOTranslation(List<AssignmentEntity> assignemnts);

	public GETAssignmentDTO getAssignmentDTOTranslation(AssignmentEntity assignement);

}
