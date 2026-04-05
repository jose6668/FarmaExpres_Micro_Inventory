package co.edu.corhuila.inventory_service.Controllers;

import co.edu.corhuila.inventory_service.Dto.InventoryBatchReportItemResponse;
import co.edu.corhuila.inventory_service.Dto.MovementBatchReportItemResponse;
import co.edu.corhuila.inventory_service.Service.BatchService;
import co.edu.corhuila.inventory_service.Service.MotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReportController {
    private final BatchService batchService;
    private final MotionService motionService;

    public ReportController(BatchService batchService, MotionService motionService) {
        this.batchService = batchService;
        this.motionService = motionService;
    }

    @GetMapping("/api/reports/inventory-batches")
    public ResponseEntity<List<InventoryBatchReportItemResponse>> listInventoryBatchesReport() {
        return ResponseEntity.ok(batchService.listInventoryBatchReport());
    }

    @GetMapping("/api/reports/movements-batches")
    public ResponseEntity<List<MovementBatchReportItemResponse>> listMovementsBatchesReport() {
        return ResponseEntity.ok(motionService.listMovementBatchReport());
    }
}

