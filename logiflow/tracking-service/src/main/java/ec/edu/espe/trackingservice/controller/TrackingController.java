package ec.edu.espe.trackingservice.controller;

import ec.edu.espe.trackingservice.dto.UbicacionDTO;
import ec.edu.espe.trackingservice.service.TrackingProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Slf4j
public class TrackingController {

    private final TrackingProducer trackingProducer;

    @PostMapping("/track")
    public ResponseEntity<String> trackUbicacion(@RequestBody UbicacionDTO ubicacionDTO) {
        log.info("Recibida solicitud de tracking: {}", ubicacionDTO);
        trackingProducer.enviarUbicacion(ubicacionDTO);
        return ResponseEntity.ok("Ubicación enviada correctamente");
    }
}

