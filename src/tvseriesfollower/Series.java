package tvseriesfollower;

public class Series {
	String name;
	int latestSeason;
	int latestEpisode;
	String subtitles;
	String sSeaEp;
	
	public Series() {
		super();
	}

	public Series(String name, int latestSeason, int latestEpisode) {
		super();
		this.name = name;
		this.latestSeason = latestSeason;
		this.latestEpisode = latestEpisode;
	}
	
	public Series(int latestSeason, int latestEpisode) {
		super();
		this.name = null;
		this.latestSeason = latestSeason;
		this.latestEpisode = latestEpisode;
	}

	public Series(String name, int latestSeason, int latestEpisode,
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
	
	public String getSSeaEp() {
		return sSeaEp;
	}

	public void setSSeaEp(String sSeaEp) {
		this.sSeaEp = sSeaEp;
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
		return "Series [name=" + name + ", latestSeason=" + latestSeason
				+ ", latestEpisode=" + latestEpisode + ", subtitles="
				+ subtitles + ", sSeaEp=" + sSeaEp + "]";
	}

}
