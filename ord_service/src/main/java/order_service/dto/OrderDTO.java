package order_service.dto;

import lombok.Data;
import order_service.model.Status;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDTO {
    private String customerName;
    private Status status;
    private BigDecimal totalPrice;
    private List<ProductDTO> products;
}
