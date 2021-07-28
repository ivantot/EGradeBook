package Brains2021.electronic.gradeBook.services.email;

import java.time.format.DateTimeFormatter;

import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import Brains2021.electronic.gradeBook.dtos.EmailObjectDTO;

@Service
public class EmailServiceImp implements EmailService {

	@Autowired
	private JavaMailSender mailSender;

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	@Override
	public void sendTemplateMessage(EmailObjectDTO object) throws Exception {

		logger.info("##EMAIL SERVICE## Accessed service for sending out parent emails.");

		MimeMessage mail = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mail, true);

		logger.info("##EMAIL SERVICE## Attempting to populatie template.");
		logger.info("##EMAIL SERVICE## Determining if email will be sent to more than one parent.");

		helper.setTo(object.getTo());
		helper.setSubject(object.getSubject());
		if (object.getCc() != null) {
			helper.setCc(object.getCc());
			logger.info("##EMAIL SERVICE## Two parents should recieve emails.");
		}
		logger.info("##EMAIL SERVICE## One parent will recieve an email.");

		String text = "<table style='border:2px dotted black;'> <tbody> <tr>\r\n"
				+ "				<td style='border-style: hidden; padding-right: 15px; padding-left: 10px;font-family:Helvetica, sans-serif; color:Gray; font-size: 12px;' colspan='2'><strong>Student</strong></td>\r\n"
				+ "				<td style='border-style: hidden; padding-right: 15px; font-family:Helvetica, sans-serif; color:Gray; font-size: 12px;'><strong>Subject</strong></td>\r\n"
				+ "				<td style='border-style: hidden; padding-right: 15px;font-family:Helvetica, sans-serif; color:Gray; font-size: 12px;'><strong>Assignment type</strong></td>\r\n"
				+ "				<td style='border-style: hidden; padding-right: 15px;font-family:Helvetica, sans-serif; color:Gray; font-size: 12px;'><strong>Grade</strong></td>\r\n"
				+ "				<td style='border-style: hidden; padding-right: 10px;font-family:Helvetica, sans-serif; color:Gray; font-size: 12px;'><strong>Grade assigned on</strong></td> </tr> <tr>\r\n"
				+ "				<td style='border-style: hidden; padding-left:10px;font-family:Helvetica, sans-serif; color:Black; font-size: 12px;'>%s</td> <td style='border-style: hidden; padding-right: 15px;font-family:Helvetica, sans-serif; color:Black; font-size: 12px;'>%s</td>\r\n"
				+ "				<td style='border-style: hidden; padding-right: 15px;font-family:Helvetica, sans-serif; color:Black; font-size: 12px;'><i>%s</i></td> <td style='border-style: hidden; padding-right: 15px;font-family:Helvetica, sans-serif; color:Black; font-size: 12px;'><i>%s</i></td>\r\n"
				+ "				<td style='border-style: hidden; padding-right: 15px;font-family:Helvetica, sans-serif; color:Black; font-size: 12px;'>%s</td> <td style='border-style: hidden; padding-right: 15px;font-family:Helvetica, sans-serif; color:Black; font-size: 12px;'>%s</td> </tr> </tbody></table>";

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

		text = String.format(text, object.getStudentName(), object.getStudentLastName(), object.getGradedSubject(),
				object.getAssignment(), object.getGrade(), object.getDate().format(formatter));

		String text1 = "<br/>Teacher responsible for creating the assignemnt and posting the grade: "
				+ object.getTeacherName() + " " + object.getTeacherLastName() + ".";

		String text2 = "<br/>Contact the school in case of any doubts.<br/><br/> Your Rotary school IT department.";

		String text3 = "<br/>Description of the assignment: " + object.getDescription() + "<br/>";

		String text4 = "Dear Parent, your kid just got graded.<br/><br/>";

		String text5 = "Grade has been overriden by higher authority. New grade assigned is <strong>"
				+ object.getOverridenGrade() + "</strong>."
				+ "<br/>Original grade available below. If in doubt contact the school.<br/><br/>";

		logger.info("##EMAIL SERVICE## Determining if grade is new or overriden.");
		if (!(object.getOverridenGrade() == null)) {
			logger.info("##EMAIL SERVICE## Overriden grade.");
			helper.setText(text5 + text + text3 + text1 + text2, true);
		} else {
			helper.setText(text4 + text + text3 + text1 + text2, true);
			logger.info("##EMAIL SERVICE## New grade.");
		}
		logger.info("##EMAIL SERVICE## Exiting service.");

		mailSender.send(mail);
	}

}
