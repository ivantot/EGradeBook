package Brains2021.electronic.gradeBook.services.studentGroup;

import java.util.List;

import Brains2021.electronic.gradeBook.dtos.out.GETStudentGroupsDTO;
import Brains2021.electronic.gradeBook.entites.StudentGroupEntity;

public interface StudentGroupService {

	public List<GETStudentGroupsDTO> GETStudentGroupsDTOtranslation(List<StudentGroupEntity> studentGroups);

	public GETStudentGroupsDTO GETStudentGroupDTOtranslation(StudentGroupEntity studentGroup);

}
