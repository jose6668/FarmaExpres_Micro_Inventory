package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Dto.ProductOutOfStockResponse;
import co.edu.corhuila.inventory_service.Entity.Motion;
import co.edu.corhuila.inventory_service.Entity.Product;
import co.edu.corhuila.inventory_service.Entity.MovementType;
import co.edu.corhuila.inventory_service.Repository.MotionRepository;
import co.edu.corhuila.inventory_service.Repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final MotionRepository motionRepository;

    public ProductService(ProductRepository productRepository,
                          MotionRepository motionRepository) {
        this.productRepository = productRepository;
        this.motionRepository = motionRepository;
    }

        // Método para crear un nuevo producto
        public Product createProduct(Product product) {
        validateProductData(product);

        if (productRepository.existsByCode(product.getCode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El código del producto ya existe"
            );
        }

        Product productSaved = productRepository.save(product);

        Motion motion = new Motion(
                MovementType.Entrance,
                productSaved.getStock(),
                productSaved
        );
        motionRepository.save(motion);

        return productSaved;
    }

    // Método para actualizar un producto existente
    @Transactional
    public Product updateProduct(Long id, Product updatedData) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));

        if (!product.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede modificar un producto eliminado"
            );
        }

        validateProductData(updatedData);

        Integer previousStock = product.getStock();

        product.setName(updatedData.getName());
        product.setUnitPrice(updatedData.getUnitPrice());
        product.setStock(updatedData.getStock());
        product.setMinimumStock(updatedData.getMinimumStock());
        product.setExpirationDate(updatedData.getExpirationDate());

        Product productSaved = productRepository.save(product);

        Integer newStock = updatedData.getStock();
        MovementType movementType;
        Integer quantityMovement;

        if (newStock > previousStock) {
            movementType = MovementType.Entrance;
            quantityMovement = newStock - previousStock;
        } else if (newStock < previousStock) {
            movementType = MovementType.Exit;
            quantityMovement = previousStock - newStock;
        } else {
            movementType = MovementType.Updated;
            quantityMovement = 0;
        }

        Motion motion = new Motion(
                movementType,
                quantityMovement,
                productSaved
        );
        motionRepository.save(motion);

        return productSaved;
    }

    // Método para eliminar un producto 
    @Transactional
    public void removeProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));

        product.setActive(false);
        productRepository.save(product);

        Motion motion = new Motion(
                MovementType.Deleted,
                product.getStock(),
                product
        );
        motionRepository.save(motion);
    }

    // Método para listar todos los productos
    public List<Product> listProducts() {
        return productRepository.findAll();
    }
    // Método para listar solo los productos activos

    public List<Product> listActiveProducts() {
        return productRepository.findByActiveTrue();
    }
    // Método para listar productos que estan vacios
    public List<ProductOutOfStockResponse> outOfStockProducts() {
        return productRepository.findByStockAndActiveTrue(0)
                .stream()
                .map(ProductOutOfStockResponse::new)
                .toList();
    }

        // Método para validar los datos del producto
    private void validateProductData(Product product) {
        if (product.getStock() == null || product.getStock() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El stock debe ser mayor o igual a 0"
            );
        }

        if (product.getMinimumStock() == null || product.getMinimumStock() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El stock mínimo debe ser mayor o igual a 0"
            );
        }
    }
}