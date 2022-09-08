import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class DataBase {
    private static final String DATABASE_URL = System.getenv("DATABASE_URL");
    private static URI dbUri;
    static {
        try {
            dbUri = new URI(DATABASE_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    private final static String username = dbUri.getUserInfo().split(":")[0];
    private final static String password = dbUri.getUserInfo().split(":")[1];
    private final static String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();


    public static void queryDb(String query) throws SQLException, URISyntaxException {
        Statement statement = null;
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            statement = connection.createStatement();
            statement.execute(query);
        } catch (SQLException Exception) {
            Exception.printStackTrace();
        } finally {
            assert statement != null;
            statement.close();
            connection.close();
        }
    }

    public static boolean queryDbResultBoolean(String query) throws SQLException {
        Statement statement = null;
        Connection connection = null;
        boolean result = false;
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            statement = connection.createStatement();
            statement.execute(query);
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                result = resultSet.getBoolean(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            assert statement != null;
            statement.close();
            connection.close();
        }
        return result;
    }

    public static long queryDbResultLong(String query) throws SQLException {
        Statement statement = null;
        Connection connection = null;
        long result = -1;
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            statement = connection.createStatement();
            statement.execute(query);
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                result = resultSet.getLong(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            assert statement != null;
            statement.close();
            connection.close();
        }
        return result;
    }

    public static int queryDbResultInt(String query) throws SQLException {
        Statement statement = null;
        Connection connection = null;
        int result = -1;
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            statement = connection.createStatement();
            statement.execute(query);
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                result = resultSet.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            assert statement != null;
            statement.close();
            connection.close();
        }
        return result;
    }
    public static String queryDbResultString(String query) throws SQLException {
        Statement statement = null;
        Connection connection = null;
        String result = "";
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            statement = connection.createStatement();
            statement.execute(query);
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                result = result + " " + resultSet.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            assert statement != null;
            statement.close();
            connection.close();
        }
        return result;
    }
}
