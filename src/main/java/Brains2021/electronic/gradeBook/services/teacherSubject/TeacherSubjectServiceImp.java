package Brains2021.electronic.gradeBook.services.teacherSubject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import Brains2021.electronic.gradeBook.dtos.in.CreateTeacherSubjectDTO;
import Brains2021.electronic.gradeBook.dtos.out.CreatedTeacherSubjectDTO;
import Brains2021.electronic.gradeBook.dtos.out.GETTeacherSubjectDTO;
import Brains2021.electronic.gradeBook.entites.StudentGroupTakingASubjectEntity;
import Brains2021.electronic.gradeBook.entites.TeacherSubjectEntity;
import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;
import Brains2021.electronic.gradeBook.repositories.SubjectRepository;
import Brains2021.electronic.gradeBook.repositories.UserRepository;
import Brains2021.electronic.gradeBook.utils.enums.ESubjectName;

@Service
public class TeacherSubjectServiceImp implements TeacherSubjectService {

	@Autowired
	private SubjectRepository subjectRepo;

	@Autowired
	private UserRepository userRepo;

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	/**
	 * 
	 * service that takes an input DTO and translates to entity and populates remaining fields
	 * used for creating Teacher - Subject combos
	 * 
	 */
	@Override
	public TeacherSubjectEntity createTeacherSubjectDTOtranslation(CreateTeacherSubjectDTO teacherSubject) {

		logger.info("##TEACHER - SUBJECT SERVICE## Service for translation from DTO to Entity.");

		// translate DTO to entity and save to db
		TeacherSubjectEntity newTeacherSubject = new TeacherSubjectEntity();
		if (teacherSubject.getSubject() != null) {
			newTeacherSubject.setSubject(subjectRepo.findByNameAndYearOfSchooling(
					ESubjectName.valueOf(teacherSubject.getSubject()), teacherSubject.getYearOfSchooling()).get());
		}
		if (teacherSubject.getUsername() != null) {
			newTeacherSubject.setTeacher((TeacherEntity) userRepo.findByUsername(teacherSubject.getUsername()).get());
		}
		newTeacherSubject.setDeleted(0);
		if (teacherSubject.getWeeklyHoursAlloted() != null) {
			newTeacherSubject.setWeeklyHoursAlloted(teacherSubject.getWeeklyHoursAlloted());
		}

		logger.info("##TEACHER - SUBJECT SERVICE## Translation done, returning to controller.");

		return newTeacherSubject;
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

		logger.info("##TEACHER - SUBJECT SERVICE## Service for translation from Entity to DTO.");

		CreatedTeacherSubjectDTO newTeacherSubjectDTO = new CreatedTeacherSubjectDTO();

		if (teacherSubject.getSubject().getName() != null) {
			newTeacherSubjectDTO.setSubject(teacherSubject.getSubject().getName().toString());
		}
		if (teacherSubject.getTeacher().getName() != null) {
			newTeacherSubjectDTO.setName(teacherSubject.getTeacher().getName());
		}
		if (teacherSubject.getTeacher().getSurname() != null) {
			newTeacherSubjectDTO.setSurname(teacherSubject.getTeacher().getSurname());
		}
		if (teacherSubject.getWeeklyHoursAlloted() != null) {
			newTeacherSubjectDTO.setWeeklyHoursAlloted(teacherSubject.getWeeklyHoursAlloted());
		}
		if (teacherSubject.getSubject().getYearOfSchooling() != null) {
			newTeacherSubjectDTO.setYearOfSchooling(teacherSubject.getSubject().getYearOfSchooling());
		}

		logger.info("##TEACHER - SUBJECT SERVICE## Translation done, returning to controller."
				+ newTeacherSubjectDTO.toString());

		return new ResponseEntity<CreatedTeacherSubjectDTO>(newTeacherSubjectDTO, HttpStatus.OK);
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for getting Teacher - Subject combos
	 * 
	 */
	@Override
	public List<GETTeacherSubjectDTO> GETTeacherSubjectsDTOtranslation(List<TeacherSubjectEntity> teacherSubjects) {
		// translate entity to DTO 

		logger.info("##TEACHER - SUBJECT SERVICE## Service for translation to DTO.");
		List<GETTeacherSubjectDTO> newTeacherSubjectDTOs = new ArrayList<>();

		for (TeacherSubjectEntity teacherSubject : teacherSubjects) {
			GETTeacherSubjectDTO teacherSubjectDTO = new GETTeacherSubjectDTO();

			if (teacherSubject.getId() != null) {
				teacherSubjectDTO.setId(teacherSubject.getId());
			}
			if (teacherSubject.getDeleted() != null) {
				teacherSubjectDTO.setDeleted(teacherSubject.getDeleted());
			}
			if (teacherSubject.getStudentGroupsTakingASubject() != null) {
				Set<String> studentGroups = new HashSet<>();
				for (StudentGroupTakingASubjectEntity studentGroupTakingASubject : teacherSubject
						.getStudentGroupsTakingASubject()) {
					studentGroups.add(studentGroupTakingASubject.getStudentGroup().getYear() + "-"
							+ studentGroupTakingASubject.getStudentGroup().getYearIndex());
				}
				teacherSubjectDTO.setStudentGroupsTakingASubject(studentGroups);
			}
			if (teacherSubject.getSubject() != null) {
				teacherSubjectDTO.setSubject(teacherSubject.getSubject().getName().toString());
			}
			if (teacherSubject.getTeacher() != null) {
				teacherSubjectDTO.setTeacher(
						teacherSubject.getTeacher().getName() + " " + teacherSubject.getTeacher().getSurname());
			}
			if (teacherSubject.getWeeklyHoursAlloted() != null) {
				teacherSubjectDTO.setWeeklyHoursAlloted(teacherSubject.getWeeklyHoursAlloted());
			}
			newTeacherSubjectDTOs.add(teacherSubjectDTO);
		}
		logger.info("##TEACHER - SUBJECT SERVICE## Translation done, returning to controller.");

		return newTeacherSubjectDTOs;
	}

	@Override
	public GETTeacherSubjectDTO GETTeacherSubjectDTOtranslation(TeacherSubjectEntity teacherSubject) {

		logger.info("##TEACHER - SUBJECT SERVICE## Service for translation to DTO.");
		GETTeacherSubjectDTO teacherSubjectDTO = new GETTeacherSubjectDTO();

		if (teacherSubject.getId() != null) {
			teacherSubjectDTO.setId(teacherSubject.getId());
		}
		if (teacherSubject.getDeleted() != null) {
			teacherSubjectDTO.setDeleted(teacherSubject.getDeleted());
		}
		if (teacherSubject.getStudentGroupsTakingASubject() != null) {
			Set<String> studentGroups = new HashSet<>();
			for (StudentGroupTakingASubjectEntity studentGroupTakingASubject : teacherSubject
					.getStudentGroupsTakingASubject()) {
				studentGroups.add(studentGroupTakingASubject.getStudentGroup().getYear() + "-"
						+ studentGroupTakingASubject.getStudentGroup().getYearIndex());
			}
			teacherSubjectDTO.setStudentGroupsTakingASubject(studentGroups);
		}
		if (teacherSubject.getSubject() != null) {
			teacherSubjectDTO.setSubject(teacherSubject.getSubject().getName().toString());
		}
		if (teacherSubject.getTeacher() != null) {
			teacherSubjectDTO
					.setTeacher(teacherSubject.getTeacher().getName() + " " + teacherSubject.getTeacher().getSurname());
		}
		if (teacherSubject.getWeeklyHoursAlloted() != null) {
			teacherSubjectDTO.setWeeklyHoursAlloted(teacherSubject.getWeeklyHoursAlloted());
		}
		logger.info("##TEACHER - SUBJECT SERVICE## Translation done, returning to controller.");

		return teacherSubjectDTO;
	}

}
