package com.example.backendprojt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.backendprojt.entity.Categoria;
import com.example.backendprojt.entity.EstadoPedido;
import com.example.backendprojt.entity.Local;
import com.example.backendprojt.entity.Pedido;
import com.example.backendprojt.entity.Producto;
import com.example.backendprojt.entity.Repartidor;
import com.example.backendprojt.entity.Rol;
import com.example.backendprojt.entity.Usuario;
import com.example.backendprojt.repository.CategoriaRepository;
import com.example.backendprojt.repository.LocalRepository;
import com.example.backendprojt.repository.PedidoRepository;
import com.example.backendprojt.repository.ProductoRepository;
import com.example.backendprojt.repository.RepartidorRepository;
import com.example.backendprojt.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IntegracionTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired EntityManager em;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired LocalRepository localRepository;
    @Autowired CategoriaRepository categoriaRepository;
    @Autowired ProductoRepository productoRepository;
    @Autowired RepartidorRepository repartidorRepository;
    @Autowired PedidoRepository pedidoRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private Local localA;
    private Local localB;
    private Producto p1;
    private Producto p2;
    private Producto p3;
    private Producto p4;
    private Repartidor repartidor;
    private int telefono = 342500000;

    @BeforeEach
    void prepararDatos() {
        crearUsuario("admin", "Admin2026!", Rol.ADMIN);
        crearUsuario("operador", "Operador2026!", Rol.OPERADOR);

        localA = crearLocal("Local A");
        localB = crearLocal("Local B");
        Categoria categoria = new Categoria();
        categoria.setNombre("Pizzas");
        categoria = categoriaRepository.save(categoria);

        p1 = crearProducto(localA, categoria, "Producto 1", "10000.00", 10);
        p2 = crearProducto(localA, categoria, "Producto 2", "5000.00", 2);
        p3 = crearProducto(localB, categoria, "Producto B", "3000.00", 5);
        p4 = crearProducto(localA, categoria, "Producto 20000", "20000.00", 5);

        repartidor = new Repartidor();
        repartidor.setNombreCompleto("Carlos Benítez");
        repartidor.setActivo(true);
        repartidor = repartidorRepository.save(repartidor);
    }

    // ---------- Autenticación ----------

    @Test
    void loginCorrectoEIncorrecto() throws Exception {
        JsonNode ok = login("admin", "Admin2026!", 200);
        assertTrue(ok.get("token").asText().length() > 20);
        assertEquals("Administrador de Prueba", ok.get("nombreCompleto").asText());
        assertEquals("Administrador de Prueba", ok.get("nombre").asText());
        assertEquals("ADMIN", ok.get("rol").asText());

        JsonNode malo = login("admin", "incorrecta", 401);
        assertEquals("Usuario o contraseña incorrectos", malo.get("message").asText());
    }

    @Test
    void elHashDelAdministradorDeDataSqlCorrespondeALaClaveDocumentada() {
        String hash = "$2a$10$nlQuA4lskuCHKf20.SP0Y.qxRBSzebNuT5bVGy43ySrDtDMz9ryQ2";
        assertTrue(passwordEncoder.matches("Admin2026!", hash));
    }

    @Test
    void accesoALosEndpointsSegunElRol() throws Exception {
        mvc.perform(get("/api/admin/usuarios")).andExpect(status().isUnauthorized());

        mvc.perform(get("/api/admin/usuarios").header(HttpHeaders.AUTHORIZATION, bearer("operador", "Operador2026!")))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/admin/usuarios").header(HttpHeaders.AUTHORIZATION, bearer("admin", "Admin2026!")))
                .andExpect(status().isOk());

        mvc.perform(get("/api/pedidos").header(HttpHeaders.AUTHORIZATION, bearer("operador", "Operador2026!")))
                .andExpect(status().isOk());

        mvc.perform(get("/api/pedidos")).andExpect(status().isUnauthorized());
    }

    // ---------- Creación de pedidos ----------

    @Test
    void creaPedidoValidoConCodigoYDescuentaStock() throws Exception {
        JsonNode pedido = crearPedido(localA, List.of(item(p1, 1), item(p2, 1)));

        assertEquals("RECIBIDO", pedido.get("estado").asText());
        assertTrue(pedido.get("codigoSeguimiento").asText().matches("^[A-Z0-9]{8}$"));
        assertEquals(0, new BigDecimal("15000.00").compareTo(pedido.get("subtotal").decimalValue()));
        assertEquals(0, BigDecimal.ZERO.compareTo(pedido.get("descuento").decimalValue()));
        assertEquals(0, new BigDecimal("16500.00").compareTo(pedido.get("total").decimalValue()));

        em.flush();
        assertEquals(9, productoRepository.findById(p1.getId()).orElseThrow().getStock());
        assertEquals(1, productoRepository.findById(p2.getId()).orElseThrow().getStock());
    }

    @Test
    void calculaElDescuentoYRespetaElBordeDeLosVeinteMil() throws Exception {
        JsonNode sinDescuento = crearPedido(localA, List.of(item(p4, 1)));
        assertEquals(0, new BigDecimal("20000.00").compareTo(sinDescuento.get("subtotal").decimalValue()));
        assertEquals(0, BigDecimal.ZERO.compareTo(sinDescuento.get("descuento").decimalValue()));
        assertEquals(0, new BigDecimal("21500.00").compareTo(sinDescuento.get("total").decimalValue()));

        JsonNode conDescuento = crearPedido(localA, List.of(item(p1, 2), item(p2, 1)));
        assertEquals(0, new BigDecimal("25000.00").compareTo(conDescuento.get("subtotal").decimalValue()));
        assertEquals(0, new BigDecimal("2500.00").compareTo(conDescuento.get("descuento").decimalValue()));
        assertEquals(0, new BigDecimal("24000.00").compareTo(conDescuento.get("total").decimalValue()));
    }

    @Test
    void rechazaProductosDeDistintosLocales() throws Exception {
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoPedido(localA, List.of(item(p1, 1), item(p3, 1)))))
                .andExpect(status().isBadRequest())
                .andExpect(status().is4xxClientError());

        em.flush();
        assertEquals(10, productoRepository.findById(p1.getId()).orElseThrow().getStock());
        assertEquals(5, productoRepository.findById(p3.getId()).orElseThrow().getStock());
    }

    @Test
    void rechazaCantidadesSuperioresAlStock() throws Exception {
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoPedido(localA, List.of(item(p2, 3)))))
                .andExpect(status().isConflict());

        em.flush();
        assertEquals(2, productoRepository.findById(p2.getId()).orElseThrow().getStock());
    }

    // ---------- Cancelación y transiciones ----------

    @Test
    void cancelarReponeElStockYEsIdempotente() throws Exception {
        JsonNode creado = crearPedido(localA, List.of(item(p1, 2)));
        String codigo = creado.get("codigoSeguimiento").asText();
        Integer id = idDe(codigo);

        em.flush();
        assertEquals(8, productoRepository.findById(p1.getId()).orElseThrow().getStock());

        JsonNode cancelado = cancelar(id, 200);
        assertEquals("CANCELADO", cancelado.get("estado").asText());

        em.flush();
        em.clear();
        assertEquals(10, productoRepository.findById(p1.getId()).orElseThrow().getStock());

        // Un reintento devuelve el mismo estado sin volver a reponer stock (RN-04).
        JsonNode reintento = cancelar(id, 200);
        assertEquals("CANCELADO", reintento.get("estado").asText());

        em.flush();
        em.clear();
        assertEquals(10, productoRepository.findById(p1.getId()).orElseThrow().getStock());
    }

    @Test
    void rechazaProductosDuplicadosPorNombreEnElMismoLocal() throws Exception {
        String cuerpo = om.writeValueAsString(Map.of(
                "localId", localA.getId(),
                "categoriaId", p1.getCategoria().getId(),
                "nombre", "Producto 1",
                "precio", new BigDecimal("1234.00"),
                "stock", 5));

        mvc.perform(post("/api/admin/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo)
                        .header(HttpHeaders.AUTHORIZATION, bearer("admin", "Admin2026!")))
                .andExpect(status().isConflict());

        // El mismo nombre en otro local sí está permitido.
        String otroLocal = om.writeValueAsString(Map.of(
                "localId", localB.getId(),
                "categoriaId", p1.getCategoria().getId(),
                "nombre", "Producto 1",
                "precio", new BigDecimal("1234.00"),
                "stock", 5));

        mvc.perform(post("/api/admin/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(otroLocal)
                        .header(HttpHeaders.AUTHORIZATION, bearer("admin", "Admin2026!")))
                .andExpect(status().isCreated());
    }

    @Test
    void unAdministradorNoPuedeQuitarseElRolNiDesactivarse() throws Exception {
        Integer adminId = usuarioRepository.findByUsername("admin").orElseThrow().getId();
        String admin = bearer("admin", "Admin2026!");

        String quitarRol = om.writeValueAsString(Map.of(
                "nombreCompleto", "Administrador de Prueba",
                "rol", "OPERADOR"));

        mvc.perform(put("/api/admin/usuarios/{id}", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(quitarRol)
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isUnprocessableEntity());

        String desactivarse = om.writeValueAsString(Map.of(
                "nombreCompleto", "Administrador de Prueba",
                "rol", "ADMIN",
                "activo", false));

        mvc.perform(put("/api/admin/usuarios/{id}", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(desactivarse)
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/admin/usuarios/{id}/desactivar", adminId)
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void rechazaTransicionesDeEstadoInvalidas() throws Exception {
        Integer id = idDe(crearPedido(localA, List.of(item(p1, 1))).get("codigoSeguimiento").asText());

        assertEquals("EN_PREPARACION", avanzar(id, null, 200).get("estado").asText());
        assertEquals("EN_CAMINO", avanzar(id, repartidor.getId(), 200).get("estado").asText());

        mvc.perform(post("/api/pedidos/{id}/cancelar", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer("operador", "Operador2026!")))
                .andExpect(status().isConflict());

        assertEquals("ENTREGADO", avanzar(id, null, 200).get("estado").asText());

        mvc.perform(post("/api/pedidos/{id}/avanzar", id).contentType(MediaType.APPLICATION_JSON).content("{}")
                        .header(HttpHeaders.AUTHORIZATION, bearer("operador", "Operador2026!")))
                .andExpect(status().isConflict());
    }

    @Test
    void noPermiteAsignarUnCuartoPedidoAlMismoRepartidor() throws Exception {
        for (int i = 1; i <= 4; i++) {
            Integer id = idDe(crearPedido(localA, List.of(item(p1, 1))).get("codigoSeguimiento").asText());
            assertEquals("EN_PREPARACION", avanzar(id, null, 200).get("estado").asText());
            int esperado = i <= 3 ? 200 : 409;
            avanzar(id, repartidor.getId(), esperado);
        }
        em.flush();
        assertEquals(3, pedidoRepository.countByRepartidor_IdAndEstado(repartidor.getId(), EstadoPedido.EN_CAMINO));
    }

    // ---------- Reporte de ventas ----------

    @Test
    void elReporteSoloIncluyeEntregadosYSinElCostoDeEnvio() throws Exception {
        Integer entregado = idDe(crearPedido(localA, List.of(item(p1, 2), item(p2, 1)))
                .get("codigoSeguimiento").asText());
        avanzar(entregado, null, 200);
        avanzar(entregado, repartidor.getId(), 200);
        avanzar(entregado, null, 200);

        // Pedido de otro local que queda sin entregar: no debe sumar.
        crearPedido(localB, List.of(item(p3, 1)));

        // Pedido cancelado: tampoco debe sumar.
        Integer cancelado = idDe(crearPedido(localA, List.of(item(p4, 1))).get("codigoSeguimiento").asText());
        cancelar(cancelado, 200);

        String hoy = LocalDate.now().toString();
        MvcResult resultado = mvc.perform(get("/api/admin/ventas")
                        .param("desde", hoy).param("hasta", hoy)
                        .header(HttpHeaders.AUTHORIZATION, bearer("admin", "Admin2026!")))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode filas = om.readTree(resultado.getResponse().getContentAsString());

        JsonNode a = fila(filas, "Local A");
        assertEquals(1, a.get("cantidadPedidos").asLong());
        assertEquals(0, new BigDecimal("22500.00").compareTo(a.get("montoVendido").decimalValue()));

        JsonNode b = fila(filas, "Local B");
        assertEquals(0, b.get("cantidadPedidos").asLong());
        assertEquals(0, BigDecimal.ZERO.compareTo(b.get("montoVendido").decimalValue()));

        MvcResult reporte = mvc.perform(get("/api/ventas/por-local")
                        .param("desde", hoy).param("hasta", hoy)
                        .header(HttpHeaders.AUTHORIZATION, bearer("admin", "Admin2026!")))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = om.readTree(reporte.getResponse().getContentAsString());
        assertEquals(1, json.get("totalPedidos").asLong());
        assertEquals(0, new BigDecimal("22500.00").compareTo(json.get("totalVendido").decimalValue()));
    }

    @Test
    void validaParametrosYRolesDelReporteDeVentas() throws Exception {
        String admin = bearer("admin", "Admin2026!");
        String operador = bearer("operador", "Operador2026!");
        String hoy = LocalDate.now().toString();

        mvc.perform(get("/api/admin/ventas").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/admin/ventas")
                        .param("desde", "no-es-fecha").param("hasta", hoy)
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/admin/ventas")
                        .param("desde", hoy).param("hasta", LocalDate.now().minusDays(1).toString())
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(get("/api/admin/ventas")
                        .param("desde", hoy).param("hasta", hoy)
                        .header(HttpHeaders.AUTHORIZATION, operador))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/ventas/por-local")
                        .param("desde", hoy).param("hasta", hoy)
                        .header(HttpHeaders.AUTHORIZATION, operador))
                .andExpect(status().isForbidden());
    }

    // ---------- Auxiliares ----------

    private Usuario crearUsuario(String username, String password, Rol rol) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setNombreCompleto(rol == Rol.ADMIN ? "Administrador de Prueba" : "Operador de Prueba");
        u.setRol(rol);
        u.setActivo(true);
        return usuarioRepository.save(u);
    }

    private Local crearLocal(String nombre) {
        Local l = new Local();
        l.setNombre(nombre);
        l.setActivo(true);
        return localRepository.save(l);
    }

    private Producto crearProducto(Local local, Categoria categoria, String nombre, String precio, int stock) {
        Producto p = new Producto();
        p.setLocal(local);
        p.setCategoria(categoria);
        p.setNombre(nombre);
        p.setPrecio(new BigDecimal(precio));
        p.setStock(stock);
        p.setActivo(true);
        return productoRepository.save(p);
    }

    private Map<String, Object> item(Producto producto, int cantidad) {
        return Map.of("productoId", producto.getId(), "cantidad", cantidad);
    }

    private String cuerpoPedido(Local local, List<Map<String, Object>> items) {
        telefono++;
        try {
            return om.writeValueAsString(Map.of(
                    "localId", local.getId(),
                    "nombre", "Cliente de Prueba",
                    "telefono", String.valueOf(telefono),
                    "direccionEntrega", "Calle 123",
                    "items", items));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private JsonNode crearPedido(Local local, List<Map<String, Object>> items) throws Exception {
        MvcResult result = mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoPedido(local, items)))
                .andExpect(status().isCreated())
                .andReturn();
        return om.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode avanzar(Integer id, Integer repartidorId, int estadoEsperado) throws Exception {
        String cuerpo = repartidorId == null ? "{}" : om.writeValueAsString(Map.of("repartidorId", repartidorId));
        MvcResult result = mvc.perform(post("/api/pedidos/{id}/avanzar", id)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo)
                        .header(HttpHeaders.AUTHORIZATION, bearer("operador", "Operador2026!")))
                .andExpect(status().is(estadoEsperado))
                .andReturn();
        return om.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode cancelar(Integer id, int estadoEsperado) throws Exception {
        MvcResult result = mvc.perform(post("/api/pedidos/{id}/cancelar", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer("operador", "Operador2026!")))
                .andExpect(status().is(estadoEsperado))
                .andReturn();
        return om.readTree(result.getResponse().getContentAsString());
    }

    private Integer idDe(String codigo) {
        Pedido pedido = pedidoRepository.buscarPorCodigo(codigo).orElseThrow();
        return pedido.getId();
    }

    private JsonNode login(String username, String password, int esperado) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().is(esperado))
                .andReturn();
        return om.readTree(result.getResponse().getContentAsString());
    }

    private String bearer(String username, String password) throws Exception {
        return "Bearer " + login(username, password, 200).get("token").asText();
    }

    private JsonNode fila(JsonNode filas, String nombre) {
        for (JsonNode fila : filas) {
            if (nombre.equals(fila.get("localNombre").asText())) return fila;
        }
        assertNotNull(null, "No hay fila para " + nombre);
        return null;
    }
}
