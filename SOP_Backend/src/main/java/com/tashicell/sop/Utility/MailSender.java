package com.tashicell.sop.Utility;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Date;
import java.util.Properties;

public class MailSender {

	public static Boolean sendMail(String destinationEmail, String sentMailFrom, File attachmentFile,String messageBody, String subject) throws Exception {
			boolean isMailSent = false;
//			final String trayMessage;

        Resource resource = new ClassPathResource("/mailConfig/mailConfig.properties");
			Properties props = PropertiesLoaderUtils.loadProperties(resource);
			
			final String isMailSendRequired =  props.getProperty("mail.isRequiredToSend");
			
			if(isMailSendRequired.equalsIgnoreCase("0") || isMailSendRequired.isEmpty()){
				return isMailSent;
			}

			if(destinationEmail == null || destinationEmail.isEmpty()){
				new Thread(){
					public void run(){
						//String errorMsg = "Mail cannot be sent. There is no mail address given to sent mail";
					}
				}.start();
				return isMailSent;
			}

        final String username = props.getProperty("mail.username");
        final String password = props.getProperty("mail.password");

        sentMailFrom = props.getProperty("mail.fromAddress");
        String host = props.getProperty("mail.host");
        String port = props.getProperty("mail.port");
        String auth = props.getProperty("mail.auth");
        String encrypt = props.getProperty("mail.encryption");
        String mailer = props.getProperty("mail.mailer");
        String startLlsEnable = props.getProperty("mail.startLlsEnable");

        //smtp configuration
        String smtpHost = props.getProperty("mail.smtpHost");
        String smtpPort = props.getProperty("mail.smtpPort");
        String smtpAuth = props.getProperty("mail.smtpAuth");
        String smtpStartLlsEnable = props.getProperty("mail.smtpStartLlsEnable");

        Properties properties = new Properties();
        properties.put(mailer,mailer);
        properties.put(smtpHost,host);
        properties.put(smtpPort,port);
        properties.put(username,username);
        properties.put(password, password);
        properties.put(encrypt,encrypt);
        properties.put(smtpAuth,auth);
        properties.put(smtpStartLlsEnable,startLlsEnable);

			//creating session of current user.
			Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
			
			final Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(sentMailFrom));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinationEmail));
			message.setSubject(subject);
			message.setSentDate(new Date());
			
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(messageBody);
            messageBodyPart.setContent(messageBody,"text/html; charset=\"utf-8\"");
			
			Multipart multipart = new MimeMultipart();
			//set text part message
			multipart.addBodyPart(messageBodyPart);

        //adding attachmentFile
        if (attachmentFile != null){
            MimeBodyPart messageBodyPart1 = new MimeBodyPart();
            messageBodyPart1.attachFile(attachmentFile);
            multipart.addBodyPart(messageBodyPart1);
        }

       // send the complete message parts
        message.setContent(multipart);

//        trayMessage = "Mail is sent successfully to "+ destinationEmail ;

        new Thread(){
            public void run(){
                try{
                    Transport.send(message);
                } catch(MessagingException e){
                    e.printStackTrace();
                    //String errorMsg = "Mail cannot be sent. Check your net connection or configuration or "+
                            //"if destination email address is valid or not.";
                }
            }
        }.start();
        isMailSent = true;
        return isMailSent;
		}
}