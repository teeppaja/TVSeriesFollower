package tvseriesfollower;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Tvseriesfollower {
	private static int server = 0;

    public static void main(String[] args) throws AddressException, MessagingException {
    	try {
    		thread();
		} catch (Throwable e) {
			e.printStackTrace();
			Email.unknownCrash(e);
			System.exit(0);
		}
    	
    }

	private static void thread() throws AddressException, MessagingException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, InterruptedException, IOException {
		while (true) {
						
			//TPB, looking for new episode
			ArrayList<Series> newEpisode = Handler.getNewEpisodeForSeries();
			newTPBStuff(newEpisode);
			
			//TPB, looking for new season
			ArrayList<Series> newSeason = Handler.getNewSeasonForSeries();
			newTPBStuff(newSeason);

			TimeUnit.MINUTES.sleep(45);
		}
	}
	
	private static void newTPBStuff(ArrayList<Series> newStuff) throws IOException, AddressException, MessagingException, InterruptedException {
		for (int i = 0; i < newStuff.size(); i++) {
			String url = null;
			String domain = null;
			ArrayList<Torrents> torrents = new ArrayList<Torrents>();
			String sSeason = Integer.toString(newStuff.get(i).getLatestSeason());
			String sEpisode = Integer.toString(newStuff.get(i).getLatestEpisode());
			if (newStuff.get(i).getLatestSeason()<10) {
				sSeason = "0"+newStuff.get(i).getLatestSeason();
			}
			if (newStuff.get(i).getLatestEpisode()<10) {
				sEpisode = "0"+newStuff.get(i).getLatestEpisode();
			}
			switch (server) {
			case 0:
				domain = "https://pirateproxy.sx"; 
				url = "https://pirateproxy.sx/search/" + newStuff.get(i).getName().toLowerCase() + "%20s" + sSeason + "e" + sEpisode + "/0/99/0";
				//https://pirateproxy.sx/search/suits%20s05e04/0/99/0
				break;
			case 1:
				domain = "http://tpb.proxyduck.com";
				url = "http://tpb.proxyduck.com/search.php?q=" + newStuff.get(i).getName().toLowerCase() + "+s" + sSeason + "e" + sEpisode + "&category=0&page=0&orderby=99";
				//http://tpb.proxyduck.com/search.php?q=suits+s05e04&category=0&page=0&orderby=99
				break;
			case 2:
				domain = "http://thepiratebay.casa";
				url = "http://thepiratebay.casa/search/" + newStuff.get(i).getName().toLowerCase() + "%20s" + sSeason + "e" + sEpisode + "/0/7/";
				//http://thepiratebay.casa/search/suits%20s05e04/0/7/
				break;
			default:
				break;
			}
			WebClient webClient = new WebClient();
			webClient.setRefreshHandler(new ThreadedRefreshHandler());
			java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
		    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		    webClient.getOptions().setPrintContentOnFailingStatusCode(false);
		    webClient.getOptions().setThrowExceptionOnScriptError(false);
		    webClient.getOptions().setCssEnabled(false);
		    webClient.getOptions().setJavaScriptEnabled(false);
		    webClient.getOptions().setGeolocationEnabled(false);
		    webClient.getOptions().setDoNotTrackEnabled(true);
		    webClient.getOptions().setPopupBlockerEnabled(true);
		    webClient.getOptions().setRedirectEnabled(false);
		    
			try {
				webClient.getPage(url);
			} catch (FailingHttpStatusCodeException | MalformedURLException e) {
				webClient.close();
				throw e;
			}
		    int status = webClient.getPage(url).getWebResponse().getStatusCode();
		    if (status>=200 && status<=299) {
		    	Page page = webClient.getPage(url);
		    	String pageSource = getPageSource(page);
		    	String regex = "<td>.*?<a href=\"(.*?)\" class=\"detLink\" title=\"Details for (.*?)\">.*?(magnet:.*?)\" title=\".*?<td align=\"right\">(.*?)</td>";
		    	Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		    	Matcher m = pattern.matcher(pageSource);
				while (m.find()) {
					try {
						Torrents torrent = new Torrents();
						torrent.setUrl(domain + m.group(1).trim());
						torrent.setName(m.group(2).trim());
						torrent.setMagnet(m.group(3).trim());
						torrent.setSeeds((Integer.parseInt(m.group(4).trim())));
						torrents.add(torrent);
					} catch (Exception e) {
						webClient.close();
						throw e;
					}
				}
				webClient.close();
				Handler.checkTPB(torrents, newStuff.get(i));
				TimeUnit.SECONDS.sleep(1);
			} else {
				webClient.close();
				if (server == 2) {
					server = 0;
				} else {
					server++;
				}
				i--;
			}
		}
	}

	/* private static void newStrikeStuff(ArrayList<Series> newStuff) throws InterruptedException {
		for (int i = 0; i < newStuff.size(); i++) {
			ArrayList<JSONObject> jsonObjects = new ArrayList<JSONObject>();
			String sSeason = Integer.toString(newStuff.get(i).getLatestSeason());
			String sEpisode = Integer.toString(newStuff.get(i).getLatestEpisode());
			if (newStuff.get(i).getLatestSeason()<10) {
				sSeason = "0"+newStuff.get(i).getLatestSeason();
			}
			if (newStuff.get(i).getLatestEpisode()<10) {
				sEpisode = "0"+newStuff.get(i).getLatestEpisode();
			}
			try {
				String url = "https://getstrike.net/api/v2/torrents/search/?phrase=" + newStuff.get(i).getName() + " S" + sSeason + "E" + sEpisode;
				WebClient webClient = new WebClient();
				java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
			    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
			    webClient.getOptions().setPrintContentOnFailingStatusCode(false);
			    webClient.getOptions().setThrowExceptionOnScriptError(false);
			    webClient.getOptions().setCssEnabled(false);
				try {
					webClient.getPage(url);
				} catch (Exception e) {
					break;
				}
			    int status = webClient.getPage(url).getWebResponse().getStatusCode();
			    if (status>=200 && status<=299) {
			    	Page page = webClient.getPage(url);
			    	String pageSource = getPageSource(page);
			    	Pattern pattern = Pattern.compile("\\{\"torrent_hash(.*?)\"\\}");
					Matcher matcher = pattern.matcher(pageSource);
					while (matcher.find()) {
						JSONObject jsonObject = new JSONObject(matcher.group(0));
						jsonObjects.add(jsonObject);
					}
				}
			    webClient.closeAllWindows();
			    if (jsonObjects.size()!=0) {
				    Handler.checkStrike(jsonObjects, newStuff.get(i));
				    jsonObjects.clear();
				}
			} catch (Exception e) {
				break;
			}
			
		    TimeUnit.SECONDS.sleep(1);
		}
		
	} */

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