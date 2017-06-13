package org.bot;

import com.vk.api.sdk.actions.Groups;
import com.vk.api.sdk.actions.Wall;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.queries.groups.GroupsGetByIdQuery;
import org.MainApp;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.MainApp.*;

/**
 * Created by killsett on 09.06.17.
 */

public class Lentach implements Runnable {
    private Logger logger = Logger.getLogger(Lentach.class);
    private static final String[] TEXTPOST = {"❕ ", ", ", "\n"};
    private String chatId;
    private String domain;
    private LentaVkBot lentaVkBot;
    private boolean check = true;
    private Integer lastId = -1;
    private String nameGroup;
    private ServiceActor serviceActor;
    private VkApiClient vkApiClient;
    private SimpleDateFormat dateFormatFull = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    private SimpleDateFormat dateFormatSmall = new SimpleDateFormat("HH:mm");

    Lentach(String chatId, String domain, LentaVkBot lentaVkBot) {
        this.chatId = chatId;
        this.domain = domain;
        this.lentaVkBot = lentaVkBot;
        init();
        new Thread(this).start();
    }

    private void init() {
        try {
            TransportClient transportClient = HttpTransportClient.getInstance();
            vkApiClient = new VkApiClient(transportClient);
            serviceActor = new ServiceActor(property.getVkontakteId(), property.getVkontakteSecretKey(), property.getVkontakteServiceKey());
            GroupsGetByIdQuery id = new Groups(vkApiClient).getById(serviceActor).groupId(domain);
            nameGroup = id.execute().get(0).getName();
            List<WallpostFull> posts = new Wall(vkApiClient).get(serviceActor).domain(domain).execute().getItems();
            lastId = Math.max(posts.get(0).getId(), posts.get(1).getId());

        } catch (ApiException | ClientException e) {
            logger.error("EX", e);
            e.printStackTrace();
        }
    }

    public String getChatId() {
        return chatId;
    }

    public String getDomain() {
        return domain;
    }

    public void stop() {
        check = false;
    }

    @Override
    public void run() {
        check = true;
        lastMessage();
        List<WallpostFull> posts = null;
        while (check) {
            try {
                posts = new Wall(vkApiClient).get(serviceActor).domain(domain).execute().getItems();
                 for (int index = 0; posts.size() > index; index++) {
                    if (lastId < posts.get(index).getId()) {
                        send(posts.get(index));
                    }
                }
                lastId = Math.max(lastId, Math.max(posts.get(0).getId(), posts.get(1).getId()));
                Thread.sleep(2000);

            } catch (ClientException | InterruptedException e) {
                logger.error("start: cl and int", e);
                e.printStackTrace();
            } catch (ApiException e) {
                logger.error("start: api", e);
                e.printStackTrace();
                if (e.getCode() == 100) {
                    check = false;
                    lentaVkBot.sendMsg(chatId, "Неправильно введен домен группы!");
                }
                break;
            }
        }
    }

//    public void start() {
//        check = true;
//        lastMessage();
//        Thread threadVkLenta = new Thread(() -> {
//            List<WallpostFull> posts = null;
//            while (check) {
//                try {
//                    posts = new Wall(vkApiClient).get(serviceActor).domain(domain).execute().getItems();
//                    for (int index = 0; posts.size() > index; index++) {
//                        if (lastId < posts.get(index).getId()) {
//                            send(posts.get(index));
//                        }
//                    }
//                    lastId = Math.max(lastId, Math.max(posts.get(0).getId(), posts.get(1).getId()));
//                    Thread.sleep(2000);
//                } catch (ClientException | InterruptedException e) {
//                    logger.error("start: cl and int", e);
//                    e.printStackTrace();
//                } catch (ApiException e) {
//                    logger.error("start: api", e);
//                    e.printStackTrace();
//                    if (e.getCode() == 100) {
//                        check = false;
//                        lentaVkBot.sendMsg(chatId, "Неправильно введен домен группы!");
//                    }
//                    break;
//                }
//            }
//        });
//        threadVkLenta.start();
//    }

    private void send(WallpostFull wallpostFull) {
        String message = convertToMessage(wallpostFull.getDate(), wallpostFull.getText());

        if (wallpostFull.getAttachments() == null) {
            lentaVkBot.sendMsg(chatId, message);
            return;
        }
        for (WallpostAttachment wallpostAttachment : wallpostFull.getAttachments()) {
            System.out.println(wallpostAttachment.getType());
            switch (wallpostAttachment.getType()) {
                case PHOTO:
                    message += "\n" + wallpostAttachment.getPhoto().getPhoto604();
                    break;
                case DOC:
                    if (wallpostAttachment.getDoc().getType() == 1)
                        lentaVkBot.sendDocument(chatId, loadFromURL(wallpostAttachment.getDoc().getUrl(), wallpostAttachment.getDoc().getTitle()));
                    else
                        lentaVkBot.sendVideo(chatId, loadFromURL(wallpostAttachment.getDoc().getUrl(), wallpostAttachment.getDoc().getTitle()));
                    break;

                case AUDIO:
                    message += "\n" + wallpostAttachment.getAudio().getUrl();
                    break;
            }
        }
        lentaVkBot.sendMsg(chatId, message);
        System.out.println(message);
    }

    public void lastMessage() {
        List<WallpostFull> posts = null;
        try {
            int i = 0;
            posts = new Wall(vkApiClient).get(serviceActor).domain(domain).execute().getItems();
            if (posts.size() > 1 && isFirstPost(posts)) i = 1;
            send(posts.get(i));
        } catch (ApiException | ClientException e) {
            logger.error("api or client", e);
            e.printStackTrace();
        }
    }

    long i = 0;

    private File loadFromURL(String urls) {
        return loadFromURL(urls, "temp" + i++);
    }

    private File loadFromURL(String urls, String name) {
        try {
            URL website = new URL(urls);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(i++ + name);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            return new File(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public void lastTenMessage() {
        List<WallpostFull> posts = null;
        try {
            int i = 0;
            posts = new Wall(vkApiClient).get(serviceActor).domain(domain).execute().getItems();
            if (posts.size() > 1 && isFirstPost(posts)) i = 1;
            int end = i + 10;
            while (i < end) {
                send(posts.get(i));
                i++;
            }
        } catch (ApiException | ClientException e) {
            logger.error("LastTenMessage", e);
            e.printStackTrace();
        }
    }

    private boolean isFirstPost(List<WallpostFull> posts) {
        return posts.get(0).getId() < posts.get(1).getId();
    }

    private String convertToMessage(Integer dateUnix, String text) {
        Date date = new Date((long) dateUnix * 1000);
        Date dateNatured = new Date();
        boolean check = false;
        if (
                dateNatured.getYear() == date.getYear() &&
                        dateNatured.getMonth() == date.getMonth() &&
                        dateNatured.getDay() == date.getDay()
                )
            check = true;
        return check ?
                TEXTPOST[0] + nameGroup + TEXTPOST[1] + dateFormatSmall.format(date) + TEXTPOST[2] + text :
                TEXTPOST[0] + nameGroup + TEXTPOST[1] + dateFormatFull.format(date) + TEXTPOST[2] + text;
    }


}
