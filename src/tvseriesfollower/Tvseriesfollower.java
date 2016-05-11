package tvseriesfollower;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
				domain = "https://thepiratebay.immunicity.eu";
				url = domain + "/search/" + newStuff.get(i).getName().toLowerCase() + "%20s" + sSeason + "e" + sEpisode + "/0/99/0";
				//https://tpb.immunicity.info/search/suits%20s05e15/0/99/0
				break;
			case 1:
				domain = "http://tpb.proxyduck.com";
				url = domain + "/search.php?q=" + newStuff.get(i).getName().toLowerCase() + "+s" + sSeason + "e" + sEpisode + "&category=0&page=0&orderby=99";
				//http://tpb.proxyduck.com/search.php?q=suits+s05e15&category=0&page=0&orderby=99
				break;
			case 2:
				domain = "https://pirateproxy.pw"; 
				url = domain + "/search/" + newStuff.get(i).getName().toLowerCase() + "%20s" + sSeason + "e" + sEpisode + "/0/99/0";
				//https://pirateproxy.pw/search/suits%20s05e15/0/99/0
				break;
			default:
				break;
			}
			
			WebClient webClient = new WebClient();
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
			webClient.setRefreshHandler(new ThreadedRefreshHandler());
		    
			try {
				webClient.getPage(url);
			    int status = webClient.getPage(url).getWebResponse().getStatusCode();
			    if (status>=200 && status<=299) {
			    	String pageSource = getPageSource(webClient.getPage(url));
			    	webClient.close();
			    	String regex = "<td>.*?<a href=\"(.*?)\" class=\"detLink\" title=\"Details for (.*?)\">.*?(magnet:.*?)\" title=\".*?title=\"Browse (.*?)\">.*?<td align=\"right\">(.*?)</td>";
			    	Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
			    	Matcher m = pattern.matcher(pageSource);
					while (m.find()) {
						try {
							Torrents torrent = new Torrents();
							torrent.setUrl(domain + m.group(1).trim());
							torrent.setName(m.group(2).trim());
							torrent.setMagnet(m.group(3).trim());
							torrent.setUploader(m.group(4).trim());
							torrent.setSeeds((Integer.parseInt(m.group(5).trim())));
							torrents.add(torrent);
						} catch (Exception e) {
							webClient.close();
							throw e;
						}
					}
					Handler.checkTPB(torrents, newStuff.get(i));
					TimeUnit.SECONDS.sleep(1);
				} else {
					if (server == 2) {
						server = 0;
					} else {
						server++;
					}
					i--;
				}
			} catch (MalformedURLException e) {
				webClient.close();
				throw e;
			} catch (SocketTimeoutException e) {
				webClient.close();
				if (server == 2) {
					server = 0;
				} else {
					server++;
				}
				i--;
				continue;
			}
			catch (FailingHttpStatusCodeException | UnknownHostException e) {
				webClient.close();
				if (server == 2) {
					server = 0;
				} else {
					server++;
				}
				i--;
				Email.knownCrash(e);
				continue;
			}
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