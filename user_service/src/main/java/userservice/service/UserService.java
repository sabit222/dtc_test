package userservice.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import userservice.model.User;
import userservice.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Получение всех пользователей
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    // Получение пользователя по имени с использованием Optional
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Сохранение пользователя
    public void createUser(User user) {
        if (user.getEmail() == null) {
            throw new IllegalArgumentException("Email must not be null");
        }
        // Если username отсутствует, сгенерировать его
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            user.setUsername(generateUsername(user.getEmail()));
        }
        userRepository.save(user);
    }

    // Метод для генерации username
    private String generateUsername(String email) {
        // Пример генерации: использовать часть email до @
        return email.split("@")[0] + "_" + System.currentTimeMillis();
    }

    // Удаление пользователя по имени
    @Transactional
    public void deleteUser(String email){
        userRepository.deleteByEmail(email);
    }

    // Реализация метода findByName
    public User findByName(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь с именем " + username + " не найден"));
    }
    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}
