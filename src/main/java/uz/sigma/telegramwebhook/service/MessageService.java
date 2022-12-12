package uz.sigma.telegramwebhook.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.sigma.telegramwebhook.DAO.UserDAO;
import uz.sigma.telegramwebhook.entity.User;
import uz.sigma.telegramwebhook.model.BotState;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    final UserDAO userDAO;


    public MessageService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public BotApiMethod<?> joinChannel(Update update) {
        String chatId = getChatId(update);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("\uD83D\uDCCE *Salom botdan foydalanish uchun kanalga a'zo bo'ling\n *" +
                "\n ❗️ *Kanallarga a'zo bo'lgach, pastdagi* \"\uD83D\uDD14 *A'zo bo'ldim*\" *tugmasini bosing*");
        sendMessage.setReplyMarkup(generateMarkup());
        return sendMessage;
    }

    private ReplyKeyboard generateMarkup() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Jizzax yoshlari / rasmiy kanal \uD83C\uDDFA\uD83C\uDDFF");
        button.setUrl("https://t.me/mychannelforbotapplication");
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("\uD83D\uDD14 A'zo bo'ldim");
        button1.setCallbackData("join");
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(button1);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(row);
        rowList.add(row1);


        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private String getChatId(Update update) {
        Long chatId = update.getMessage().getChatId();
        return chatId.toString();
    }

    public BotApiMethod<?> showMenu(User user) {
        user.setState(BotState.FIO);
        userDAO.save(user);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("*Ism familyangizni kiriting*:");
        return sendMessage;
    }

    public BotApiMethod<?> fio(SendMessage sendMessage, User user, String text) {
        user.setFio(text);
        user.setState(BotState.AGE);
        userDAO.save(user);

        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("*Yoshingizni kiriting:*");
        return sendMessage;
    }

    public BotApiMethod<?> age(SendMessage sendMessage, User user, String text) {
        user.setAge(text);
        user.setState(BotState.WORKING_OR_STUDY);
        userDAO.save(user);

        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("*Ish yoki O'qish joyingizni kiriting:*");
        return sendMessage;
    }

    public BotApiMethod<?> work(SendMessage sendMessage, User user, String text) {
        user.setWork(text);
        user.setState(BotState.PHONE_NUMBER);
        userDAO.save(user);

        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("*Tel raqamingizni kiriting va keyin botni qayta ishga tushiring:*");
        sendMessage.setReplyMarkup(generateReplyKeyboardMarkup(user));
        return sendMessage;
    }

    private ReplyKeyboard generateReplyKeyboardMarkup(User user) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(false);
        KeyboardButton button = new KeyboardButton();
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();

        if (user.getState().equals(BotState.PHONE_NUMBER)) {
            button.setText("Jo'natish");
            button.setRequestContact(true);
            keyboardRow.add(button);
            keyboardRowList.add(keyboardRow);
            markup.setKeyboard(keyboardRowList);
        } else if (user.getState().equals(BotState.ADMIN)) {
            button.setText("Ma'lumotlar");
            keyboardRow.add(button);
            keyboardRowList.add(keyboardRow);
            markup.setKeyboard(keyboardRowList);
        }
        return markup;
    }

    public BotApiMethod<?> getContact(SendMessage sendMessage, User user, String text, long admin_id) {

        String number = text;

        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setChatId(user.getChatId());

        boolean b = number.startsWith("+");

        if (b) {
            number = number.substring(1, number.length() - 1);
        }
        boolean isNumeric = number.chars().allMatch(Character::isDigit);

        if (isNumeric) {
            user.setPhoneNumber(number);
            user.setState(BotState.SEND);
            user.setBlock(true);
            userDAO.save(user);
            return sendMessageAdmin(user, admin_id);
        } else {
            user.setState(BotState.PHONE_NUMBER);
            sendMessage.setText("*Iltimos telefon raqamini to'gri kiriting*");
            userDAO.save(user);
            return sendMessage;
        }
    }

    public BotApiMethod<?> sendMessageAdmin(User user, long admin_id) {
        User admin = userDAO.findById(admin_id);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(admin.getChatId());

        sendMessage.setText("FIO: " + user.getFio() + " \n"
                + "Yosh: " + user.getAge() + " \n"
                + "Ish yoki O'qish joy: " + user.getWork() + " \n"
                + "Telefon raqam: " + user.getPhoneNumber());
        return sendMessage;
    }

    public BotApiMethod<?> notification(SendMessage sendMessage, User user) {
        user.setState(BotState.NOTIFICATION);
        user.setBlock(true);
        userDAO.save(user);

        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("✅ *Arizangiz qabul qilindi* ");
        return sendMessage;
    }

    public BotApiMethod<?> error(SendMessage sendMessage) {
        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("❗*Bot sizga hizmat ko'rsatishni yakunlagan*");
        return sendMessage;
    }

    public BotApiMethod<?> doneMessage(SendMessage sendMessage, User user) {
        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("✅ *Siz allaqachon ro'yxatdan o'tib bo'ldingiz*");
        return sendMessage;
    }

    public BotApiMethod<?> adminPanel(SendMessage sendMessage, User user) {
        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("*Foydalanuvchilar ro'yhatini ko'rish uchun 'Ma'lumotlar' tugmasini bosing*");
        sendMessage.setReplyMarkup(generateReplyKeyboardMarkup(user));
        return sendMessage;
    }

    public BotApiMethod<?> statistics(SendMessage sendMessage) {
        List<User> all = userDAO.findAll();
        int size = all.size();

        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("*Foydalanuvchilar soni:* " + (size - 1) + " *ta*");
        return sendMessage;
    }


    private String checkPhoneNumber(String phoneNumber) {
        return phoneNumber.startsWith("+") ? phoneNumber : "+" + phoneNumber;
    }

    public BotApiMethod<?> getShareContact(User user, Contact contact, long admin_id) {
        String number = checkPhoneNumber(contact.getPhoneNumber());

        user.setPhoneNumber(number);
        user.setState(BotState.SEND);
        user.setBlock(true);
        userDAO.save(user);

        return sendMessageAdmin(user, admin_id);
    }

    public BotApiMethod<?> errorJoinChannel(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getCallbackQuery().getFrom().getId().toString());
        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText("️❗*Siz hali ham kanalga a'zo bo'lmadingiz*");
        return sendMessage;
    }
}
