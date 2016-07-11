package tvseriesfollower;

import java.util.ArrayList;

public class StringHelper {

	public ArrayList<Serie> stringify(ArrayList<Serie> seriesList) {
		for (int i = 0; i < seriesList.size(); i++) {
			String sSeason = null;
			String sEpisode = null;
			if (seriesList.get(i).getLatestSeason()<10) {
				sSeason = "0"+seriesList.get(i).getLatestSeason();
			} else {
				sSeason = Integer.toString(seriesList.get(i).getLatestSeason());
			}
			if (seriesList.get(i).getLatestEpisode()<10) {
				sEpisode = "0"+seriesList.get(i).getLatestEpisode();
			} else {
				sEpisode = Integer.toString(seriesList.get(i).getLatestEpisode());
			}
			seriesList.get(i).setStringifiedSeasonAndEpisode("s" + sSeason + "e" + sEpisode);
		}
		return seriesList;		
	}

}
