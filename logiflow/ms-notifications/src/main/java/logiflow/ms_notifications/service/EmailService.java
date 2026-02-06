package logiflow.ms_notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@logiflow.com");

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public String buildOrderCreatedEmailBody(String customerName, String orderId, Double totalAmount) {
        return String.format(
                "Estimado/a %s,\n\n" +
                "Su pedido ha sido creado exitosamente.\n\n" +
                "Detalles del pedido:\n" +
                "ID del Pedido: %s\n" +
                "Monto Total: $%.2f\n\n" +
                "Gracias por su compra.\n\n" +
                "Saludos,\n" +
                "Equipo LogiFlow",
                customerName, orderId, totalAmount
        );
    }

    public String buildOrderStatusUpdatedEmailBody(String customerName, String orderId,
                                                   String previousStatus, String newStatus) {
        return String.format(
                "Estimado/a %s,\n\n" +
                "El estado de su pedido ha sido actualizado.\n\n" +
                "Detalles del pedido:\n" +
                "ID del Pedido: %s\n" +
                "Estado anterior: %s\n" +
                "Nuevo estado: %s\n\n" +
                "Gracias por su preferencia.\n\n" +
                "Saludos,\n" +
                "Equipo LogiFlow",
                customerName, orderId, previousStatus, newStatus
        );
    }
}

