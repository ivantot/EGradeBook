package Brains2021.electronic.gradeBook.services.studentGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import Brains2021.electronic.gradeBook.dtos.out.GETStudentGroupsDTO;
import Brains2021.electronic.gradeBook.entites.StudentGroupEntity;
import Brains2021.electronic.gradeBook.entites.StudentGroupTakingASubjectEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentEntity;

@Service
public class StudentGroupServiceImp implements StudentGroupService {

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	@Override
	public List<GETStudentGroupsDTO> GETStudentGroupsDTOtranslation(List<StudentGroupEntity> studentGroups) {

		logger.info("##STUDENT GROUP SERVICE## Service for translation to DTO.");
		List<GETStudentGroupsDTO> newStudentGroupsDTOs = new ArrayList<>();

		for (StudentGroupEntity studentGroup : studentGroups) {
			GETStudentGroupsDTO studentGroupDTO = new GETStudentGroupsDTO();

			if (studentGroup.getId() != null) {
				studentGroupDTO.setId(studentGroup.getId());
			}
			if (studentGroup.getDeleted() != null) {
				studentGroupDTO.setDeleted(studentGroup.getDeleted());
			}
			if (studentGroup.getStudents() != null) {
				Set<String> students = new HashSet<>();
				for (StudentEntity student : studentGroup.getStudents()) {
					students.add(student.getName() + " " + student.getSurname());
				}
				studentGroupDTO.setStudents(students);
			}
			if (studentGroup.getHomeroomTeacher() != null) {
				studentGroupDTO.setHomeroomTeacher(studentGroup.getHomeroomTeacher().getName() + " "
						+ studentGroup.getHomeroomTeacher().getSurname());
			}
			if (studentGroup.getSubjectsTaken() != null) {
				Set<String> subjects = new HashSet<>();
				for (StudentGroupTakingASubjectEntity subject : studentGroup.getSubjectsTaken()) {
					subjects.add(subject.getTeacherSubject().getSubject().getName().toString());
				}
				studentGroupDTO.setSubjectsTaken(subjects);
			}
			if (studentGroup.getYear() != null) {
				studentGroupDTO.setDesignation(studentGroup.getYear() + "-" + studentGroup.getYearIndex());
			}
			newStudentGroupsDTOs.add(studentGroupDTO);
		}
		logger.info("##STUDENT GROUP SERVICE## Translation done, returning to controller.");

		return newStudentGroupsDTOs;
	}

	@Override
	public GETStudentGroupsDTO GETStudentGroupDTOtranslation(StudentGroupEntity studentGroup) {

		logger.info("##STUDENT GROUP SERVICE## Service for translation to DTO.");
		GETStudentGroupsDTO studentGroupDTO = new GETStudentGroupsDTO();

		if (studentGroup.getId() != null) {
			studentGroupDTO.setId(studentGroup.getId());
		}
		if (studentGroup.getDeleted() != null) {
			studentGroupDTO.setDeleted(studentGroup.getDeleted());
		}
		if (studentGroup.getStudents() != null) {
			Set<String> students = new HashSet<>();
			for (StudentEntity student : studentGroup.getStudents()) {
				students.add(student.getName() + " " + student.getSurname());
			}
			studentGroupDTO.setStudents(students);
		}
		if (studentGroup.getHomeroomTeacher() != null) {
			studentGroupDTO.setHomeroomTeacher(
					studentGroup.getHomeroomTeacher().getName() + " " + studentGroup.getHomeroomTeacher().getSurname());
		}
		if (studentGroup.getSubjectsTaken() != null) {
			Set<String> subjects = new HashSet<>();
			for (StudentGroupTakingASubjectEntity subject : studentGroup.getSubjectsTaken()) {
				subjects.add(subject.getTeacherSubject().getSubject().getName().toString());
			}
			studentGroupDTO.setSubjectsTaken(subjects);
		}
		if (studentGroup.getYear() != null) {
			studentGroupDTO.setDesignation(studentGroup.getYear() + "-" + studentGroup.getYearIndex());
		}

		logger.info("##STUDENT GROUP SERVICE## Translation done, returning to controller.");

		return studentGroupDTO;
	}

}
