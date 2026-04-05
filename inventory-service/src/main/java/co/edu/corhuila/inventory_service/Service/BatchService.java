package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Dto.BatchResponse;
import co.edu.corhuila.inventory_service.Dto.CreateBatchRequest;
import co.edu.corhuila.inventory_service.Dto.InventoryBatchReportItemResponse;
import co.edu.corhuila.inventory_service.Entity.Batch;
import co.edu.corhuila.inventory_service.Entity.BatchStatus;
import co.edu.corhuila.inventory_service.Entity.Product;
import co.edu.corhuila.inventory_service.Repository.BatchRepository;
import co.edu.corhuila.inventory_service.Repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class BatchService {
    private static final DateTimeFormatter BATCH_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;

    public BatchService(BatchRepository batchRepository, ProductRepository productRepository) {
        this.batchRepository = batchRepository;
        this.productRepository = productRepository;
    }

    public List<BatchResponse> listBatchesByProduct(Long productId) {
        Product product = findProductOrThrow(productId);
        refreshBatchStatuses(productId);
        return batchRepository.findByProductIdOrderByExpirationDateAsc(product.getId())
                .stream()
                .map(BatchResponse::new)
                .toList();
    }

    @Transactional
    public BatchResponse createBatch(Long productId, CreateBatchRequest request) {
        Product product = findProductOrThrow(productId);
        if (!product.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No se pueden crear lotes en productos retirados");
        }

        validateCreateBatchRequest(request);

        String normalizedCode = request.getBatchCode().trim();
        if (batchRepository.existsByProductIdAndBatchCodeIgnoreCase(productId, normalizedCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El lote ya existe para este producto"
            );
        }

        Batch batch = new Batch();
        batch.setProduct(product);
        batch.setBatchCode(normalizedCode);
        batch.setExpirationDate(request.getExpirationDate());
        batch.setInitialStock(request.getInitialStock());
        batch.setAvailableStock(request.getInitialStock());
        batch.setStatus(resolveBatchStatus(request.getInitialStock(), request.getExpirationDate()));

        Batch saved = batchRepository.save(batch);
        syncProductStock(product);
        return new BatchResponse(saved);
    }

    public List<InventoryBatchReportItemResponse> listInventoryBatchReport() {
        refreshBatchStatusesForAllProducts();
        return batchRepository.findAll()
                .stream()
                .map(InventoryBatchReportItemResponse::new)
                .toList();
    }

    public List<BatchRepository.FefoSnapshotProjection> getFefoSnapshot() {
        return batchRepository.findFefoSnapshot();
    }

    @Transactional
    public Batch createInitialBatchForLegacyProduct(Product product) {
        String defaultBatchCode = "INIT-" + product.getCode();
        if (batchRepository.existsByProductIdAndBatchCodeIgnoreCase(product.getId(), defaultBatchCode)) {
            return batchRepository.findByProductIdOrderByExpirationDateAsc(product.getId())
                    .stream()
                    .filter(batch -> defaultBatchCode.equalsIgnoreCase(batch.getBatchCode()))
                    .findFirst()
                    .orElse(null);
        }

        Batch batch = new Batch();
        batch.setProduct(product);
        batch.setBatchCode(defaultBatchCode);
        batch.setExpirationDate(product.getExpirationDate());
        batch.setInitialStock(product.getStock());
        batch.setAvailableStock(product.getStock());
        batch.setStatus(resolveBatchStatus(product.getStock(), product.getExpirationDate()));
        return batchRepository.save(batch);
    }

    @Transactional
    public Batch createBatchForInventoryEntry(Product product, Integer quantity, LocalDate expirationDate) {
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto requerido");
        }
        if (!product.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "No se pueden registrar entradas para productos retirados"
            );
        }
        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity debe ser mayor a 0");
        }
        if (expirationDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "expirationDate es obligatorio");
        }
        if (expirationDate.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "expirationDate no puede corresponder a una fecha vencida"
            );
        }

        Batch batch = new Batch();
        batch.setProduct(product);
        batch.setBatchCode(generateInventoryEntryBatchCode(product));
        batch.setExpirationDate(expirationDate);
        batch.setInitialStock(quantity);
        batch.setAvailableStock(quantity);
        batch.setStatus(resolveBatchStatus(quantity, expirationDate));

        Batch saved = batchRepository.save(batch);
        syncProductStock(product);
        return saved;
    }

    @Transactional
    public void syncProductStock(Product product) {
        recalculateProductStockFromActiveBatches(product, true);
    }

    public void refreshProductStockSnapshot(Product product) {
        recalculateProductStockFromActiveBatches(product, false);
    }

    @Transactional
    public void syncLegacySingleBatchFromProduct(Product product) {
        List<Batch> batches = batchRepository.findByProductIdOrderByExpirationDateAsc(product.getId());
        if (batches.size() != 1) {
            return;
        }

        Batch batch = batches.get(0);
        if (batch.getStatus() == BatchStatus.RETIRED) {
            return;
        }

        String expectedBatchCode = "INIT-" + product.getCode();
        if (!expectedBatchCode.equalsIgnoreCase(batch.getBatchCode())) {
            return;
        }

        batch.setExpirationDate(product.getExpirationDate());
        batch.setInitialStock(product.getStock());
        batch.setAvailableStock(product.getStock());
        batch.setStatus(resolveBatchStatus(product.getStock(), product.getExpirationDate(), batch.getStatus()));
        batchRepository.save(batch);
    }

    @Transactional
    public void refreshBatchStatusesForAllProducts() {
        batchRepository.findAll()
                .forEach(batch -> {
                    BatchStatus oldStatus = batch.getStatus();
                    batch.setStatus(resolveBatchStatus(batch.getAvailableStock(), batch.getExpirationDate(), oldStatus));
                    if (oldStatus != batch.getStatus()) {
                        batchRepository.save(batch);
                    }
                });
    }

    @Transactional
    public void refreshBatchStatuses(Long productId) {
        batchRepository.findByProductIdOrderByExpirationDateAsc(productId)
                .forEach(batch -> {
                    BatchStatus oldStatus = batch.getStatus();
                    batch.setStatus(resolveBatchStatus(batch.getAvailableStock(), batch.getExpirationDate(), oldStatus));
                    if (oldStatus != batch.getStatus()) {
                        batchRepository.save(batch);
                    }
                });
    }

    public Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    @Transactional
    public void retireBatchesByProduct(Long productId) {
        List<Batch> batches = batchRepository.findByProductIdOrderByExpirationDateAsc(productId);
        for (Batch batch : batches) {
            batch.setStatus(BatchStatus.RETIRED);
            batchRepository.save(batch);
        }
    }

    public Batch findBatchOrThrow(Long productId, Long batchId) {
        return batchRepository.findByIdAndProductId(batchId, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lote no encontrado para el producto"));
    }

    public Batch findPreferredBatchForProduct(Product product) {
        if (product == null || product.getId() == null) {
            return null;
        }

        List<Batch> availableBatches = batchRepository.findByProductIdOrderByExpirationDateAsc(product.getId())
                .stream()
                .filter(batch -> batch.getStatus() != BatchStatus.RETIRED)
                .toList();

        if (availableBatches.isEmpty()) {
            return null;
        }

        String initialBatchCode = "INIT-" + product.getCode();
        return availableBatches.stream()
                .filter(batch -> initialBatchCode.equalsIgnoreCase(batch.getBatchCode()))
                .findFirst()
                .orElse(availableBatches.get(0));
    }

    @Transactional
    public Batch findOrCreatePreferredBatchForProduct(Product product) {
        Batch preferredBatch = findPreferredBatchForProduct(product);
        if (preferredBatch != null) {
            return preferredBatch;
        }
        return createInitialBatchForLegacyProduct(product);
    }

    public BatchStatus resolveBatchStatus(Integer availableStock, LocalDate expirationDate) {
        return resolveBatchStatus(availableStock, expirationDate, BatchStatus.ACTIVE);
    }

    public BatchStatus resolveBatchStatus(Integer availableStock, LocalDate expirationDate, BatchStatus currentStatus) {
        if (currentStatus == BatchStatus.RETIRED) {
            return BatchStatus.RETIRED;
        }
        if (expirationDate != null && expirationDate.isBefore(LocalDate.now())) {
            return BatchStatus.EXPIRED;
        }
        if (availableStock == null || availableStock == 0) {
            return BatchStatus.OUT_OF_STOCK;
        }
        return BatchStatus.ACTIVE;
    }

    private void validateCreateBatchRequest(CreateBatchRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload de lote requerido");
        }
        if (request.getBatchCode() == null || request.getBatchCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "batchCode es obligatorio");
        }
        if (request.getExpirationDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "expirationDate es obligatorio");
        }
        if (request.getInitialStock() == null || request.getInitialStock() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "initialStock debe ser mayor o igual a 0");
        }
    }

    private String generateInventoryEntryBatchCode(Product product) {
        String productCode = sanitizeBatchSegment(product.getCode());
        String dateSegment = LocalDate.now().format(BATCH_DATE_FORMAT);
        String prefix = "LOT-" + productCode + "-" + dateSegment + "-";

        List<Batch> existingBatches = batchRepository.findByProductIdOrderByExpirationDateAsc(product.getId());
        int nextSequence = existingBatches.stream()
                .map(Batch::getBatchCode)
                .filter(Objects::nonNull)
                .filter(code -> code.startsWith(prefix))
                .map(code -> code.substring(prefix.length()))
                .map(this::parseBatchSequence)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1;

        return prefix + String.format("%03d", nextSequence);
    }

    private Integer parseBatchSequence(String rawSequence) {
        try {
            return Integer.parseInt(rawSequence);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String sanitizeBatchSegment(String value) {
        if (value == null || value.isBlank()) {
            return "GEN";
        }

        String sanitized = value.trim().toUpperCase().replaceAll("[^A-Z0-9]+", "");
        if (sanitized.isBlank()) {
            return "GEN";
        }

        return sanitized.length() <= 6 ? sanitized : sanitized.substring(0, 6);
    }

    private void recalculateProductStockFromActiveBatches(Product product, boolean persistProduct) {
        List<Batch> batches = batchRepository.findByProductIdOrderByExpirationDateAsc(product.getId());
        LocalDate today = LocalDate.now();

        if (persistProduct) {
            for (Batch batch : batches) {
                BatchStatus oldStatus = batch.getStatus();
                BatchStatus resolvedStatus = resolveBatchStatus(
                        batch.getAvailableStock(),
                        batch.getExpirationDate(),
                        oldStatus
                );
                if (resolvedStatus != oldStatus) {
                    batch.setStatus(resolvedStatus);
                    batchRepository.save(batch);
                }
            }
        }

        List<Batch> activeConsumableBatches = batches.stream()
                .filter(batch -> batch.getStatus() == BatchStatus.ACTIVE)
                .filter(batch -> batch.getAvailableStock() != null && batch.getAvailableStock() > 0)
                .filter(batch -> batch.getExpirationDate() != null && !batch.getExpirationDate().isBefore(today))
                .sorted(Comparator.comparing(Batch::getExpirationDate).thenComparing(Batch::getId))
                .toList();

        int totalConsumableStock = activeConsumableBatches.stream()
                .map(Batch::getAvailableStock)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        LocalDate nextExpirationDate = activeConsumableBatches.isEmpty()
                ? null
                : activeConsumableBatches.get(0).getExpirationDate();

        product.setStock(totalConsumableStock);
        product.setExpirationDate(nextExpirationDate);

        if (persistProduct) {
            productRepository.save(product);
        }
    }
}
