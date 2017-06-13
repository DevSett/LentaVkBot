package org;

import org.apache.log4j.Logger;
import org.bot.LentaVkBot;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.unit.DatabaseService;

/**
 * Created by Wojcimierz on 09.12.2016.
 */
public class MainApp {
    private static final SessionFactory ourSessionFactory;
    private static Logger logger = Logger.getLogger(MainApp.class);
    private static LentaVkBot lentaVkBot;
    public static DatabaseService serviceSingelton;
    public static final Property property = new Property();
    static {
        try {
            Configuration configuration = new Configuration();
            configuration.configure();

            ourSessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Session getSession() throws HibernateException {
        return ourSessionFactory.openSession();
    }

    public static void main(final String[] args) throws Exception {
        final Session session = getSession();
        serviceSingelton = new DatabaseService(session);
        Thread threadBotVk = new Thread(() -> {
            ApiContextInitializer.init();
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
            try {
                telegramBotsApi.registerBot(lentaVkBot = new LentaVkBot());
            } catch (TelegramApiException e) {
                e.printStackTrace();
                logger.error("Error TelegramApi: ", e);
            }
        });
        threadBotVk.start();
    }
}