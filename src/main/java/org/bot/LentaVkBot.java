package org.bot;

import org.apache.log4j.Logger;
import org.model.DomainsEntity;
import org.model.UsersEntity;
import org.telegram.telegrambots.api.methods.send.*;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.MainApp.property;
import static org.MainApp.serviceSingelton;

/**
 * Created by killsett on 09.06.2017.
 */
public class LentaVkBot extends TelegramLongPollingBot {
    private Logger logger = Logger.getLogger(LentaVkBot.class);
    private static final String TEXT_HELP = "Список доступных команд: \n/bind \"имя домена группы\" - привязка к группе вконтакте.\n" +
            "/unbindall - остановить все уведомления.\n" +
            "/unbind \"название домена группы\" - Остановить уведомление от определенной группы.\n" +
            "/last - получение последней новости от всех подписанных групп.\n" +
            "/lastten \"название домена группы\" - Последние десять новостей группы.\n" +
            "/help - список команд.\n" +
            "/list - список подписок.";
    private static final String TEXT_START = "Привет я Лента вк и могу тебя уведомлять о новых новстях в твоей любимой группе!";
    private static final String TEXT_NOTIFY_ALL = "Все уведомления приостоновлены!";
    private static final String TEXT_NOTIFY = "Уведомления приостоновлены!";

    private List<Lentach> lentaches = new ArrayList<>();

    public LentaVkBot() {
        List<DomainsEntity> listD = serviceSingelton.list("DomainsEntity");
        List<UsersEntity> listU = serviceSingelton.list("UsersEntity");

        for (DomainsEntity domainsEntity : listD) {
            for (UsersEntity usersEntity : listU) {
                if (usersEntity.getId() == domainsEntity.getUserId()) {
                    connectVK(usersEntity.getChatId(), "/bind "+domainsEntity.getDomain());
                }
            }
        }
    }

    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (!serviceSingelton.isExistsUser(message.getChatId().toString())) {
            UsersEntity usersEntity = new UsersEntity();
            usersEntity.setChatId(message.getChatId().toString());
            serviceSingelton.create(usersEntity);
        }
        if (message != null && message.hasText()) {
            String text = message.getText();
            int space = -1;
            Commands commands = Commands.UNCNOWN;
            if ((space = text.indexOf(' ')) != -1) {
                text = text.substring(0, space);
            }
            for (Commands commands1 : Commands.values()) {
                if (text.equals(commands1.getCommand()))
                    commands = commands1;
            }
            switch (commands) {
                case START:
                    sendMsg(message.getChatId().toString(), TEXT_START);
                    break;
                case HELP:
                    sendMsg(message.getChatId().toString(), TEXT_HELP);
                    break;
                case CONNECT:
                    connectVK(message.getChatId().toString(), message.getText());
                    break;
                case LASTTEN:
                    lastTenVK(message.getChatId().toString(), message.getText());
                    break;
                case LAST:
                    lastVK(message.getChatId().toString());
                    break;
                case STOPALL:
                    stopVK(message.getChatId().toString(), message.getText(), true);
                    sendMsg(message.getChatId().toString(), TEXT_NOTIFY_ALL);
                    break;
                case STOP:
                    stopVK(message.getChatId().toString(), message.getText(), false);
                    sendMsg(message.getChatId().toString(), TEXT_NOTIFY);
                    break;
                case LIST:
                    sendMsg(message.getChatId().toString(),serviceSingelton.getListDomains(message.getChatId().toString()));
                    break;
                default:
                    sendMsg(message.getChatId().toString(), TEXT_HELP);
            }

        }

    }

    private void lastVK(String chatId) {
        for (int i = 0; i < lentaches.size(); i++) {
            if (lentaches.get(i).getChatId().equals(chatId)) {
                lentaches.get(i).lastMessage();
            }
        }
    }

    private void lastTenVK(String chatId, String text) {
        String domain = text.substring(text.indexOf(' ') + 1);
        for (int i = 0; i < lentaches.size(); i++) {
            if (lentaches.get(i).getChatId().equals(chatId) &&
                    lentaches.get(i).getDomain().equals(domain)) {
                lentaches.get(i).lastTenMessage();
            }
        }
    }

    private void stopVK(String chatId, String text, boolean isStoppedAll) {
        for (int i = 0; i < lentaches.size(); i++) {

            if (lentaches.get(i).getChatId().equals(chatId)) {
                if (!isStoppedAll) {
                    String domain = text.substring(text.indexOf(' ') + 1);
                    if (lentaches.get(i).getDomain().equals(domain)) {
                        serviceSingelton.removeDomains(chatId, domain);
                        lentaches.get(i).stop();
                        lentaches.remove(i);

                        stopVK(chatId, text, false);
                        break;
                    }
                } else {
                    lentaches.get(i).stop();
                    lentaches.remove(i);
                    serviceSingelton.removeDomains(chatId, null);
                    stopVK(chatId, text, true);
                    break;
                }

            }
        }
    }


    public String getBotUsername() {
        return property.getTelegramBotName();
    }

    public String getBotToken() {
        return property.getTelegramApiKey();
    }

    public void sendMsg(String chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.enableHtml(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("sndMsg", e);
            e.printStackTrace();
        }
    }

    public void sendPhoto(String chatId, File file) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setNewPhoto(file);
        try {
            sendPhoto(sendPhoto);
        } catch (TelegramApiException e) {
            logger.error("sndPhotos", e);
            e.printStackTrace();
        }
    }

    public void sendDocument(String chatId, File file) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setNewDocument(file);
        try {
            sendDocument(sendDocument);
        } catch (TelegramApiException e) {
            logger.error("sndDocument", e);
            e.printStackTrace();
        }
    }

    public void sendVideo(String chatId, File file) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chatId);
        sendVideo.setNewVideo(file);
        try {
            sendVideo(sendVideo);
        } catch (TelegramApiException e) {
            logger.error("sndVideo", e);
            e.printStackTrace();
        }
    }

    public void sendAudio(String chatId, File file) {
        SendAudio sendAudio = new SendAudio();
        sendAudio.setChatId(chatId);
        sendAudio.setNewAudio(file);
        try {
            sendAudio(sendAudio);
        } catch (TelegramApiException e) {
            logger.error("sndAudio", e);
            e.printStackTrace();
        }
    }


    public void connectVK(String chatId, String text) {
        if (!text.contains(" ")) return;
        String domain = text.substring(text.indexOf(' ') + 1);
        boolean check = true;
        for (int i = 0; i < lentaches.size(); i++) {
            if (chatId.equals(lentaches.get(i).getChatId()) &&
                    domain.equals(lentaches.get(i).getDomain())) {
                check = false;
                lentaches.get(i).start();
            }
        }
        if (check) {
            UsersEntity usersEntity = (UsersEntity) serviceSingelton.find("UsersEntity", chatId);
            if (!serviceSingelton.isExistsDomain(usersEntity.getId(), domain)) {
                DomainsEntity domainsEntity = new DomainsEntity();
                domainsEntity.setUserId(usersEntity.getId());
                domainsEntity.setDomain(domain);
                serviceSingelton.create(domainsEntity);
            }
            lentaches.add(new Lentach(chatId, domain, this));
            lentaches.get(lentaches.size() - 1).start();
        }
    }
}