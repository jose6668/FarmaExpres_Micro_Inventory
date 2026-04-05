package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Dto.ActiveInventorySummaryResponse;
import co.edu.corhuila.inventory_service.Dto.ActiveInventoryTableItemResponse;
import co.edu.corhuila.inventory_service.Dto.LowStockReportItemResponse;
import co.edu.corhuila.inventory_service.Entity.Product;
import co.edu.corhuila.inventory_service.Repository.MotionRepository;
import co.edu.corhuila.inventory_service.Repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MotionRepository motionRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldReturnActiveInventorySummary() {
        Product productOne = new Product(
                "Acetaminofen 500mg",
                "ACM-001",
                100,
                new BigDecimal("2500"),
                LocalDate.of(2027, 12, 31),
                20
        );

        Product productTwo = new Product(
                "Ibuprofeno 400mg",
                "IBU-001",
                80,
                new BigDecimal("3200"),
                LocalDate.of(2027, 6, 30),
                15
        );

        Product productThree = new Product(
                "Loratadina 10mg",
                "LOR-001",
                60,
                new BigDecimal("6400"),
                LocalDate.of(2027, 11, 20),
                10
        );

        when(productRepository.findByActiveTrue())
                .thenReturn(List.of(productOne, productTwo, productThree));

        ActiveInventorySummaryResponse response = productService.getActiveInventorySummary();

        assertEquals(240, response.getTotalStock());
        assertEquals(new BigDecimal("890000"), response.getTotalInventoryValue());
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void shouldReturnZeroSummaryWhenThereAreNoActiveProducts() {
        when(productRepository.findByActiveTrue()).thenReturn(List.of());

        ActiveInventorySummaryResponse response = productService.getActiveInventorySummary();

        assertEquals(0, response.getTotalStock());
        assertEquals(BigDecimal.ZERO, response.getTotalInventoryValue());
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void shouldReturnActiveInventoryTable() {
        Product productOne = new Product(
                "Ibuprofeno 400mg",
                "IBU-001",
                80,
                new BigDecimal("3200"),
                LocalDate.of(2027, 6, 30),
                15
        );

        Product productTwo = new Product(
                "Losartan 50 mg",
                "LOS-001",
                75,
                new BigDecimal("14100"),
                LocalDate.of(2027, 8, 10),
                20
        );

        when(productRepository.findByActiveTrue())
                .thenReturn(List.of(productOne, productTwo));

        List<ActiveInventoryTableItemResponse> response = productService.getActiveInventoryTable();

        assertEquals(2, response.size());
        assertEquals("IBU-001", response.get(0).getCode());
        assertEquals("Ibuprofeno 400mg", response.get(0).getName());
        assertEquals(80, response.get(0).getStock());
        assertEquals(new BigDecimal("3200"), response.get(0).getUnitPrice());
        assertEquals(new BigDecimal("256000"), response.get(0).getTotalValue());
        assertEquals("LOS-001", response.get(1).getCode());
        assertEquals(new BigDecimal("1057500"), response.get(1).getTotalValue());
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void shouldReturnEmptyActiveInventoryTableWhenThereAreNoProducts() {
        when(productRepository.findByActiveTrue()).thenReturn(List.of());

        List<ActiveInventoryTableItemResponse> response = productService.getActiveInventoryTable();

        assertEquals(List.of(), response);
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void shouldReturnOnlyCriticalLowStockProducts() {
        Product criticalProduct = new Product(
                "Amoxicillin 500mg",
                "AMX-001",
                5,
                new BigDecimal("3000"),
                LocalDate.of(2027, 12, 31),
                10
        );
        criticalProduct.setId(1L);

        Product alertProduct = new Product(
                "Acetaminophen 500mg",
                "ACM-001",
                19,
                new BigDecimal("2500"),
                LocalDate.of(2027, 12, 31),
                20
        );
        alertProduct.setId(2L);

        Product inactiveCriticalProduct = new Product(
                "Ibuprofeno 400mg",
                "IBU-001",
                2,
                new BigDecimal("2800"),
                LocalDate.of(2027, 11, 15),
                8
        );
        inactiveCriticalProduct.setId(3L);
        inactiveCriticalProduct.setActive(false);

        when(productRepository.findByActiveTrue())
                .thenReturn(List.of(criticalProduct, alertProduct));

        List<LowStockReportItemResponse> response = productService.getCriticalLowStockProducts();

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals("AMX-001", response.get(0).getCode());
        assertEquals("Amoxicillin 500mg", response.get(0).getName());
        assertEquals(5, response.get(0).getStock());
        assertEquals(10, response.get(0).getMinimumStock());
        assertEquals(50, response.get(0).getCoverage());
        assertEquals("50%", response.get(0).getCoverageLabel());
        assertEquals("Critico", response.get(0).getStatus());
        assertEquals("Reponer 15 unidades", response.get(0).getSuggestion());
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void shouldReturnCriticalProductsOrderedByCoverageAndName() {
        Product secondByCoverage = new Product(
                "Vitamina C 1g",
                "VIT-001",
                5,
                new BigDecimal("1500"),
                LocalDate.of(2027, 10, 10),
                10
        );
        secondByCoverage.setId(2L);

        Product firstByCoverage = new Product(
                "Loratadina 10mg",
                "LOR-001",
                2,
                new BigDecimal("1800"),
                LocalDate.of(2027, 9, 15),
                10
        );
        firstByCoverage.setId(1L);

        when(productRepository.findByActiveTrue())
                .thenReturn(List.of(secondByCoverage, firstByCoverage));

        List<LowStockReportItemResponse> response = productService.getCriticalLowStockProducts();

        assertEquals(2, response.size());
        assertEquals("LOR-001", response.get(0).getCode());
        assertEquals(20, response.get(0).getCoverage());
        assertEquals("VIT-001", response.get(1).getCode());
        assertEquals(50, response.get(1).getCoverage());
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void shouldReturnOnlyAlertLowStockProducts() {
        Product criticalProduct = new Product(
                "Amoxicillin 500mg",
                "AMX-001",
                5,
                new BigDecimal("3000"),
                LocalDate.of(2027, 12, 31),
                10
        );
        criticalProduct.setId(1L);

        Product alertProduct = new Product(
                "Acetaminophen 500mg",
                "ACM-001",
                19,
                new BigDecimal("2500"),
                LocalDate.of(2027, 12, 31),
                20
        );
        alertProduct.setId(2L);

        Product alertProductTwo = new Product(
                "Loratadina 10mg",
                "LOR-001",
                6,
                new BigDecimal("1800"),
                LocalDate.of(2027, 10, 10),
                10
        );
        alertProductTwo.setId(3L);

        when(productRepository.findByActiveTrue())
                .thenReturn(List.of(criticalProduct, alertProduct, alertProductTwo));

        List<LowStockReportItemResponse> response = productService.getAlertLowStockProducts();

        assertEquals(2, response.size());
        assertEquals("LOR-001", response.get(0).getCode());
        assertEquals(60, response.get(0).getCoverage());
        assertEquals("Alerta", response.get(0).getStatus());
        assertEquals("Reponer 14 unidades", response.get(0).getSuggestion());
        assertEquals("ACM-001", response.get(1).getCode());
        assertEquals(95, response.get(1).getCoverage());
        assertEquals("95%", response.get(1).getCoverageLabel());
        assertEquals("Alerta", response.get(1).getStatus());
        assertEquals("Reponer 21 unidades", response.get(1).getSuggestion());
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void shouldReturnAllLowStockProductsOrderedByPriorityAndCoverage() {
        Product criticalProduct = new Product(
                "Amoxicillin 500mg",
                "AMX-001",
                5,
                new BigDecimal("3000"),
                LocalDate.of(2027, 12, 31),
                10
        );
        criticalProduct.setId(1L);

        Product moreCriticalProduct = new Product(
                "Loratadina 10mg",
                "LOR-001",
                2,
                new BigDecimal("1800"),
                LocalDate.of(2027, 10, 10),
                10
        );
        moreCriticalProduct.setId(2L);

        Product alertProduct = new Product(
                "Acetaminophen 500mg",
                "ACM-001",
                19,
                new BigDecimal("2500"),
                LocalDate.of(2027, 12, 31),
                20
        );
        alertProduct.setId(3L);

        Product alertProductTwo = new Product(
                "Cetirizina 10mg",
                "CET-001",
                6,
                new BigDecimal("2200"),
                LocalDate.of(2027, 9, 12),
                10
        );
        alertProductTwo.setId(4L);

        Product healthyProduct = new Product(
                "Ibuprofeno 400mg",
                "IBU-001",
                40,
                new BigDecimal("2800"),
                LocalDate.of(2027, 11, 15),
                8
        );
        healthyProduct.setId(5L);

        when(productRepository.findByActiveTrue())
                .thenReturn(List.of(alertProduct, healthyProduct, criticalProduct, alertProductTwo, moreCriticalProduct));

        List<LowStockReportItemResponse> response = productService.getAllLowStockProducts();

        assertEquals(4, response.size());
        assertEquals("LOR-001", response.get(0).getCode());
        assertEquals("Critico", response.get(0).getStatus());
        assertEquals("AMX-001", response.get(1).getCode());
        assertEquals("Critico", response.get(1).getStatus());
        assertEquals("CET-001", response.get(2).getCode());
        assertEquals("Alerta", response.get(2).getStatus());
        assertEquals("ACM-001", response.get(3).getCode());
        assertEquals("Alerta", response.get(3).getStatus());
        verify(productRepository).findByActiveTrue();
    }
}
