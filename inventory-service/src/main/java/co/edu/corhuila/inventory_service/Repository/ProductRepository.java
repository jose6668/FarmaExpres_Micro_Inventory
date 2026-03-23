package co.edu.corhuila.inventory_service.Repository;

import co.edu.corhuila.inventory_service.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByCode(String code);

    List<Product> findAll();
    
    List<Product> findByActiveTrue();

    List<Product> findByStockAndActiveTrue(Integer stock);
}
