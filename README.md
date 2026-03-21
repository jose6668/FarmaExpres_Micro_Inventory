# FarmaExpres_Micro_Inventory


Microservicio encargado de la gestión de inventario dentro del ecosistema **FarmaExpres**. Su responsabilidad principal es administrar productos, controlar stock disponible y registrar movimientos de inventario generados por las operaciones del servicio.

## Objetivo

Este microservicio permite:

- Registrar productos en inventario.
- Consultar productos disponibles y activos.
- Actualizar información de productos y existencias.
- Realizar eliminación lógica de productos.
- Identificar productos sin stock.
- Registrar movimientos de inventario para trazabilidad.
- Exponer endpoints básicos de monitoreo y estado del servicio.

## Tecnologías utilizadas

- Java 17
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- Spring Security
- JWT
- PostgreSQL
- Maven
- Docker
- Spring Boot Actuator

Estas dependencias se observan en el `pom.xml`, junto con soporte de pruebas y documentación con REST Docs/Asciidoctor.

## Estructura del proyecto

```text
inventory-service/
├── Dockerfile
├── pom.xml
├── mvnw / mvnw.cmd
├── src/
│   ├── main/
│   │   ├── java/co/edu/corhuila/inventory_service/
│   │   │   ├── Config/
│   │   │   ├── Controllers/
│   │   │   ├── Dto/
│   │   │   ├── Entity/
│   │   │   ├── Repository/
│   │   │   ├── Service/
│   │   │   └── exception/
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
```

La estructura sigue una organización por capas: controlador, servicio, repositorio, entidades, DTOs y manejo global de errores.

## Arquitectura general

El microservicio adopta una arquitectura por capas:

### 1. Capa de presentación
Expone la API REST a través de controladores Spring.

Ejemplo principal:
- `ProductoController` bajo la ruta base `/api/products`.

### 2. Capa de negocio
Implementa las reglas del dominio relacionadas con productos y movimientos.

- `ProductoService` valida duplicidad de código, actualiza stock, realiza eliminación lógica y registra movimientos de inventario.

### 3. Capa de persistencia
Gestiona acceso a base de datos mediante Spring Data JPA.

- Repositorios para entidades de productos y movimientos.

### 4. Capa transversal
Incluye seguridad, manejo de errores y observabilidad.

- Seguridad basada en JWT y roles.
- Manejo global de excepciones con `GlobalExceptionHandler`.
- Actuator para salud e información del servicio.

## Modelo de dominio

### Producto
La entidad `Producto` representa un artículo de inventario e incluye, entre otros, los siguientes atributos:

- `id`
- `nombre`
- `codigo` (único)
- `stock`
- `precio`
- `activo`
- `fechavencimiento`

La eliminación de productos se maneja de forma lógica mediante el atributo `activo`.

### Movimiento
La entidad `Movimiento` registra cambios relevantes del inventario:

- `id`
- `tipo`
- `cantidad`
- `fecha`
- relación con `Producto`

Esto permite trazabilidad sobre entradas, salidas, actualizaciones y eliminaciones.

## Reglas de negocio implementadas

A partir del servicio actual, se identifican estas reglas:

- No se puede crear un producto con un `codigo` ya existente.
- Al crear un producto, se registra un movimiento de tipo `ENTRADA`.
- Al actualizar stock, se registra el movimiento correspondiente (`ENTRADA`, `SALIDA` o `ACTUALIZADO`).
- Al eliminar un producto, no se borra físicamente: se marca como inactivo y se registra un movimiento de `ELIMINACION`.
- Existe una consulta especializada para productos sin stock.

## Seguridad

La seguridad está implementada con Spring Security y un filtro JWT. El comportamiento observado es:

- Público:
  - `/status`
  - `/actuator/health`
  - `/actuator/info`
- Solo `ADMIN`:
  - `POST /api/products/**`
  - `PUT /api/products/**`
  - `DELETE /api/products/**`
- `ADMIN` o `EMPLEADO`:
  - `GET /api/products/**`

La validación del token se realiza antes del filtro estándar de autenticación de Spring.

## Configuración

El archivo `application.yaml` define:

- Puerto del servicio: `8082`
- Nombre de la aplicación: `inventory-service`
- Conexión a PostgreSQL mediante variables de entorno
- Exposición de endpoints `health` e `info`
- Secreto JWT por variable de entorno

Variables requeridas:

```env
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
```

La configuración actual usa `spring.jpa.hibernate.ddl-auto: update`, adecuada para desarrollo, pero no recomendada para producción sin un control formal de migraciones.

## Ejecución local

### Requisitos previos

- Java 17
- Maven 3.9+
- PostgreSQL

### 1. Clonar repositorio

```bash
git clone -b HU-doc-dev https://github.com/jose6668/FarmaExpres_Micro_Inventory.git
cd FarmaExpres_Micro_Inventory/inventory-service
```

### 2. Definir variables de entorno

En Linux/macOS:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/inventory_db
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=tu_clave_jwt
```

En Windows PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/inventory_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
$env:JWT_SECRET="tu_clave_jwt"
```

### 3. Ejecutar el servicio

Con Maven Wrapper:

```bash
./mvnw spring-boot:run
```

O con Maven instalado:

```bash
mvn spring-boot:run
```

El servicio quedará disponible en:

```text
http://localhost:8082
```

## Ejecución con Docker

El proyecto incluye un `Dockerfile` multi-stage que compila el servicio con Maven y ejecuta el `.jar` sobre Eclipse Temurin 17. Expone el puerto `8082`.

### Construir imagen

```bash
docker build -t inventory-service .
```

### Ejecutar contenedor

```bash
docker run -p 8082:8082 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/inventory_db \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  -e JWT_SECRET=tu_clave_jwt \
  inventory-service
```

## Endpoints principales

### Estado del servicio

```http
GET /status
```

### Monitoreo

```http
GET /actuator/health
GET /actuator/info
```

### Productos

```http
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}
GET    /api/products
GET    /api/products/activos
GET    /api/products/sin-stock
```

La API también acepta `/api/products/Assets` como ruta alternativa para productos activos, aunque sería preferible unificar la convención de nombres.

## Ejemplo de payload para crear o actualizar producto

```json
{
  "nombre": "Acetaminofén 500mg",
  "codigo": "MED-001",
  "stock": 100,
  "precio": 8500,
  "fechavencimiento": "2027-12-31"
}
```

Campos esperados según la entidad `Producto`.

## Respuestas de error

El servicio cuenta con un manejador global de excepciones que retorna una estructura uniforme con información como:

- fecha y hora
- código HTTP
- error
- mensaje
- ruta
- nombre del servicio

Esto facilita depuración e integración entre microservicios.

## Pruebas

El proyecto incluye dependencias de pruebas para Spring Boot, JPA, seguridad y REST Docs. Actualmente se observa al menos una prueba básica de carga de contexto en `ProductoControllerIntegrationTest`.

Ejecución:

```bash
./mvnw test
```
