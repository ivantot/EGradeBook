package Brains2021.electronic.gradeBook.controllers;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.entites.users.TeacherEntity;
import Brains2021.electronic.gradeBook.repositories.TeacherRepository;
import Brains2021.electronic.gradeBook.security.Views;
import Brains2021.electronic.gradeBook.utils.RESTError;

@RestController
@RequestMapping(path = "/api/v1/teachers")
public class TeacherController {

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TeacherRepository teacherRepo;

	/*********************************************************************************************************
	 * PUT endpoint for administrator looking to change teachers weekly hours capacity. -- postman code 023 --
	 * 
	 * @param id
	 * @return if ok set new hours
	 *********************************************************************************************************/
	@Secured({ "ROLE_ADMIN", "ROLE_HEADMASTER" })
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, path = "/changeCapacity/{username}")
	public ResponseEntity<?> changeTeacherCapacity(@PathVariable String username, @RequestParam Integer newHours) {

		logger.info("**CHANGE TEACHER HOUR CAPACITY** Access to the endpoint successful.");

		logger.info("**CHANGE TEACHER HOUR CAPACITY** Attempt to find active teacher in database.");
		// initial check for existance in db
		Optional<TeacherEntity> ogTeacher = teacherRepo.findByUsername(username);
		if (ogTeacher.isEmpty() || ogTeacher.get().getDeleted() == 1) {
			logger.warn("**CHANGE TEACHER HOUR CAPACITY** Teacher not in database or deleted.");
			return new ResponseEntity<RESTError>(
					new RESTError(1030,
							"Username not found in database or is deleted, please provide a valid username."),
					HttpStatus.NOT_FOUND);
		}
		logger.info("**CHANGE TEACHER HOUR CAPACITY** Attempt successful.");

		logger.info("**CHANGE TEACHER HOUR CAPACITY** Assigning new hours and saving the teacher.");
		// change hours and save
		Integer oldHours = ogTeacher.get().getWeeklyHourCapacity();
		ogTeacher.get().setWeeklyHourCapacity(newHours);
		teacherRepo.save(ogTeacher.get());
		logger.info("**CHANGE TEACHER HOUR CAPACITY** Attempt successful, all done.");
		return new ResponseEntity<String>("Teacher " + ogTeacher.get().getName() + " " + ogTeacher.get().getSurname()
				+ " had weekly hours capacity changed from " + oldHours + " to "
				+ ogTeacher.get().getWeeklyHourCapacity() + ".", HttpStatus.OK);
	}

}
