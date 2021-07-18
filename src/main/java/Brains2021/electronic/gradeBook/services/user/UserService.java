package Brains2021.electronic.gradeBook.services.user;

import org.springframework.http.ResponseEntity;

import Brains2021.electronic.gradeBook.dtos.in.CreateParentDTO;
import Brains2021.electronic.gradeBook.dtos.in.CreateStudentDTO;
import Brains2021.electronic.gradeBook.dtos.in.CreateTeacherDTO;
import Brains2021.electronic.gradeBook.dtos.in.UpdateUserDTO;
import Brains2021.electronic.gradeBook.dtos.out.GetChildrenDTO;
import Brains2021.electronic.gradeBook.dtos.out.GetParentsDTO;
import Brains2021.electronic.gradeBook.dtos.out.GetUserDTO;
import Brains2021.electronic.gradeBook.entites.users.ParentEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentEntity;
import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;
import Brains2021.electronic.gradeBook.entites.users.UserEntity;

public interface UserService {

	public String createJWTToken(UserEntity user);

	public Boolean isRoleInEnum(String role);

	public String encodePassword(String passwordToEncode);

	public StudentEntity createStudentDTOtranslation(CreateStudentDTO student);

	public ResponseEntity<?> createdStudentDTOtranslation(StudentEntity student);

	public ParentEntity createParentDTOtranslation(CreateParentDTO parent);

	public ResponseEntity<?> createdParentDTOtranslation(ParentEntity parent);

	public TeacherEntity createTeacherDTOtranslation(CreateTeacherDTO teacher);

	public ResponseEntity<?> createdTeacherDTOtranslation(TeacherEntity teacher);

	public UserEntity updateUserDTOtranslation(UpdateUserDTO updatedUser, UserEntity ogUser);

	public ResponseEntity<?> updatedUserDTOtranslation(UserEntity user);

	public ResponseEntity<?> deletedUserDTOtranslation(UserEntity user);

	public TeacherEntity updateTeacherRole(TeacherEntity teacher, String role, Double bonus);

	public GetUserDTO foundUserDTOtranslation(UserEntity user);

	public GetChildrenDTO foundChildrenDTOtranslation(StudentEntity student);

	public GetParentsDTO foundParentsDTOtranslation(ParentEntity parent);

}
