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

    public Product createProduct(Product product) {

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

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));
    }


    @Transactional
    public Product updateProduct(Long id, Product Updateddata) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));

        Integer Previousstock = product.getStock();
        // Actualizar datos
        product.setName(Updateddata.getName());
        product.setUnitPrice(Updateddata.getUnitPrice());
        product.setStock(Updateddata.getStock());
        Product productSaved = productRepository.save(product);
        // Determinar tipo de movimiento
        Integer Newstock = Updateddata.getStock();
        MovementType movementType;
        Integer quantityMovement;
        if (!product.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede modificar un producto eliminado");

        }else if (Newstock > Previousstock ) {
            movementType = MovementType.Entrance;
            quantityMovement = Newstock - Previousstock;
        } else if (Newstock < Previousstock) {
            movementType = MovementType.Exit;
            quantityMovement = Previousstock - Newstock;
        } else {
            movementType = MovementType.Updated;
            quantityMovement = 0;
        }

        // Registrar movimiento
        Motion motion = new Motion(
                movementType,
                quantityMovement,
                productSaved
        );

        motionRepository.save(motion);

        return productSaved;
    }

    @Transactional
    public void removeProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));

        product.setActive(false);
        productRepository.save(product);

        // Registrar movimiento de eliminación
        Motion motion = new Motion(
                MovementType.Deleted,
                product.getStock(),
                product
        );

        motionRepository.save(motion);

    }

    public List<Product> listProducts() {
        return productRepository.findAll();

    }
    
    public List<Product> listActiveProducts() {
    return productRepository.findByActiveTrue();
    }

    public List<ProductOutOfStockResponse> outOfStockProducts() {
    return productRepository.findByStockAndActiveTrue(0)
            .stream()
            .map(ProductOutOfStockResponse::new)
            .toList();
    }

   

}

