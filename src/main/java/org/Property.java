package org;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by killsett on 13.06.17.
 */
public class Property {
    private String vkontakteId;
    private String vkontakteSecretKey;
    private String vkontakteServiceKey;
    private String telegramBotName;
    private String telegramApiKey;

    public Property() {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream("config.properties");

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            vkontakteId = prop.getProperty("VkontakteId");
            vkontakteSecretKey= prop.getProperty("VkontakteSecretKey");
            vkontakteServiceKey = prop.getProperty("VkontakteServiceKey");
            telegramBotName = prop.getProperty("TelegramBotName");
            telegramApiKey = prop.getProperty("TelegramApiKey");

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Integer getVkontakteId() {
        return Integer.valueOf(vkontakteId);
    }

    public String getVkontakteSecretKey() {
        return vkontakteSecretKey;
    }

    public String getVkontakteServiceKey() {
        return vkontakteServiceKey;
    }

    public String getTelegramBotName() {
        return telegramBotName;
    }

    public String getTelegramApiKey() {
        return telegramApiKey;
    }
}
