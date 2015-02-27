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
    private static Connection conn = null;
    private static Statement statement = null;

	public static void check(ArrayList<String> magnets) throws AddressException, 
	MessagingException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		ArrayList<Series> series = getSeries();
		ArrayList<String> followers;
		String message;
		String topic;
		String seriesEpisode = "";
		int nSeason = -1;
		int nEpisode = -1;
		boolean SxxExx;
		for (int i = 0; i < magnets.size(); i++) {
			Pattern pattern = Pattern.compile("[S]\\d{2}[E]\\d{2}");
			nSeason = -1;
			nEpisode = -1;
			SxxExx = false;
			Matcher matcher = pattern.matcher(magnets.get(i));
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
				matcher = pattern.matcher(magnets.get(i));
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

			for (int j = 0; j < series.size(); j++) {
				if (magnets.get(i).toLowerCase().replace(".", "").contains(series.get(j).getName().toLowerCase().replace(" ", "")) 
						&& (magnets.get(i).contains("720p") || magnets.get(i).contains("1080p")) 
						&& (series.get(j).getLatestSeason()<nSeason || (series.get(j).getLatestSeason()==nSeason && series.get(j).getLatestEpisode()<nEpisode))) {
					topic = "New episode of " + series.get(j).getName() + " released";
					message = "Hello!<br>New episode (" + seriesEpisode + ") of " + series.get(j).getName() + " is out.<br>"
							+ "You can download the episode by clicking the following link or by copying the magnet URL and adding it manyally to your torrent client:<br>"
							+ "<a href=\"" + magnets.get(i) + "\">" + magnets.get(i) + "</a><br><br><br><i>-TVSeriesFollower</i>";
					followers = getFollowersforSeries(series.get(j).getName());
					Series s = new Series (series.get(j).getName(), nSeason, nEpisode);
					setEpisode(s);
					for (int z = 0; z < followers.size(); z++) {
						Email.Send(followers.get(z), topic, message);
						//System.out.println("Mailia lähtee: " + followers.get(z) + " sarja: " + topic + " season:" + nSeason + " episode:" + nEpisode);
					}
					
				}
			}

		}
	}
	
	private static void setEpisode(Series s) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, AddressException, MessagingException {
		try {
			openConnection();
			statement = conn.createStatement();
			String sql = "update TVSeriesFollower.Series set latest_season = ?, latest_episode = ? WHERE name = ?";
			PreparedStatement lause = conn.prepareStatement(sql);
			lause.setInt(1, s.getLatestSeason());
			lause.setInt(2, s.getLatestEpisode());
			lause.setString(3, s.getName());
			lause.executeUpdate();
		} catch (Exception e) {
			Email.Send("t.s.partanen@gmail.com", "HÄLYTYS", "TVSeriesFollower ei voinut päivittää tietokantaansa ja on sammutettu");
			System.exit(0);
		}
		finally {
			closeConnection();
		}
		
	}
	
	private static ArrayList<Series> getSeries() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
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
