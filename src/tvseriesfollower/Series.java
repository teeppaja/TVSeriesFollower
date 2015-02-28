package tvseriesfollower;

public class Series {
	String name;
	int latestSeason;
	int latestEpisode;
	
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	@Override
	public String toString() {
		return "Series [name=" + name + ", latestSeason=" + latestSeason
				+ ", latestEpisode=" + latestEpisode + "]";
	}

}
