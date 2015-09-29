package tvseriesfollower;

public class Torrents {
	String url, name, magnet, uploader;
	int seeds;
	
	public Torrents() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Torrents(String url, String name, String magnet, String uploader, int seeds) {
		super();
		this.url = url;
		this.name = name;
		this.magnet = magnet;
		this.uploader = uploader;
		this.seeds = seeds;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMagnet() {
		return magnet;
	}

	public void setMagnet(String magnet) {
		this.magnet = magnet;
	}

	public String getUploader() {
		return uploader;
	}

	public void setUploader(String uploader) {
		this.uploader = uploader;
	}

	public int getSeeds() {
		return seeds;
	}

	public void setSeeds(int seeds) {
		this.seeds = seeds;
	}

	@Override
	public String toString() {
		return "Torrents [url=" + url + ", name=" + name + ", magnet=" + magnet
				+ ", uploader=" + uploader + ", seeds=" + seeds + "]";
	}
	
}
