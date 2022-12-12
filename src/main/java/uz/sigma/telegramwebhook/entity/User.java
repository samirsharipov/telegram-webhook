package uz.sigma.telegramwebhook.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;


@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@Entity
public class User {
    @Id
    private Long id;

    private String chatId;

    private String username;

    private String fio;

    private String age;

    private String phoneNumber;

    private String state;

    private String work;

    private String application;

    private boolean isBlock;

    private boolean isAdmin;

    public User(Long id, String chatId, String state) {
        this.id = id;
        this.chatId = chatId;
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User tgUser = (User) o;
        return id != null && Objects.equals(id, tgUser.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
