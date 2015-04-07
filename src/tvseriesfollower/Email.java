package tvseriesfollower;

//Otettu netistä ja muokattu omaan käyttöön sopivaksi

import com.sun.mail.smtp.SMTPTransport;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Email {
	final private static String username = "TVSeriesFollower";
	final private static String password = "x";
	final private static String eol = System.getProperty("line.separator");
    private Email() {
    }
    
    /**
     * 
     * @param recipientEmail TO recipient
     * @param title title of the message
     * @param message message to be sent
     * @throws AddressException if the email address parse failed
     * @throws MessagingException if the connection is dead or not in the connected state or if the message is not a MimeMessage
     */
    
	public static void massMail(ArrayList<String> followers, String emailTitle, String emailMessage) throws AddressException, MessagingException {
		for (int z = 0; z < followers.size(); z++) {
			Email.Send(followers.get(z), emailTitle, emailMessage);
		}
	}
	
	public static void UnkownCrash(Throwable e) throws AddressException, MessagingException {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
    	final String recipientEmail = "t.s.partanen@gmail.com";
    	final String title = "TVSeriesFollower has crashed";
    	final String message = "TVSeriesFollower has encountered an unsuspected crash. Stack trace: " + sw.toString();
        Email.Send(recipientEmail, title, message);
	}
    
    public static void Error(int errors, Date lasterrordate, long difference) throws AddressException, MessagingException {
    	final String recipientEmail = "t.s.partanen@gmail.com";
    	final String title = "TVSeriesFollower has encountered too many errors";
    	final String message = "TVSeriesFollower has encountered " + errors + " errors." + eol + "Last time you received this email: " + lasterrordate + "," + eol
    			+ "which was " + difference + " hours ago.";
        Email.Send(recipientEmail, title, message);
    }

    /**
     * Send email using GMail SMTP server.
     *
     * @param username GMail username
     * @param password GMail password
     * @param recipientEmail TO recipient
     * @param ccEmail CC recipient. Can be empty if there is no CC recipient
     * @param title title of the message
     * @param message message to be sent
     * @throws AddressException if the email address parse failed
     * @throws MessagingException if the connection is dead or not in the connected state or if the message is not a MimeMessage
     */
    public static void Send(String recipientEmail, String title, String message) throws AddressException, MessagingException {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        // Get a Properties object
        Properties props = System.getProperties();
        props.setProperty("mail.smtps.host", "smtp.gmail.com");
        props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        props.setProperty("mail.smtps.auth", "true");

        /*
        If set to false, the QUIT command is sent and the connection is immediately closed. If set 
        to true (the default), causes the transport to wait for the response to the QUIT command.

        ref :   http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/package-summary.html
                http://forum.java.sun.com/thread.jspa?threadID=5205249
                smtpsend.java - demo program from javamail
        */
        props.put("mail.smtps.quitwait", "false");

        Session session = Session.getInstance(props, null);

        // -- Create a new message --
        final MimeMessage msg = new MimeMessage(session);

        // -- Set the FROM and TO fields --
        msg.setFrom(new InternetAddress(username + "<" + username + "@gmail.com>"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail, false));

        msg.setSubject(title);
        msg.setContent(message, "text/html");
        msg.setSentDate(new Date());

        SMTPTransport t = (SMTPTransport)session.getTransport("smtps");

        t.connect("smtp.gmail.com", username, password);
        t.sendMessage(msg, msg.getAllRecipients());      
        t.close();
    }

}