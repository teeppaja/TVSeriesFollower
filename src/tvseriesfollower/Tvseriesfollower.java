package tvseriesfollower;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.json.JSONObject;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class Tvseriesfollower {

    public static void main(String[] args) throws IOException, AddressException, MessagingException, InterruptedException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
    	Thread();
    }

	private static void Thread() throws AddressException, MessagingException, InterruptedException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, FailingHttpStatusCodeException, MalformedURLException, IOException {
		int errors = 0;
		Date lasterrordate = new Date();
		String generate_URL;
		ArrayList<String> magnets = new ArrayList<String>();
		String inputLine;
		String all="";


		while (true) {
			//EZTV
			try {
				generate_URL = "https://eztv.ch/sort/50/";
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
				if (errors==100) {
					long difference = TimeUnit.MILLISECONDS.toHours(new Date().getTime() - lasterrordate.getTime());
					Email.Error(errors, lasterrordate, difference);
					lasterrordate = new Date();
					errors=0;
				}
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
			
			//STRIKE, looking for new episode
			ArrayList<Series> newEpisode = Handler.getNewEpisodeForSeries();
			
			for (int i = 0; i < newEpisode.size(); i++) {
				ArrayList<JSONObject> jsonObjects = new ArrayList<JSONObject>();
				String sSeason = Integer.toString(newEpisode.get(i).getLatestSeason());
				String sEpisode = Integer.toString(newEpisode.get(i).getLatestEpisode());
				if (newEpisode.get(i).getLatestSeason()<10) {
					sSeason = "0"+newEpisode.get(i).getLatestSeason();
				}
				if (newEpisode.get(i).getLatestEpisode()<10) {
					sEpisode = "0"+newEpisode.get(i).getLatestEpisode();
				}
				String url = "https://getstrike.net/api/v2/torrents/search/?phrase=" + newEpisode.get(i).getName() + " S" + sSeason + "E" + sEpisode;
				WebClient webClient = new WebClient();
			    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
			    webClient.getOptions().setPrintContentOnFailingStatusCode(false);
			    int status = webClient.getPage(url).getWebResponse().getStatusCode();
			    if (status>=200 && status<=299) {
			    	Page page = webClient.getPage(url);
			    	String pageSource = getPageSource(page);
			    	pattern = Pattern.compile("\\{\"torrent_hash(.*?)\"\\}");
					matcher = pattern.matcher(pageSource);
					while (matcher.find()) {
						JSONObject jsonObject = new JSONObject(matcher.group(0));
						jsonObjects.add(jsonObject);
					}
				}
			    webClient.closeAllWindows();
			    if (jsonObjects.size()!=0) {
				    Handler.checkStrike(jsonObjects, newEpisode.get(i));
				    jsonObjects.clear();
				}
			    TimeUnit.SECONDS.sleep(1);
			}
			
			//STRIKE, looking for new season
			ArrayList<Series> newSeason = Handler.getNewSeasonForSeries();
			
			for (int i = 0; i < newSeason.size(); i++) {
				ArrayList<JSONObject> jsonObjects = new ArrayList<JSONObject>();
				String sSeason = Integer.toString(newSeason.get(i).getLatestSeason());
				String sEpisode = Integer.toString(newSeason.get(i).getLatestEpisode());
				if (newSeason.get(i).getLatestSeason()<10) {
					sSeason = "0"+newSeason.get(i).getLatestSeason();
				}
				if (newSeason.get(i).getLatestEpisode()<10) {
					sEpisode = "0"+newSeason.get(i).getLatestEpisode();
				}
				String url = "https://getstrike.net/api/v2/torrents/search/?phrase=" + newSeason.get(i).getName() + " S" + sSeason + "E" + sEpisode;
				WebClient webClient = new WebClient();
			    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
			    webClient.getOptions().setPrintContentOnFailingStatusCode(false);
			    int status = webClient.getPage(url).getWebResponse().getStatusCode();
			    if (status>=200 && status<=299) {
			    	Page page = webClient.getPage(url);
			    	String pageSource = getPageSource(page);
			    	pattern = Pattern.compile("\\{\"torrent_hash(.*?)\"\\}");
					matcher = pattern.matcher(pageSource);
					while (matcher.find()) {
						JSONObject jsonObject = new JSONObject(matcher.group(0));
						jsonObjects.add(jsonObject);
					}
				}
			    webClient.closeAllWindows();
			    if (jsonObjects.size()!=0) {
				    Handler.checkStrike(jsonObjects, newSeason.get(i));
				    jsonObjects.clear();
				}
			    TimeUnit.SECONDS.sleep(1);
			}

			TimeUnit.MINUTES.sleep(45);
		}
	}
	
	private static String getPageSource(Page page) {
		if(page instanceof HtmlPage) {
			return ((HtmlPage)page).asXml();
		} else if(page instanceof JavaScriptPage) {
			return ((JavaScriptPage)page).getContent();
		} else if(page instanceof TextPage) {
			return ((TextPage)page).getContent();
		} else {
			return ((UnexpectedPage)page).getWebResponse().getContentAsString();
		}
	}
}
