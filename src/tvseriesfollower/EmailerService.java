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

public class EmailerService {
	// TODO: load credentials from a file
	final private String username = "TVSeriesFollower";
	final private String password = "x"; //Ofc not a real password. The program will crash if you don't change this to the real one.
	final private String eol = System.getProperty("line.separator");
    
    /**
     * 
     * @param recipientEmail TO recipient
     * @param title title of the message
     * @param message message to be sent
     * @throws AddressException if the email address parse failed
     * @throws MessagingException if the connection is dead or not in the connected state or if the message is not a MimeMessage
     */
    
	public void sendMassMail(Serie serie, Torrent torrent, ArrayList<String> followers) {
		String emailTitle = "New episode of " + serie.getName() + " released";
		String emailMessage = "Hello!<br>New episode " + "of " + serie.getName() + " is out.<br>"
				+ "You can download the episode by clicking the following link or by copying the magnet URL and adding it manyally to your torrent client:<br>"
				+ "<a href=\"" + torrent.getMagnet() + "\">" + torrent.getMagnet() + "</a><br><br>Link to the torrent page: " 
				+ "<a href=\"" + torrent.getUrl() + "\">" + torrent.getUrl() + "</a><br><br>"
				+ "You can find subtitles for this episode from here once they are released:<br>" 
				+ "<a href=\"" + serie.getSubtitles() + "\">" + serie.getSubtitles() + "</a><br><br><br><i>-TVSeriesFollower</i>";
		for (int i = 0; i < followers.size(); i++) {
			try {
				sendEmail(followers.get(i), emailTitle, emailMessage);
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void unknownCrash(Throwable e) throws AddressException, MessagingException {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
    	final String recipientEmail = "t.s.partanen@gmail.com";
    	final String title = "TVSeriesFollower has crashed";
    	final String message = "TVSeriesFollower has encountered an unsuspected crash. Stacktrace: " + sw;
        sendEmail(recipientEmail, title, message);
	}
	
	public void knownCrash(Throwable e) throws AddressException, MessagingException {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
    	final String recipientEmail = "t.s.partanen@gmail.com";
    	final String title = "Hälytys - Virhe";
    	final String message = "TVSeriesFollower encountered an error while processing a web page. It should still be running. Stacktrace:" + sw;
        sendEmail(recipientEmail, title, message);
	}
    
    public void error(int errors, Date lasterrordate, long difference) throws AddressException, MessagingException {
    	final String recipientEmail = "t.s.partanen@gmail.com";
    	final String title = "TVSeriesFollower has encountered too many errors";
    	final String message = "TVSeriesFollower has encountered " + errors + " errors." + eol + "Last time you received this email: " + lasterrordate + "," + eol
    			+ "which was " + difference + " hours ago.";
        sendEmail(recipientEmail, title, message);
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
    private void sendEmail(String recipientEmail, String title, String message) throws AddressException, MessagingException {
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