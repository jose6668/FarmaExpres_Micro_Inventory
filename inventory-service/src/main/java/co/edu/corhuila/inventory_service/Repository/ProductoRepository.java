package co.edu.corhuila.inventory_service.Repository;

import co.edu.corhuila.inventory_service.Entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByCodigo(String codigo);

    List<Producto> findAll();

    List<Producto> findByActivoTrue();

    List<Producto> findByStockAndActivoTrue(Integer stock);

}
