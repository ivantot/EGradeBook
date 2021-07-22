package Brains2021.electronic.gradeBook.services.teacherSubject;

import org.springframework.http.ResponseEntity;

import Brains2021.electronic.gradeBook.dtos.in.CreateTeacherSubjectDTO;
import Brains2021.electronic.gradeBook.entites.TeacherSubjectEntity;

public interface TeacherSubjectService {

	public TeacherSubjectEntity createTeacherSubjectDTOtranslation(CreateTeacherSubjectDTO teacherSubject);

	public ResponseEntity<?> createdTeacherSubjectDTOtranslation(TeacherSubjectEntity teacherSubject);
	
}