package com.atg.atgtools.mail;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.beans.factory.annotation.Autowired;
/*import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;*/
import org.springframework.stereotype.Service;

import com.sun.mail.smtp.SMTPTransport;


public class EmailSenderResource {


	
	//private static final String SMTP_SERVER = "smtp.office365.com";
	private static final String SMTP_SERVER = "mailhost.gha.kfplc.com";
	private static final String SMTP_PORT = "25";
    private static final String USERNAME = "karanamshyam.sunder@kingfisher.com";
    private static final String PASSWORD = "Feb@2021";

    private static final String EMAIL_FROM = "AUTOMATION@PRE-PRODBOT";
    private static final String EMAIL_TO = "karanamshyam.sunder@kingfisher.com";
    private static final String EMAIL_TO_CC = "";

    private static final String EMAIL_SUBJECT = "Test Send Email via SMTP ";
    private static final String EMAIL_TEXT = "Hello Java Mail \n ABC123";

	/*
	 * public void sendSimpleMessage(final Mail mail){
	 * 
	 * 
	 * SimpleMailMessage message = new SimpleMailMessage();
	 * message.setSubject(mail.getSubject()); message.setText(mail.getContent());
	 * message.setTo(mail.getTo()); message.setFrom(mail.getFrom());
	 * 
	 * emailSender.send(message); }
	 */
    
	/*
	 * public void sendMimeMessage(final Mail mail) { MimeMessage message =
	 * emailSender.createMimeMessage();
	 * 
	 * // use the true flag to indicate you need a multipart message
	 * MimeMessageHelper helper; try { helper = new MimeMessageHelper(message,
	 * true); helper.setTo(mail.getTo()); helper.setSubject(mail.getSubject());
	 * helper.setFrom(mail.getFrom());
	 * 
	 * // use the true flag to indicate the text included is HTML
	 * helper.setText(mail.getContent(), true); emailSender.send(message); } catch
	 * (MessagingException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 * 
	 * }
	 */
    
    public void sendMimeMessageWOSpring(String to,String subject, String messageBody) {
    	Properties prop = System.getProperties();
    	 /*prop.put("mail.smtp.host", SMTP_SERVER);
    	 prop.put("mail.smtp.port", "587");*/
    	
    	prop.put("mail.smtp.host", SMTP_SERVER);
   	 	prop.put("mail.smtp.port", SMTP_PORT);
        //prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.auth", "false");
        prop.put("mail.smtp.ehlo", "false");
        prop.put("mail.smtp.user", USERNAME);
        prop.put("mail.smtp.password", PASSWORD);
        //prop.put("mail.smtp.host", SMTP_SERVER);
        prop.put("mail.debug", "false");
        //prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop, null);
        Message msg = new MimeMessage(session);

        try {

            msg.setFrom(new InternetAddress(EMAIL_FROM));

            msg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to, false));

            msg.setSubject(subject);

            // text
            MimeBodyPart p1 = new MimeBodyPart();
            p1.setText(messageBody);

            // file
			/*
			 * MimeBodyPart p2 = new MimeBodyPart(); FileDataSource fds = new
			 * FileDataSource("path/example.txt"); p2.setDataHandler(new DataHandler(fds));
			 * p2.setFileName(fds.getName());
			 */

            Multipart mp = new MimeMultipart();
            mp.addBodyPart(p1);
            //mp.addBodyPart(p2);

            msg.setContent(mp);

            
			SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
			
			// connect
            t.connect();
			
			// send
            t.sendMessage(msg, msg.getAllRecipients());
			
            System.out.println("Response: " + t.getLastServerResponse());

            t.close();

        } catch (MessagingException e) {
        	System.out.println("Error sending mail. ");
        	e.printStackTrace();
        }
    
    }
    
    public static void main(String[] args) {
    	EmailSenderResource ems= new EmailSenderResource();
    	ems.sendMimeMessageWOSpring(EMAIL_TO,EMAIL_SUBJECT,EMAIL_TEXT);
	}
    	
}
