package de.godcipher.antiac.hibernate;

import de.godcipher.antiac.hibernate.entity.LogEntry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

@Slf4j
public class HibernateUtil {

  @Getter private static SessionFactory sessionFactory;

  public static void setupHibernate() {
    try {
      StandardServiceRegistry registry = HibernateConfig.getHibernateConfiguration();
      MetadataSources sources = new MetadataSources(registry);
      sources.addAnnotatedClass(LogEntry.class);

      sessionFactory = sources.buildMetadata().buildSessionFactory();
    } catch (Exception e) {
      log.error("Initial SessionFactory creation failed", e);
      throw new ExceptionInInitializerError("Initial SessionFactory creation failed" + e);
    }
  }

  public static void shutdown() {
    getSessionFactory().close();
  }
}