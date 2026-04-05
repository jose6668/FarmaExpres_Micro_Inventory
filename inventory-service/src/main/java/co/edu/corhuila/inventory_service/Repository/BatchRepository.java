package co.edu.corhuila.inventory_service.Repository;

import co.edu.corhuila.inventory_service.Entity.Batch;
import co.edu.corhuila.inventory_service.Entity.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    interface FefoSnapshotProjection {
        Long getProductId();
        String getProductCode();
        String getProductName();
        Integer getOperationalStock();
        String getNextBatchCode();
        java.time.LocalDate getNextExpirationDate();
        Integer getActiveBatchesCount();
    }

    List<Batch> findByProductIdOrderByExpirationDateAsc(Long productId);

    @Query("""
            SELECT b
            FROM Batch b
            WHERE b.product.id = :productId
              AND b.status IN :statuses
              AND b.availableStock > 0
              AND b.expirationDate >= CURRENT_DATE
            ORDER BY b.createdAt ASC, b.id ASC
            """)
    List<Batch> findConsumableBatchesByProductIdOrderByCreatedAtAsc(Long productId, Collection<BatchStatus> statuses);

    boolean existsByProductIdAndBatchCodeIgnoreCase(Long productId, String batchCode);

    Optional<Batch> findByIdAndProductId(Long batchId, Long productId);

    @Query("""
            SELECT b
            FROM Batch b
            WHERE b.product.id = :productId
              AND b.status IN :statuses
              AND b.availableStock > 0
              AND b.expirationDate >= CURRENT_DATE
            ORDER BY b.expirationDate ASC, b.id ASC
            """)
    List<Batch> findConsumableBatchesByProductId(Long productId, Collection<BatchStatus> statuses);

    @Query(value = """
            WITH consumable_batches AS (
              SELECT
                b.product_id,
                b.batch_code,
                b.expiration_date::date AS expiration_date,
                b.available_stock,
                b.id AS batch_id
              FROM batch b
              WHERE b.status = 'ACTIVE'
                AND b.available_stock > 0
                AND b.expiration_date::date >= (CURRENT_TIMESTAMP AT TIME ZONE 'America/Bogota')::date
            ),
            ranked_batches AS (
              SELECT
                cb.*,
                ROW_NUMBER() OVER (
                  PARTITION BY cb.product_id
                  ORDER BY cb.expiration_date ASC, cb.batch_id ASC
                ) AS rn
              FROM consumable_batches cb
            ),
            agg AS (
              SELECT
                cb.product_id,
                SUM(cb.available_stock)::int AS operational_stock,
                COUNT(*)::int AS active_batches_count
              FROM consumable_batches cb
              GROUP BY cb.product_id
            ),
            next_batch AS (
              SELECT
                rb.product_id,
                rb.batch_code AS next_batch_code,
                rb.expiration_date AS next_expiration_date
              FROM ranked_batches rb
              WHERE rb.rn = 1
            )
            SELECT
              p.id AS productId,
              p.code AS productCode,
              p.name AS productName,
              COALESCE(a.operational_stock, 0) AS operationalStock,
              nb.next_batch_code AS nextBatchCode,
              nb.next_expiration_date AS nextExpirationDate,
              COALESCE(a.active_batches_count, 0) AS activeBatchesCount
            FROM product p
            LEFT JOIN agg a
              ON a.product_id = p.id
            LEFT JOIN next_batch nb
              ON nb.product_id = p.id
            WHERE p.asset = TRUE
            ORDER BY p.name ASC
            """, nativeQuery = true)
    List<FefoSnapshotProjection> findFefoSnapshot();
}
