package order_service.repository;

import order_service.model.Order;
import order_service.model.Status;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(Status status);

    @Query("SELECT o FROM Order o WHERE o.totalPrice BETWEEN :minPrice and :maxPrice")
    List<Order> findOrdersByPriceRange(@Param("minPrice")BigDecimal minPrice,
                                       @Param("maxPrice")BigDecimal maxPrice);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.totalPrice BETWEEN :minPrice AND :maxPrice")
    List<Order> findOrdersByStatusAndPriceRange(@Param("status") Status status,
                                                @Param("minPrice") BigDecimal minPrice,
                                                @Param("maxPrice") BigDecimal maxPrice);
}
