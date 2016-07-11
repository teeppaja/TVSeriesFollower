package tvseriesfollower;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseConnector {
	// TODO: load database URL from a file
	private final String dbURL = "jdbc:derby://localhost:1527/MyDB";
    private Connection connection;
    private  PreparedStatement statement;
	
	public ArrayList<String> getFollowersforSerie(String serieName) {
		ArrayList<String> followers = new ArrayList<String>();
		openConnection();
		String sql = "select address from TVSeriesFollower.Usersseries WHERE name = ?";
		try {
			statement = connection.prepareStatement(sql);
			statement.setString(1, serieName);
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				String address = results.getString("address");
				followers.add(address);
			}
		} catch (SQLException e) {
			// TODO: handle exception
		}
		closeConnection();
		
		return followers;
	}
	
	public void setEpisode(Serie serie) {
		openConnection();
		String sql = "update TVSeriesFollower.Series set latest_season = ?, latest_episode = ? WHERE name = ?";
		try {
			statement = connection.prepareStatement(sql);
			statement.setInt(1, serie.getLatestSeason());
			statement.setInt(2, serie.getLatestEpisode());
			statement.setString(3, serie.getName());
			statement.executeUpdate();
		} catch (Exception e) {
			// TODO: handle exception
		}
		closeConnection();
	}
	
	public ArrayList<Serie> getAllSeries() {
		ArrayList<Serie> series = new ArrayList<Serie>();
		openConnection();
		String sql = "select * from TVSeriesFollower.Series";
		try {
			statement = connection.prepareStatement(sql);
	        ResultSet results = statement.executeQuery();
	        while (results.next()) {
				String name = results.getString("name");
				int season = results.getInt("latest_season");
				int episode = results.getInt("latest_episode");
				String subtitles = results.getString("subtitles");
				Serie s = new Serie(name, season, episode, subtitles);
				series.add(s);
			}
		} catch (SQLException e) {
			// TODO: handle exception
		} finally {
			closeConnection();
		}
		return series;
	}
	
	private void openConnection() {
		try {
			Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
			connection = DriverManager.getConnection(dbURL);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void closeConnection() {
		try {
			if (statement != null) {
				statement.close();
				statement = null;
			}
			if (connection != null) {
                connection.close();
                connection = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
