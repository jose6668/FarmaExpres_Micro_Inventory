package co.edu.corhuila.inventory_service.Controllers;

import co.edu.corhuila.inventory_service.Dto.ActiveInventorySummaryResponse;
import co.edu.corhuila.inventory_service.Dto.ActiveInventoryTableItemResponse;
import co.edu.corhuila.inventory_service.Dto.BatchResponse;
import co.edu.corhuila.inventory_service.Dto.CreateBatchRequest;
import co.edu.corhuila.inventory_service.Dto.FefoSnapshotResponse;
import co.edu.corhuila.inventory_service.Dto.LowStockReportItemResponse;
import co.edu.corhuila.inventory_service.Dto.ProductOutOfStockResponse;
import co.edu.corhuila.inventory_service.Entity.Product;
import co.edu.corhuila.inventory_service.Service.BatchService;
import co.edu.corhuila.inventory_service.Service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final BatchService batchService;

    public ProductController(ProductService productService, BatchService batchService) {
        this.productService = productService;
        this.batchService = batchService;
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {

        Product updated = productService.updateProduct(id, product);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeProduct(@PathVariable Long id) {
        productService.removeProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<Product> listProducts() {
        return productService.listProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping({"/Assets", "/assets", "/active"})
    public ResponseEntity<List<Product>> listActiveProducts() {
        return ResponseEntity.ok(productService.listActiveProducts());
    }

    @GetMapping("/active-table")
    public ResponseEntity<List<ActiveInventoryTableItemResponse>> getActiveInventoryTable() {
        return ResponseEntity.ok(productService.getActiveInventoryTable());
    }

    @GetMapping("/active-summary")
    public ResponseEntity<ActiveInventorySummaryResponse> getActiveInventorySummary() {
        return ResponseEntity.ok(productService.getActiveInventorySummary());
    }

    @GetMapping("/fefo-snapshot")
    public ResponseEntity<FefoSnapshotResponse> getFefoSnapshot() {
        return ResponseEntity.ok(productService.getFefoSnapshot());
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<ProductOutOfStockResponse>> outOfStockProducts() {
        return ResponseEntity.ok(productService.outOfStockProducts());
    }

    @GetMapping({"/low-stock", "/low-stock-report"})
    public ResponseEntity<List<LowStockReportItemResponse>> getAllLowStockProducts() {
        return ResponseEntity.ok(productService.getAllLowStockProducts());
    }

    @GetMapping({"/low-stock/critical", "/low-stock-report/critical"})
    public ResponseEntity<List<LowStockReportItemResponse>> getCriticalLowStockProducts() {
        return ResponseEntity.ok(productService.getCriticalLowStockProducts());
    }

    @GetMapping({"/low-stock/alert", "/low-stock-report/alert"})
    public ResponseEntity<List<LowStockReportItemResponse>> getAlertLowStockProducts() {
        return ResponseEntity.ok(productService.getAlertLowStockProducts());
    }

    @GetMapping("/{productId}/batches")
    public ResponseEntity<List<BatchResponse>> listBatchesByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(batchService.listBatchesByProduct(productId));
    }

    @PostMapping("/{productId}/batches")
    public ResponseEntity<BatchResponse> createBatchByProduct(
            @PathVariable Long productId,
            @RequestBody CreateBatchRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batchService.createBatch(productId, request));
    }
}
