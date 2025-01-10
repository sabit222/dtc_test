package userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
@Entity
@Table(name = "role")
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private ERole name;

    // Конструктор для создания роли с помощью Enum
    public Role(ERole name) {
        this.name = name;  // Устанавливаем значение для поля name
    }

    // Конструктор по умолчанию для JPA
    public Role() {
    }
}