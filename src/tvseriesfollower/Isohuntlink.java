package tvseriesfollower;

public class Isohuntlink {
	String name, url, linkName;
	int season, episode, seeds;
	boolean trusted;
	
	public Isohuntlink() {
		super();
	}
	
	public Isohuntlink(String name, String url, String linkName, int season,
			int episode, int seeds, boolean trusted) {
		super();
		this.name = name;
		this.url = url;
		this.linkName = linkName;
		this.season = season;
		this.episode = episode;
		this.seeds = seeds;
		this.trusted = trusted;
	}

	public Isohuntlink(String name, String url, String linkName, int season,
			int episode, int seeds) {
		super();
		this.name = name;
		this.url = url;
		this.linkName = linkName;
		this.season = season;
		this.episode = episode;
		this.seeds = seeds;
	}
	
	public Isohuntlink(String url, String linkName, int season,
			int episode, int seeds) {
		super();
		this.url = url;
		this.linkName = linkName;
		this.season = season;
		this.episode = episode;
		this.seeds = seeds;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLinkName() {
		return linkName;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	public int getEpisode() {
		return episode;
	}

	public void setEpisode(int episode) {
		this.episode = episode;
	}

	public int getSeeds() {
		return seeds;
	}

	public void setSeeds(int seeds) {
		this.seeds = seeds;
	}

	public boolean isTrusted() {
		return trusted;
	}

	public void setTrusted(boolean trusted) {
		this.trusted = trusted;
	}
	
	@Override
	public String toString() {
		return "Isohuntlink [name=" + name + ", url=" + url + ", linkName="
				+ linkName + ", season=" + season + ", episode=" + episode
				+ ", seeds=" + seeds + ", trusted=" + trusted + "]";
	}
}
