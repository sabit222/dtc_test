package orderservice.repository;

import orderservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByName(String name);
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
}
