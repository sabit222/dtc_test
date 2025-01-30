package order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class UserDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String role;

    public UserDTO() {} // Пустой конструктор (нужен для Jackson)

    public UserDTO(String firstname, String email) {
        this.firstname = firstname;
        this.email = email;
    }

    // Геттеры и сеттеры
    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
