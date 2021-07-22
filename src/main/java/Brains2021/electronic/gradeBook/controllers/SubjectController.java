package Brains2021.electronic.gradeBook.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import Brains2021.electronic.gradeBook.dtos.in.CreateSubjectDTO;
import Brains2021.electronic.gradeBook.repositories.SubjectRepository;
import Brains2021.electronic.gradeBook.security.Views;
import Brains2021.electronic.gradeBook.services.subject.SubjectService;
import Brains2021.electronic.gradeBook.utils.RESTError;
import Brains2021.electronic.gradeBook.utils.enums.ESubjectName;

@RestController
@RequestMapping(path = "/api/v1/subjects")
public class SubjectController {

	@Autowired
	private SubjectRepository subjectRepo;

	@Autowired
	private SubjectService subjectService;

	/******************************************************************************************************
	 * POST endpoint for administrator looking to create new subject, meant to be accessed by IT specialist
	 * -- postman code adm005 --
	 * 
	 * @param subject
	 * @return if ok, new subject
	 ******************************************************************************************************/
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.POST, path = "/admin/newSubject")
	public ResponseEntity<?> postNewSubject(@Valid @RequestBody CreateSubjectDTO subject) {

		// check if subject name allowed
		if (!subjectService.isSubjectInEnum(subject.getName())) {
			return new ResponseEntity<RESTError>(
					new RESTError(2000, "Subject name not allowed, check ESubjectName for details."),
					HttpStatus.BAD_REQUEST);
		}

		// check db for subject
		if (subjectRepo.findByName(ESubjectName.valueOf(subject.getName())).isPresent()) {
			return new ResponseEntity<RESTError>(new RESTError(2001, "Subject already in the database."),
					HttpStatus.BAD_REQUEST);
		}

		return subjectService.createdSubjectDTOtranslation(subjectService.createSubjectDTOtranslation(subject));

	}
}
