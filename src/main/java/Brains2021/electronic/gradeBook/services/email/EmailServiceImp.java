package Brains2021.electronic.gradeBook.services.email;

import java.time.format.DateTimeFormatter;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import Brains2021.electronic.gradeBook.dtos.EmailObjectDTO;

@Service
public class EmailServiceImp implements EmailService {

	@Autowired
	private JavaMailSender mailSender;

	@Override
	public void sendTemplateMessage(EmailObjectDTO object) throws Exception {

		MimeMessage mail = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mail, true);

		helper.setTo(object.getTo());
		helper.setSubject(object.getSubject());
		helper.setCc(object.getCc());

		String text = "<table style='width:1000px; margin-left: auto; \r\n"
				+ "  margin-right: auto; border:2px dotted black;'> <tbody> <tr>\r\n"
				+ "				<td style='border-style: hidden; padding-right: 40px; padding-left: 20px;font-family:Helvetica, sans-serif; color:Gray; font-size: 14px;' colspan='2'><strong>Student</strong></td>\r\n"
				+ "				<td style='border-style: hidden; padding-right: 40px; font-family:Helvetica, sans-serif; color:Gray; font-size: 14px;'><strong>Subject</strong></td>\r\n"
				+ "				<td style='border-style: hidden; padding-right: 40px;font-family:Helvetica, sans-serif; color:Gray; font-size: 14px;'><strong>Assignment type</strong></td>\r\n"
				+ "				<td style='border-style: hidden; padding-right: 40px;font-family:Helvetica, sans-serif; color:Gray; font-size: 14px;'><strong>Grade</strong></td>\r\n"
				+ "				<td style='border-style: hidden; padding-right: 20px;font-family:Helvetica, sans-serif; color:Gray; font-size: 14px;'><strong>Grade assigned on</strong></td> </tr> <tr>\r\n"
				+ "				<td style='border-style: hidden; padding-left:20px;font-family:Helvetica, sans-serif; color:Black; font-size: 14px;'>%s</td> <td style='border-style: hidden; padding-right: 40px;font-family:Helvetica, sans-serif; color:Black; font-size: 14px;'>%s</td>\r\n"
				+ "				<td style='border-style: hidden; padding-right: 40px;font-family:Helvetica, sans-serif; color:Black; font-size: 14px;'><i>%s</i></td> <td style='border-style: hidden; padding-right: 40px;font-family:Helvetica, sans-serif; color:Black; font-size: 14px;'><i>%s</i></td>\r\n"
				+ "				<td style='border-style: hidden; padding-right: 40px;font-family:Helvetica, sans-serif; color:Black; font-size: 14px;'>%s</td> <td style='border-style: hidden; padding-right: 40px;font-family:Helvetica, sans-serif; color:Black; font-size: 14px;'>%s</td> </tr> </tbody></table>";

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

		text = String.format(text, object.getStudentName(), object.getStudentLastName(), object.getGradedSubject(),
				object.getAssignment(), object.getGrade(), object.getDate().format(formatter));

		String text1 = "<br/>Teacher responsible for creating the assignemnt and posting the grade: "
				+ object.getTeacherName() + " " + object.getTeacherLastName() + ".";

		String text2 = "<br/>Contact the school in case of any doubts.<br/><br/> Your Rotary school IT department.";
		helper.setText(text + text1 + text2, true);
		mailSender.send(mail);
	}

}
