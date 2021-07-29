package Brains2021.electronic.gradeBook.services.teacherSubject;

import java.util.List;

import org.springframework.http.ResponseEntity;

import Brains2021.electronic.gradeBook.dtos.in.CreateTeacherSubjectDTO;
import Brains2021.electronic.gradeBook.dtos.out.GETTeacherSubjectDTO;
import Brains2021.electronic.gradeBook.entites.TeacherSubjectEntity;

public interface TeacherSubjectService {

	public TeacherSubjectEntity createTeacherSubjectDTOtranslation(CreateTeacherSubjectDTO teacherSubject);

	public ResponseEntity<?> createdTeacherSubjectDTOtranslation(TeacherSubjectEntity teacherSubject);

	public List<GETTeacherSubjectDTO> GETTeacherSubjectsDTOtranslation(List<TeacherSubjectEntity> teacherSubjects);

	public GETTeacherSubjectDTO GETTeacherSubjectDTOtranslation(TeacherSubjectEntity teacherSubject);

}