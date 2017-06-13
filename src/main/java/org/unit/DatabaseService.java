package org.unit;

import org.hibernate.query.Query;
import org.hibernate.Session;
import org.model.DomainsEntity;
import org.model.UsersEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by killsett on 11.06.17.
 */
public class DatabaseService {
    private Session session;

    public DatabaseService(Session session) {
        this.session = session;
    }

    public Long count(String tableName) {

        Query q = session.createQuery("select count(*) from " + tableName);
        return (Long) q.getSingleResult();

    }


    public boolean isEmpty(String tableName) {
        Query q = session.createQuery("select count(*) from " + tableName);
        return (Long) q.getSingleResult() == 0;

    }

    public List list(String tableName) {
        Query q = session.createQuery("select i from " + tableName + " i");
        return q.getResultList();
    }

    public Object update(Object object) {
        session.beginTransaction();
        Object ob = session.merge(object);
        session.getTransaction().commit();
        return ob;
    }


    public void create(Object object) {
        session.beginTransaction();
        session.persist(object);
        session.getTransaction().commit();
    }

    public void remove(Long id) {
        session.beginTransaction();
        session.remove(id);
        session.getTransaction().commit();
    }

    public Object find(Class classEntity, Long id) {
        Object object = session.find(classEntity, id);
        return object;
    }

    public Object find(String tableName, String chatId) {
        Query q = session.createQuery("select i from " + tableName + " i");
        List<UsersEntity> list = q.getResultList();
        for (UsersEntity usersEntity : list) {
            if (isEquals(usersEntity.getChatId(), chatId)) {
                return usersEntity;
            }
        }
        return null;
    }

    public boolean isExistsUser(String chatId) {
        Query q = session.createQuery("select chatId from UsersEntity ");
        List<String> list = q.getResultList();
        for (String s : list) {
            if (isEquals(s, chatId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isExistsDomain(long id, String domain) {
        List<DomainsEntity> list = list("DomainsEntity");
        for (DomainsEntity domainsEntity : list) {
            if (domainsEntity.getUserId() == id && isEquals(domainsEntity.getDomain(), domain)) {
                return true;
            }
        }
        return false;
    }

    public void removeDomains(String chatId, String domainIs) {
        session.beginTransaction();
        List<UsersEntity> listUsers = list("UsersEntity");
        List<DomainsEntity> listDomains = list("DomainsEntity");

        List<Long> idUsers = new ArrayList<>();
        for (UsersEntity usersEntity : listUsers) {
            if (isEquals(usersEntity.getChatId(), chatId)) {
                idUsers.add(usersEntity.getId());
            }
        }
        for (DomainsEntity domain : listDomains) {
            for (Long idUser : idUsers) {
                if (idUser.equals(domain.getUserId())) {
                    if (domainIs != null) {
                        if (isEquals(domainIs, domain.getDomain())) session.remove(domain);

                    } else {
                        session.remove(domain);
                    }
                }
            }
        }
        session.getTransaction().commit();
    }

    private boolean isEquals(String domainIs, String domain2) {
        if (domainIs == null || domain2 == null) return false;
        return domainIs.equals(domain2);
    }


    public String getListDomains(String chatId) {
        String returntedL = "Список подписок:\n";
        int i = 1;
        List<UsersEntity> usersEntities = list("UsersEntity");
        Long id = -1l;
        for (UsersEntity usersEntity : usersEntities) {
            if (usersEntity.getChatId().equals(chatId)) {
                id = usersEntity.getId();
            }
        }
        if (id == -1) return returntedL;

        List<DomainsEntity> domainsEntities = list("DomainsEntity");
        for (DomainsEntity domainsEntity : domainsEntities) {
            if (domainsEntity.getUserId() == id) {
                returntedL += i++ + ". " + domainsEntity.getDomain() + "\n";
            }
        }
        return returntedL;
    }
}
