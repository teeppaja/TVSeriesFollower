package tvseriesfollower;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Handler {
	private static String dbURL = "jdbc:derby:C:/Users/Teemu/MyDB";
    private static String eol = System.getProperty("line.separator");
    private static Connection conn = null;
    private static Statement statement = null;
    private static ArrayList<String> followers;
    private static String emailMessage;
    private static String emailTitle;
    private static String torUrl;
    private static String torName;
    private static String sSeeds;
    private static int torSeeds;
    private static String seriesEpisode;
    private static int nSeason;
    private static int nEpisode;
    private static boolean SxxExx;
    private static Pattern pattern;
    private static Matcher matcher;
    private static Series serie;

    /**
     * Checks given list for new episodes of wanted series
     * @param magnets list of magnet-links
     * @throws AddressException
     * @throws MessagingException
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
	public static void check(ArrayList<String> magnets) throws AddressException, 
	MessagingException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		ArrayList<Series> series = getSeries();
		try {
			for (int i = 0; i < magnets.size(); i++) {
				Series serie = (serieNumbers(magnets.get(i)));

				for (int j = 0; j < series.size(); j++) {
					if (isThisNew (magnets.get(i), series.get(j))==true) {
						followers = getFollowersforSeries(series.get(j).getName());
						serie.setName(series.get(j).getName());
						emailMessage = "Hello!<br>New episode (" + seriesEpisode + ") of " + series.get(j).getName() + " is out.<br>"
								+ "You can download the episode by clicking the following link or by copying the magnet URL and adding it manyally to your torrent client:<br>"
								+ "<a href=\"" + magnets.get(i) + "\">" + magnets.get(i) + "</a><br><br><br><i>-TVSeriesFollower</i>";
						setEpisode(serie);
						Email.massMail(followers, emailTitle, emailMessage);
					}
				}
			}
		} catch (Exception e) {
			Email.Send("t.s.partanen@gmail.com", "HÄLYTYS - eztv", "TVSeriesFollower kohtasi virheen käsitellessään eztv-dataa ja on sammutettu." + eol 
					+ "Alla virhekoodi:" + eol + e.toString());
			System.exit(0);
		}
	}
	
	/**
	 * Checks given urls for new episodes of wanted series
	 * @param urls list of substrings from html-code. One substring contains all information about one torrent
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public static void isoHuntCheck(ArrayList<String> urls) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, AddressException, MessagingException {
		ArrayList<Series> series = getSeries();
		ArrayList<Isohuntlink> isohuntlinks = new ArrayList<Isohuntlink>(); //List on neatly packed information
		ArrayList<Isohuntlink> enoughseeds = new ArrayList<Isohuntlink>(); //Same as isohuntlinks, but contains only torrents that have enough seeds
		
		for (int i = 0; i < urls.size(); i++) {
			
			try {
				emailMessage = null;
				emailTitle = null;
				torUrl = torrentUrl(urls.get(i)); //URL to single torrent file
				torName = torrentName(urls.get(i)); //Name given to a single torrent
				torSeeds = torrentSeeds(urls.get(i)); //Number of seeds for a single torrent
				Series serie = serieNumbers(torName); //Season and episode taken from torrent name. Note that Serie-object's parameter 'name' is null
				nSeason = serie.getLatestSeason(); //Season is put to global variable, other methods will need it
				nEpisode = serie.getLatestEpisode(); //Episode is put to global variable, other methods will need it
				Isohuntlink ihlink = new Isohuntlink(torUrl, torName, serie.getLatestSeason(), serie.getLatestEpisode(), torSeeds);
				isohuntlinks.add(ihlink);
			} catch (Exception e) {
				Email.Send("t.s.partanen@gmail.com", "HÄLYTYS - isoHunt", "TVSeriesFollower kohtasi virheen käsitellessään isoHunt-dataa ja on sammutettu." + eol 
						+ "Alla virhekoodi:" + eol + e.toString());
				System.exit(0);
			}
			
			for (int j = 0; j < isohuntlinks.size(); j++) {
				if (isohuntlinks.get(j).getSeeds()>=2000) {
					enoughseeds.add(isohuntlinks.get(j));
				}
			}
			
			for (int j = 0; j < enoughseeds.size(); j++) {
				System.out.println(enoughseeds.get(j).getLinkName() + " " + enoughseeds.get(j).getSeeds());
				for (int z = 0; z < series.size(); z++) {
					if (isThisNew (torName, series.get(z))==true) {
						followers = getFollowersforSeries(series.get(z).getName());
						serie.setName(series.get(z).getName());
						emailMessage = "Hello!<br>New episode (" + seriesEpisode + ") of " + serie.getName() + " is out.<br>"
								+ "Link to the torrent:<br>"
								+ "<a href=\"" + torUrl + "\">" + torUrl + "</a><br>" 
								+ "Easier clickable link to the torrent file coming soon!" + "<br><br><i>-TVSeriesFollower</i>";
						setEpisode(serie);
						Email.massMail(followers, emailTitle, emailMessage);
					}
				}
			}
			isohuntlinks.clear();
			enoughseeds.clear();
		}
	}
	
	/**
	 * Takes a url to a torrent file from given html-substring
	 * @param url given html-substring
	 * @return link to a torrent
	 */
	private static String torrentUrl(String url) {
		torUrl = null;
		pattern = Pattern.compile("(.*)\"><span>");
		matcher = pattern.matcher(url);
		while (matcher.find()) {
			torUrl = "https://isohunt.to" + matcher.group(1);
		}
		return torUrl;
	}
	
	/**
	 * Takes a torrent's name from given html-substring
	 * @param url given html-substring
	 * @return name, that the uploader has given to the torrent. User sees this name in the isoHunt file listing, if the search would have been done manually
	 */
	private static String torrentName(String url) {
		torName = null;
		pattern = Pattern.compile("\"><span>(.*)</span></a>");
		matcher = pattern.matcher(url);
		while (matcher.find()) {
			torName = matcher.group(1);
		}
		return torName;
	}
	
	/**
	 * Takes a torrent's seeds from given html-substring
	 * @param url given html-substring
	 * @return amount of seeds of a torrent
	 */
	private static int torrentSeeds(String url) {
		sSeeds = null;
		torSeeds = -1;
		seriesEpisode = null;
		pattern = Pattern.compile("<td class=\"seeders-row \\w{2}\">(.*)</td><td class=\"rating-row\">");
		matcher = pattern.matcher(url);
		while (matcher.find()) {
			sSeeds = matcher.group(1);
			torSeeds = Integer.parseInt(sSeeds);
		}
		return torSeeds;
	}
	
	/**
	 * Parses season and episode from given url or magnet
	 * @param input url or magnet, where the season and episode is wanted
	 * @return Serie-object that contains the season and episode, but doesn't contain the name of the serie
	 * @throws AddressException
	 * @throws MessagingException
	 */
	private static Series serieNumbers (String input) throws AddressException, MessagingException {
		pattern = Pattern.compile("[S]\\d{2}[E]\\d{2}");
		nSeason = -1;
		nEpisode = -1;
		SxxExx = false;
		matcher = pattern.matcher(input);
		while (matcher.find()) {
			try {
				seriesEpisode = matcher.group();
				nSeason = Integer.parseInt(seriesEpisode.substring(1, 3));
				nEpisode = Integer.parseInt(seriesEpisode.substring(4));
				SxxExx = true;
			} catch (Exception e) {
				Email.Send("t.s.partanen@gmail.com", "HÄLYTYS", "TVSeriesFollower ei osannut parsea seasonia/episodia (SxxExx) ja on sammutettu.");
				System.exit(0);
			}
		}
		if (SxxExx==false) {
			pattern = Pattern.compile("\\d[x]\\d{2}");
			matcher = pattern.matcher(input);
			while (matcher.find()) {
				try {
					seriesEpisode = matcher.group();
					nSeason = Integer.parseInt(seriesEpisode.substring(0, seriesEpisode.indexOf("x")));
					nEpisode = Integer.parseInt(seriesEpisode.substring(seriesEpisode.indexOf("x")+1));
				} catch (Exception e) {
					Email.Send("t.s.partanen@gmail.com", "HÄLYTYS", "TVSeriesFollower ei osannut parsea seasonia/episodia (SSxEE) ja on sammutettu.");
					System.exit(0);
				}
			}
		}
		serie = new Series(nSeason, nEpisode);
		return serie;
	}
	
	/**
	 * Compares url or magnet to current information from database and checks if given url or magnet contains new episode
	 * @param input html-substring or magnet that potentially contains new episode of a wanted serie
	 * @param series Serie name and known latest season and episode from database
	 * @return
	 */
	private static boolean isThisNew(String input, Series series) {
		input = input.replace(" ", "");
		input = input.replace(".", "");
		if (input.toLowerCase().contains(series.getName().toLowerCase().replace(" ", "")) 
				&& (input.contains("720p") || input.contains("1080p")) 
				&& (series.getLatestSeason()<nSeason || (series.getLatestSeason()==nSeason && series.getLatestEpisode()<nEpisode))) {
			emailTitle = "New episode of " + series.getName() + " released";
			return true;
		}
		return false;
	}
	
	/**
	 * Gets series information from database and increases episode number by one. This is used for searching series with new episodes
	 * @return Most recent serie information from database, where episode number is increased by one
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static ArrayList<Series> getNewEpisodeForSeries() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		ArrayList<Series> series = getSeries();
		ArrayList<Series> newEpisode = new ArrayList<>();
		for (int i = 0; i < series.size(); i++) {
			newEpisode.add(new Series(series.get(i).getName(), series.get(i).getLatestSeason(), series.get(i).getLatestEpisode()+1));
		}
		return newEpisode;
	}
	
	/**
	 * Gets series information from database and increases season number by one and sets episode number to one. This is used for searching series with new season
	 * @return Most recent serie information from database, where season number is increased by one
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static ArrayList<Series> getNewSeasonForSeries() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		ArrayList<Series> series = getSeries();
		ArrayList<Series> newSeason = new ArrayList<>();
		for (int i = 0; i < series.size(); i++) {
			newSeason.add(new Series(series.get(i).getName(), series.get(i).getLatestSeason()+1, 1));
		}
		return newSeason;
	}
	
	/**
	 * Sets given serie information to database
	 * @param serie new information that needs to be updated to the database
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws AddressException
	 * @throws MessagingException
	 */
	private static void setEpisode(Series serie) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, AddressException, MessagingException {
		try {
			openConnection();
			statement = conn.createStatement();
			String sql = "update TVSeriesFollower.Series set latest_season = ?, latest_episode = ? WHERE name = ?";
			PreparedStatement lause = conn.prepareStatement(sql);
			lause.setInt(1, serie.getLatestSeason());
			lause.setInt(2, serie.getLatestEpisode());
			lause.setString(3, serie.getName());
			lause.executeUpdate();
		} catch (Exception e) {
			Email.Send("t.s.partanen@gmail.com", "HÄLYTYS", "TVSeriesFollower ei voinut päivittää tietokantaansa ja on sammutettu");
			closeConnection();
			System.exit(0);
		}
		finally {
			closeConnection();
		}
	}
	
	/**
	 * Retrieves serie information from the database
	 * @return wanted serie information
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static ArrayList<Series> getSeries() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		ArrayList<Series> series = new ArrayList<Series>();
		openConnection();
		 try
	        {
	            statement = conn.createStatement();
	            ResultSet results = statement.executeQuery("select * from TVSeriesFollower.Series");
	            while (results.next()) {
					String name = results.getString("name");
					int season = results.getInt("latest_season");
					int episode = results.getInt("latest_episode");
					Series s = new Series(name, season, episode);
					series.add(s);
				}
	        }
	        catch (SQLException sqlExcept)
	        {
	        }
		 finally {
			 closeConnection();
		 }
		return series;
	}
	
	/**
	 * Retrieves followers for a serie
	 * @param followedSeries Serie name, which followers are wanted
	 * @return list of followers for given serie
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private static ArrayList<String> getFollowersforSeries(String followedSeries) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		ArrayList<String> followers = new ArrayList<String>();
		openConnection();
		try
        {
            statement = conn.createStatement();
            ResultSet results = statement.executeQuery("select address from TVSeriesFollower.Usersseries WHERE name = '" + followedSeries + "'");
            while (results.next()) {
				String address = results.getString("address");
				followers.add(address);
			}
        }
        catch (SQLException sqlExcept)
        {
        }
	 finally {
		 closeConnection();
	 }
		return followers;
	}
	
	/**
	 * Opens connection to the database
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	private static void openConnection() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		conn = DriverManager.getConnection(dbURL);
	}
	
	/**
	 * Closes connection to the database
	 */
	private static void closeConnection() {
		try {
			if (statement != null) {
				statement.close();
			}
			if (conn != null) {
                DriverManager.getConnection(dbURL + ";shutdown=true");
                conn.close();
			}
		} catch (Exception e) {
		}
	}
}
