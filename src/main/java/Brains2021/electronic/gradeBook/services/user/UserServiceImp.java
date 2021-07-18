package Brains2021.electronic.gradeBook.services.user;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import Brains2021.electronic.gradeBook.dtos.in.CreateParentDTO;
import Brains2021.electronic.gradeBook.dtos.in.CreateStudentDTO;
import Brains2021.electronic.gradeBook.dtos.in.CreateTeacherDTO;
import Brains2021.electronic.gradeBook.dtos.in.UpdateUserDTO;
import Brains2021.electronic.gradeBook.dtos.out.CreatedParentDTO;
import Brains2021.electronic.gradeBook.dtos.out.CreatedStudentDTO;
import Brains2021.electronic.gradeBook.dtos.out.CreatedTeacherDTO;
import Brains2021.electronic.gradeBook.dtos.out.DeletedUserDTO;
import Brains2021.electronic.gradeBook.dtos.out.GetChildrenDTO;
import Brains2021.electronic.gradeBook.dtos.out.GetParentsDTO;
import Brains2021.electronic.gradeBook.dtos.out.GetUserDTO;
import Brains2021.electronic.gradeBook.dtos.out.UpdatedUserDTO;
import Brains2021.electronic.gradeBook.entites.users.ParentEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentEntity;
import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;
import Brains2021.electronic.gradeBook.entites.users.UserEntity;
import Brains2021.electronic.gradeBook.repositories.RoleRepository;
import Brains2021.electronic.gradeBook.repositories.UserRepository;
import Brains2021.electronic.gradeBook.utils.Encryption;
import Brains2021.electronic.gradeBook.utils.enums.ERole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class UserServiceImp implements UserService {

	@Autowired
	UserRepository userRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Value("${spring.security.secret-key}")
	private String securityKey;

	@Value("${spring.securty.token-duration}")
	private Integer tokenDuration;

	/**
	 * 
	 * service for generating tokens, sets authorities and token duration and signs token using key from app.props
	 * 
	 */
	@Override
	public String createJWTToken(UserEntity user) {
		List<GrantedAuthority> grantedAuthority = AuthorityUtils
				.commaSeparatedStringToAuthorityList(user.getRole().getName().toString());
		String token = Jwts.builder().setId("softtekJWT").setSubject(user.getUsername())
				.claim("authorities",
						grantedAuthority.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + this.tokenDuration))
				.signWith(SignatureAlgorithm.HS512, this.securityKey).compact();
		return token;
	}

	/**
	 * 
	 * check if ERole enum contains the provided role 
	 * 
	 */
	@Override
	public Boolean isRoleInEnum(String role) {
		ERole[] allRoles = ERole.values();
		for (ERole eRole : allRoles) {
			//do comparison
			if (role.equals(eRole.toString())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * quickly encode a password
	 * 
	 */

	@Override
	public String encodePassword(String passwordToEncode) {
		return Encryption.getPasswordEncoded(passwordToEncode);
	}

	/**
	 * 
	 * service that takes an input DTO and translates to entity and populates remaining fields
	 * used for creating Students
	 * 
	 */
	@Override
	public StudentEntity createStudentDTOtranslation(CreateStudentDTO student) {

		StudentEntity newStudent = new StudentEntity();

		newStudent.setName(student.getName());
		newStudent.setSurname(student.getSurname());
		newStudent.setEmail(student.getEmail());
		newStudent.setUsername(student.getUsername());
		newStudent.setPassword(student.getPassword());
		newStudent.setRepeatedPassword(student.getRepeatedPassword());
		newStudent.setJmbg(student.getJmbg());
		newStudent.setDateOfBirth(student.getDateOfBirth());
		newStudent.setRole(roleRepo.findByName(ERole.ROLE_STUDENT).get());
		newStudent.setDeleted(false);

		return newStudent;

	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for creating Students
	 * 
	 */
	@Override
	public ResponseEntity<?> createdStudentDTOtranslation(StudentEntity student) {

		CreatedStudentDTO newStudentDTO = new CreatedStudentDTO();

		newStudentDTO.setName(student.getName());
		newStudentDTO.setSurname(student.getSurname());
		newStudentDTO.setDateOfBirth(student.getDateOfBirth());
		newStudentDTO.setEmail(student.getEmail());
		newStudentDTO.setJmbg(student.getJmbg());
		newStudentDTO.setUsername(student.getUsername());

		return new ResponseEntity<CreatedStudentDTO>(newStudentDTO, HttpStatus.OK);
	}

	/**
	 * 
	 * service that takes an input DTO and translates to entity and populates remaining fields
	 * used for creating Parents
	 * 
	 */
	@Override
	public ParentEntity createParentDTOtranslation(CreateParentDTO parent) {

		ParentEntity newParent = new ParentEntity();

		newParent.setName(parent.getName());
		newParent.setSurname(parent.getSurname());
		newParent.setEmail(parent.getEmail());
		newParent.setUsername(parent.getUsername());
		newParent.setPassword(parent.getPassword());
		newParent.setRepeatedPassword(parent.getRepeatedPassword());
		newParent.setJmbg(parent.getJmbg());
		newParent.setDateOfBirth(parent.getDateOfBirth());
		newParent.setPhoneNumber(parent.getPhoneNumber());
		newParent.setRole(roleRepo.findByName(ERole.ROLE_PARENT).get());
		newParent.setDeleted(false);

		return newParent;
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for creating Parents
	 * 
	 */
	@Override
	public ResponseEntity<?> createdParentDTOtranslation(ParentEntity parent) {

		CreatedParentDTO newParentDTO = new CreatedParentDTO();

		newParentDTO.setName(parent.getName());
		newParentDTO.setSurname(parent.getSurname());
		newParentDTO.setDateOfBirth(parent.getDateOfBirth());
		newParentDTO.setEmail(parent.getEmail());
		newParentDTO.setJmbg(parent.getJmbg());
		newParentDTO.setUsername(parent.getUsername());
		newParentDTO.setPhoneNumber(parent.getPhoneNumber());

		return new ResponseEntity<CreatedParentDTO>(newParentDTO, HttpStatus.OK);

	}

	/**
	 * 
	 * service that takes an input DTO and translates to entity and populates remaining fields
	 * used for creating Teachers
	 * 
	 */
	@Override
	public TeacherEntity createTeacherDTOtranslation(CreateTeacherDTO teacher) {

		TeacherEntity newTeacher = new TeacherEntity();

		newTeacher.setName(teacher.getName());
		newTeacher.setSurname(teacher.getSurname());
		newTeacher.setEmail(teacher.getEmail());
		newTeacher.setUsername(teacher.getUsername());
		newTeacher.setPassword(teacher.getPassword());
		newTeacher.setRepeatedPassword(teacher.getRepeatedPassword());
		newTeacher.setJmbg(teacher.getJmbg());
		newTeacher.setDateOfBirth(teacher.getDateOfBirth());
		newTeacher.setStartOfEmployment(teacher.getStartOfEmployment());
		if (teacher.getSalary() != null) {
			newTeacher.setSalary(teacher.getSalary());
		} else {
			newTeacher.setSalary(60000.00);
		}
		newTeacher.setRole(roleRepo.findByName(ERole.ROLE_TEACHER).get());
		newTeacher.setIsAdministrator(false);
		newTeacher.setIsHomeroomTeacher(false);
		newTeacher.setIsPrincipal(false);
		newTeacher.setDeleted(false);

		return newTeacher;
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for creating Teachers
	 * 
	 */
	@Override
	public ResponseEntity<?> createdTeacherDTOtranslation(TeacherEntity teacher) {

		CreatedTeacherDTO newTeacherDTO = new CreatedTeacherDTO();

		newTeacherDTO.setName(teacher.getName());
		newTeacherDTO.setSurname(teacher.getSurname());
		newTeacherDTO.setDateOfBirth(teacher.getDateOfBirth());
		newTeacherDTO.setEmail(teacher.getEmail());
		newTeacherDTO.setJmbg(teacher.getJmbg());
		newTeacherDTO.setUsername(teacher.getUsername());
		newTeacherDTO.setSalary(teacher.getSalary());
		newTeacherDTO.setStartOfEmployment(teacher.getStartOfEmployment());

		return new ResponseEntity<CreatedTeacherDTO>(newTeacherDTO, HttpStatus.OK);
	}

	/**
	 * 
	 * service that takes an input DTO along with existing User and updates fields
	 * used for editing Users general info
	 * 
	 */
	@Override
	public UserEntity updateUserDTOtranslation(UpdateUserDTO updatedUser, UserEntity ogUser) {

		if (updatedUser.getName() != null && !updatedUser.getName().isBlank()) {
			ogUser.setName(updatedUser.getName());
		}
		if (updatedUser.getSurname() != null && !updatedUser.getSurname().isBlank()) {
			ogUser.setSurname(updatedUser.getSurname());
		}
		if (updatedUser.getEmail() != null && !updatedUser.getEmail().isBlank()) {
			ogUser.setEmail(updatedUser.getEmail());
		}
		if (updatedUser.getUsername() != null && !updatedUser.getUsername().isBlank()) {
			ogUser.setUsername(updatedUser.getUsername());
		}
		if (updatedUser.getJmbg() != null && !updatedUser.getJmbg().isBlank()) {
			ogUser.setJmbg(updatedUser.getJmbg());
		}
		if (updatedUser.getDateOfBirth() != null) {
			ogUser.setDateOfBirth(updatedUser.getDateOfBirth());
		}

		return ogUser;
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for editing Users general info
	 * 
	 */
	@Override
	public ResponseEntity<?> updatedUserDTOtranslation(UserEntity ogUser) {

		UpdatedUserDTO updatedUserDTO = new UpdatedUserDTO();

		updatedUserDTO.setName(ogUser.getName());
		updatedUserDTO.setSurname(ogUser.getSurname());
		updatedUserDTO.setDateOfBirth(ogUser.getDateOfBirth());
		updatedUserDTO.setEmail(ogUser.getEmail());
		updatedUserDTO.setJmbg(ogUser.getJmbg());
		updatedUserDTO.setUsername(ogUser.getUsername());

		return new ResponseEntity<UpdatedUserDTO>(updatedUserDTO, HttpStatus.OK);
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for deleting Users
	 * 
	 */
	@Override
	public ResponseEntity<?> deletedUserDTOtranslation(UserEntity user) {

		DeletedUserDTO deletedUser = new DeletedUserDTO();
		deletedUser.setName(user.getName());
		deletedUser.setSurname(user.getSurname());
		deletedUser.setRole(user.getRole().getName().toString());
		deletedUser.setUsername(user.getUsername());

		return new ResponseEntity<DeletedUserDTO>(deletedUser, HttpStatus.OK);
	}

	/**
	 * 
	 * service that takes care of asigning roles to teachers along with salary bonuses
	 * used for changing teacher's role
	 * 
	 */
	@Override
	public TeacherEntity updateTeacherRole(TeacherEntity teacher, String role, Double bonus) {

		teacher.setRole(roleRepo.findByName(ERole.valueOf(role)).get());

		// logic for assigning role-specific salary bonuses
		if (role.equals(ERole.ROLE_ADMIN.toString())) {
			teacher.setIsAdministrator(true);
			teacher.setIsHomeroomTeacher(false);
			teacher.setIsPrincipal(false);
			teacher.setSalaryPrincipalBonus(0.00);
			teacher.setSalaryHomeroomBonus(0.00);
			teacher.setSalaryAdminBonus(bonus);
		}
		if (role.equals(ERole.ROLE_HOMEROOM.toString())) {
			teacher.setIsAdministrator(false);
			teacher.setIsHomeroomTeacher(true);
			teacher.setIsPrincipal(false);
			teacher.setSalaryPrincipalBonus(0.00);
			teacher.setSalaryAdminBonus(0.00);
			teacher.setSalaryHomeroomBonus(bonus);

		}
		if (role.equals(ERole.ROLE_HEADMASTER.toString())) {
			teacher.setIsAdministrator(false);
			teacher.setIsHomeroomTeacher(false);
			teacher.setIsPrincipal(true);
			teacher.setSalaryAdminBonus(0.00);
			teacher.setSalaryHomeroomBonus(0.00);
			teacher.setSalaryPrincipalBonus(bonus);
		}
		if (role.equals(ERole.ROLE_TEACHER.toString())) {
			teacher.setIsAdministrator(false);
			teacher.setIsHomeroomTeacher(false);
			teacher.setIsPrincipal(false);
			teacher.setSalaryPrincipalBonus(0.00);
			teacher.setSalaryAdminBonus(0.00);
			teacher.setSalaryHomeroomBonus(0.00);
		}

		return userRepo.save(teacher);
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for fetching Users
	 * 
	 */
	@Override
	public GetUserDTO foundUserDTOtranslation(UserEntity user) {

		GetUserDTO getUser = new GetUserDTO();
		getUser.setName(user.getName());
		getUser.setSurname(user.getSurname());
		getUser.setRole(user.getRole().getName().toString());
		getUser.setUsername(user.getUsername());
		getUser.setEmail(user.getEmail());

		return getUser;
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for fetching Students via Parent
	 * 
	 */
	@Override
	public GetChildrenDTO foundChildrenDTOtranslation(StudentEntity student) {

		GetChildrenDTO getStudent = new GetChildrenDTO();
		getStudent.setName(student.getName());
		getStudent.setSurname(student.getSurname());
		getStudent.setRole(student.getRole().getName().toString());
		getStudent.setUsername(student.getUsername());
		getStudent.setBelongsToStudentGroup(student.getBelongsToStudentGroup());
		Map<String, String> parents = new HashMap<String, String>();
		for (ParentEntity parent : student.getParents()) {
			parents.put(parent.getUsername(), parent.getPhoneNumber());
		}
		getStudent.setParents(parents);

		return getStudent;
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for fetching Parents via Student
	 * 
	 */
	@Override
	public GetParentsDTO foundParentsDTOtranslation(ParentEntity parent) {

		GetParentsDTO getParent = new GetParentsDTO();
		getParent.setName(parent.getName());
		getParent.setSurname(parent.getSurname());
		getParent.setRole(parent.getRole().getName().toString());
		getParent.setUsername(parent.getUsername());
		getParent.setPhoneNumber(parent.getPhoneNumber());
		Set<String> children = new HashSet<String>();
		for (StudentEntity child : parent.getChildren()) {
			children.add(child.getUsername());
		}
		getParent.setChildren(children);

		return getParent;
	}

}
