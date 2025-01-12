package orderservice.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Integer productId;

    private String name;

    private Double price;

    private Integer quantity;
}
