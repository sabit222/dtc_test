package order_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long productId;
    private String name;
    private BigDecimal price;
    private Integer quantity;
}
