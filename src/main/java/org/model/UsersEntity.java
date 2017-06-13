package org.model;

import javax.persistence.*;

/**
 * Created by killsett on 11.06.17.
 */
@Entity
@Table(name = "Users", schema = "main", catalog = "")
public class UsersEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;
    private String chatId;

    public UsersEntity() {
    }

    @Column(name = "id", nullable = false)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "ChatId", nullable = false, length = -1)
    public String getChatId() {
        return chatId;
    }

    public UsersEntity(long id, String chatId) {
        this.id = id;
        this.chatId = chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UsersEntity that = (UsersEntity) o;

        if (id != that.id) return false;
        if (chatId != null ? !chatId.equals(that.chatId) : that.chatId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (chatId != null ? chatId.hashCode() : 0);
        return result;
    }
}
