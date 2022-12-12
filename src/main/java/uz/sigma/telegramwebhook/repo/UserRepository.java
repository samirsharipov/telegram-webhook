package uz.sigma.telegramwebhook.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.sigma.telegramwebhook.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
