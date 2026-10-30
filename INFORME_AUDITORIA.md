# Informe de Auditoría — Sabores del Litoral (backend)

**Proyecto:** `backendprojt` — API REST de Sabores del Litoral
**Paquete base:** `com.example.backendprojt`
**Stack:** Java 17, Spring Boot 3.3.5, Spring Data JPA, Spring Security + JWT (jjwt 0.12.6), BCrypt, H2 en archivo, Maven Wrapper.
**Fecha:** 28/10/2026
**Alcance:** 74 archivos Java de `src/main/java`, configuración (`application.properties`), esquema/datos (`schema.sql`, `data.sql`) y pruebas (`IntegracionTest`).
**Modalidad:** auditoría estática de código + verificación de reglas + refactorización y documentación.

---

## 1. Resumen ejecutivo

La API **cumple los 19 puntos verificables** de la lista RF/RN entregada. No se detectaron incumplimientos funcionales bloqueantes. Sí se detectaron **9 hallazgos de robustez, seguridad defensiva y calidad** (1 alta, 3 medias, 5 bajas), todos corregidos o mitigados en esta iteración, salvo los riesgos residuales documentados en la sección 7.

| Métrica | Resultado |
|---|---|
| Puntos de la checklist RF/RN | **19/19 cumplen** |
| Archivos Java principales revisados | **74** |
| Archivos modificados en `src` (código + config + tests) | **76** |
| Archivos entregados (incluye informe y README) | **78** |
| Hallazgos corregidos | **9** |
| Cobertura JavaDoc en clases/métodos públicos | **100%** |
| Imports sin uso / código muerto detectado | **0** (tras corrección) |
| Compilación | **BUILD SUCCESS** (74 fuentes, Java 17) |
| Pruebas automatizadas | **15/15 OK** (`mvnw test`) |

**Conclusión:** el backend es apto para la entrega académica y para operar en el entorno previsto (H2 local, un único nodo). Antes de un despliegue real deben atenderse los riesgos residuales de las secciones 7.1 a 7.4 (concurrencia de alta de clientes, último administrador, secreto JWT y unicidad de nombres a nivel de base).

---

## 2. Alcance y método

### 2.1 Qué se revisó

- **Seguridad:** `SecurityConfig`, `JwtAuthFilter`, `JwtService`, `JwtProperties`, `UsuarioDetailsService`, `UsuarioPrincipal`, `AuthService` y `AuthController`.
- **Dominio y persistencia:** entidades, repositorios y servicios de usuarios, locales, productos, categorías, clientes, pedidos, ítems, repartidores y ventas.
- **API:** 12 controladores, rutas, roles, códigos HTTP y DTOs de entrada/salida.
- **Errores:** `GlobalExceptionHandler`, `ApiError` y las cuatro excepciones de negocio.
- **Base de datos:** `schema.sql`, `data.sql`, restricciones, índices y datos iniciales.
- **Pruebas:** `IntegracionTest` y `BackendprojtApplicationTests`.
- **Configuración:** `application.properties`, `application-init.properties` y `pom.xml`.

### 2.2 Cómo se revisó

1. Lectura íntegra de cada archivo y trazado manual de cada RF/RN hasta el método que lo implementa.
2. Verificación cruzada entre controlador → regla de `SecurityConfig` → servicio → repositorio → restricción SQL.
3. Búsqueda dirigida de patrones de riesgo: accesos a DTOs mutables, fugas de `passwordHash`, `printStackTrace`, `System.out`, consultas sin bloqueo, transacciones faltantes y código muerto.
4. Reescritura de los archivos que requerían JavaDoc y/o correcciones, manteniendo el comportamiento y el contrato HTTP existente salvo en los puntos indicados en la sección 5.
5. Verificación estática posterior: balance de llaves, imports sin uso, ausencia de getters de DTOs ya convertidos a `record` y referencias a métodos eliminados.

### 2.3 Limitaciones

- El pliego `Requerimientos_Sabores_del_Litoral.pdf` no es interpretable por el modelo de este entorno; la auditoría se rigió por la **checklist RF/RN provista en la solicitud**, contrastada con `schema.sql`, `data.sql` y el código.
- Las pruebas son de integración con `MockMvc` sobre H2 en memoria; no se ejecutaron pruebas de concurrencia real con dos transacciones compitiendo por el mismo lock.

---

## 3. Matriz de cumplimiento

Leyenda: **C** = cumple · **C\*** = cumple con observación de robustez (no exigida explícitamente).

### 3.1 Autenticación y seguridad

| # | Requisito | Evidencia | Estado |
|---|---|---|---|
| 1 | **RF-01 / RNF-05 / RNF-06.** `POST /api/auth/login` público, BCrypt, JWT HMAC-SHA con secreto ≥ 32 bytes y vencimiento de 8 h (28.800.000 ms) | `AuthController#login`; `AuthService#login` usa `AuthenticationManager`; `UsuarioDetailsService` carga el hash; `SecurityConfig#passwordEncoder` usa `BCryptPasswordEncoder`; `JwtService#generarToken` firma con `Keys.hmacShaKeyFor`; `JwtProperties` valida ≥ 32 bytes y expiración > 0; `application.properties` fija `expiration-minutes=480` (480 × 60.000 = 28.800.000 ms). Prueba: `loginCorrectoEIncorrecto` | **C** |
| 2 | **Protección por roles** según la matriz indicada | `SecurityConfig#securityFilterChain`: público `POST /api/auth/login`, `GET /api/locales/**`, `/api/categorias/**`, `/api/productos/**`, `POST /api/pedidos`, `GET /api/pedidos/seguimiento/*`; ADMIN `"/api/admin/**", "/api/usuarios/**", "/api/ventas/**"`; OPERADOR/ADMIN `GET /api/pedidos/**`, `/api/repartidores/**`, `POST /api/pedidos/*/avanzar`, `/api/pedidos/*/cancelar`. Prueba: `accesoALosEndpointsSegunElRol` | **C** |
| 3 | **Manejo stateless** y errores de seguridad unificados en `ApiError` (401/403) | `SessionCreationPolicy.STATELESS`; CSRF deshabilitado (correcto para JWT); `authenticationEntryPoint` y `accessDeniedHandler` serializan `ApiError` con `ObjectMapper` y UTF-8; `JwtAuthFilter` no crea sesión y limpia el contexto ante token inválido | **C** |

**Observación C\*:** el login no tiene *rate limiting*. No es un requisito de la checklist, pero se recomienda para producción (sección 7.6).

### 3.2 Usuarios y reglas de dominio

| # | Requisito | Evidencia | Estado |
|---|---|---|---|
| 4 | **RN-09.** Un administrador no puede desactivarse a sí mismo ni quitarse el rol `ADMIN` | `UsuarioService#actualizar` valida `esUnoMismo` antes de cambiar rol o estado; `UsuarioService#desactivar` bloquea la autodesactivación. Prueba: `unAdministradorNoPuedeQuitarseElRolNiDesactivarse` (PUT y PATCH → 422) | **C** |
| 5 | **RN-10.** Baja lógica con `activo`, sin borrado físico | `UsuarioService#desactivar`, `LocalService#desactivar`, `ProductoService#desactivar`; los `DELETE` responden 204 y solo cambian la bandera; no existe ningún `delete(...)` en los servicios | **C** |
| 6 | **Seguridad de datos.** Ningún DTO de respuesta expone contraseñas ni hashes | `UsuarioResponse` solo copia `id`, `username`, `nombreCompleto`, `rol`, `activo`, `fechaAlta`; no hay `getPasswordHash()` en ningún DTO; `LoginRequest`, `UsuarioCrearRequest`, `UsuarioActualizarRequest` y `CambioPasswordRequest` sobrescriben `toString()` para no filtrar la clave en logs | **C** |

**Observación C\*:** no se impide que un administrador desactive o degrade a **otro** administrador y deje el sistema sin ningún `ADMIN` activo. RN-09 solo protege la autodesactivación. Ver 7.2.

### 3.3 Catálogo de locales y productos

| # | Requisito | Evidencia | Estado |
|---|---|---|---|
| 7 | **RF-03 / RF-04.** CRUD administrativo de locales y productos con control de duplicados por nombre | `AdminLocalController` (`GET`, `GET/{id}`, `POST` 201, `PUT`, `DELETE` 204) y `AdminProductoController` (ídem); `LocalRepository#existsByNombreIgnoreCase` / `...AndIdNot`; `ProductoRepository#existsByLocal_IdAndNombreIgnoreCase` / `...AndIdNot`; validación en `LocalService#crear/actualizar` y `ProductoService#crear/actualizar` → 409. Prueba: `rechazaProductosDuplicadosPorNombreEnElMismoLocal` | **C** |
| 8 | **RF-05.** Carta agrupada por categorías; productos sin stock visibles con `disponible: false` | `LocalController#carta` → `ProductoService#carta` agrupa en `LinkedHashMap` por categoría; `ProductoRepository#buscarPublicos` **no** filtra por stock; `ProductoService#aDto` calcula `disponible = stock != null && stock > 0`; `ProductoResponse` incluye `disponible` y `stock` | **C** |

**Observación C\*:** el control de duplicados de producto es por local (decisión de diseño: dos locales pueden vender el mismo plato). La unicidad se valida en servicio, no hay índice único en base para `(local_id, nombre)`; dos altas concurrentes con el mismo nombre podrían colarse. Ver 7.4.

### 3.4 Motor transaccional de pedidos

| # | Requisito | Evidencia | Estado |
|---|---|---|---|
| 9 | **RN-01.** Local activo y todos los productos activos y del local indicado | `PedidoService#crear`: `local.isActivo()`; por cada ítem verifica existencia, `p.getLocal().getId().equals(local.getId())`, `p.isActivo()` y stock suficiente | **C** |
| 10 | **RN-02 / Concurrencia.** Bloqueo pesimista ordenado por ID sobre productos | `ProductoRepository#bloquearPorIds` con `@Lock(PESSIMISTIC_WRITE)` y `order by p.id`; `Set<Integer>` evita duplicados; se invoca dentro del `@Transactional` de `crear` antes de leer/descontar stock | **C** |
| 11 | **RN-06.** Precio unitario inmutable capturado al registrar | `ItemPedido#precioUnitario` se asigna desde `p.getPrecio()` en la creación; los DTOs (`PedidoResponse.ItemResponse`, `PedidoDetalleResponse`) leen `i.getPrecioUnitario()`, nunca el precio actual del producto | **C** |
| 12 | **RN-07.** Reutilización o creación automática de cliente por teléfono | `PedidoService#crear`: `clienteRepository.findByTelefono(telefono)` y, si no existe, crea `Cliente` con nombre y teléfono normalizados | **C** |
| 13 | **Cálculos financieros.** Subtotal por ítems; 10% si subtotal > $20.000,00 estricto; `HALF_UP` a 2 decimales; envío fijo $1.500,00 | `PedidoService`: `UMBRAL_DESCUENTO = 20000.00`, `compareTo(...) > 0` (en el borde exacto no aplica), `PORCENTAJE_DESCUENTO = 0.10`, `COSTO_ENVIO = 1500.00`, `setScale(2, RoundingMode.HALF_UP)` en precio, subtotal, descuento y total. Prueba: `calculaElDescuentoYRespetaElBordeDeLosVeinteMil` | **C** |
| 14 | **RN-08.** Código único de 8 caracteres alfanuméricos; seguimiento público sin datos sensibles | `PedidoService#generarCodigoUnico` usa `SecureRandom`, alfabeto `A-Z0-9`, longitud 8, hasta 20 intentos y `existsByCodigoSeguimiento`; `PedidoController#seguimiento` → `consultarPorCodigo` devuelve `PedidoResponse` (código, estado, local, fecha, ítems e importes), **sin** id interno, teléfono ni dirección. Prueba: regex `^[A-Z0-9]{8}$` | **C** |

### 3.5 Operaciones de personal y repartidores

| # | Requisito | Evidencia | Estado |
|---|---|---|---|
| 15 | **RN-03.** Asignación en `EN_CAMINO` con repartidor activo, < 3 pedidos en camino y bloqueo pesimista | `GestionPedidoService#validarRepartidor`: exige `repartidorId`, `repartidorRepository.bloquearPorId` (`PESSIMISTIC_WRITE`), `isActivo()` y `countByRepartidor_IdAndEstado(..., EN_CAMINO) < 3`; se ejecuta dentro del `@Transactional` de `avanzar`. Prueba: `noPermiteAsignarUnCuartoPedidoAlMismoRepartidor` | **C** |
| 16 | **RN-04 / Cancelación.** Solo desde `RECIBIDO` o `EN_PREPARACION`; reposición atómica en la misma transacción; idempotente | `GestionPedidoService#cancelar`: si ya está `CANCELADO` devuelve el detalle sin reponer (idempotencia); rechaza otros estados con 409; `ProductoRepository#reponerStock` es un `UPDATE` atómico (`stock = stock + :cantidad`) ejecutado en la misma transacción, ordenado por id de producto. Prueba: `cancelarReponeElStockYEsIdempotente` | **C** |
| 17 | **RN-05.** Transiciones estrictas `RECIBIDO → EN_PREPARACION → EN_CAMINO → ENTREGADO` | `GestionPedidoService`: mapa `SIGUIENTE` (`EnumMap`); si no hay siguiente → 409; `ENTREGADO` y `CANCELADO` son terminales. Prueba: `rechazaTransicionesDeEstadoInvalidas` | **C** |
| 18 | **RF-12.** Repartidores en calle, incluidos los de 0 pedidos | `GestionPedidoService#enCalle` parte de `findByActivoTrueOrderByIdAsc()` y completa con `getOrDefault(id, List.of())`; incluye repartidores sin pedidos y tolera pedidos sin repartidor (`continue`) | **C** |

### 3.6 Manejo global de errores

| # | Requisito | Evidencia | Estado |
|---|---|---|---|
| 19 | **RNF-08.** `ApiError` con `timestamp`, `status`, `error`, `message`, `path`, `detalles`; sin stacktraces | `ApiError` (record con `@JsonInclude(NON_NULL)` y fábrica `de(...)`); `GlobalExceptionHandler` mapea 400/401/404/405/409/415/422/500; los 500 registran la traza **solo en el log del servidor** y responden “Ocurrió un error inesperado”; los `DataIntegrityViolationException` no exponen el mensaje de SQL | **C** |

---

## 4. Matriz de endpoints y roles

| Método | Ruta | Rol | Éxito | Errores previstos |
|---|---|---|---|---|
| POST | `/api/auth/login` | Público | 200 | 400, 401 |
| GET | `/api/locales` · `/api/locales/{id}` · `/api/locales/{id}/carta` | Público | 200 | 404 |
| GET | `/api/categorias` | Público | 200 | — |
| GET | `/api/productos` · `/api/productos/{id}` | Público | 200 | 404 |
| POST | `/api/pedidos` | Público | 201 | 400, 404, 409, 422 |
| GET | `/api/pedidos/seguimiento/{codigo}` | Público | 200 | 404 |
| GET | `/api/pedidos` · `/api/pedidos/{id}` · `/api/pedidos/en-calle` | OPERADOR/ADMIN | 200 | 400, 401, 403, 404 |
| POST | `/api/pedidos/{id}/avanzar` | OPERADOR/ADMIN | 200 | 400, 401, 403, 404, 409 |
| POST | `/api/pedidos/{id}/cancelar` | OPERADOR/ADMIN | 200 | 401, 403, 404, 409 |
| GET | `/api/repartidores` | OPERADOR/ADMIN | 200 | 401, 403 |
| GET/POST/PUT/DELETE | `/api/admin/locales[/{id}]` | ADMIN | 200/201/204 | 400, 401, 403, 404, 409 |
| GET/POST/PUT/DELETE | `/api/admin/productos[/{id}]` | ADMIN | 200/201/204 | 400, 401, 403, 404, 409 |
| GET/POST/PUT/PATCH | `/api/admin/usuarios` y `/api/usuarios` | ADMIN | 200/201/204 | 400, 401, 403, 404, 409, 422 |
| GET | `/api/admin/ventas` | ADMIN | 200 | 400, 401, 403, 422 |
| GET | `/api/ventas/por-local` | ADMIN | 200 | 400, 401, 403, 422 |

**Nota:** `UsuarioController` expone las mismas operaciones en `/api/admin/usuarios` y `/api/usuarios` por compatibilidad con la app móvil; ambas quedan cubiertas por reglas ADMIN.

---

## 5. Hallazgos y correcciones aplicadas

| ID | Severidad | Hallazgo | Corrección aplicada | Archivos |
|---|---|---|---|---|
| **H-01** | Alta | Los DTOs de entrada `PedidoRequest`, `ProductoRequest`, `LocalRequest` y `AvanzarPedidoRequest` eran clases mutables con setters, contra la buena práctica exigida | Se convirtieron en `record` inmutables; se actualizaron todos los usos a accesores (`localId()`, `items()`, `repartidorId()`, etc.) y se conservaron las validaciones Jakarta | `dto/PedidoRequest.java`, `dto/ProductoRequest.java`, `dto/LocalRequest.java`, `dto/AvanzarPedidoRequest.java`, servicios y controladores afectados |
| **H-02** | Media | El alta/edición de productos no impedía nombres duplicados dentro del mismo local (solo el local tenía control) | Se agregaron `existsByLocal_IdAndNombreIgnoreCase` y `...AndIdNot`; `ProductoService#crear/actualizar` responde 409 con mensaje funcional. Se agregó prueba de duplicado y de mismo nombre en otro local | `repository/ProductoRepository.java`, `service/ProductoService.java`, `IntegracionTest.java` |
| **H-03** | Media | La cancelación no era idempotente: un reintento sobre un pedido ya `CANCELADO` devolvía 409 | `GestionPedidoService#cancelar` devuelve el detalle con 200 si ya está cancelado, **sin** volver a reponer stock; solo repone en la primera cancelación. Se actualizó la prueba para verificar que el stock no se duplica | `service/GestionPedidoService.java`, `IntegracionTest.java` |
| **H-04** | Media | `JwtProperties` no validaba el secreto ni la vigencia; un secreto corto fallaba tarde y con un mensaje criptográfico poco claro | Constructor compacto del `record`: exige ≥ 32 bytes y `expirationMinutes > 0`. Además el secreto es configurable por `APP_JWT_SECRET` con valor por defecto solo para desarrollo | `security/JwtProperties.java`, `src/main/resources/application.properties` |
| **H-05** | Baja | El manejador global no registraba los errores 409/500 en el servidor, dificultando el diagnóstico | Se agregó SLF4J: `warn` para violaciones de integridad y `error` con traza para errores no controlados; al cliente solo se devuelve el mensaje genérico (RNF-08) | `exception/GlobalExceptionHandler.java` |
| **H-06** | Baja | `JwtService#principalDesde` era código muerto (el filtro recarga el usuario desde la base) y arrastraba un import innecesario | Se eliminó el método y el import de `Rol`; `JwtService` quedó con `generarToken` y `parsear` | `security/JwtService.java` |
| **H-07** | Baja | `GestionPedidoService#enCalle` asumía que todo pedido `EN_CAMINO` tenía repartidor | Se agregó guarda `if (p.getRepartidor() == null) continue;` (defensiva; la consulta ya filtra `is not null`) | `service/GestionPedidoService.java` |
| **H-08** | Baja | Ausencia casi total de JavaDoc en controladores, servicios, repositorios, entidades, DTOs, seguridad y excepciones | JavaDoc exhaustivo en los 74 archivos: responsabilidad, reglas RF/RN, rutas, roles, códigos HTTP, `@param`, `@return`, `@throws` y transaccionalidad | Todos los archivos de `src/main/java` |
| **H-09** | Baja | Faltaban pruebas para las reglas corregidas (duplicados, idempotencia, autoprotección) | Se agregaron/actualizaron `rechazaProductosDuplicadosPorNombreEnElMismoLocal`, `cancelarReponeElStockYEsIdempotente` y `unAdministradorNoPuedeQuitarseElRolNiDesactivarse` | `src/test/java/.../IntegracionTest.java` |

### 5.1 Puntos que ya cumplían y solo se documentaron

Para evitar cambios innecesarios, se mantuvo el comportamiento en:

- Matriz de roles y respuestas 401/403 en `ApiError`.
- RN-09 en `UsuarioService` (ya estaba implementada).
- Bloqueo pesimista de productos y repartidor, y conteo de pedidos en camino.
- Cálculo de descuento con borde estricto en $20.000,00 y redondeo `HALF_UP`.
- Baja lógica en usuarios, locales y productos.
- DTOs de respuesta sin hashes ni contraseñas.
- Reporte de ventas (RF-11): solo `ENTREGADO`, rango inclusivo y sin costo de envío.

---

## 6. Cambios de comportamiento visibles para el cliente

Solo hay dos cambios funcionales deliberados; el resto es documentación, inmutabilidad o validación defensiva:

1. **Cancelación repetida:** antes `409`, ahora `200` con el detalle del pedido ya cancelado (idempotencia RN-04).
2. **Nombre de producto duplicado en el mismo local:** antes se permitía, ahora `409 Conflict` con mensaje funcional.

Además, si `APP_JWT_SECRET` no está definida se usa el secreto de desarrollo; en producción debe definirse (ver 7.3).

---

## 7. Riesgos residuales y recomendaciones

### 7.1 Alta concurrente de clientes por teléfono — **Media**

`PedidoService#crear` hace `findByTelefono` y, si no existe, `save`. Dos primeras compras simultáneas del mismo teléfono pueden chocar contra `cliente.telefono UNIQUE`: una gana y la otra recibe 409 por `DataIntegrityViolationException`.

**Recomendación:** reintentar la operación completa una vez ante conflicto (nueva transacción) o usar un `INSERT ... ON CONFLICT`/upsert nativo. No se implementó aquí porque la recuperación dentro de la misma transacción marcada como *rollback-only* no es fiable.

### 7.2 Último administrador — **Media**

RN-09 impide la autodesactivación, pero un administrador puede desactivar o degradar a **otro** administrador. Si queda uno solo y otro lo desactiva, el sistema se queda sin acceso administrativo.

**Recomendación:** antes de desactivar o cambiar el rol de un `ADMIN`, contar los administradores activos y rechazar la operación si es el último (HTTP 422).

### 7.3 Secreto JWT por defecto — **Media**

`app.jwt.secret` tiene un valor de desarrollo y ahora admite `APP_JWT_SECRET`. Si se despliega sin definir la variable, cualquiera que conozca el repositorio puede firmar tokens válidos.

**Recomendación:** en producción, definir `APP_JWT_SECRET` con al menos 32 bytes aleatorios y fallar el arranque si la variable no está presente (por ejemplo, sin valor por defecto en el perfil productivo).

### 7.4 Unicidad de nombre de producto a nivel de base — **Baja**

La validación es de servicio; no hay índice único `(local_id, nombre)` (ni sobre `lower(nombre)`). Dos altas concurrentes podrían insertar duplicados.

**Recomendación:** agregar un índice único normalizado en `schema.sql` y en la entidad, o una migración Flyway/Liquibase.

### 7.5 Otros puntos — **Baja**

- **Sin paginación** en listados de usuarios, pedidos, productos y ventas. Con volúmenes altos conviene `Pageable`.
- **CORS abierto** (`allowedOriginPatterns("*")`). Restringir a los orígenes reales en producción.
- **Sin migraciones versionadas:** `schema.sql` es destructivo y solo corre con el perfil `init`. Para producción usar Flyway/Liquibase.
- **H2 en archivo** no es apta para múltiples instancias; el diseño asume un nodo.
- **Sin rate limiting** en login y alta pública de pedidos.
- **Sin `@Version`** en entidades; los flujos críticos están cubiertos con bloqueo pesimista, pero las ediciones administrativas concurrentes pueden pisarse.
- **Sin revocación de JWT:** el logout no invalida el token; una baja o cambio de rol sí surte efecto inmediato porque el filtro recarga el usuario.
- **Repartidores sin CRUD administrativo:** se cargan por `data.sql`; la checklist solo exige consulta (RF-12), pero un ABM sería el siguiente paso natural.
- **Pruebas de concurrencia:** las pruebas actuales son de integración con `@Transactional`; no ejercitan dos transacciones reales compitiendo por el mismo lock.

---

## 8. Verificación técnica realizada

| Verificación | Resultado |
|---|---|
| Lectura y trazado RF/RN → código | Completa (19/19) |
| Balance de llaves en los 74 archivos | Sin desbalances |
| Imports sin uso (heurística de identificador no referenciado) | 0 |
| Accesos a getters de DTOs convertidos a `record` | 0 |
| Referencias a `principalDesde` (método eliminado) | 0 |
| `printStackTrace` / `System.out` / `TODO` / `FIXME` | 0 |
| Exposición de `passwordHash` en DTOs | 0 |
| Compilación (`mvnw clean test-compile`) | **BUILD SUCCESS** — 74 fuentes principales + 2 de prueba, `javac` release 17 |
| Pruebas (`mvnw test`) | **BUILD SUCCESS** — 15 pruebas, 0 fallos, 0 errores, 0 omitidas |
| Entorno de ejecución | JDK 17.0.20.1 (Temurin) + Maven Wrapper 3.3.2, H2 en memoria para pruebas |

Detalle de la suite:

| Clase | Pruebas | Resultado |
|---|---|---|
| `BackendprojtApplicationTests` | 1 (`contextLoads`) | OK |
| `IntegracionTest` | 14 | OK |

Cobertura funcional de las pruebas: login correcto/incorrecto y hash del administrador de `data.sql`; matriz de roles; alta de pedido con descuento de stock; descuento y borde de $20.000; productos de distintos locales; stock insuficiente; cancelación con reposición e idempotencia; duplicados de producto por local; autoprotección del administrador (RN-09); transiciones inválidas; cuarto pedido por repartidor; reporte de ventas (solo entregados, sin envío) y validación de parámetros/roles del reporte.

**Reproducción:**

```bash
# Desde la carpeta del backend (donde está pom.xml)
./mvnw clean test          # Linux / macOS
.\mvnw.cmd clean test      # Windows
```

Con perfil `init` para recrear la base:

```bash
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=init
```

---

## 9. Archivos entregados

- `INFORME_AUDITORIA.md` (este documento).
- 74 archivos Java de `src/main/java` con JavaDoc completo y correcciones.
- `src/main/resources/application.properties` con secreto JWT configurable por entorno y vigencia documentada.
- `src/test/java/com/example/backendprojt/IntegracionTest.java` ampliado con las pruebas de las reglas corregidas.
- `README.md` con la sección 11 de auditoría y la variable `APP_JWT_SECRET`.

### 9.1 Listado de archivos modificados o agregados (78)

**Raíz (1):** `BackendprojtApplication.java`

**Controladores (12):** `AdminLocalController`, `AdminProductoController`, `AdminVentasController`, `AuthController`, `CategoriaController`, `GestionPedidoController`, `LocalController`, `PedidoController`, `ProductoController`, `RepartidorController`, `UsuarioController`, `VentaController`.

**DTOs (22):** `ApiError`, `AvanzarPedidoRequest`, `CambioPasswordRequest`, `CartaResponse`, `CategoriaResponse`, `LocalRequest`, `LocalResponse`, `LoginRequest`, `LoginResponse`, `PedidoDetalleResponse`, `PedidoRequest`, `PedidoResponse`, `PedidoResumenResponse`, `ProductoRequest`, `ProductoResponse`, `RepartidorEnCalleResponse`, `RepartidorResponse`, `ReporteVentasResponse`, `UsuarioActualizarRequest`, `UsuarioCrearRequest`, `UsuarioResponse`, `VentaLocalResponse`.

**Entidades (10):** `Categoria`, `Cliente`, `EstadoPedido`, `ItemPedido`, `Local`, `Pedido`, `Producto`, `Repartidor`, `Rol`, `Usuario`.

**Excepciones (5):** `ConflictoException`, `GlobalExceptionHandler`, `RecursoNoEncontradoException`, `ReglaNegocioException`, `SolicitudInvalidaException`.

**Repositorios (9):** `CategoriaRepository`, `ClienteRepository`, `ItemPedidoRepository`, `LocalRepository`, `PedidoRepository`, `ProductoRepository`, `RepartidorRepository`, `UsuarioRepository`, `VentaRepository`.

**Seguridad (6):** `JwtAuthFilter`, `JwtProperties`, `JwtService`, `SecurityConfig`, `UsuarioDetailsService`, `UsuarioPrincipal`.

**Servicios (9):** `AuthService`, `CategoriaService`, `GestionPedidoService`, `LocalService`, `PedidoService`, `ProductoService`, `RepartidorService`, `UsuarioService`, `VentaService`.

**Total `src/main/java`: 74 archivos.**

**Configuración (1):** `src/main/resources/application.properties`.

**Pruebas (1):** `src/test/java/com/example/backendprojt/IntegracionTest.java`.

**Documentación (2):** `INFORME_AUDITORIA.md` (nuevo) y `README.md` (sección 11).

> Conteo reproducible:
> `(Get-ChildItem src/main/java -Recurse -Filter *.java | Measure-Object).Count` → `74`.

---

## 10. Conclusión

El backend de Sabores del Litoral **satisface íntegramente** la checklist RF/RN auditada, con una arquitectura Controller–Service–Repository consistente, DTOs inmutables, transacciones bien delimitadas, bloqueos pesimistas en los puntos críticos y un contrato de error uniforme. Las correcciones de esta iteración eliminan los incumplimientos de calidad detectados (DTOs mutables, duplicados de producto, cancelación no idempotente, configuración JWT sin validar) y elevan la documentación al 100%.

Los riesgos residuales son de **endurecimiento para producción** (concurrencia de alta de clientes, último administrador, secreto JWT, índice único de nombres, paginación y CORS), no defectos funcionales de la entrega.

**Verificación final:** `mvnw clean test-compile` compila las 74 fuentes principales y las 2 de prueba con Java 17 (`BUILD SUCCESS`), y `mvnw test` ejecuta **15 pruebas sin fallos** (`BUILD SUCCESS`). El proyecto queda compilable y verificable tal como se entrega.
