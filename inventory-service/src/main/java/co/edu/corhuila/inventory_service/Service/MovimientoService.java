package co.edu.corhuila.inventory_service.Service;


import co.edu.corhuila.inventory_service.Dto.MovimientoResponse;
import co.edu.corhuila.inventory_service.Entity.Movimiento;
import co.edu.corhuila.inventory_service.Entity.Producto;
import co.edu.corhuila.inventory_service.Entity.TipoMovimiento;
import co.edu.corhuila.inventory_service.Repository.MovimientoRepository;
import co.edu.corhuila.inventory_service.Repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;

    public MovimientoService(MovimientoRepository movimientoRepository,
                             ProductoRepository productoRepository) {
        this.movimientoRepository = movimientoRepository;

    }



    public List<MovimientoResponse> listarMovimientos() {
        return movimientoRepository.findAll()
                .stream()
                .map(MovimientoResponse::new)
                .toList();
    }


}
