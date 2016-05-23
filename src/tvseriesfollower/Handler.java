package tvseriesfollower;

import java.util.ArrayList;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

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
	
	public static void checkResults(ArrayList<Torrents> torrents, Series serie) throws AddressException, MessagingException {
		for (int i = 0; i < torrents.size(); i++) {
			if (torrents.get(i).getName().contains(serie.getName().replace(" ", "").toLowerCase()) && 
					torrents.get(i).getName().contains(serie.getSSeaEp()) &&
					(torrents.get(i).getName().contains("720p") || torrents.get(i).getName().contains("1080p")) && 
					torrents.get(i).getSeeds()>=1000) {
				System.out.println("Match löytyi: " + torrents.get(i));
				System.out.println("Mihin matchasi: " + serie);
				try {
					followers = getFollowersforSeries(serie.getName());
					emailTitle = "New episode of " + serie.getName() + " released";
					emailMessage = "Hello!<br>New episode " + "of " + serie.getName() + " is out.<br>"
							+ "You can download the episode by clicking the following link or by copying the magnet URL and adding it manyally to your torrent client:<br>"
							+ "<a href=\"" + torrents.get(i).getMagnet() + "\">" + torrents.get(i).getMagnet() + "</a><br><br>Link to the torrent page: " 
							+ "<a href=\"" + torrents.get(i).getUrl() + "\">" + torrents.get(i).getUrl() + "</a><br><br>"
							+ "You can find subtitles for this episode from here once they are released:<br>" 
							+ "<a href=\"" + serie.getSubtitles() + "\">" + serie.getSubtitles() + "</a><br><br><br><i>-TVSeriesFollower</i>";
					setEpisode(serie);
					Email.massMail(followers, emailTitle, emailMessage);
					break;
				} catch (Throwable e) {
					e.printStackTrace();
					Email.unknownCrash(e);
					System.exit(0);
				}
			}
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
		newEpisode = stringify(newEpisode);
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
		newSeason = stringify(newSeason);
		return newSeason;
	}
	
	private static ArrayList<Series> stringify(ArrayList<Series> series) {
		for (int i = 0; i < series.size(); i++) {
			String sSeason = null;
			String sEpisode = null;
			if (series.get(i).getLatestSeason()<10) {
				sSeason = "0"+series.get(i).getLatestSeason();
			} else {
				sSeason = Integer.toString(series.get(i).getLatestSeason());
			}
			if (series.get(i).getLatestEpisode()<10) {
				sEpisode = "0"+series.get(i).getLatestEpisode();
			} else {
				sEpisode = Integer.toString(series.get(i).getLatestEpisode());
			}
			series.get(i).setSSeaEp("s" + sSeason + "e" + sEpisode);
		}
		return series;		
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
			closeConnection();
			Email.unknownCrash(e);
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