package tvseriesfollower;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TorrentChecker {
	private final DatabaseConnector databaseConnector = new DatabaseConnector();
	private final int seedLimit = 1000;
    
    public void checkNewStuff() {
    	StringHelper stringHelper = new StringHelper();
		ArrayList<Serie> allSeries = databaseConnector.getAllSeries();
		
		ArrayList<Serie> newEpisodeList = new ArrayList<Serie>();
		for (int i = 0; i < allSeries.size(); i++) {
			newEpisodeList.add(new Serie(allSeries.get(i).getName(), allSeries.get(i).getLatestSeason(), allSeries.get(i).getLatestEpisode() + 1, allSeries.get(i).getSubtitles()));
		}
		newEpisodeList = stringHelper.stringify(newEpisodeList);
		newStuff(newEpisodeList);
		
		ArrayList<Serie> newSeasonList = new ArrayList<Serie>();
		for (int i = 0; i < allSeries.size(); i++) {
			newSeasonList.add(new Serie(allSeries.get(i).getName(), allSeries.get(i).getLatestSeason()+1, 1, allSeries.get(i).getSubtitles()));
		}
		newSeasonList = stringHelper.stringify(newSeasonList);
		newStuff(newSeasonList);
	}
	
	private void newStuff(ArrayList<Serie> newStuffList) {
		String domain = "https://kat.cr";
		for (int i = 0; i < newStuffList.size(); i++) {
			ArrayList<Torrent> torrents = new ArrayList<Torrent>();
			WebBrowser webBrowser = new WebBrowser();
			String pageSource =  webBrowser.getTorrentPageSource(domain, newStuffList.get(i));
	    	if (pageSource != null) {
				String regex = "<a class=\"icon16\" href=\"(.*?)\" title=\"Verified Torrent\">.*?'name': '(.*?)',.*?'magnet': '(.*?)' .*?href=\"/user/(.*?)/\">.*?<td class=\"green center\">(.*?)</td>";
		    	Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
	    		Matcher m = pattern.matcher(pageSource);
				while (m.find()) {
					Torrent torrent = new Torrent();
					torrent.setUrl(domain + m.group(1).trim());
					torrent.setName(m.group(2).trim().replaceAll("(%20|,|\\s|\\.)", "").toLowerCase());
					torrent.setMagnet(m.group(3).trim());
					torrent.setUploader(m.group(4).trim());
					torrent.setSeeds((Integer.parseInt(m.group(5).trim())));
					torrents.add(torrent);
				}
				checkResults(torrents, newStuffList.get(i));
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
	}

	private void checkResults(ArrayList<Torrent> torrents, Serie serie) {
		for (int i = 0; i < torrents.size(); i++) {
			if (torrents.get(i).getName().contains(serie.getName().replace(" ", "").toLowerCase()) && 
					torrents.get(i).getName().contains(serie.getStringifiedSeasonAndEpisode()) &&
					(torrents.get(i).getName().contains("720p") || torrents.get(i).getName().contains("1080p")) && 
					torrents.get(i).getSeeds() >= seedLimit) {
				ArrayList<String> serieFollowers = databaseConnector.getFollowersforSerie(serie.getName());
				databaseConnector.setEpisode(serie);
				EmailerService emailerService = new EmailerService();
				emailerService.sendMassMail(serie, torrents.get(i), serieFollowers);
				break;
			}
		}		
	}
	
}