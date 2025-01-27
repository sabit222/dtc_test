package order_service.controller;

import lombok.RequiredArgsConstructor;
import order_service.dto.ProductDTO;
import order_service.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Получить список всех продуктов.
     */
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(){
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Получить продукт по ID.
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId){
        ProductDTO product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }
    /**
     * Создать новый продукт.
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO){
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.ok(createdProduct);
    }
    /**
     * Обновить существующий продукт.
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductDTO updatedProductDTO
    ){
        ProductDTO updatedProduct = productService.updateProduct(productId, updatedProductDTO);
        return ResponseEntity.ok(updatedProduct);
    }
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId){
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}
