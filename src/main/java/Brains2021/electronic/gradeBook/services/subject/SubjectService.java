package Brains2021.electronic.gradeBook.services.subject;

import org.springframework.http.ResponseEntity;

import Brains2021.electronic.gradeBook.dtos.in.CreateSubjectDTO;
import Brains2021.electronic.gradeBook.entites.SubjectEntity;

public interface SubjectService {

	public Boolean isSubjectInEnum(String subject);

	public SubjectEntity createSubjectDTOtranslation(CreateSubjectDTO subject);

	public ResponseEntity<?> createdSubjectDTOtranslation(SubjectEntity subject);

	public SubjectEntity updateSubjectDTOtranslation(CreateSubjectDTO subject, Long ogSubjectID);
}