package tvseriesfollower;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;


public class Tvseriesfollower {

    public static void main(String[] args) throws IOException, AddressException, MessagingException, InterruptedException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
    	Thread();
    }

	private static void Thread() throws AddressException, MessagingException, InterruptedException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		int errors = 0;
		Date lasterrordate = new Date();
		String generate_URL = "https://eztv.ch/sort/50/";
		ArrayList<String> magnets = new ArrayList<String>();
		String inputLine;
		String all="";
		
		while (true) {
			try {
				URL data = new URL(generate_URL);
				HttpURLConnection con = (HttpURLConnection) data.openConnection();
				con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				while ((inputLine = in.readLine()) != null) {
					all=all + inputLine;
				}
				in.close();
				con.disconnect();
			} catch (Exception e) {
				errors = errors+1;
				if (errors==10) {
					long difference = TimeUnit.MILLISECONDS.toHours(new Date().getTime() - lasterrordate.getTime());
					Email.Error(errors, lasterrordate, difference);
					lasterrordate = new Date();
					errors=0;
				}
				//e.printStackTrace();
			}
			Pattern pattern = Pattern.compile("magnet:(.*?)\" class");
			Matcher matcher = pattern.matcher(all);
			while (matcher.find()) {
				magnets.add("magnet:" + matcher.group(1));
			}
			Handler.check(magnets);
			magnets.clear();
			all = "";
			inputLine = "";
			TimeUnit.MINUTES.sleep(30);
		}
	}
}