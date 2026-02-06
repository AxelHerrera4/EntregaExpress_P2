package com.logiflow.pedidoservice.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests para la clase Direccion")
class DireccionTest {

    private Direccion direccion;

    @BeforeEach
    void setUp() {
        direccion = Direccion.builder()
                .calle("Av Principal")
                .numero("123")
                .ciudad("Quito")
                .provincia("Pichincha")
                .build();
    }

    @Test
    @DisplayName("Debería crear una dirección con todos los campos")
    void deberiaCrearDireccionConTodosLosCampos() {
        assertNotNull(direccion);
        assertEquals("Av Principal", direccion.getCalle());
        assertEquals("123", direccion.getNumero());
        assertEquals("Quito", direccion.getCiudad());
        assertEquals("Pichincha", direccion.getProvincia());
    }

    @Test
    @DisplayName("Debería permitir construir con builder pattern")
    void deberiaPermitirBuilderPattern() {
        Direccion direccionBuilder = Direccion.builder()
                .calle("Calle Test")
                .numero("456A")
                .ciudad("Guayaquil")
                .provincia("Guayas")
                .build();

        assertNotNull(direccionBuilder);
        assertEquals("Calle Test", direccionBuilder.getCalle());
        assertEquals("456A", direccionBuilder.getNumero());
        assertEquals("Guayaquil", direccionBuilder.getCiudad());
        assertEquals("Guayas", direccionBuilder.getProvincia());
    }

    @Test
    @DisplayName("Debería soportar números alfanuméricos")
    void deberiaSoportarNumerosAlfanumericos() {
        direccion.setNumero("123A");
        assertEquals("123A", direccion.getNumero());

        direccion.setNumero("B456");
        assertEquals("B456", direccion.getNumero());

        direccion.setNumero("12C34");
        assertEquals("12C34", direccion.getNumero());
    }

    @Test
    @DisplayName("Debería permitir calles con espacios y números")
    void deberiaPermitirCallesConEspaciosYNumeros() {
        direccion.setCalle("Av 9 de Octubre");
        assertEquals("Av 9 de Octubre", direccion.getCalle());

        direccion.setCalle("Calle 123 Norte");
        assertEquals("Calle 123 Norte", direccion.getCalle());
    }

    @Test
    @DisplayName("Debería ser equivalente usando equals y hashCode")
    void deberiaSerEquivalenteUsandoEqualsYHashCode() {
        Direccion direccion1 = Direccion.builder()
                .calle("Av Principal")
                .numero("123")
                .ciudad("Quito")
                .provincia("Pichincha")
                .build();

        Direccion direccion2 = Direccion.builder()
                .calle("Av Principal")
                .numero("123")
                .ciudad("Quito")
                .provincia("Pichincha")
                .build();

        assertEquals(direccion1, direccion2);
        assertEquals(direccion1.hashCode(), direccion2.hashCode());
    }

    @Test
    @DisplayName("Debería ser diferente con datos diferentes")
    void deberiaSerDiferenteConDatosDiferentes() {
        Direccion direccion2 = Direccion.builder()
                .calle("Av Secundaria")
                .numero("123")
                .ciudad("Quito")
                .provincia("Pichincha")
                .build();

        assertNotEquals(direccion, direccion2);
    }

    @Test
    @DisplayName("Debería generar toString legible")
    void deberiaGenerarToStringLegible() {
        String toString = direccion.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("Av Principal"));
        assertTrue(toString.contains("123"));
        assertTrue(toString.contains("Quito"));
        assertTrue(toString.contains("Pichincha"));
    }

    @Test
    @DisplayName("Debería crear direccion sin args constructor")
    void deberiaCrearDireccionSinArgsConstructor() {
        Direccion direccionVacia = new Direccion();
        assertNotNull(direccionVacia);
        assertNull(direccionVacia.getCalle());
        assertNull(direccionVacia.getNumero());
        assertNull(direccionVacia.getCiudad());
        assertNull(direccionVacia.getProvincia());
    }

    @Test
    @DisplayName("Debería crear direccion con all args constructor")
    void deberiaCrearDireccionConAllArgsConstructor() {
        Direccion direccionCompleta = new Direccion("Calle Test", "789", "Cuenca", "Azuay", -2.9001, -79.0059);

        assertNotNull(direccionCompleta);
        assertEquals("Calle Test", direccionCompleta.getCalle());
        assertEquals("789", direccionCompleta.getNumero());
        assertEquals("Cuenca", direccionCompleta.getCiudad());
        assertEquals("Azuay", direccionCompleta.getProvincia());
        assertEquals(-2.9001, direccionCompleta.getLatitud());
        assertEquals(-79.0059, direccionCompleta.getLongitud());
    }
}
