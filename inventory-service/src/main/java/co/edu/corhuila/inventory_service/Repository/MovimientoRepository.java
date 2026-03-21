package co.edu.corhuila.inventory_service.Repository;


import co.edu.corhuila.inventory_service.Entity.Movimiento;
import co.edu.corhuila.inventory_service.Entity.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {



    List<Movimiento> findByTipo(TipoMovimiento tipo);
}
