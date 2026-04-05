package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Dto.ActiveInventorySummaryResponse;
import co.edu.corhuila.inventory_service.Dto.ActiveInventoryTableItemResponse;
import co.edu.corhuila.inventory_service.Dto.AdjustmentDetailItem;
import co.edu.corhuila.inventory_service.Dto.FefoSnapshotItemResponse;
import co.edu.corhuila.inventory_service.Dto.FefoSnapshotResponse;
import co.edu.corhuila.inventory_service.Dto.LowStockReportItemResponse;
import co.edu.corhuila.inventory_service.Dto.ProductOutOfStockResponse;
import co.edu.corhuila.inventory_service.Entity.Batch;
import co.edu.corhuila.inventory_service.Entity.Motion;
import co.edu.corhuila.inventory_service.Entity.MovementType;
import co.edu.corhuila.inventory_service.Entity.Product;
import co.edu.corhuila.inventory_service.Repository.MotionRepository;
import co.edu.corhuila.inventory_service.Repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
public class ProductService {
    private static final String SYSTEM_USER_NAME = "SYSTEM_INIT";
    private static final String SYSTEM_USER_EMAIL = "system@farmaexpres.local";
    private static final String SYSTEM_USER_ROLE = "SYSTEM";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Set<String> FORMA_FARMACEUTICA_OPTIONS = Set.of(
            "TABLETA", "CAPSULA", "JARABE", "SUSPENSION", "INYECTABLE",
            "CREMA", "GOTAS", "AMPOLLA", "SUPOSITORIO", "OTRO"
    );
    private static final Set<String> VIA_ADMINISTRACION_OPTIONS = Set.of(
            "ORAL", "INTRAVENOSA", "INTRAMUSCULAR", "SUBCUTANEA", "TOPICA",
            "INHALATORIA", "OFTALMICA", "OTICA", "NASAL", "RECTAL",
            "VAGINAL", "OTRA"
    );
    private static final Set<String> UNIDAD_MEDIDA_OPTIONS = Set.of(
            "UNIDAD", "BLISTER", "CAJA", "FRASCO", "VIAL",
            "AMPOLLA", "TUBO", "SOBRE", "JERINGA", "UI",
            "MCG", "ML", "MG", "G"
    );
    private static final Set<String> TEMPERATURA_CONSERVACION_OPTIONS = Set.of(
            "AMBIENTE", "REFRIGERADO", "CONGELADO", "CONTROLADA", "NO_APLICA"
    );

    private final ProductRepository productRepository;
    private final MotionRepository motionRepository;
    private final BatchService batchService;

    public ProductService(ProductRepository productRepository,
                          MotionRepository motionRepository,
                          BatchService batchService) {
        this.productRepository = productRepository;
        this.motionRepository = motionRepository;
        this.batchService = batchService;
    }

    public Product createProduct(Product product) {
        normalizeProductData(product);
        validateProductData(product, false);

        if (productRepository.existsByCode(product.getCode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El codigo del producto ya existe"
            );
        }

        Product productSaved = productRepository.save(product);
        Batch initialBatch = batchService.createInitialBatchForLegacyProduct(productSaved);
        batchService.syncProductStock(productSaved);
        MotionActorContext actor = extractMotionActor();

        Motion motion = new Motion(
                MovementType.Entrance,
                productSaved.getStock(),
                productSaved,
                "Creacion de producto",
                actor.userId(),
                actor.userName(),
                actor.userEmail(),
                actor.userRole()
        );
        motion.setBatch(initialBatch);
        motionRepository.save(motion);

        return productSaved;
    }

    @Transactional
    public Product updateProduct(Long id, Product updatedData) {
        normalizeProductData(updatedData);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));

        if (!product.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede modificar un producto eliminado"
            );
        }

        // En edicion de producto, stock y vencimiento pertenecen al dominio de lotes/movimientos.
        // Se ignoran en este endpoint para no bloquear cambios validos de metadatos.
        updatedData.setStock(product.getStock());
        updatedData.setExpirationDate(product.getExpirationDate());

        validateProductData(updatedData, true);

        String previousName = product.getName();
        String previousNombreGenerico = product.getNombreGenerico();
        String previousConcentracion = product.getConcentracion();
        String previousFormaFarmaceutica = product.getFormaFarmaceutica();
        String previousPresentacion = product.getPresentacion();
        BigDecimal previousUnitPrice = product.getUnitPrice();
        Integer previousStockMaximo = product.getStockMaximo();
        BigDecimal previousPrecioCompra = product.getPrecioCompra();
        BigDecimal previousPrecioVenta = product.getPrecioVenta();
        Boolean previousRequiereReceta = product.getRequiereReceta();
        String previousLaboratorio = product.getLaboratorio();
        String previousRegistroSanitario = product.getRegistroSanitario();
        String previousViaAdministracion = product.getViaAdministracion();
        String previousUnidadMedida = product.getUnidadMedida();
        String previousUbicacionAlmacen = product.getUbicacionAlmacen();
        String previousTemperaturaConservacion = product.getTemperaturaConservacion();
        String previousObservaciones = product.getObservaciones();
        Integer previousStock = product.getStock();
        Integer previousMinimumStock = product.getMinimumStock();
        LocalDate previousExpirationDate = product.getExpirationDate();

        product.setName(updatedData.getName());
        product.setNombreGenerico(updatedData.getNombreGenerico());
        product.setConcentracion(updatedData.getConcentracion());
        product.setFormaFarmaceutica(updatedData.getFormaFarmaceutica());
        product.setPresentacion(updatedData.getPresentacion());
        product.setUnitPrice(updatedData.getUnitPrice());
        product.setStockMaximo(updatedData.getStockMaximo());
        product.setPrecioCompra(updatedData.getPrecioCompra());
        product.setPrecioVenta(updatedData.getPrecioVenta());
        product.setRequiereReceta(updatedData.getRequiereReceta());
        product.setLaboratorio(updatedData.getLaboratorio());
        product.setRegistroSanitario(updatedData.getRegistroSanitario());
        product.setViaAdministracion(updatedData.getViaAdministracion());
        product.setUnidadMedida(updatedData.getUnidadMedida());
        product.setUbicacionAlmacen(updatedData.getUbicacionAlmacen());
        product.setTemperaturaConservacion(updatedData.getTemperaturaConservacion());
        product.setObservaciones(updatedData.getObservaciones());
        product.setStock(updatedData.getStock());
        product.setMinimumStock(updatedData.getMinimumStock());
        product.setExpirationDate(updatedData.getExpirationDate());

        boolean nonStockChanges = !Objects.equals(previousName, updatedData.getName())
                || !Objects.equals(previousNombreGenerico, updatedData.getNombreGenerico())
                || !Objects.equals(previousConcentracion, updatedData.getConcentracion())
                || !Objects.equals(previousFormaFarmaceutica, updatedData.getFormaFarmaceutica())
                || !Objects.equals(previousPresentacion, updatedData.getPresentacion())
                || !areBigDecimalValuesEqual(previousUnitPrice, updatedData.getUnitPrice())
                || !Objects.equals(previousStockMaximo, updatedData.getStockMaximo())
                || !areBigDecimalValuesEqual(previousPrecioCompra, updatedData.getPrecioCompra())
                || !areBigDecimalValuesEqual(previousPrecioVenta, updatedData.getPrecioVenta())
                || !Objects.equals(previousRequiereReceta, updatedData.getRequiereReceta())
                || !Objects.equals(previousLaboratorio, updatedData.getLaboratorio())
                || !Objects.equals(previousRegistroSanitario, updatedData.getRegistroSanitario())
                || !Objects.equals(previousViaAdministracion, updatedData.getViaAdministracion())
                || !Objects.equals(previousUnidadMedida, updatedData.getUnidadMedida())
                || !Objects.equals(previousUbicacionAlmacen, updatedData.getUbicacionAlmacen())
                || !Objects.equals(previousTemperaturaConservacion, updatedData.getTemperaturaConservacion())
                || !Objects.equals(previousObservaciones, updatedData.getObservaciones())
                || !Objects.equals(previousMinimumStock, updatedData.getMinimumStock());

        if (!nonStockChanges) {
            return product;
        }

        Product productSaved = productRepository.save(product);
        batchService.syncLegacySingleBatchFromProduct(productSaved);
        MotionActorContext actor = extractMotionActor();

        if (nonStockChanges) {
            Motion updatedMotion = new Motion(
                    MovementType.Updated,
                    0,
                    productSaved,
                    "Ajuste de datos del producto",
                    actor.userId(),
                    actor.userName(),
                    actor.userEmail(),
                    actor.userRole()
            );

            List<AdjustmentDetailItem> adjustmentDetail = buildAdjustmentDetail(
                    previousName,
                    previousNombreGenerico,
                    previousConcentracion,
                    previousFormaFarmaceutica,
                    previousPresentacion,
                    previousUnitPrice,
                    previousStockMaximo,
                    previousPrecioCompra,
                    previousPrecioVenta,
                    previousRequiereReceta,
                    previousLaboratorio,
                    previousRegistroSanitario,
                    previousViaAdministracion,
                    previousUnidadMedida,
                    previousUbicacionAlmacen,
                    previousTemperaturaConservacion,
                    previousObservaciones,
                    previousMinimumStock,
                    previousExpirationDate,
                    updatedData
            );
            if (adjustmentDetail.isEmpty()) {
                adjustmentDetail.add(new AdjustmentDetailItem(
                        "producto",
                        "Producto",
                        "Sin detalle previo",
                        "Se actualizaron datos del producto",
                        "text"
                ));
            }
            updatedMotion.setAdjustmentSummary(buildAdjustmentSummary(adjustmentDetail));
            updatedMotion.setAdjustmentDetail(serializeAdjustmentDetail(adjustmentDetail));
            motionRepository.save(updatedMotion);
        }

        return productSaved;
    }

    @Transactional
    public void removeProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));

        product.setActive(false);
        product.setStock(0);
        productRepository.save(product);
        batchService.retireBatchesByProduct(product.getId());
        MotionActorContext actor = extractMotionActor();

        Motion motion = new Motion(
                MovementType.Deleted,
                product.getStock(),
                product,
                "Eliminacion logica del producto",
                actor.userId(),
                actor.userName(),
                actor.userEmail(),
                actor.userRole()
        );
        motionRepository.save(motion);
    }

    public List<Product> listProducts() {
        return productRepository.findAll();
    }

    public List<Product> listActiveProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .peek(batchService::refreshProductStockSnapshot)
                .toList();
    }

    public Product getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));
        batchService.refreshProductStockSnapshot(product);
        return product;
    }

    public List<ProductOutOfStockResponse> outOfStockProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .peek(batchService::refreshProductStockSnapshot)
                .filter(product -> product.getStock() != null && product.getStock() == 0)
                .map(ProductOutOfStockResponse::new)
                .toList();
    }

    public List<LowStockReportItemResponse> getAllLowStockProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .peek(batchService::refreshProductStockSnapshot)
                .filter(this::isLowStockProduct)
                .sorted(this::compareLowStockProducts)
                .map(product -> buildLowStockResponse(product, resolveLowStockStatus(product)))
                .toList();
    }

    public List<LowStockReportItemResponse> getCriticalLowStockProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .peek(batchService::refreshProductStockSnapshot)
                .filter(this::isCriticalLowStockProduct)
                .sorted(this::compareLowStockProducts)
                .map(product -> buildLowStockResponse(product, "Critico"))
                .toList();
    }

    public List<LowStockReportItemResponse> getAlertLowStockProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .peek(batchService::refreshProductStockSnapshot)
                .filter(this::isAlertLowStockProduct)
                .sorted(this::compareLowStockProducts)
                .map(product -> buildLowStockResponse(product, "Alerta"))
                .toList();
    }

    public List<ActiveInventoryTableItemResponse> getActiveInventoryTable() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(product -> {
                    batchService.refreshProductStockSnapshot(product);
                    BigDecimal unitPrice = product.getUnitPrice() != null
                            ? product.getUnitPrice()
                            : BigDecimal.ZERO;
                    int stock = product.getStock() != null
                            ? product.getStock()
                            : 0;

                    return new ActiveInventoryTableItemResponse(
                            product.getCode(),
                            product.getName(),
                            stock,
                            unitPrice,
                            unitPrice.multiply(BigDecimal.valueOf(stock))
                    );
                })
                .toList();
    }

    private void validateProductData(Product product, boolean isUpdate) {
        validateRequiredText(product.getName(), "El nombre del producto es obligatorio");
        validateRequiredText(product.getNombreGenerico(), "El nombre generico es obligatorio");
        validateRequiredText(product.getConcentracion(), "La concentracion es obligatoria");
        validateRequiredText(product.getFormaFarmaceutica(), "La forma farmaceutica es obligatoria");
        validateRequiredText(product.getPresentacion(), "La presentacion es obligatoria");
        validateRequiredText(product.getViaAdministracion(), "La via de administracion es obligatoria");
        validateRequiredText(product.getUnidadMedida(), "La unidad de medida es obligatoria");
        validateRequiredText(product.getUbicacionAlmacen(), "La ubicacion de almacen es obligatoria");
        validateRequiredText(product.getTemperaturaConservacion(), "La temperatura de conservacion es obligatoria");
        validateAllowedOption(product.getFormaFarmaceutica(), FORMA_FARMACEUTICA_OPTIONS, "forma farmaceutica");
        validateAllowedOption(product.getViaAdministracion(), VIA_ADMINISTRACION_OPTIONS, "via de administracion");
        validateAllowedOption(product.getUnidadMedida(), UNIDAD_MEDIDA_OPTIONS, "unidad de medida");
        validateAllowedOption(product.getTemperaturaConservacion(), TEMPERATURA_CONSERVACION_OPTIONS, "temperatura de conservacion");

        if (product.getRequiereReceta() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El campo requiereReceta es obligatorio"
            );
        }

        if (!isUpdate) {
            if (product.getStock() == null || product.getStock() < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El stock debe ser mayor o igual a 0"
                );
            }
        }

        if (product.getMinimumStock() == null || product.getMinimumStock() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El stock minimo debe ser mayor o igual a 0"
            );
        }

        if (product.getStockMaximo() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El stock maximo es obligatorio"
            );
        }

        if (product.getStockMaximo() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El stock maximo debe ser mayor o igual a 0"
            );
        }

        if (product.getPrecioCompra() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El precio de compra es obligatorio"
            );
        }

        if (product.getPrecioCompra().signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El precio de compra no puede ser negativo"
            );
        }

        if (product.getPrecioVenta() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El precio de venta es obligatorio"
            );
        }

        if (product.getPrecioVenta().signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El precio de venta no puede ser negativo"
            );
        }
    }

    private void validateRequiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private void validateAllowedOption(String value, Set<String> allowedOptions, String fieldLabel) {
        if (!allowedOptions.contains(value)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valor no permitido para " + fieldLabel + ": " + value
            );
        }
    }

    private void normalizeProductData(Product product) {
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud es obligatorio");
        }

        product.setCode(trimToNull(product.getCode()));
        product.setName(trimToNull(product.getName()));
        product.setNombreGenerico(trimToNull(product.getNombreGenerico()));
        product.setConcentracion(trimToNull(product.getConcentracion()));
        product.setFormaFarmaceutica(normalizeCatalogValue(product.getFormaFarmaceutica()));
        product.setPresentacion(trimToNull(product.getPresentacion()));
        product.setViaAdministracion(normalizeCatalogValue(product.getViaAdministracion()));
        product.setUnidadMedida(normalizeCatalogValue(product.getUnidadMedida()));
        product.setUbicacionAlmacen(trimToNull(product.getUbicacionAlmacen()));
        product.setTemperaturaConservacion(normalizeCatalogValue(product.getTemperaturaConservacion()));
        product.setLaboratorio(trimToNull(product.getLaboratorio()));
        product.setRegistroSanitario(trimToNull(product.getRegistroSanitario()));
        product.setObservaciones(trimToNull(product.getObservaciones()));
    }

    private String normalizeCatalogValue(String value) {
        String normalizedValue = trimToNull(value);
        if (normalizedValue == null) {
            return null;
        }
        return normalizedValue.toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isCriticalLowStockProduct(Product product) {
        if (product == null || !Boolean.TRUE.equals(product.getActive())) {
            return false;
        }

        Integer stock = product.getStock();
        Integer minimumStock = product.getMinimumStock();

        if (stock == null || minimumStock == null || minimumStock <= 0) {
            return false;
        }

        return stock <= minimumStock && (stock * 2) <= minimumStock;
    }

    private boolean isLowStockProduct(Product product) {
        return isCriticalLowStockProduct(product) || isAlertLowStockProduct(product);
    }

    private boolean isAlertLowStockProduct(Product product) {
        if (product == null || !Boolean.TRUE.equals(product.getActive())) {
            return false;
        }

        Integer stock = product.getStock();
        Integer minimumStock = product.getMinimumStock();

        if (stock == null || minimumStock == null || minimumStock <= 0) {
            return false;
        }

        return stock <= minimumStock && (stock * 2) > minimumStock;
    }

    private String resolveLowStockStatus(Product product) {
        if (isCriticalLowStockProduct(product)) {
            return "Critico";
        }
        if (isAlertLowStockProduct(product)) {
            return "Alerta";
        }
        return "";
    }

    private int compareLowStockProducts(Product left, Product right) {
        Comparator<Product> comparator = Comparator
                .comparingInt(this::statusPriority)
                .thenComparingInt(this::calculateCoverage)
                .thenComparing(
                        Product::getName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

        return comparator.compare(left, right);
    }

    private int statusPriority(Product product) {
        return isCriticalLowStockProduct(product) ? 0 : 1;
    }

    private LowStockReportItemResponse buildLowStockResponse(Product product, String status) {
        int stock = product.getStock();
        int minimumStock = product.getMinimumStock();
        int coverage = calculateCoverage(product);
        int suggestedUnits = calculateSuggestedUnits(stock, minimumStock);

        return new LowStockReportItemResponse(
                product.getId(),
                product.getCode() != null ? product.getCode() : "",
                product.getName() != null ? product.getName() : "",
                stock,
                minimumStock,
                coverage,
                coverage + "%",
                status,
                "Reponer " + suggestedUnits + " unidades"
        );
    }

    private int calculateCoverage(Product product) {
        int stock = product.getStock() != null ? product.getStock() : 0;
        int minimumStock = product.getMinimumStock() != null ? product.getMinimumStock() : 0;

        if (minimumStock <= 0) {
            return 0;
        }

        return (int) Math.round((stock * 100.0) / minimumStock);
    }

    private int calculateSuggestedUnits(int stock, int minimumStock) {
        int missingUnits = minimumStock - stock;
        return minimumStock + Math.max(missingUnits, 0);
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

    private List<AdjustmentDetailItem> buildAdjustmentDetail(
            String previousName,
            String previousNombreGenerico,
            String previousConcentracion,
            String previousFormaFarmaceutica,
            String previousPresentacion,
            BigDecimal previousUnitPrice,
            Integer previousStockMaximo,
            BigDecimal previousPrecioCompra,
            BigDecimal previousPrecioVenta,
            Boolean previousRequiereReceta,
            String previousLaboratorio,
            String previousRegistroSanitario,
            String previousViaAdministracion,
            String previousUnidadMedida,
            String previousUbicacionAlmacen,
            String previousTemperaturaConservacion,
            String previousObservaciones,
            Integer previousMinimumStock,
            LocalDate previousExpirationDate,
            Product updatedData
    ) {
        List<AdjustmentDetailItem> detail = new ArrayList<>();

        if (!Objects.equals(previousName, updatedData.getName())) {
            detail.add(new AdjustmentDetailItem(
                    "nombre",
                    "Nombre",
                    previousName,
                    updatedData.getName(),
                    "text"
            ));
        }

        if (!areBigDecimalValuesEqual(previousUnitPrice, updatedData.getUnitPrice())) {
            detail.add(new AdjustmentDetailItem(
                    "precio",
                    "Precio",
                    previousUnitPrice,
                    updatedData.getUnitPrice(),
                    "currency"
            ));
        }

        if (!Objects.equals(previousMinimumStock, updatedData.getMinimumStock())) {
            detail.add(new AdjustmentDetailItem(
                    "stockMinimo",
                    "Stock minimo",
                    previousMinimumStock,
                    updatedData.getMinimumStock(),
                    "number"
            ));
        }

        if (!Objects.equals(previousExpirationDate, updatedData.getExpirationDate())) {
            detail.add(new AdjustmentDetailItem(
                    "fechaVencimiento",
                    "Fecha de vencimiento",
                    previousExpirationDate != null ? previousExpirationDate.toString() : null,
                    updatedData.getExpirationDate() != null ? updatedData.getExpirationDate().toString() : null,
                    "date"
            ));
        }

        if (!Objects.equals(previousNombreGenerico, updatedData.getNombreGenerico())) {
            detail.add(new AdjustmentDetailItem(
                    "nombreGenerico",
                    "Nombre generico",
                    previousNombreGenerico,
                    updatedData.getNombreGenerico(),
                    "text"
            ));
        }

        if (!Objects.equals(previousConcentracion, updatedData.getConcentracion())) {
            detail.add(new AdjustmentDetailItem(
                    "concentracion",
                    "Concentracion",
                    previousConcentracion,
                    updatedData.getConcentracion(),
                    "text"
            ));
        }

        if (!Objects.equals(previousFormaFarmaceutica, updatedData.getFormaFarmaceutica())) {
            detail.add(new AdjustmentDetailItem(
                    "formaFarmaceutica",
                    "Forma farmaceutica",
                    previousFormaFarmaceutica,
                    updatedData.getFormaFarmaceutica(),
                    "text"
            ));
        }

        if (!Objects.equals(previousPresentacion, updatedData.getPresentacion())) {
            detail.add(new AdjustmentDetailItem(
                    "presentacion",
                    "Presentacion",
                    previousPresentacion,
                    updatedData.getPresentacion(),
                    "text"
            ));
        }

        if (!Objects.equals(previousStockMaximo, updatedData.getStockMaximo())) {
            detail.add(new AdjustmentDetailItem(
                    "stockMaximo",
                    "Stock maximo",
                    previousStockMaximo,
                    updatedData.getStockMaximo(),
                    "number"
            ));
        }

        if (!areBigDecimalValuesEqual(previousPrecioCompra, updatedData.getPrecioCompra())) {
            detail.add(new AdjustmentDetailItem(
                    "precioCompra",
                    "Precio compra",
                    previousPrecioCompra,
                    updatedData.getPrecioCompra(),
                    "currency"
            ));
        }

        if (!areBigDecimalValuesEqual(previousPrecioVenta, updatedData.getPrecioVenta())) {
            detail.add(new AdjustmentDetailItem(
                    "precioVenta",
                    "Precio venta",
                    previousPrecioVenta,
                    updatedData.getPrecioVenta(),
                    "currency"
            ));
        }

        if (!Objects.equals(previousRequiereReceta, updatedData.getRequiereReceta())) {
            detail.add(new AdjustmentDetailItem(
                    "requiereReceta",
                    "Requiere receta",
                    previousRequiereReceta,
                    updatedData.getRequiereReceta(),
                    "text"
            ));
        }

        if (!Objects.equals(previousLaboratorio, updatedData.getLaboratorio())) {
            detail.add(new AdjustmentDetailItem(
                    "laboratorio",
                    "Laboratorio",
                    previousLaboratorio,
                    updatedData.getLaboratorio(),
                    "text"
            ));
        }

        if (!Objects.equals(previousRegistroSanitario, updatedData.getRegistroSanitario())) {
            detail.add(new AdjustmentDetailItem(
                    "registroSanitario",
                    "Registro sanitario",
                    previousRegistroSanitario,
                    updatedData.getRegistroSanitario(),
                    "text"
            ));
        }

        if (!Objects.equals(previousViaAdministracion, updatedData.getViaAdministracion())) {
            detail.add(new AdjustmentDetailItem(
                    "viaAdministracion",
                    "Via administracion",
                    previousViaAdministracion,
                    updatedData.getViaAdministracion(),
                    "text"
            ));
        }

        if (!Objects.equals(previousUnidadMedida, updatedData.getUnidadMedida())) {
            detail.add(new AdjustmentDetailItem(
                    "unidadMedida",
                    "Unidad de medida",
                    previousUnidadMedida,
                    updatedData.getUnidadMedida(),
                    "text"
            ));
        }

        if (!Objects.equals(previousUbicacionAlmacen, updatedData.getUbicacionAlmacen())) {
            detail.add(new AdjustmentDetailItem(
                    "ubicacionAlmacen",
                    "Ubicacion almacen",
                    previousUbicacionAlmacen,
                    updatedData.getUbicacionAlmacen(),
                    "text"
            ));
        }

        if (!Objects.equals(previousTemperaturaConservacion, updatedData.getTemperaturaConservacion())) {
            detail.add(new AdjustmentDetailItem(
                    "temperaturaConservacion",
                    "Temperatura conservacion",
                    previousTemperaturaConservacion,
                    updatedData.getTemperaturaConservacion(),
                    "text"
            ));
        }

        if (!Objects.equals(previousObservaciones, updatedData.getObservaciones())) {
            detail.add(new AdjustmentDetailItem(
                    "observaciones",
                    "Observaciones",
                    previousObservaciones,
                    updatedData.getObservaciones(),
                    "text"
            ));
        }

        return detail;
    }

    private String buildAdjustmentSummary(List<AdjustmentDetailItem> adjustmentDetail) {
        return adjustmentDetail.stream()
                .map(item -> item.getLabel() + ": "
                        + formatValueForSummary(item.getBefore(), item.getFormat())
                        + " -> "
                        + formatValueForSummary(item.getAfter(), item.getFormat()))
                .reduce((left, right) -> left + "; " + right)
                .orElse(null);
    }

    private String formatValueForSummary(Object value, String format) {
        if (value == null) {
            return "null";
        }

        if ("currency".equals(format)) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "CO"));
            symbols.setGroupingSeparator('.');
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.##", symbols);
            return "$" + decimalFormat.format(value);
        }

        return String.valueOf(value);
    }

    private String serializeAdjustmentDetail(List<AdjustmentDetailItem> adjustmentDetail) {
        try {
            return OBJECT_MAPPER.writeValueAsString(adjustmentDetail);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo serializar el detalle del ajuste"
            );
        }
    }

    private boolean areBigDecimalValuesEqual(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.compareTo(right) == 0;
    }

    public ActiveInventorySummaryResponse getActiveInventorySummary() {
        List<Product> activeProducts = productRepository.findByActiveTrue()
                .stream()
                .peek(batchService::refreshProductStockSnapshot)
                .toList();

        int totalStock = activeProducts.stream()
                .map(Product::getStock)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        BigDecimal totalInventoryValue = activeProducts.stream()
                .map(product -> {
                    BigDecimal unitPrice = product.getUnitPrice() != null
                            ? product.getUnitPrice()
                            : BigDecimal.ZERO;
                    int stock = product.getStock() != null
                            ? product.getStock()
                            : 0;
                    return unitPrice.multiply(BigDecimal.valueOf(stock));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ActiveInventorySummaryResponse(totalStock, totalInventoryValue);
    }

    public FefoSnapshotResponse getFefoSnapshot() {
        List<FefoSnapshotItemResponse> items = batchService.getFefoSnapshot()
                .stream()
                .map(row -> new FefoSnapshotItemResponse(
                        row.getProductId(),
                        row.getProductCode(),
                        row.getProductName(),
                        row.getOperationalStock(),
                        row.getNextBatchCode(),
                        row.getNextExpirationDate(),
                        row.getActiveBatchesCount()
                ))
                .toList();

        return new FefoSnapshotResponse(
                OffsetDateTime.now(),
                items.size(),
                items
        );
    }

    private record MotionActorContext(Long userId, String userName, String userEmail, String userRole) {}
}
