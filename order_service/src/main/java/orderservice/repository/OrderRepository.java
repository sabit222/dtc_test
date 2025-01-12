package orderservice.repository;

import orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository  extends JpaRepository<Order, Integer> {

    List<Order> findByStatus(String status);
    List<Order> findByCustomerName(String customerName);

}
