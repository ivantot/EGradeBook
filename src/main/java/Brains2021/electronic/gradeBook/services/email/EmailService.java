package Brains2021.electronic.gradeBook.services.email;

import Brains2021.electronic.gradeBook.dtos.EmailObjectDTO;

public interface EmailService {

	public void sendTemplateMessage(EmailObjectDTO object) throws Exception;

}
