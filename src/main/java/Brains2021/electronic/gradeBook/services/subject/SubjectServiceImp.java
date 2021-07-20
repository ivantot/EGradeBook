package Brains2021.electronic.gradeBook.services.subject;

import org.springframework.stereotype.Service;

import Brains2021.electronic.gradeBook.utils.enums.ESubjectName;

@Service
public class SubjectServiceImp implements SubjectService {

	/**
	 * 
	 * check if ESubject enum contains the provided subject 
	 * 
	 */
	@Override
	public Boolean isSubjectInEnum(String subject) {
		ESubjectName[] allSubjects = ESubjectName.values();
		for (ESubjectName eSubjectName : allSubjects) {
			//do comparison
			if (subject.equals(eSubjectName.toString())) {
				return true;
			}
		}
		return false;
	}
}
