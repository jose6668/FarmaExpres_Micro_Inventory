package co.edu.corhuila.inventory_service.Controllers;



import co.edu.corhuila.inventory_service.Dto.MovimientoResponse;
import co.edu.corhuila.inventory_service.Service.MovimientoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movements")
public class MovimientoController {

    private final MovimientoService movimientoService;

    public MovimientoController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }



    @GetMapping
    public ResponseEntity<List<MovimientoResponse>> listarMovimientos() {
        return ResponseEntity.ok(movimientoService.listarMovimientos());
    }
}
