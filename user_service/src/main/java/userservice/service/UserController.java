package userservice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import userservice.model.User;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Получение всех пользователей
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Получение пользователя по имени
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("User with name " + username + " not found"));
    }

    // Создание нового пользователя
    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        try {
            userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Удаление пользователя по имени
    @DeleteMapping("/delete/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
        try {
            userService.deleteUser(email); // Вызываем метод сервиса для удаления пользователя
            return ResponseEntity.ok().build(); // Возвращаем статус 200 OK
        } catch (Exception e) {
            // В случае ошибки возвращаем статус 400 или другой, в зависимости от ошибки
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Получение пользователя по email
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        try {
            User user = userService.getUserByEmail(email);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}