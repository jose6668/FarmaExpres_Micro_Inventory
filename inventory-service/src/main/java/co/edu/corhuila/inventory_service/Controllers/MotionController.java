package co.edu.corhuila.inventory_service.Controllers;



import co.edu.corhuila.inventory_service.Dto.FefoConsumeRequest;
import co.edu.corhuila.inventory_service.Dto.InventoryEntryRequest;
import co.edu.corhuila.inventory_service.Dto.InventoryEntryResponse;
import co.edu.corhuila.inventory_service.Dto.InventoryExitRequest;
import co.edu.corhuila.inventory_service.Dto.MotionResponse;
import co.edu.corhuila.inventory_service.Dto.MovementExecutionResponse;
import co.edu.corhuila.inventory_service.Dto.MovementRequest;
import co.edu.corhuila.inventory_service.Dto.UserActivityReportResponse;
import co.edu.corhuila.inventory_service.Service.MotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MotionController {

    private final MotionService motionService;

    public MotionController(MotionService motionService) {
        this.motionService = motionService;
    }



    @GetMapping({"/api/movements", "/api/motions", "/api/Motion"})
    public ResponseEntity<List<MotionResponse>> listMotion() {
        return ResponseEntity.ok(motionService.listMotion());
    }

    @GetMapping("/api/movements/filter-by-user")
    public ResponseEntity<List<MotionResponse>> listMotionByUser(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(motionService.listMotionByUser(userId));
    }

    @GetMapping("/api/movements/entrance")
    public ResponseEntity<List<MotionResponse>> listEntranceMotion() {
        return ResponseEntity.ok(motionService.listEntranceMotion());
    }

    @GetMapping("/api/movements/exit")
    public ResponseEntity<List<MotionResponse>> listExitMotion() {
        return ResponseEntity.ok(motionService.listExitMotion());
    }

    @GetMapping("/api/movements/updated")
    public ResponseEntity<List<MotionResponse>> listUpdatedMotion() {
        return ResponseEntity.ok(motionService.listUpdatedMotion());
    }

    @GetMapping("/api/movements/report/users-activity")
    public ResponseEntity<List<UserActivityReportResponse>> listUsersActivityReport(
            @RequestParam(required = false) String role) {
        return ResponseEntity.ok(motionService.listUsersActivityReport(role));
    }

    @PostMapping("/api/movements")
    public ResponseEntity<MovementExecutionResponse> createMovement(@RequestBody MovementRequest request) {
        return ResponseEntity.ok(motionService.createMovement(request));
    }

    @PostMapping("/api/movements/entries")
    public ResponseEntity<InventoryEntryResponse> registerInventoryEntry(@RequestBody InventoryEntryRequest request) {
        return ResponseEntity.ok(motionService.registerInventoryEntry(request));
    }

    @PostMapping("/api/movements/exits")
    public ResponseEntity<MovementExecutionResponse> registerInventoryExit(@RequestBody InventoryExitRequest request) {
        return ResponseEntity.ok(motionService.registerInventoryExit(request));
    }

    @PostMapping("/api/movements/consume-fefo")
    public ResponseEntity<MovementExecutionResponse> consumeFefo(@RequestBody FefoConsumeRequest request) {
        return ResponseEntity.ok(motionService.consumeFefo(request));
    }
}
