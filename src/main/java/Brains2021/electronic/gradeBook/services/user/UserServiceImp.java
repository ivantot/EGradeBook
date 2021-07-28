package Brains2021.electronic.gradeBook.services.user;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
import Brains2021.electronic.gradeBook.entites.users.StudentParentEntity;
import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;
import Brains2021.electronic.gradeBook.entites.users.UserEntity;
import Brains2021.electronic.gradeBook.repositories.RoleRepository;
import Brains2021.electronic.gradeBook.repositories.StudentParentRepository;
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

	@Autowired
	private StudentParentRepository studentParentRepo;

	@Value("${spring.security.secret-key}")
	private String securityKey;

	@Value("${spring.securty.token-duration}")
	private Integer tokenDuration;

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

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
	 * service for checkinf if user is with admin role
	 * 
	 */
	@Override
	public Boolean amIAdmin() {
		if (userRepo.findByUsername(whoAmI()).get().getRole().getName().equals(ERole.ROLE_ADMIN)) {
			return true;
		}
		return false;
	}

	@Override
	public Boolean amITeacher() {
		if (userRepo.findByUsername(whoAmI()).get().getRole().getName().equals(ERole.ROLE_TEACHER)) {
			return true;
		}
		return false;
	}

	@Override
	public Boolean amIStudent() {
		if (userRepo.findByUsername(whoAmI()).get().getRole().getName().equals(ERole.ROLE_STUDENT)) {
			return true;
		}
		return false;
	}

	@Override
	public Boolean amIParent() {
		if (userRepo.findByUsername(whoAmI()).get().getRole().getName().equals(ERole.ROLE_PARENT)) {
			return true;
		}
		return false;
	}

	@Override
	public Boolean amIHeadmaster() {
		if (userRepo.findByUsername(whoAmI()).get().getRole().getName().equals(ERole.ROLE_HEADMASTER)) {
			return true;
		}
		return false;
	}

	@Override
	public Boolean amIHomeroom() {
		if (userRepo.findByUsername(whoAmI()).get().getRole().getName().equals(ERole.ROLE_HOMEROOM)) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * check for relation between users 
	 * 
	 */
	@Override
	public Boolean areWeRelated(ParentEntity parent, StudentEntity student) {
		if (studentParentRepo.findByStudentAndParentAndDeleted(student, parent, 0).isPresent()) {
			return true;
		}
		return false;
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

	@Override
	public String whoAmI() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username;

		if (principal instanceof UserDetails) {
			username = ((UserDetails) principal).getUsername();
		} else {
			username = principal.toString();
		}
		return username;
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
		newStudent.setStudentUniqueNumber(student.getStudentUniqueNumber());
		newStudent.setDateOfBirth(student.getDateOfBirth());
		newStudent.setRole(roleRepo.findByName(ERole.ROLE_STUDENT).get());
		newStudent.setDeleted(0);

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
		newStudentDTO.setStudentUniqueNumber(student.getStudentUniqueNumber());
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
		newParent.setDeleted(0);

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

		logger.info("##POST NEW TEACHER## Entered service for DTO translation to entity.");
		TeacherEntity newTeacher = new TeacherEntity();
		logger.info("##POST NEW TEACHER## Translating started.");
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
		newTeacher.setWeeklyHourCapacity(teacher.getWeeklyHourCapacity());
		newTeacher.setRole(roleRepo.findByName(ERole.ROLE_TEACHER).get());
		newTeacher.setIsAdministrator(0);
		newTeacher.setIsHomeroomTeacher(0);
		newTeacher.setIsHeadmaster(0);
		newTeacher.setDeleted(0);

		logger.info("##POST NEW TEACHER## Translation complete, exiting service and returning to endpoint.");
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

		logger.info("##POST NEW TEACHER## Entered service for DTO translation to entity.");
		CreatedTeacherDTO newTeacherDTO = new CreatedTeacherDTO();

		logger.info("##POST NEW TEACHER## Translating started.");
		newTeacherDTO.setName(teacher.getName());
		newTeacherDTO.setSurname(teacher.getSurname());
		newTeacherDTO.setDateOfBirth(teacher.getDateOfBirth());
		newTeacherDTO.setEmail(teacher.getEmail());
		newTeacherDTO.setJmbg(teacher.getJmbg());
		newTeacherDTO.setUsername(teacher.getUsername());
		newTeacherDTO.setSalary(teacher.getSalary());
		newTeacherDTO.setWeeklyHourCapacity(teacher.getWeeklyHourCapacity());
		newTeacherDTO.setStartOfEmployment(teacher.getStartOfEmployment());

		logger.info(
				"##POST NEW TEACHER## Translation complete, exiting service and returning to endpoint. All actions complete, teacher created.\n"
						+ newTeacherDTO.toString());
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

		logger.info(deletedUser.toString());
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
			teacher.setIsAdministrator(1);
			teacher.setIsHomeroomTeacher(0);
			teacher.setIsHeadmaster(0);
			teacher.setSalaryHeadmasterBonus(0.00);
			teacher.setSalaryHomeroomBonus(0.00);
			teacher.setSalaryAdminBonus(bonus);
		}
		if (role.equals(ERole.ROLE_HOMEROOM.toString())) {
			teacher.setIsAdministrator(0);
			teacher.setIsHomeroomTeacher(1);
			teacher.setIsHeadmaster(0);
			teacher.setSalaryHeadmasterBonus(0.00);
			teacher.setSalaryAdminBonus(0.00);
			teacher.setSalaryHomeroomBonus(bonus);

		}
		if (role.equals(ERole.ROLE_HEADMASTER.toString())) {
			teacher.setIsAdministrator(0);
			teacher.setIsHomeroomTeacher(0);
			teacher.setIsHeadmaster(1);
			teacher.setSalaryAdminBonus(0.00);
			teacher.setSalaryHomeroomBonus(0.00);
			teacher.setSalaryHeadmasterBonus(bonus);
		}
		if (role.equals(ERole.ROLE_TEACHER.toString())) {
			teacher.setIsAdministrator(0);
			teacher.setIsHomeroomTeacher(0);
			teacher.setIsHeadmaster(0);
			teacher.setSalaryHeadmasterBonus(0.00);
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
	public GetChildrenDTO foundChildrenDTOtranslation(StudentParentEntity student) {

		logger.info("##GET CHILDREN## Entered service for DTO translation to entity.");
		List<StudentParentEntity> ogParents = studentParentRepo.findByStudent(student.getStudent());

		logger.info("##GET CHILDREN## Setting all fields in Entity.");
		GetChildrenDTO getStudent = new GetChildrenDTO();
		getStudent.setName(student.getStudent().getName());
		getStudent.setSurname(student.getStudent().getSurname());
		getStudent.setRole(student.getStudent().getRole().getName().toString());
		getStudent.setUsername(student.getStudent().getUsername());
		if (student.getStudent().getBelongsToStudentGroup() != null) {
			getStudent.setStudentGroup(student.getStudent().getBelongsToStudentGroup().getYear() + "-"
					+ student.getStudent().getBelongsToStudentGroup().getYearIndex());
		}
		Map<String, String> parents = new HashMap<String, String>();
		for (StudentParentEntity parent : ogParents) {
			parents.put(parent.getParent().getName() + " " + parent.getParent().getSurname(),
					parent.getParent().getPhoneNumber());
		}
		getStudent.setParents(parents);
		logger.info("##GET CHILDREN## Exiting service for DTO translation to entity.");
		return getStudent;
	}

	/**
	 * 
	 * service that takes an entity and translates to DTO for pretty output
	 * used for fetching Parents via Student
	 * 
	 */
	@Override
	public GetParentsDTO foundParentsDTOtranslation(StudentParentEntity parent) {

		logger.info("##GET PARENTS## Entered service for DTO translation to entity.");
		List<StudentParentEntity> ogChildren = studentParentRepo.findByParent(parent.getParent());

		logger.info("##GET PARENTS## Setting all fields in Entity.");
		GetParentsDTO getParent = new GetParentsDTO();
		getParent.setName(parent.getParent().getName());
		getParent.setSurname(parent.getParent().getSurname());
		getParent.setRole(parent.getParent().getRole().getName().toString());
		getParent.setUsername(parent.getParent().getUsername());
		getParent.setPhoneNumber(parent.getParent().getPhoneNumber());
		Set<String> children = new HashSet<String>();
		for (StudentParentEntity child : ogChildren) {
			children.add(child.getStudent().getName() + " " + child.getStudent().getSurname());
		}
		getParent.setChildren(children);
		logger.info("##GET PARENTS## Exiting service for DTO translation to entity.");
		return getParent;
	}

}
