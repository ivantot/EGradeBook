package Brains2021.electronic.gradeBook.controllers;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.entites.StudentGroupEntity;
import Brains2021.electronic.gradeBook.entites.SubjectEntity;
import Brains2021.electronic.gradeBook.entites.TeacherSubjectEntity;
import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;
import Brains2021.electronic.gradeBook.entites.users.UserEntity;
import Brains2021.electronic.gradeBook.repositories.StudentGroupRepository;
import Brains2021.electronic.gradeBook.repositories.SubjectRepository;
import Brains2021.electronic.gradeBook.repositories.TeacherSubjectRepository;
import Brains2021.electronic.gradeBook.repositories.UserRepository;
import Brains2021.electronic.gradeBook.security.Views;
import Brains2021.electronic.gradeBook.services.subject.SubjectService;
import Brains2021.electronic.gradeBook.utils.RESTError;
import Brains2021.electronic.gradeBook.utils.enums.ERole;
import Brains2021.electronic.gradeBook.utils.enums.ESubjectName;

@RestController
@RequestMapping(path = "/api/v1/teacherSubject")
public class TeacherSubjectController {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private SubjectService subjectService;

	@Autowired
	private SubjectRepository subjectRepo;

	@Autowired
	private TeacherSubjectRepository teacherSubjectRepo;

	@Autowired
	private StudentGroupRepository studentGroupRepository;

	/***************************************************************************************
	 * POST endpoint for administrator looking to assign subject to teacher.
	 * -- postman code adm007 / hdm100 --
	 * 
	 * @param subject
	 * @param username
	 * @return if ok teacher will be assigned to teach a subject
	 **************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Headmaster.class)
	@RequestMapping(method = RequestMethod.POST, path = "/assign")
	public ResponseEntity<?> assignSubjectToTeacher(@RequestParam String username, @RequestParam String subject) {

		if (!subjectService.isSubjectInEnum(subject)) {
			return new ResponseEntity<RESTError>(
					new RESTError(2000, "Subject name not allowed, check ESubjectName for details."),
					HttpStatus.BAD_REQUEST);
		}

		Optional<SubjectEntity> ogSubject = subjectRepo.findByName(ESubjectName.valueOf(subject));

		if (ogSubject.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(5030, "Subject not found in database, please provide a valid subject."),
					HttpStatus.NOT_FOUND);
		}

		Optional<UserEntity> ogUser = userRepo.findByUsername(username);

		if (ogUser.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(1030, "Username not found in database, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}

		// check if user is a teacher

		if (!ogUser.get().getRole().getName().equals(ERole.ROLE_ADMIN)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_HEADMASTER)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_HOMEROOM)
				&& !ogUser.get().getRole().getName().equals(ERole.ROLE_TEACHER)) {
			return new ResponseEntity<RESTError>(
					new RESTError(1060, "Not a teaching user, unable to assign a subject."), HttpStatus.BAD_REQUEST);
		}

		TeacherSubjectEntity newTeacherSubject = new TeacherSubjectEntity();
		newTeacherSubject.setDeleted(false);
		newTeacherSubject.setSubject(ogSubject.get());
		newTeacherSubject.setTeacher((TeacherEntity) ogUser.get());

		teacherSubjectRepo.save(newTeacherSubject);

		return new ResponseEntity<String>("Teacher " + username + " to teach " + subject + ".", HttpStatus.OK);
	}

	/***************************************************************************************
	 * PUT endpoint for administrator or headmaster looking to link a student group to a subject
	 * -- postman code adm017 --
	 * 
	 * @param student group
	 * @param subject taught by a teacher
	 * @return if ok, student group linked to a subject
	 ***************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Headmaster.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/assignSubjectToStudentGroup")
	public ResponseEntity<?> assignToStudentGroup(Long studentGroupID, Long subjectTaughtID) {

		Optional<StudentGroupEntity> studentGroup = studentGroupRepository.findByIdAndDeletedFalse(studentGroupID);

		if (studentGroup.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(5040, "Active student group not found in database, please provide a valid id."),
					HttpStatus.NOT_FOUND);
		}

		Optional<TeacherSubjectEntity> teacherSubject = teacherSubjectRepo.findByIdAndDeletedFalse(subjectTaughtID);

		if (teacherSubject.isEmpty()) {
			return new ResponseEntity<RESTError>(
					new RESTError(5050, "Active subject taught not found in database, please provide a valid id."),
					HttpStatus.NOT_FOUND);
		}

		//get all student groups taking a subject and add the new one, then return to teacher teaching the subject 
		Set<StudentGroupEntity> studentGoups = teacherSubject.get().getStudentGroupsTaking();
		studentGoups.add(studentGroup.get());
		teacherSubject.get().setStudentGroupsTaking(studentGoups);
		teacherSubjectRepo.save(teacherSubject.get());

		return new ResponseEntity<String>("Subject " + teacherSubject.get().getSubject().getName().toString()
				+ " taught by " + teacherSubject.get().getTeacher().getName() + " asigned to student group "
				+ studentGroup.get().getYear() + "-" + studentGroup.get().getYearIndex() + ".", HttpStatus.OK);
	}

}
