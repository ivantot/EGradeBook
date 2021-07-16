package Brains2021.electronic.gradeBook.services.user;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import Brains2021.electronic.gradeBook.dtos.in.CreateUserDTO;
import Brains2021.electronic.gradeBook.dtos.out.CreatedUserDTO;
import Brains2021.electronic.gradeBook.entites.users.UserEntity;
import Brains2021.electronic.gradeBook.entites.users.ParentEntity;
import Brains2021.electronic.gradeBook.entites.users.StudentEntity;
import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;
import Brains2021.electronic.gradeBook.repositories.ParentRepository;
import Brains2021.electronic.gradeBook.repositories.RoleRepository;
import Brains2021.electronic.gradeBook.repositories.StudentRepository;
import Brains2021.electronic.gradeBook.repositories.TeacherRepository;
import Brains2021.electronic.gradeBook.utils.Encryption;
import Brains2021.electronic.gradeBook.utils.RESTError;
import Brains2021.electronic.gradeBook.utils.enums.ERole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class UserServiceImp implements UserService {

	@Autowired
	private RoleRepository roleRepo;

	@Autowired
	private TeacherRepository teacherRepo;

	@Autowired
	private ParentRepository parentRepo;

	@Autowired
	private StudentRepository studentRepo;

	@Value("${spring.security.secret-key}")
	private String securityKey;

	@Value("${spring.securty.token-duration}")
	private Integer tokenDuration;

	@Override
	public String createJWTToken(UserEntity user) {
		List<GrantedAuthority> grantedAuthority = AuthorityUtils
				.commaSeparatedStringToAuthorityList(user.getRole().getName().toString());
		String token = Jwts.builder().setId("softtekJWT").setSubject(user.getUsername())
				.claim("authorities",
						grantedAuthority.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + this.tokenDuration))
				.signWith(SignatureAlgorithm.HS512, this.securityKey).compact(); // string authorities i u filteru
		return token;
	}

	@Override
	public CreatedUserDTO translateToDTO(UserEntity user) {
		// 
		return null;
	}

	@Override
	public ResponseEntity<?> translateFromDTO(CreateUserDTO user) {

		// translate all parameters and populate if any are missing using logic per role provided

		//TODO think about additional checks - username existing

		if (user.getRole().equals(ERole.ROLE_ADMIN.toString())) {

			TeacherEntity newTeacher = new TeacherEntity();

			newTeacher.setName(user.getName());
			newTeacher.setSurname(user.getSurname());
			newTeacher.setEmail(user.getEmail());
			newTeacher.setUsername(user.getUsername());
			newTeacher.setPassword(Encryption.getPasswordEncoded(user.getPassword()));
			newTeacher.setJmbg(user.getJmbg());
			newTeacher.setDateOfBirth(user.getDateOfBirth());
			newTeacher.setStartOfEmployment(user.getStartOfEmployment());
			newTeacher.setSalary(user.getSalary());
			newTeacher.setRole(roleRepo.findByName(ERole.valueOf(user.getRole())).get());
			if (user.getAdminBonus() == null) {
				return new ResponseEntity<RESTError>(new RESTError(2, "Administrator must have a salary bonus."),
						HttpStatus.BAD_REQUEST);
			}
			newTeacher.setSalaryAdminBonus(user.getAdminBonus());
			newTeacher.setIsPrincipal(false);
			newTeacher.setIsAdministrator(true);
			newTeacher.setIsHomeroomTeacher(false);
			newTeacher.setDeleted(false);
			if (user.getSalary() == null) {
				newTeacher.setSalary(60000.00);
			}
			return new ResponseEntity<TeacherEntity>(teacherRepo.save(newTeacher), HttpStatus.OK);
		}

		if (user.getRole().equals(ERole.ROLE_HOMEROOM.toString())) {

			TeacherEntity newTeacher = new TeacherEntity();

			newTeacher.setName(user.getName());
			newTeacher.setSurname(user.getSurname());
			newTeacher.setEmail(user.getEmail());
			newTeacher.setUsername(user.getUsername());
			newTeacher.setPassword(Encryption.getPasswordEncoded(user.getPassword()));
			newTeacher.setJmbg(user.getJmbg());
			newTeacher.setDateOfBirth(user.getDateOfBirth());
			newTeacher.setStartOfEmployment(user.getStartOfEmployment());
			newTeacher.setSalary(user.getSalary());
			newTeacher.setRole(roleRepo.findByName(ERole.valueOf(user.getRole())).get());
			if (user.getHomeroomBonus() == null) {
				return new ResponseEntity<RESTError>(new RESTError(3, "Homeroom teacher must have a salary bonus."),
						HttpStatus.BAD_REQUEST);
			}
			newTeacher.setSalaryHomeroomBonus(user.getHomeroomBonus());
			newTeacher.setIsPrincipal(false);
			newTeacher.setIsAdministrator(false);
			newTeacher.setIsHomeroomTeacher(true);
			newTeacher.setDeleted(false);
			if (user.getSalary() == null) {
				newTeacher.setSalary(60000.00);
			}
			return new ResponseEntity<TeacherEntity>(teacherRepo.save(newTeacher), HttpStatus.OK);
		}

		if (user.getRole().equals(ERole.ROLE_PRINCIPAL.toString())) {

			TeacherEntity newTeacher = new TeacherEntity();

			newTeacher.setName(user.getName());
			newTeacher.setSurname(user.getSurname());
			newTeacher.setEmail(user.getEmail());
			newTeacher.setUsername(user.getUsername());
			newTeacher.setPassword(Encryption.getPasswordEncoded(user.getPassword()));
			newTeacher.setJmbg(user.getJmbg());
			newTeacher.setDateOfBirth(user.getDateOfBirth());
			newTeacher.setStartOfEmployment(user.getStartOfEmployment());
			newTeacher.setSalary(user.getSalary());
			newTeacher.setRole(roleRepo.findByName(ERole.valueOf(user.getRole())).get());
			if (user.getPrincipalBonus() == null) {
				return new ResponseEntity<RESTError>(new RESTError(4, "Principal must have a salary bonus."),
						HttpStatus.BAD_REQUEST);
			}
			newTeacher.setSalaryPrincipalBonus(user.getPrincipalBonus());
			newTeacher.setIsPrincipal(true);
			newTeacher.setIsAdministrator(false);
			newTeacher.setIsHomeroomTeacher(false);
			newTeacher.setDeleted(false);
			if (user.getSalary() == null) {
				newTeacher.setSalary(60000.00);
			}
			return new ResponseEntity<TeacherEntity>(teacherRepo.save(newTeacher), HttpStatus.OK);
		}

		if (user.getRole().equals(ERole.ROLE_TEACHER.toString())) {

			TeacherEntity newTeacher = new TeacherEntity();

			newTeacher.setName(user.getName());
			newTeacher.setSurname(user.getSurname());
			newTeacher.setEmail(user.getEmail());
			newTeacher.setUsername(user.getUsername());
			newTeacher.setPassword(Encryption.getPasswordEncoded(user.getPassword()));
			newTeacher.setJmbg(user.getJmbg());
			newTeacher.setDateOfBirth(user.getDateOfBirth());
			newTeacher.setStartOfEmployment(user.getStartOfEmployment());
			newTeacher.setSalary(user.getSalary());
			newTeacher.setRole(roleRepo.findByName(ERole.valueOf(user.getRole())).get());
			newTeacher.setIsPrincipal(false);
			newTeacher.setIsAdministrator(false);
			newTeacher.setIsHomeroomTeacher(false);
			newTeacher.setDeleted(false);
			if (user.getSalary() == null) {
				newTeacher.setSalary(60000.00);
			}
			return new ResponseEntity<TeacherEntity>(teacherRepo.save(newTeacher), HttpStatus.OK);
		}

		if (user.getRole().equals(ERole.ROLE_PARENT.toString())) {

			ParentEntity newParent = new ParentEntity();

			newParent.setName(user.getName());
			newParent.setSurname(user.getSurname());
			newParent.setEmail(user.getEmail());
			newParent.setUsername(user.getUsername());
			newParent.setPassword(Encryption.getPasswordEncoded(user.getPassword()));
			newParent.setJmbg(user.getJmbg());
			newParent.setDateOfBirth(user.getDateOfBirth());
			newParent.setPhoneNumber(user.getPhoneNumber());
			newParent.setRole(roleRepo.findByName(ERole.valueOf(user.getRole())).get());
			newParent.setDeleted(false);
			return new ResponseEntity<ParentEntity>(parentRepo.save(newParent), HttpStatus.OK);
		}

		if (user.getRole().equals(ERole.ROLE_STUDENT.toString())) {

			StudentEntity newStudent = new StudentEntity();

			newStudent.setName(user.getName());
			newStudent.setSurname(user.getSurname());
			newStudent.setEmail(user.getEmail());
			newStudent.setUsername(user.getUsername());
			newStudent.setPassword(Encryption.getPasswordEncoded(user.getPassword()));
			newStudent.setJmbg(user.getJmbg());
			newStudent.setDateOfBirth(user.getDateOfBirth());
			newStudent.setRole(roleRepo.findByName(ERole.valueOf(user.getRole())).get());
			newStudent.setDeleted(false);
			return new ResponseEntity<StudentEntity>(studentRepo.save(newStudent), HttpStatus.OK);
		}

		// return concrete object - one of inhereting classes
		return null;
	}

}
