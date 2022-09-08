import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Buttons {
    public void createButtons(SendMessage sendMessage, String... text) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();

        KeyboardRow keyboardRowUp = new KeyboardRow();
        keyboardRowUp.add(new KeyboardButton(text[0]));
        keyboardRowUp.add(new KeyboardButton(text[1]));


        KeyboardRow keyboardRowDown = new KeyboardRow();
        keyboardRowDown.add(new KeyboardButton(text[2]));

        keyboardRowList.add(keyboardRowUp);
        keyboardRowList.add(keyboardRowDown);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    public static SendMessage inlineKeyMessageOptions(long chatId) throws SQLException {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        //опции добавить подпись Утверждено
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        if (!(DataBase.queryDbResultBoolean("select optionsapproved from users where chatId=" + chatId + ";"))) {
            inlineKeyboardButton1.setText("Добавить подпись Утверждено");
            inlineKeyboardButton1.setCallbackData("/setApprovedTrue");
        } else {
            inlineKeyboardButton1.setText("Убрать подпись Утверждено");
            inlineKeyboardButton1.setCallbackData("/setApprovedFalse");
        }
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);

        //опции добавить подпись pdf
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        if (!(DataBase.queryDbResultBoolean("select pdfneed from users where chatId=" + chatId + ";"))) {
            inlineKeyboardButton2.setText("Добавить PDF");
            inlineKeyboardButton2.setCallbackData("/setPdfTrue");
        } else {
            inlineKeyboardButton2.setText("Убрать PDF");
            inlineKeyboardButton2.setCallbackData("/setPdfFalse");
        }
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow2.add(inlineKeyboardButton2);

        //опции добавить подпись word
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        if (!(DataBase.queryDbResultBoolean("select wordneed from users where chatId=" + chatId + ";"))) {
            inlineKeyboardButton3.setText("Добавить WORD");
            inlineKeyboardButton3.setCallbackData("/setWordTrue");
        } else {
            inlineKeyboardButton3.setText("Убрать WORD");
            inlineKeyboardButton3.setCallbackData("/setWordFalse");
        }
        keyboardButtonsRow2.add(inlineKeyboardButton3);

        //складываем ряды
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Настройки для изменения:");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return sendMessage;
    }
}