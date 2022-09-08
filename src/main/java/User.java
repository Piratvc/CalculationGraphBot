import org.telegram.telegrambots.meta.api.objects.Message;
import java.net.URISyntaxException;
import java.sql.*;
public class User {
    public String createUser(Message message) throws SQLException {
        Long chatId = message.getChatId();
        String userName = message.getFrom().getFirstName();
        String lastName = message.getFrom().getLastName();
        String countryCode = message.getFrom().getLanguageCode();
        String query = "insert into users(chatId,name,lastname,amount,CountryCode)" +
                "values(" + chatId + ",'" + userName + "','" + lastName + "',0,'" + countryCode + "');";
        int amount;
        try {
            if (!checkChatId(chatId)) {
                DataBase.queryDb(query);
                return "Спасибо за регистрацию, <b>" + userName + "</b> !";
            } else {
                query = "select amount from users where chatId=" + chatId + ";";
                amount = DataBase.queryDbResultInt(query);
                return "<b>" + userName + "</b>, добро пожаловать!" +
                        "\nВы уже расчитали <b>" + amount + "</b> графиков";
            }
        } catch (SQLException | URISyntaxException exception) {
            exception.printStackTrace();
        }
        return "Спасибо за регистрацию, <b>" + userName + "</b> !";
    }

    public boolean checkChatId(Long chatId) throws SQLException, URISyntaxException {
        try {
            String query = "select chatId from users where chatId=" + chatId + ";";
            long resultId = DataBase.queryDbResultLong(query);
            return resultId == chatId;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return true;
    }

    public void givePoint(long chatId) throws SQLException, URISyntaxException {
        String query = "select amount from users where chatId=" + chatId + ";";
        int amount = DataBase.queryDbResultInt(query);
        amount += 1;
        query = "update users set amount=" + amount + " where chatId=" + chatId + ";";
        DataBase.queryDb(query);
    }
}