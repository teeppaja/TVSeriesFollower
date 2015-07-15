package tvseriesfollower;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Handler {
	private static String dbURL = "jdbc:derby://localhost:1527/MyDB";
    private static Connection conn = null;
    private static PreparedStatement statement = null;
    private static ArrayList<String> followers;
    private static String emailMessage;
    private static String emailTitle;
    private static String seriesEpisode;

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
				Series serie = infoFromMagnet(magnets.get(i), series);
				if (serie != null) {
					followers = getFollowersforSeries(serie.getName());
					emailMessage = "Hello!<br>New episode (" + seriesEpisode + ") of " + serie.getName() + " is out.<br>"
							+ "You can download the episode by clicking the following link or by copying the magnet URL and adding it manyally to your torrent client:<br>"
							+ "<a href=\"" + magnets.get(i) + "\">" + magnets.get(i) + "</a><br><br>"
							+ "You can find subtitles for this episode from here once they are released:<br>" 
							+ "<a href=\"" + serie.getSubtitles() + "\">" + serie.getSubtitles() + "</a><br><br><br><i>-TVSeriesFollower</i>";
					setEpisode(serie);
					Email.massMail(followers, emailTitle, emailMessage);
					series = getSeries();
				} else {
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Email.knownCrash(e, "EZTV");
			System.exit(0);
		}
	}
	
	public static void checkStrike(ArrayList<JSONObject> jsonObjects, Series serie) throws AddressException, MessagingException {
		for (int i = 0; i < jsonObjects.size(); i++) {
			int sseeds = (Integer) jsonObjects.get(i).get("seeds");
			String title = (String) jsonObjects.get(i).get("torrent_title");
			title = title.replace(" ", "").replace(",", "").replace(".", "");
			String category = (String) jsonObjects.get(i).get("torrent_category");
			if (category.equalsIgnoreCase("TV") && sseeds>2000 && title.toLowerCase().contains(serie.getName().toLowerCase().replace(" ", "")) 
					&& (title.contains("720p") || title.contains("1080p"))) {
				String magnet = (String) jsonObjects.get(i).get("magnet_uri");
				String url = (String) jsonObjects.get(i).get("page");
				try {
					followers = getFollowersforSeries(serie.getName());
					emailTitle = "New episode of " + serie.getName() + " released";
					emailMessage = "Hello!<br>New episode " + "of " + serie.getName() + " is out.<br>"
							+ "You can download the episode by clicking the following link or by copying the magnet URL and adding it manyally to your torrent client:<br>"
							+ "<a href=\"" + magnet + "\">" + magnet + "</a><br><br>Link to the torrent page: " + "<a href=\"" + url + "\">" + url + "</a><br><br>"
							+ "You can find subtitles for this episode from here once they are released:<br>" 
							+ "<a href=\"" + serie.getSubtitles() + "\">" + serie.getSubtitles() + "</a><br><br><br><i>-TVSeriesFollower</i>";
					setEpisode(serie);
					Email.massMail(followers, emailTitle, emailMessage);
					break;
				} catch (Throwable e) {
					e.printStackTrace();
					Email.knownCrash(e, "Strike");
					System.exit(0);
				}
			}
		}		
	}
	
	/**
	 * Parses season and episode from given url or magnet
	 * @param magnet url or magnet, where the season and episode is wanted
	 * @param series 
	 * @return Serie-object that contains the season and episode, but doesn't contain the name of the serie
	 * @throws AddressException
	 * @throws MessagingException
	 */
	private static Series infoFromMagnet (String magnet, ArrayList<Series> series) throws AddressException, MessagingException {
		boolean gotName = false;
		Series serie = new Series();
		int i;
		
		//Serie name from magnet. Might as well check the resolution
		for (i = 0; i < series.size(); i++) {
			if (magnet.toLowerCase().replace("%20", "").replace(".", "").contains(series.get(i).getName().toLowerCase().replace(" ", "").replace(".", "")) 
					&& (magnet.toLowerCase().contains("720p") || magnet.toLowerCase().contains("1080p"))) {
				serie.setName(series.get(i).getName());
				serie.setSubtitles(series.get(i).getSubtitles());
				gotName = true;
				break;
			}
		}
		
		//If a name in magnet matched one from database we have a reason to continue
		if (gotName==true) {
			int nSeason = -1;
			int nEpisode = -1;
			boolean SxxExx = false;
			Pattern pattern = Pattern.compile("[S]\\d{2}[E]\\d{2}");
			Matcher matcher = pattern.matcher(magnet);
			while (matcher.find()) {
				try {
					seriesEpisode = matcher.group();
					nSeason = Integer.parseInt(seriesEpisode.substring(1, 3));
					nEpisode = Integer.parseInt(seriesEpisode.substring(4));
					SxxExx = true;
				} catch (Exception e) {
					e.printStackTrace();
					Email.send("t.s.partanen@gmail.com", "HÄLYTYS", "TVSeriesFollower ei osannut parsea seasonia/episodia (SxxExx) ja on sammutettu.");
					System.exit(0);
				}
			}
			if (SxxExx==false) {
				pattern = Pattern.compile("\\d[x]\\d{,2}");
				matcher = pattern.matcher(magnet);
				while (matcher.find()) {
					try {
						seriesEpisode = matcher.group();
						nSeason = Integer.parseInt(seriesEpisode.substring(0, seriesEpisode.indexOf("x")));
						nEpisode = Integer.parseInt(seriesEpisode.substring(seriesEpisode.indexOf("x")+1));
					} catch (Exception e) {
						Email.send("t.s.partanen@gmail.com", "HÄLYTYS", "TVSeriesFollower ei osannut parsea seasonia/episodia (SSxEE) ja on sammutettu.");
						System.exit(0);
					}
				}
			}
			serie.setLatestSeason(nSeason);
			serie.setLatestEpisode(nEpisode);
			
			//Check if identified episode is new
			if (isThisNew(serie, series.get(i))==true) {
				return serie;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Compares url or magnet to current information from database and checks if given url or magnet contains new episode
	 * @param series 
	 * @param input html-substring or magnet that potentially contains new episode of a wanted serie
	 * @param series Serie name and known latest season and episode from database
	 * @return
	 */
	private static boolean isThisNew(Series newRelease, Series dbInfo) {
		if (dbInfo.getLatestSeason()<newRelease.getLatestSeason() || (dbInfo.getLatestSeason()==dbInfo.getLatestSeason() && dbInfo.getLatestEpisode()<newRelease.getLatestEpisode())) {
			emailTitle = "New episode of " + newRelease.getName() + " released";
			return true;
		} else {
			return false;
		}
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
			newEpisode.add(new Series(series.get(i).getName(), series.get(i).getLatestSeason(), series.get(i).getLatestEpisode()+1, series.get(i).getSubtitles()));
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
			newSeason.add(new Series(series.get(i).getName(), series.get(i).getLatestSeason()+1, 1, series.get(i).getSubtitles()));
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
			String sql = "update TVSeriesFollower.Series set latest_season = ?, latest_episode = ? WHERE name = ?";
			statement = conn.prepareStatement(sql);
			statement.setInt(1, serie.getLatestSeason());
			statement.setInt(2, serie.getLatestEpisode());
			statement.setString(3, serie.getName());
			statement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Email.send("t.s.partanen@gmail.com", "HÄLYTYS", "TVSeriesFollower ei voinut päivittää tietokantaansa ja on sammutettu");
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
		try {
			openConnection();
			String sql = "select * from TVSeriesFollower.Series";
            statement = conn.prepareStatement(sql);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
				String name = results.getString("name");
				int season = results.getInt("latest_season");
				int episode = results.getInt("latest_episode");
				String subtitles = results.getString("subtitles");
				Series s = new Series(name, season, episode, subtitles);
				series.add(s);
			}
		}
	        catch (SQLException sqlExcept) {
	        	sqlExcept.printStackTrace();
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
		try {
			openConnection();
			String sql = "select address from TVSeriesFollower.Usersseries WHERE name = ?";
			statement = conn.prepareStatement(sql);
			statement.setString(1, followedSeries);
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				String address = results.getString("address");
				followers.add(address);
			}
		}
        catch (SQLException sqlExcept) {
        	sqlExcept.printStackTrace();
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
		Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
		conn = DriverManager.getConnection(dbURL);
	}
	
	/**
	 * Closes connection to the database
	 */
	private static void closeConnection() {
		try {
			if (statement != null) {
				statement.close();
				statement = null;
			}
			if (conn != null) {
                conn.close();
                conn = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}