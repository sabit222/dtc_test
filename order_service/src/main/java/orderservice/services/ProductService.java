package orderservice.services;

import lombok.RequiredArgsConstructor;
import orderservice.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

}
