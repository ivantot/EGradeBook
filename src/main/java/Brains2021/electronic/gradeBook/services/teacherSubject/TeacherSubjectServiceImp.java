package Brains2021.electronic.gradeBook.services.teacherSubject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import Brains2021.electronic.gradeBook.dtos.in.CreateTeacherSubjectDTO;
import Brains2021.electronic.gradeBook.dtos.out.CreatedTeacherSubjectDTO;
import Brains2021.electronic.gradeBook.entites.TeacherSubjectEntity;
import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;
import Brains2021.electronic.gradeBook.repositories.SubjectRepository;
import Brains2021.electronic.gradeBook.repositories.TeacherSubjectRepository;
import Brains2021.electronic.gradeBook.repositories.UserRepository;
import Brains2021.electronic.gradeBook.utils.enums.ESubjectName;

@Service
public class TeacherSubjectServiceImp implements TeacherSubjectService {

	@Autowired
	private TeacherSubjectRepository teacherSubjectRepo;

	@Autowired
	private SubjectRepository subjectRepo;

	@Autowired
	private UserRepository userRepo;

	/**
	 * 
	 * service that takes an input DTO and translates to entity and populates remaining fields
	 * used for creating Teacher - Subject combos
	 * 
	 */
	@Override
	public TeacherSubjectEntity createTeacherSubjectDTOtranslation(CreateTeacherSubjectDTO teacherSubject) {

		// translate DTO to entity and save to db
		TeacherSubjectEntity newTeacherSubject = new TeacherSubjectEntity();
		newTeacherSubject
				.setSubject(subjectRepo.findByNameAndYearOfSchooling(ESubjectName.valueOf(teacherSubject.getSubject()),
						teacherSubject.getYearOfSchooling()).get());
		newTeacherSubject.setTeacher((TeacherEntity) userRepo.findByUsername(teacherSubject.getUsername()).get());
		newTeacherSubject.setDeleted(false);
		newTeacherSubject.setWeeklyHoursAlloted(teacherSubject.getWeeklyHoursAlloted());

		return teacherSubjectRepo.save(newTeacherSubject);
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for creating Teacher - Subject combos
	 * 
	 */
	@Override
	public ResponseEntity<?> createdTeacherSubjectDTOtranslation(TeacherSubjectEntity teacherSubject) {
		// translate entity to DTO 

		CreatedTeacherSubjectDTO newTeacherSubjectDTO = new CreatedTeacherSubjectDTO();

		newTeacherSubjectDTO.setSubject(teacherSubject.getSubject().getName().toString());
		newTeacherSubjectDTO.setName(teacherSubject.getTeacher().getName());
		newTeacherSubjectDTO.setSurname(teacherSubject.getTeacher().getSurname());
		newTeacherSubjectDTO.setWeeklyHoursAlloted(teacherSubject.getWeeklyHoursAlloted());
		newTeacherSubjectDTO.setYearOfSchooling(teacherSubject.getSubject().getYearOfSchooling());

		return new ResponseEntity<CreatedTeacherSubjectDTO>(newTeacherSubjectDTO, HttpStatus.OK);
	}

}
