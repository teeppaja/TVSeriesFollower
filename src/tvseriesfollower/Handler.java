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
			//e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void isoHuntCheck(ArrayList<String> urls) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, AddressException, MessagingException {
		ArrayList<Series> series = getSeries();
		
		for (int i = 0; i < urls.size(); i++) {
			try {
				emailMessage = null;
				emailTitle = null;
				torUrl = torrentUrl(urls.get(i));
				torName = torrentName(urls.get(i));
				torSeeds = torrentSeeds(urls.get(i));
				Series serie = serieNumbers(torName);
				nSeason = serie.getLatestSeason();
				nEpisode = serie.getLatestEpisode();
				
				for (int j = 0; j < series.size(); j++) {
					if (isThisNew (torName, series.get(j))==true && torSeeds>2000) {
						followers = getFollowersforSeries(series.get(j).getName());
						serie.setName(series.get(j).getName());
						emailMessage = "Hello!<br>New episode (" + seriesEpisode + ") of " + serie.getName() + " is out.<br>"
								+ "Link to the torrent:<br>"
								+ "<a href=" + torUrl + "\">" + torUrl + "</a><br>" 
								+ "Easier clickable link to the torrent file coming soon!" + "<br><br><i>-TVSeriesFollower</i>";
						setEpisode(serie);
						Email.massMail(followers, emailTitle, emailMessage);
					}
				}
			} catch (Exception e) {
				Email.Send("t.s.partanen@gmail.com", "HÄLYTYS - isoHunt", "TVSeriesFollower kohtasi virheen käsitellessään isoHunt-dataa ja on sammutettu." + eol 
						+ "Alla virhekoodi:" + eol + e.toString());
				//e.printStackTrace();
				System.exit(0);
			}
		}
	}
	
	private static String torrentUrl(String url) {
		torUrl = null;
		pattern = Pattern.compile("(.*)\"><span>");
		matcher = pattern.matcher(url);
		while (matcher.find()) {
			torUrl = "https://isohunt.to" + matcher.group(1);
		}
		return torUrl;
	}
	
	private static String torrentName(String url) {
		torName = null;
		pattern = Pattern.compile("\"><span>(.*)</span></a>");
		matcher = pattern.matcher(url);
		while (matcher.find()) {
			torName = matcher.group(1);
		}
		return torName;
	}
	
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
	
	private static boolean isThisNew(String input, Series series) {
		if (input.toLowerCase().replace(" ", "").contains(series.getName().toLowerCase().replace(" ", "")) 
				&& (input.contains("720p") || input.contains("1080p")) 
				&& (series.getLatestSeason()<nSeason || (series.getLatestSeason()==nSeason && series.getLatestEpisode()<nEpisode))) {
			emailTitle = "New episode of " + series.getName() + " released";
			return true;
		}
		return false;
	}
	
	public static ArrayList<Series> getNewEpisodeForSeries() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		ArrayList<Series> series = getSeries();
		ArrayList<Series> newEpisode = new ArrayList<>();
		for (int i = 0; i < series.size(); i++) {
			newEpisode.add(new Series(series.get(i).getName(), series.get(i).getLatestSeason(), series.get(i).getLatestEpisode()+1));
		}
		return newEpisode;
	}
	
	public static ArrayList<Series> getNewSeasonForSeries() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		ArrayList<Series> series = getSeries();
		ArrayList<Series> newSeason = new ArrayList<>();
		for (int i = 0; i < series.size(); i++) {
			newSeason.add(new Series(series.get(i).getName(), series.get(i).getLatestSeason()+1, 1));
		}
		return newSeason;
	}
	
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
	            //sqlExcept.printStackTrace();
	        }
		 finally {
			 closeConnection();
		 }
		return series;
	}
	
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
            //sqlExcept.printStackTrace();
        }
	 finally {
		 closeConnection();
	 }
		return followers;
	}
	
	private static void openConnection() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		conn = DriverManager.getConnection(dbURL);
	}
	
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
