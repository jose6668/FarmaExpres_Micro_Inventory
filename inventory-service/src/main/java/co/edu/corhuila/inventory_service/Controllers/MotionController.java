package co.edu.corhuila.inventory_service.Controllers;



import co.edu.corhuila.inventory_service.Dto.MotionResponse;
import co.edu.corhuila.inventory_service.Service.MotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/Motion")
public class MotionController {

    private final MotionService motionService;

    public MotionController(MotionService motionService) {
        this.motionService = motionService;
    }



    @GetMapping
    public ResponseEntity<List<MotionResponse>> listMotion() {
        return ResponseEntity.ok(motionService.listMotion());
    }
}
