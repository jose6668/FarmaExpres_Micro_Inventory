package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Dto.ProductoSinStockResponse;
import co.edu.corhuila.inventory_service.Entity.Movimiento;
import co.edu.corhuila.inventory_service.Entity.Producto;
import co.edu.corhuila.inventory_service.Entity.TipoMovimiento;
import co.edu.corhuila.inventory_service.Repository.MovimientoRepository;
import co.edu.corhuila.inventory_service.Repository.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final MovimientoRepository movimientoRepository;

    public ProductoService(ProductoRepository productoRepository,
                           MovimientoRepository movimientoRepository) {
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    public Producto crearProducto(Producto producto) {

        if (productoRepository.existsByCodigo(producto.getCodigo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El código del producto ya existe"
            );
        }

        Producto productoGuardado = productoRepository.save(producto);

        Movimiento movimiento = new Movimiento(
                TipoMovimiento.ENTRADA,
                productoGuardado.getStock(),
                productoGuardado
        );

        movimientoRepository.save(movimiento);

        return productoGuardado;
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));
    }


    @Transactional
    public Producto actualizarProducto(Long id, Producto datosActualizados) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));

        Integer stockAnterior = producto.getStock();
        // Actualizar datos
        producto.setNombre(datosActualizados.getNombre());
        producto.setPrecio(datosActualizados.getPrecio());
        producto.setStock(datosActualizados.getStock());
        Producto productoGuardado = productoRepository.save(producto);
        // Determinar tipo de movimiento
        Integer stockNuevo = datosActualizados.getStock();
        TipoMovimiento tipoMovimiento;
        Integer cantidadMovimiento;
        if (!producto.isActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede modificar un producto eliminado");

        }else if (stockNuevo > stockAnterior) {
            tipoMovimiento = TipoMovimiento.ENTRADA;
            cantidadMovimiento = stockNuevo - stockAnterior;
        } else if (stockNuevo < stockAnterior) {
            tipoMovimiento = TipoMovimiento.SALIDA;
            cantidadMovimiento = stockAnterior - stockNuevo;
        } else {
            tipoMovimiento = TipoMovimiento.ACTUALIZADO;
            cantidadMovimiento = 0;
        }

        // Registrar movimiento
        Movimiento movimiento = new Movimiento(
                tipoMovimiento,
                cantidadMovimiento,
                productoGuardado
        );

        movimientoRepository.save(movimiento);

        return productoGuardado;
    }

    @Transactional
    public void eliminarProducto(Long id) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));

        producto.setActivo(false);
        productoRepository.save(producto);

        // Registrar movimiento de eliminación
        Movimiento movimiento = new Movimiento(
                TipoMovimiento.ELIMINACION,
                producto.getStock(),
                producto
        );

        movimientoRepository.save(movimiento);

    }

    public List<Producto> listarProductos() {
        return productoRepository.findAll();

    }


    public List<Producto> listarProductosActivos() {
        return productoRepository.findByActivoTrue();

    }

    public List<ProductoSinStockResponse> productosSinStock() {

        return productoRepository.findByStockAndActivoTrue(0)
                .stream()
                .map(ProductoSinStockResponse::new)
                .toList();
    }

}

