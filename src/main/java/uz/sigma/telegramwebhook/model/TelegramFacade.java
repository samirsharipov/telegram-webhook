package uz.sigma.telegramwebhook.model;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import uz.sigma.telegramwebhook.DAO.UserDAO;
import uz.sigma.telegramwebhook.service.MessageService;
import uz.sigma.telegramwebhook.entity.User;


@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TelegramFacade {

    final UserDAO userDAO;

    final MessageService service;

    @Value("${telegrambot.adminId}")
    private long admin_id;

    public TelegramFacade(UserDAO userDAO, MessageService service) {
        this.userDAO = userDAO;
        this.service = service;
    }


    public BotApiMethod<?> handleUpdate(Update update, ChatMember chatMember) {

        Message message = update.getMessage();

        if (update.hasCallbackQuery()) {

            if (!userDAO.isExist(update.getCallbackQuery().getFrom().getId())) {
                if (update.getCallbackQuery().getFrom().getId().equals(admin_id)) {
                    userDAO.save(new User(update.getCallbackQuery().getFrom().getId(), update.getCallbackQuery().getFrom().getId().toString(), BotState.ADMIN));
                } else {
                    userDAO.save(new User(update.getCallbackQuery().getFrom().getId(), update.getCallbackQuery().getFrom().getId().toString(), BotState.START));
                }
            }

            User user = userDAO.findById(update.getCallbackQuery().getFrom().getId());
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();

            if (data.equals("join")) {

                if (chatMember.getStatus().equals("member")) {
                    return service.showMenu(user);
                }
                return service.errorJoinChannel(update);
            }
        } else {

            if (!userDAO.isExist(message.getFrom().getId())) {
                if (message.getFrom().getId().equals(admin_id)) {
                    userDAO.save(new User(message.getFrom().getId(), update.getMessage().getChatId().toString(), BotState.ADMIN));
                } else {
                    userDAO.save(new User(message.getFrom().getId(), message.getChatId().toString(), BotState.START));
                }
            }

            User user = userDAO.findById(message.getFrom().getId());
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(message.getChatId()));


            if (message.hasText()) {
                String text = update.getMessage().getText();

                if (text.equals("/start")) {
                    if (chatMember.getStatus().equals("creator") || chatMember.getStatus().equals("administrator") || chatMember.getStatus().equals("member")) {
                        if (user.getState().equals(BotState.ADMIN)) {
                            return service.adminPanel(sendMessage, user);
                        } else if (user.isBlock()) {
                            return service.doneMessage(sendMessage, user);
                        } else {
                            return service.showMenu(user);
                        }
                    } else {
                        return service.joinChannel(update);
                    }
                } else if (text.equals("Ma'lumotlar") && user.getState().equals(BotState.ADMIN)) {
                    return service.statistics(sendMessage);
                }

                return switch (user.getState()) {
                    case (BotState.FIO) -> service.fio(sendMessage, user, text);
                    case (BotState.AGE) -> service.age(sendMessage, user, text);
                    case (BotState.WORKING_OR_STUDY) -> service.work(sendMessage, user, text);
                    case (BotState.PHONE_NUMBER) -> service.getContact(sendMessage, user, text, admin_id);
                    case (BotState.SEND) -> service.notification(sendMessage, user);
                    default -> service.error(sendMessage);
                };
            } else if (message.hasContact()) {
                if (user.getState().equals(BotState.PHONE_NUMBER)) {
                    return service.getShareContact(user, message.getContact(), admin_id);
                }
                return service.notification(sendMessage, user);
            }
        }
        return null;
    }
}
