package tvseriesfollower;

public class Serie {
	String name;
	int latestSeason;
	int latestEpisode;
	String subtitles;
	String stringifiedSeasonAndEpisode;
	
	public Serie() {
		super();
	}

	public Serie(String name, int latestSeason, int latestEpisode) {
		super();
		this.name = name;
		this.latestSeason = latestSeason;
		this.latestEpisode = latestEpisode;
	}
	
	public Serie(int latestSeason, int latestEpisode) {
		super();
		this.name = null;
		this.latestSeason = latestSeason;
		this.latestEpisode = latestEpisode;
	}

	public Serie(String name, int latestSeason, int latestEpisode,
			String subtitles) {
		super();
		this.name = name;
		this.latestSeason = latestSeason;
		this.latestEpisode = latestEpisode;
		this.subtitles = subtitles;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getStringifiedSeasonAndEpisode() {
		return stringifiedSeasonAndEpisode;
	}

	public void setStringifiedSeasonAndEpisode(String stringifiedSeasonAndEpisode) {
		this.stringifiedSeasonAndEpisode = stringifiedSeasonAndEpisode;
	}

	public int getLatestSeason() {
		return latestSeason;
	}

	public void setLatestSeason(int latestSeason) {
		this.latestSeason = latestSeason;
	}

	public int getLatestEpisode() {
		return latestEpisode;
	}

	public void setLatestEpisode(int latestEpisode) {
		this.latestEpisode = latestEpisode;
	}

	public String getSubtitles() {
		return subtitles;
	}

	public void setSubtitles(String subtitles) {
		this.subtitles = subtitles;
	}

	@Override
	public String toString() {
		return "Serie [name = " + name + ", latestSeason = " + latestSeason
				+ ", latestEpisode = " + latestEpisode + ", subtitles = "
				+ subtitles + ", stringifiedSeasonAndEpisode = " + stringifiedSeasonAndEpisode + "]";
	}

}
