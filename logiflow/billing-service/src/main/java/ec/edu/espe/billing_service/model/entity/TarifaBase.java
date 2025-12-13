package ec.edu.espe.billing_service.model.entity;
import lombok.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tarifas_base")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifaBase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(name = "tipo_entrega", nullable = false, unique = true)
    private String tipoEntrega;

    @Column(name = "tarifa_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaBase;

}
