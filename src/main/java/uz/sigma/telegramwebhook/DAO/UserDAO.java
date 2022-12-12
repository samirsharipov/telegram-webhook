package uz.sigma.telegramwebhook.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.sigma.telegramwebhook.entity.User;
import uz.sigma.telegramwebhook.repo.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserDAO {
    private final UserRepository repository;

    @Autowired
    public UserDAO(UserRepository tgUserRepository) {
        repository = tgUserRepository;
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public User findById(long id) {
        Optional<User> optionalUser = repository.findById(id);
        return optionalUser.orElseGet(User::new);
    }

    public void save(User tgUser) {
        repository.save(tgUser);
    }

    public boolean isExist(long id) {
        Optional<User> optionalUser = repository.findById(id);
        if (optionalUser.isEmpty()) {
            return false;
        }
        return true;
    }
}
