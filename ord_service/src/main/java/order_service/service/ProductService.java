package order_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import order_service.dto.ProductDTO;
import order_service.model.Product;
import order_service.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    /**
     * Получить список всех продуктов.
     */
    public List<ProductDTO> getAllProducts(){
        return productRepository.findAll()
                .stream()
                .map(this::toProductDTO)
                .collect(Collectors.toList());
    }
    /**
     * Получить продукт по ID.
     */
    public ProductDTO getProductById(Long productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new RuntimeException("Product not found with ID: " + productId));
        return toProductDTO(product);
    }

    /**
     * Создать новый продукт.
     */
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO){
        Product product = toProductEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        return toProductDTO(savedProduct);
    }
    /**
     * Обновить существующий продукт.
     */
    @Transactional
    public ProductDTO updateProduct(Long productId, ProductDTO updateProductDTO){
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        // Обновить поля продукта
        existingProduct.setName(updateProductDTO.getName());
        existingProduct.setPrice(updateProductDTO.getPrice());
        existingProduct.setQuantity(updateProductDTO.getQuantity());

        Product updatedProduct = productRepository.save(existingProduct);
        return toProductDTO(updatedProduct);
    }

    /**
     * Удалить продукт.
     */
    @Transactional
    public void deleteProduct(Long productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
        productRepository.delete(product);
    }

    /**
     * Преобразование Product в ProductDTO.
     */
    private ProductDTO toProductDTO(Product product){
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        return dto;
    }
    /**
     * Преобразование ProductDTO в Product.
     */
    private Product toProductEntity(ProductDTO dto){
        Product product = new Product();
        product.setProductId(dto.getProductId());
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        return product;
    }
}
