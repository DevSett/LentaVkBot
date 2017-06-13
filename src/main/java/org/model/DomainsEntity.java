package org.model;

import javax.persistence.*;

/**
 * Created by killsett on 11.06.17.
 */
@Entity
@Table(name = "Domains", schema = "main", catalog = "")
public class DomainsEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;
    private long userId;
    private String domain;

    public DomainsEntity() {
    }

    @Column(name = "ID", nullable = false)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "UserId", nullable = false)
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "Domain", nullable = false, length = -1)
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public DomainsEntity(long id, long userId, String domain) {
        this.id = id;
        this.userId = userId;
        this.domain = domain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DomainsEntity that = (DomainsEntity) o;

        if (id != that.id) return false;
        if (userId != that.userId) return false;
        if (domain != null ? !domain.equals(that.domain) : that.domain != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (userId ^ (userId >>> 32));
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        return result;
    }
}
