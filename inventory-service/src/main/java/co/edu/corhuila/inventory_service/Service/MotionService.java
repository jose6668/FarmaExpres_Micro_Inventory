package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Dto.FefoConsumeRequest;
import co.edu.corhuila.inventory_service.Dto.FefoConsumptionItemResponse;
import co.edu.corhuila.inventory_service.Dto.InventoryEntryRequest;
import co.edu.corhuila.inventory_service.Dto.InventoryEntryResponse;
import co.edu.corhuila.inventory_service.Dto.InventoryExitRequest;
import co.edu.corhuila.inventory_service.Dto.MotionResponse;
import co.edu.corhuila.inventory_service.Dto.MovementBatchReportItemResponse;
import co.edu.corhuila.inventory_service.Dto.MovementExecutionResponse;
import co.edu.corhuila.inventory_service.Dto.MovementRequest;
import co.edu.corhuila.inventory_service.Dto.UserActivityReportResponse;
import co.edu.corhuila.inventory_service.Entity.Batch;
import co.edu.corhuila.inventory_service.Entity.BatchStatus;
import co.edu.corhuila.inventory_service.Entity.Motion;
import co.edu.corhuila.inventory_service.Entity.MovementType;
import co.edu.corhuila.inventory_service.Entity.Product;
import co.edu.corhuila.inventory_service.Repository.BatchRepository;
import co.edu.corhuila.inventory_service.Repository.MotionRepository;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class MotionService {

    private static final String SYSTEM_USER_NAME = "Sistema";
    private static final String SYSTEM_USER_EMAIL = "system@farmaexpres.local";
    private static final String SYSTEM_USER_ROLE = "Automatico";
    private static final Set<String> ALLOWED_ENTRY_REASONS = new LinkedHashSet<>(Arrays.asList(
            "Compra proveedor",
            "Devolucion",
            "Donacion",
            "Ajuste inventario"
    ));
    private static final Set<String> ALLOWED_EXIT_REASONS = new LinkedHashSet<>(Arrays.asList(
            "Dispensacion",
            "Ajuste inventario",
            "Merma",
            "Vencimiento"
    ));

    private final MotionRepository motionRepository;
    private final BatchRepository batchRepository;
    private final BatchService batchService;

    public MotionService(
            MotionRepository motionRepository,
            BatchRepository batchRepository,
            BatchService batchService
    ) {
        this.motionRepository = motionRepository;
        this.batchRepository = batchRepository;
        this.batchService = batchService;
    }

    public List<MotionResponse> listMotion() {
        return motionRepository.findAll()
                .stream()
                .map(MotionResponse::new)
                .toList();
    }

    public List<MotionResponse> listMotionByUser(Long userId) {
        List<Motion> motions = userId == null
                ? motionRepository.findAllByOrderByDateTimeDesc()
                : motionRepository.findByUserIdOrderByDateTimeDesc(userId);

        return motions.stream()
                .map(MotionResponse::new)
                .toList();
    }

    public List<MotionResponse> listEntranceMotion() {
        return motionRepository.findByType(MovementType.Entrance)
                .stream()
                .map(MotionResponse::new)
                .toList();
    }

    public List<MotionResponse> listExitMotion() {
        return motionRepository.findByType(MovementType.Exit)
                .stream()
                .map(MotionResponse::new)
                .toList();
    }

    public List<MotionResponse> listUpdatedMotion() {
        return motionRepository.findByType(MovementType.Updated)
                .stream()
                .map(MotionResponse::new)
                .toList();
    }

    public List<UserActivityReportResponse> listUsersActivityReport(String role) {
        Map<UserActivityKey, UserActivityAccumulator> activityByUser = new LinkedHashMap<>();
        String normalizedRoleFilter = normalizeRole(role);

        for (Motion motion : motionRepository.findAllByOrderByDateTimeDesc()) {
            String userRole = firstNotBlank(motion.getUserRole(), SYSTEM_USER_ROLE);
            if (normalizedRoleFilter != null && !userRole.equalsIgnoreCase(normalizedRoleFilter)) {
                continue;
            }

            UserActivityKey key = new UserActivityKey(
                    motion.getUserId(),
                    firstNotBlank(motion.getUserName(), SYSTEM_USER_NAME),
                    userRole
            );

            UserActivityAccumulator accumulator = activityByUser.computeIfAbsent(
                    key,
                    ignored -> new UserActivityAccumulator()
            );

            accumulator.totalMovements++;

            if (motion.getType() == MovementType.Entrance) {
                accumulator.totalEntrances++;
            }

            if (motion.getType() == MovementType.Exit) {
                accumulator.totalExits++;
            }
        }

        return activityByUser.entrySet()
                .stream()
                .map(entry -> toResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator
                        .comparing(UserActivityReportResponse::getTotalMovements, Comparator.reverseOrder())
                        .thenComparing(UserActivityReportResponse::getUserName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public InventoryEntryResponse registerInventoryEntry(InventoryEntryRequest request) {
        validateInventoryEntryRequest(request);
        Product product = batchService.findProductOrThrow(request.getProductId());
        if (!product.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "No se pueden registrar entradas para productos retirados"
            );
        }

        Batch createdBatch = batchService.createBatchForInventoryEntry(
                product,
                request.getQuantity(),
                request.getExpirationDate()
        );

        MotionActorContext actor = extractMotionActor();
        Motion motion = new Motion(
                MovementType.Entrance,
                request.getQuantity(),
                product,
                request.getReason(),
                actor.userId(),
                actor.userName(),
                actor.userEmail(),
                actor.userRole()
        );
        motion.setObservation(normalizeOptionalText(request.getDetail()));
        motion.setBatch(createdBatch);
        motionRepository.save(motion);

        return new InventoryEntryResponse(
                MovementType.Entrance.name(),
                product.getId(),
                request.getQuantity(),
                request.getReason(),
                normalizeOptionalText(request.getDetail()),
                List.of(new FefoConsumptionItemResponse(
                        createdBatch.getId(),
                        createdBatch.getBatchCode(),
                        createdBatch.getExpirationDate(),
                        request.getQuantity()
                ))
        );
    }

    @Transactional
    public MovementExecutionResponse registerInventoryExit(InventoryExitRequest request) {
        validateInventoryExitRequest(request);
        Product product = batchService.findProductOrThrow(request.getProductId());
        if (!product.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "No se pueden registrar salidas para productos retirados"
            );
        }

        batchService.refreshBatchStatuses(product.getId());
        List<Batch> consumableBatches = batchRepository.findConsumableBatchesByProductIdOrderByCreatedAtAsc(
                product.getId(),
                List.of(BatchStatus.ACTIVE)
        );

        int totalAvailable = consumableBatches.stream()
                .map(Batch::getAvailableStock)
                .mapToInt(Integer::intValue)
                .sum();

        if (totalAvailable < request.getQuantity()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "La cantidad solicitada supera el stock total disponible por lote"
            );
        }

        int remaining = request.getQuantity();
        List<FefoConsumptionItemResponse> allocations = new ArrayList<>();
        MotionActorContext actor = extractMotionActor();
        String normalizedReason = request.getReason().trim();
        String normalizedDetail = normalizeOptionalText(request.getDetail());

        for (Batch batch : consumableBatches) {
            if (remaining == 0) {
                break;
            }

            int canTake = Math.min(batch.getAvailableStock(), remaining);
            if (canTake == 0) {
                continue;
            }

            batch.setAvailableStock(batch.getAvailableStock() - canTake);
            batch.setStatus(batchService.resolveBatchStatus(
                    batch.getAvailableStock(),
                    batch.getExpirationDate(),
                    batch.getStatus()
            ));
            batchRepository.save(batch);

            Motion motion = new Motion(
                    MovementType.Exit,
                    canTake,
                    product,
                    normalizedReason,
                    actor.userId(),
                    actor.userName(),
                    actor.userEmail(),
                    actor.userRole()
            );
            motion.setObservation(normalizedDetail);
            motion.setBatch(batch);
            motionRepository.save(motion);

            allocations.add(new FefoConsumptionItemResponse(
                    batch.getId(),
                    batch.getBatchCode(),
                    batch.getExpirationDate(),
                    canTake
            ));
            remaining -= canTake;
        }

        batchService.syncProductStock(product);

        return new MovementExecutionResponse(
                MovementType.Exit.name(),
                product.getId(),
                request.getQuantity(),
                normalizedReason,
                normalizedDetail,
                allocations
        );
    }

    @Transactional
    public MovementExecutionResponse createMovement(MovementRequest request) {
        validateMovementRequest(request);
        Product product = batchService.findProductOrThrow(request.getProductId());
        MovementType movementType = parseMovementType(request.getType());

        if (movementType == MovementType.Exit && request.getBatchId() == null) {
            FefoConsumeRequest fefo = new FefoConsumeRequestAdapter(request);
            return consumeFefo(fefo);
        }

        if (request.getBatchId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "batchId es obligatorio para el movimiento indicado"
            );
        }

        Batch batch = batchService.findBatchOrThrow(product.getId(), request.getBatchId());
        if (batch.getStatus() == BatchStatus.RETIRED) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "No se permiten movimientos sobre lotes retirados"
            );
        }

        applyMovementToBatch(batch, movementType, request.getQuantity());
        batch.setStatus(batchService.resolveBatchStatus(
                batch.getAvailableStock(),
                batch.getExpirationDate(),
                batch.getStatus()
        ));
        batchRepository.save(batch);
        batchService.syncProductStock(product);

        MotionActorContext actor = extractMotionActor();
        Motion motion = new Motion(
                movementType,
                request.getQuantity(),
                product,
                composeReason(request.getReason(), request.getDetail()),
                actor.userId(),
                actor.userName(),
                actor.userEmail(),
                actor.userRole()
        );
        motion.setObservation(request.getDetail());
        motion.setBatch(batch);
        motionRepository.save(motion);

        return new MovementExecutionResponse(
                movementType.name(),
                product.getId(),
                request.getQuantity(),
                request.getReason(),
                request.getDetail(),
                List.of(new FefoConsumptionItemResponse(
                        batch.getId(),
                        batch.getBatchCode(),
                        batch.getExpirationDate(),
                        request.getQuantity()
                ))
        );
    }

    @Transactional
    public MovementExecutionResponse consumeFefo(FefoConsumeRequest request) {
        validateFefoRequest(request);
        Product product = batchService.findProductOrThrow(request.getProductId());
        batchService.refreshBatchStatuses(product.getId());

        List<Batch> consumableBatches = batchRepository.findConsumableBatchesByProductId(
                product.getId(),
                List.of(BatchStatus.ACTIVE)
        );

        int totalAvailable = consumableBatches.stream()
                .map(Batch::getAvailableStock)
                .mapToInt(Integer::intValue)
                .sum();

        if (totalAvailable < request.getQuantity()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "La cantidad solicitada supera el stock disponible por lote"
            );
        }

        int remaining = request.getQuantity();
        List<FefoConsumptionItemResponse> allocations = new ArrayList<>();
        MotionActorContext actor = extractMotionActor();
        String reason = composeReason(request.getReason(), request.getDetail());

        for (Batch batch : consumableBatches) {
            if (remaining == 0) {
                break;
            }

            int canTake = Math.min(batch.getAvailableStock(), remaining);
            if (canTake == 0) {
                continue;
            }

            batch.setAvailableStock(batch.getAvailableStock() - canTake);
            batch.setStatus(batchService.resolveBatchStatus(
                    batch.getAvailableStock(),
                    batch.getExpirationDate(),
                    batch.getStatus()
            ));
            batchRepository.save(batch);

            Motion motion = new Motion(
                    MovementType.Exit,
                    canTake,
                    product,
                    reason,
                    actor.userId(),
                    actor.userName(),
                    actor.userEmail(),
                    actor.userRole()
            );
            motion.setObservation(request.getDetail());
            motion.setBatch(batch);
            motionRepository.save(motion);

            allocations.add(new FefoConsumptionItemResponse(
                    batch.getId(),
                    batch.getBatchCode(),
                    batch.getExpirationDate(),
                    canTake
            ));
            remaining -= canTake;
        }

        batchService.syncProductStock(product);

        return new MovementExecutionResponse(
                MovementType.Exit.name(),
                product.getId(),
                request.getQuantity(),
                request.getReason(),
                request.getDetail(),
                allocations
        );
    }

    public List<MovementBatchReportItemResponse> listMovementBatchReport() {
        return motionRepository.findByBatchIsNotNullOrderByDateTimeDesc()
                .stream()
                .map(MovementBatchReportItemResponse::new)
                .toList();
    }

    private void applyMovementToBatch(Batch batch, MovementType movementType, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity debe ser mayor a 0");
        }

        if (movementType == MovementType.Exit) {
            if (batch.getAvailableStock() < quantity) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Quantity exceeds available stock for batch " + batch.getBatchCode()
                );
            }
            batch.setAvailableStock(batch.getAvailableStock() - quantity);
            return;
        }

        if (movementType == MovementType.Entrance) {
            batch.setAvailableStock(batch.getAvailableStock() + quantity);
            return;
        }

        throw new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Solo se permiten movimientos Entrance/Exit con batchId"
        );
    }

    private MovementType parseMovementType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type es obligatorio");
        }

        String normalized = rawType.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ENTRANCE" -> MovementType.Entrance;
            case "EXIT" -> MovementType.Exit;
            case "UPDATED" -> MovementType.Updated;
            case "DELETED" -> MovementType.Deleted;
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "type invalido. Valores permitidos: ENTRANCE, EXIT, UPDATED, DELETED"
            );
        };
    }

    private void validateMovementRequest(MovementRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload requerido");
        }
        if (request.getProductId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId es obligatorio");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity debe ser mayor a 0");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reason es obligatorio");
        }
    }

    private void validateFefoRequest(FefoConsumeRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload requerido");
        }
        if (request.getProductId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId es obligatorio");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity debe ser mayor a 0");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reason es obligatorio");
        }
    }

    private void validateInventoryEntryRequest(InventoryEntryRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload requerido");
        }
        if (request.getProductId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId es obligatorio");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity debe ser mayor a 0");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reason es obligatorio");
        }
        if (!ALLOWED_ENTRY_REASONS.contains(request.getReason().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "reason invalido. Valores permitidos: " + String.join(", ", ALLOWED_ENTRY_REASONS)
            );
        }
        if (request.getExpirationDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "expirationDate es obligatorio");
        }
        if (request.getExpirationDate().isBefore(java.time.LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "expirationDate no puede corresponder a una fecha vencida"
            );
        }
    }

    private void validateInventoryExitRequest(InventoryExitRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload requerido");
        }
        if (request.getProductId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId es obligatorio");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity debe ser mayor a 0");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reason es obligatorio");
        }
        if (!ALLOWED_EXIT_REASONS.contains(request.getReason().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "reason invalido. Valores permitidos: " + String.join(", ", ALLOWED_EXIT_REASONS)
            );
        }
    }

    private String normalizeOptionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String composeReason(String reason, String detail) {
        return reason;
    }

    private String normalizeRole(String role) {
        return role == null || role.isBlank() ? null : role.trim();
    }

    private UserActivityReportResponse toResponse(UserActivityKey key, UserActivityAccumulator accumulator) {
        return new UserActivityReportResponse(
                key.userId(),
                key.userName(),
                key.userRole(),
                accumulator.totalMovements,
                accumulator.totalEntrances,
                accumulator.totalExits,
                resolveActivityLevel(accumulator.totalMovements)
        );
    }

    private String resolveActivityLevel(Long totalMovements) {
        if (totalMovements >= 10) {
            return "Alta";
        }
        if (totalMovements >= 4) {
            return "Media";
        }
        return "Baja";
    }

    private String firstNotBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private MotionActorContext extractMotionActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new MotionActorContext(null, SYSTEM_USER_NAME, SYSTEM_USER_EMAIL, SYSTEM_USER_ROLE);
        }

        String email = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(authority -> authority.replace("ROLE_", ""))
                .orElse(SYSTEM_USER_ROLE);

        String name = null;
        Long userId = null;
        Object details = authentication.getDetails();
        if (details instanceof Claims claims) {
            name = claims.get("name", String.class);
            userId = claims.get("userId", Long.class);
        }

        return new MotionActorContext(
                userId,
                name == null || name.isBlank() ? SYSTEM_USER_NAME : name,
                email == null || email.isBlank() ? SYSTEM_USER_EMAIL : email,
                role == null || role.isBlank() ? SYSTEM_USER_ROLE : role
        );
    }

    private record UserActivityKey(Long userId, String userName, String userRole) {
    }

    private static class UserActivityAccumulator {
        private long totalMovements;
        private long totalEntrances;
        private long totalExits;
    }

    private record MotionActorContext(Long userId, String userName, String userEmail, String userRole) {
    }

    private static class FefoConsumeRequestAdapter extends FefoConsumeRequest {
        private final Long productId;
        private final Integer quantity;
        private final String reason;
        private final String detail;

        private FefoConsumeRequestAdapter(MovementRequest request) {
            this.productId = request.getProductId();
            this.quantity = request.getQuantity();
            this.reason = request.getReason();
            this.detail = request.getDetail();
        }

        @Override
        public Long getProductId() {
            return productId;
        }

        @Override
        public Integer getQuantity() {
            return quantity;
        }

        @Override
        public String getReason() {
            return reason;
        }

        @Override
        public String getDetail() {
            return detail;
        }
    }
}
