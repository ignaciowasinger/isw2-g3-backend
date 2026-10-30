# Sabores del Litoral

Proyecto integrador de Ingeniería de Software II × Programación.

INTEGRANTES: IGNACIO ALBRECHT, MAXIMO STEBLER, IGNACIO WASINGER, SEBASTIAN ZURSCHMITTER

- **Backend:** Spring Boot 3.3.5, API REST, Spring Security + JWT, JPA/Hibernate y H2 en archivo.
- **App móvil:** React Native + Expo Router (carpeta `mobile/`).

---

## 1. Requisitos previos

- JDK 17 o superior (probado con JDK 26).
- Maven Wrapper incluido (`mvnw` / `mvnw.cmd`). No hace falta instalar Maven.
- Para la app móvil: **Node.js 24** (Expo SDK 58 no funciona con Node 20), npm y Expo Go en el celular o un emulador Android / simulador iOS. Ver la sección 4.

---

## 2. Base de datos (H2 en archivo)

La base se guarda en `data/sabores_litoral.mv.db` (dentro de la carpeta del backend) y **persiste entre reinicios**.

`schema.sql` borra y vuelve a crear las tablas. Por eso solo se ejecuta con el perfil `init`:

Desde la carpeta del backend (donde está `pom.xml`):

```bash
# Windows
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=init

# Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.profiles=init
```

Al terminar la carga, detené el proceso con `Ctrl+C`.

Ese perfil carga:

- el administrador inicial,
- las 5 categorías,
- los 3 repartidores.

Locales, productos, clientes, pedidos e ítems arrancan vacíos.

> **Atención:** volver a ejecutar el perfil `init` borra todos los datos existentes.
> Para reiniciar la base desde cero, eliminá la carpeta `data/` y repetí el paso.

> **Una sola instancia a la vez:** H2 bloquea el archivo de la base. Si arrancás otra instancia mientras la primera sigue corriendo, vas a ver `Database may be already in use`. Detené la instancia anterior con el botón rojo de IntelliJ antes de arrancar la siguiente. El perfil `init` se usa una vez y se detiene; para el día a día se arranca sin perfil.

Después, los arranques normales conservan los datos:

```bash
.\mvnw.cmd spring-boot:run
```

### Desde IntelliJ IDEA

1. Abrí el proyecto `backendprojt`.
2. Ejecutá `BackendprojtApplication`.
3. La primera vez, agregá en *Run → Edit Configurations → Program arguments*:
   `--spring.profiles.active=init`
4. Después de la carga inicial, quitá ese argumento y volvé a arrancar normalmente.

El backend queda en `http://localhost:8080`.

---

## 3. Ejecutar las pruebas

```bash
# Windows
.\mvnw.cmd clean test

# Linux / macOS
./mvnw clean test
```

Las pruebas usan H2 en memoria (`src/test/resources/application.properties`) y no tocan la base de `data/`.

Incluyen:

- login correcto e incorrecto;
- acceso según roles (público, OPERADOR, ADMIN);
- creación válida de pedidos;
- rechazo de productos de distintos locales;
- rechazo de cantidades superiores al stock;
- cálculo del descuento y borde exacto de $20.000;
- generación del código de seguimiento;
- cancelación con reposición de stock;
- rechazo de transiciones de estado inválidas;
- límite de 3 pedidos EN_CAMINO por repartidor;
- reporte de ventas que excluye pedidos no entregados y el costo de envío.

---

## 4. Iniciar la app móvil con Expo

Requisito: **Node.js 24** (Expo SDK 58 exige `^22.13.0`, `^24.3.0`, `^26.0.0` o `>=27.0.0`). El Node del sistema en este equipo es 20.17 y Expo no arranca con esa versión. Node 24.22.1 quedó instalado en:

```
C:\Users\Nacho\AppData\Local\Programs\nodejs-24
```

### Opción rápida: `dev.cmd`

Desde la carpeta `mobile/`, en la terminal de IntelliJ o de Windows:

```bat
cd mobile
dev.cmd
```

El script antepone Node 24 al `PATH` y ejecuta `expo start`. Acepta los mismos parámetros:

```bat
dev.cmd --android
dev.cmd --web
dev.cmd --clear
```

Si Node 24 está en otra ruta, definí `SABORES_NODE` antes de ejecutarlo:

```bat
set "SABORES_NODE=C:\ruta\a\node24"
dev.cmd
```

### Opción manual

```bat
cd mobile
npm install
set "PATH=C:\Users\Nacho\AppData\Local\Programs\nodejs-24;%PATH%"
npx.cmd expo start
```

> En PowerShell los shims `npm.ps1` / `npx.ps1` pueden estar bloqueados por la política de ejecución del sistema. Usá `npm.cmd` y `npx.cmd`, como se muestra arriba.

Abrí la app con Expo Go (QR) o con `a` (Android) / `i` (iOS) en la terminal.

### Desde IntelliJ IDEA

1. *Settings → Languages & Frameworks → Node.js*: elegí `C:\Users\Nacho\AppData\Local\Programs\nodejs-24\node.exe` como intérprete.
2. Agregá una configuración **npm** con `package.json` = `mobile/package.json` y comando `start`.
3. Alternativa sin configurar nada: abrí la terminal de IntelliJ, `cd mobile` y ejecutá `dev.cmd`.

En `mobile/README.md` están la estructura, los comandos de verificación y las decisiones de la app.

### URL de la API

La app lee `EXPO_PUBLIC_API_URL`. Si no está definida usa `http://10.0.2.2:8080` (emulador Android).

| Dónde corre la app | Valor |
|---|---|
| Emulador Android | `http://10.0.2.2:8080` (alias de tu PC) |
| Simulador iOS (Mac) | `http://localhost:8080` |
| Celular físico con Expo Go | `http://IP_DE_TU_PC:8080`, con celular y PC en la misma red Wi-Fi |

Creá `mobile/.env`:

```bash
EXPO_PUBLIC_API_URL=http://192.168.0.10:8080
```

Reiniciá Expo cada vez que cambies el archivo:

```bat
dev.cmd --clear
```

Para obtener la IP de tu PC:

- Windows: `ipconfig` (IPv4 del adaptador Wi-Fi).
- Linux / macOS: `ip a` o `ifconfig`.

Si el celular no conecta, verificá que el puerto 8080 esté permitido en el firewall de la PC.
`localhost` dentro de un celular o emulador apunta al propio dispositivo, no a tu PC.

---

## 5. Administrador inicial

Definido en `src/main/resources/data.sql`:

| Usuario | Contraseña |
|---|---|
| `admin` | `Admin2026!` |

En la app: **Acceso del personal**. El hash BCrypt del archivo corresponde a esa contraseña.

---

## 6. Probar el flujo completo

1. Iniciá sesión como `admin`.
2. **Locales** → creá un local.
3. **Productos** → creá productos con categoría, precio y stock.
4. Cerrá sesión. Desde el inicio elegí el local, agregá productos al carrito y confirmá el pedido con nombre, teléfono (8 a 15 dígitos) y dirección.
5. Guardá el **código de seguimiento** de 8 caracteres y consultalo en **Seguir mi pedido**. No muestra teléfono ni dirección.
6. Volvé a ingresar como personal: **Pedidos** → abrí el pedido → pasalo a preparación → despachalo eligiendo repartidor → marcalo entregado.
7. Se puede cancelar mientras esté en `RECIBIDO` o `EN_PREPARACION`; el stock se repone.
8. **Pedidos en la calle** muestra los repartidores activos y sus pedidos en camino.
9. **Ventas por local** (solo ADMIN) suma únicamente pedidos `ENTREGADO`, con `subtotal - descuento` y sin el costo de envío.

### Flujo mínimo con curl

```bash
# Login
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"Admin2026!\"}"

# Catálogo público
curl -s http://localhost:8080/api/locales
curl -s http://localhost:8080/api/categorias

# Reporte de ventas (reemplazar TOKEN y las fechas)
curl -s "http://localhost:8080/api/admin/ventas?desde=2026-10-01&hasta=2026-10-31" \
  -H "Authorization: Bearer TOKEN"
```

---

## 7. Funcionalidades implementadas

### Público

- Ver locales activos y sus cartas.
- Ver categorías y productos disponibles.
- Crear pedidos sin registro de usuario.
- Consultar un pedido por código de seguimiento (sin datos personales).

### Personal (OPERADOR y ADMIN)

- Login con JWT (8 horas).
- Listar y filtrar pedidos por estado.
- Ver detalle del pedido.
- Avanzar estados: `RECIBIDO → EN_PREPARACION → EN_CAMINO → ENTREGADO`.
- Cancelar pedidos en `RECIBIDO` o `EN_PREPARACION`.
- Asignar repartidor al despachar.
- Ver repartidores y pedidos en la calle.

### Administrador

- CRUD lógico de usuarios, locales y productos.
- Reporte de ventas por local y por período.

### Reglas de negocio

- Descuento del 10 % cuando el subtotal supera los $20.000 (exactamente $20.000 no lleva descuento).
- Envío fijo de $1.500.
- Stock validado y descontado en una única transacción, con bloqueo pesimista de los productos.
- Cancelación con reposición de stock en la misma transacción.
- Máximo 3 pedidos `EN_CAMINO` por repartidor.
- Un pedido solo puede tener productos de un mismo local.
- Código de seguimiento único de 8 caracteres alfanuméricos.
- Montos con `BigDecimal` y redondeo `HALF_UP`.

---

## 8. Endpoints principales

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/auth/login` | Público |
| GET | `/api/locales`, `/api/locales/{id}`, `/api/locales/{id}/carta` | Público |
| GET | `/api/categorias` | Público |
| GET | `/api/productos`, `/api/productos/{id}` | Público |
| POST | `/api/pedidos` | Público |
| GET | `/api/pedidos/seguimiento/{codigo}` | Público |
| GET | `/api/pedidos`, `/api/pedidos/{id}`, `/api/pedidos/en-calle` | OPERADOR / ADMIN |
| POST | `/api/pedidos/{id}/avanzar`, `/api/pedidos/{id}/cancelar` | OPERADOR / ADMIN |
| GET | `/api/repartidores` | OPERADOR / ADMIN |
| GET/POST/PUT/PATCH | `/api/admin/usuarios/**` | ADMIN |
| GET/POST/PUT/DELETE | `/api/admin/locales/**`, `/api/admin/productos/**` | ADMIN |
| GET | `/api/admin/ventas?desde=AAAA-MM-DD&hasta=AAAA-MM-DD` | ADMIN |
| GET | `/api/ventas/por-local?desde=...&hasta=...` | ADMIN |

Los errores usan el formato:

```json
{
  "timestamp": "2026-10-27T18:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Stock insuficiente",
  "path": "/api/pedidos",
  "detalles": []
}
```

---

## 9. Estructura del backend

```
src/main/java/com/example/backendprojt/
├── controller/
├── dto/
├── entity/
├── exception/
├── repository/
├── security/
└── service/
```

- `security/`: JWT, filtro de autenticación, `UserDetails` y reglas de acceso.
- `repository/`: acceso a datos y consulta del reporte de ventas.
- `service/`: reglas de negocio (pedidos, stock, usuarios y ventas).
- `exception/`: excepciones de negocio y manejador global de errores.

---

## 10. Limitaciones conocidas

- La app móvil está pensada para Android/iOS. La versión web de Expo sirve para pruebas rápidas: el almacenamiento seguro cae a memoria y la sesión no sobrevive a un refresco de página.
- No se configuró HTTPS. Para un build de producción hay que usar HTTPS o habilitar tráfico en claro en Android.
- El secreto JWT de `application.properties` es de desarrollo. En producción definí `APP_JWT_SECRET` con al menos 32 caracteres.
- H2 en archivo no está pensado para varias instancias simultáneas del backend.
- Dos pedidos simultáneos con un teléfono nuevo podrían chocar contra la restricción única de `cliente.telefono`. Es un caso de concurrencia poco frecuente; no afecta el stock.
- El reporte incluye los locales sin ventas con cantidad 0 y monto 0.

---

## 11. Auditoría de cumplimiento RF/RN

El informe completo está en [`INFORME_AUDITORIA.md`](INFORME_AUDITORIA.md). Resume la verificación de los 19 puntos de la checklist (autenticación/JWT, roles, usuarios y RN-09/RN-10, catálogo RF-03/04/05, motor de pedidos RN-01/02/06/07/08, operaciones RN-03/04/05 y RF-12, errores globales RNF-08), los hallazgos corregidos y los riesgos residuales para producción.

Todas las clases de `src/main/java` incluyen JavaDoc con rutas, roles, códigos HTTP, parámetros, retornos y transaccionalidad.

### Variables de entorno relevantes

| Variable | Uso | Valor por defecto |
|---|---|---|
| `APP_JWT_SECRET` | Clave HMAC-SHA del JWT (mínimo 32 caracteres) | Secreto de desarrollo de `application.properties` |

En producción definí siempre `APP_JWT_SECRET`; si no, cualquiera con acceso al repositorio podría firmar tokens válidos.
