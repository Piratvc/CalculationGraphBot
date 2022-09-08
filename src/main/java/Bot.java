import com.vdurmont.emoji.EmojiParser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.io.*;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Slf4j
public class Bot extends TelegramLongPollingBot {
    private static final String BOT_TOKEN = System.getenv("TOKEN");
    Buttons buttons = new Buttons();
    private final String buttonRegistrarion = "Личный кабинет";
    private final String buttonExample = "Примеры запросов";
    private final String buttonContact = "Контакт автора";
    private final String startComand = "/start";

    @Override
    public String getBotUsername() {
        return "HeatCalculationBot";
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if ((update.hasMessage() && update.getMessage().hasText())) {
                Message message = update.getMessage();
                Long chatId = message.getChatId();
                User userCreator = new User();

                log.info("New message from User: {}, chatId: {}, with text: {}",
                        message.getFrom().getUserName(), message.getFrom().getId(), message.getText());

                DataBase.queryDb("update users set datelastuse = '" + getDate() + "' where chatId=" + chatId + ";");
                switch (message.getText()) {
                    case "/adminGetNumberUsers" -> {
                        if (chatId == 724559120) {
                            sendMsg(chatId, "Бота использовали " +
                                    DataBase.queryDbResultLong("select count(*) from public.users;") + " аккаунта");
                        }
                    }
                    case "/adminGetNameUsers" -> {
                        if (chatId == 724559120) {
                            sendMsg(chatId, "Бота использовали эти люди " +
                                    DataBase.queryDbResultString("select name from public.users;").replace(" ", "\n"));
                        }
                    }
                    case buttonExample -> {
                        sendMsg(chatId, "Для примера, можно скопировать " +
                                "одно из этих сообщений " + EmojiParser.parseToUnicode(":point_down:") + " и отправить боту");
                        sendMsg(chatId, "<b>150 70 20 -24</b>");
                        sendMsg(chatId, "<b>150 70 90 70 18 -26</b>");
                        sendMsg(chatId, "<b>110 70 90 70 80 60 20 -24</b>");
                    }
                    case buttonRegistrarion -> {
                        try {
                            sendMsg(chatId, userCreator.createUser(message));
                            execute(Buttons.inlineKeyMessageOptions(message.getChatId()));
                        } catch (SQLException e) {
                            e.printStackTrace();
                            sendMsg(chatId, "Ошибка личного кабинета, напишите @piratvc1, исправим");
                        }
                    }
                    case buttonContact ->
                        sendMsg(chatId, "По вопросам, пожеланиям и предложениям писать @piratvc1");

                    case startComand -> {
                        sendMsg(chatId, userCreator.createUser(message));
                        sendMsg(chatId, "Вас приветствует бот, позволяющий расчитывать графики" +
                                " теплоснабжения." +
                                "\nПри успешном расчете Вы получаете pdf и xlsx файлы." +
                                " Для начала работы с ботом введите данные для расчета (температуры в °C, через пробел), например: " +
                                "\n<b>150 70 20 -24</b>, где " +
                                "\n<b>150</b> - темепература подачи," +
                                "\n<b>70</b> - температура обратки," +
                                "\n<b>20</b> - расчетная температура внутреннего воздуха," +
                                "\n<b>-24</b> - наружная температура" +
                                "\n\nТак же возможен расчет сразу двух или трех графиков в один файл, попробуйте кнопку <b>" + buttonExample + "</b>");
                    }
                    default -> {
                        try {
                            if (userCreator.checkChatId(chatId)) {
                                sendMsg(chatId, "Идет расчёт графика...");
                                tableBuilder(message);
                                User user = new User();
                                user.givePoint(chatId);
                            } else {
                                sendMsg(chatId, "Вы не зарегистрированы, нажмите кнопку " + buttonRegistrarion);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendMsg(chatId, "Формат сообщения неправильный");
                        }
                    }
                }
            } else if (update.hasCallbackQuery()) {
                try {
                    AnswerCallbackQuery answer = new AnswerCallbackQuery();
                    answer.setText(update.getCallbackQuery().getData());
                    answer.setCallbackQueryId(update.getCallbackQuery().getId());
                    answer.setShowAlert(false);
                    long chatId = update.getCallbackQuery().getFrom().getId();
                    switch (update.getCallbackQuery().getData()) {
                        case "/setApprovedTrue" -> {
                            String query = "update users set optionsapproved = true where chatId=" + chatId + ";";
                            DataBase.queryDb(query);
                            answer.setText("Подпись Утверждено добавлена");

                            execute(answer);
                        }
                        case "/setApprovedFalse" -> {
                            String query = "update users set optionsapproved = false where chatId=" + chatId + ";";
                            DataBase.queryDb(query);
                            answer.setText("Подпись Утверждено убрана");
                            execute(answer);
                        }
                        case "/setPdfTrue" -> {
                            String query = "update users set pdfneed = true where chatId=" + chatId + ";";
                            DataBase.queryDb(query);
                            answer.setText("PDF формат добавлен");
                            execute(answer);
                        }
                        case "/setPdfFalse" -> {
                            String query = "update users set pdfneed = false where chatId=" + chatId + ";";
                            DataBase.queryDb(query);
                            answer.setText("PDF формат удален");
                            execute(answer);
                        }
                        case "/setWordTrue" -> {
                            String query = "update users set wordneed = true where chatId=" + chatId + ";";
                            DataBase.queryDb(query);
                            answer.setText("WORD формат добавлен");
                            execute(answer);
                        }
                        case "/setWordFalse" -> {
                            String query = "update users set wordneed = false where chatId=" + chatId + ";";
                            DataBase.queryDb(query);
                            answer.setText("WORD формат удален");
                            execute(answer);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tableBuilder(Message message) throws FileNotFoundException, SQLException {
        String textTemperature = message.getText();
        String[] subStr = textTemperature.split(" ");
        Long chatId = message.getChatId();

        Table table = new Table();
        table.setApprovedHeater(DataBase.queryDbResultBoolean("select optionsapproved from users where chatId=" + chatId + ";"));
        table.setTempinside(Integer.parseInt(subStr[subStr.length - 2]));

        File fileXlx = null;
        switch (subStr.length) {
            case 4:
                int t1 = Integer.parseInt(subStr[0]);
                int t2 = Integer.parseInt(subStr[1]);
                int tIns = Integer.parseInt(subStr[2]);
                int tOut = Integer.parseInt(subStr[3]);
                fileXlx = table.writeArrayInFile(table.calculateHeatGraph(t1, t2, tIns, tOut));
                break;
            case 6:
                t1 = Integer.parseInt(subStr[0]);
                t2 = Integer.parseInt(subStr[1]);
                int t21 = Integer.parseInt(subStr[2]);
                int t22 = Integer.parseInt(subStr[3]);
                tIns = Integer.parseInt(subStr[4]);
                tOut = Integer.parseInt(subStr[5]);
                fileXlx = table.writeArrayInFile(table.calculateHeatGraph(t1, t2, t21, t22, tIns, tOut));
                break;
            case 8:
                t1 = Integer.parseInt(subStr[0]);
                t2 = Integer.parseInt(subStr[1]);
                t21 = Integer.parseInt(subStr[2]);
                t22 = Integer.parseInt(subStr[3]);
                int t31 = Integer.parseInt(subStr[4]);
                int t32 = Integer.parseInt(subStr[5]);
                tIns = Integer.parseInt(subStr[6]);
                tOut = Integer.parseInt(subStr[7]);
                fileXlx = table.writeArrayInFile(table.calculateHeatGraph(t1, t2, t21, t22, t31, t32, tIns, tOut));
                break;
        }
        //Конвертация и отправка
        File filePdf = null;
        File fileWord = null;
        try {
            sendDocument(chatId, EmojiParser.parseToUnicode(":point_up_2:") + " Ваш график в формате Excel " + EmojiParser.parseToUnicode(":white_check_mark:"), new InputFile(fileXlx));
            //если нужен pdf то отсылаем
            if (DataBase.queryDbResultBoolean("select pdfneed from users where chatId=" + chatId + ";")) {
                filePdf = table.convertExelToPdf();
                sendDocument(chatId, EmojiParser.parseToUnicode(":point_up_2:") + " Ваш график в формате Pdf " + EmojiParser.parseToUnicode(":white_check_mark:"), new InputFile(filePdf));
            }
            //если нужен word то отсылаем
            if (DataBase.queryDbResultBoolean("select wordneed from users where chatId=" + chatId + ";")) {
                fileWord = table.convertExelToWord();
                sendDocument(chatId, EmojiParser.parseToUnicode(":point_up_2:") + " Ваш график в формате Word (не на всех телефонах корректное отображение, на компьютере отображается правильно) " + EmojiParser.parseToUnicode(":white_check_mark:"), new InputFile(fileWord));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Ошибка создания или отправки файла");
            sendMsg(chatId, "Ошибка отправки или создания файла");
        } finally {
            try {
                assert fileXlx != null;
                fileXlx.delete();
                if (filePdf != null) {
                    filePdf.delete();
                }
                if (fileWord != null) {
                    fileWord.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Ошибка удаления файлов");
            }
        }
    }

    @SneakyThrows
    private void sendDocument(Long chatId, String caption, InputFile file) {
        try {
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(String.valueOf(chatId));
            sendDocument.setCaption(caption);
            sendDocument.setDocument(file);
            execute(sendDocument);
            System.out.println("Отсылаем документ " + file.getAttachName() + " пользователю " + chatId);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Ошибка отправки документа");
        }
    }

    private void sendMsg(Long chatId, String text) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId.toString());
            sendMessage.setText(text);
            sendMessage.setParseMode("HTML");
            buttons.createButtons(sendMessage, buttonRegistrarion, buttonContact, buttonExample);
            execute(sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String getDate() {
        return String.valueOf(LocalDateTime.now()).substring(0,19).replace("T", " ");
    }
}
