package co.edu.corhuila.inventory_service.Service;


import co.edu.corhuila.inventory_service.Dto.MotionResponse;
import co.edu.corhuila.inventory_service.Repository.MotionRepository;
import co.edu.corhuila.inventory_service.Repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MotionService {

    private final MotionRepository motionRepository;

    public MotionService(MotionRepository motionRepository,
                         ProductRepository productRepository) {
        this.motionRepository = motionRepository;

    }



    public List<MotionResponse> listMotion() {
        return motionRepository.findAll()
                .stream()
                .map(MotionResponse::new)
                .toList();
    }


}
