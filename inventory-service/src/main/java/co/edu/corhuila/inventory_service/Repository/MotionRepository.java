package co.edu.corhuila.inventory_service.Repository;


import co.edu.corhuila.inventory_service.Entity.Motion;
import co.edu.corhuila.inventory_service.Entity.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MotionRepository extends JpaRepository<Motion, Long> {



    List<Motion> findByType(MovementType Type);
}
