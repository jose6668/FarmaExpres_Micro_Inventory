# FarmaExpres Micro Inventory

Microservicio de inventario para el ecosistema FarmaExpres. Este servicio administra productos farmaceuticos, lotes, movimientos de inventario, consultas FEFO, reportes operativos y endpoints de salud.

## Alcance actual

El modulo `inventory-service` permite:

- Crear, consultar, actualizar y retirar logicamente productos.
- Gestionar inventario por lotes con fecha de vencimiento y estado operativo.
- Registrar entradas y salidas de inventario.
- Consumir stock siguiendo FEFO (`First Expired, First Out`).
- Consultar productos activos, agotados y con stock bajo.
- Generar reportes de lotes, movimientos por lote y actividad de usuarios.
- Proteger endpoints con Spring Security y JWT.

## Tecnologias

- Java 17
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA
- Spring Security
- JWT (`jjwt`)
- PostgreSQL
- Maven Wrapper
- Docker
- Spring Boot Actuator

## Estructura del proyecto

```text
FarmaExpres_Micro_Inventory/
|-- README.md
`-- inventory-service/
    |-- Dockerfile
    |-- mvnw
    |-- mvnw.cmd
    |-- pom.xml
    `-- src/
        |-- main/
        |   |-- java/co/edu/corhuila/inventory_service/
        |   |   |-- Config/
        |   |   |-- Controllers/
        |   |   |-- Dto/
        |   |   |-- Entity/
        |   |   |-- Repository/
        |   |   |-- Service/
        |   |   |-- exception/
        |   |   |-- InventoryServiceApplication.java
        |   |   `-- StatusController.java
        |   `-- resources/
        |       `-- application.yaml
        `-- test/
            `-- java/co/edu/corhuila/inventory_service/
```

## Componentes principales

### Productos

La entidad `Product` almacena informacion operativa y farmaceutica del medicamento:

- Identificacion: `id`, `code`, `name`, `nombreGenerico`
- Clasificacion: `concentracion`, `formaFarmaceutica`, `presentacion`
- Inventario: `stock`, `minimumStock`, `stockMaximo`, `active`
- Costos: `unitPrice`, `precioCompra`, `precioVenta`
- Control sanitario: `requiereReceta`, `registroSanitario`, `laboratorio`
- Logistica: `viaAdministracion`, `unidadMedida`, `ubicacionAlmacen`, `temperaturaConservacion`
- Trazabilidad: `expirationDate`, `observaciones`

### Lotes

La entidad `Batch` modela el inventario real por lote:

- `batchCode`
- `expirationDate`
- `initialStock`
- `availableStock`
- `status`: `ACTIVE`, `OUT_OF_STOCK`, `EXPIRED`, `RETIRED`

El stock operativo del producto se recalcula a partir de los lotes consumibles.

### Movimientos

La entidad `Motion` registra trazabilidad del inventario:

- `type`: `Entrance`, `Exit`, `Updated`, `Deleted`
- `amount`
- `dateTime`
- `reason`
- Datos del usuario autenticado: `userId`, `userName`, `userEmail`, `userRole`
- Relacion opcional con `Batch`

## Endpoints principales

### Productos

- `POST /api/products`: crea un producto y genera movimiento inicial.
- `GET /api/products`: lista todos los productos.
- `GET /api/products/{id}`: consulta un producto por id.
- `PUT /api/products/{id}`: actualiza metadatos del producto.
- `DELETE /api/products/{id}`: retiro logico del producto.
- `GET /api/products/assets` y `GET /api/products/active`: lista productos activos.
- `GET /api/products/out-of-stock`: lista productos agotados.
- `GET /api/products/low-stock`: reporte de stock bajo.
- `GET /api/products/low-stock/critical`: productos en estado critico.
- `GET /api/products/low-stock/alert`: productos en alerta.
- `GET /api/products/active-table`: tabla resumida de inventario activo.
- `GET /api/products/active-summary`: resumen consolidado de stock y valor.
- `GET /api/products/fefo-snapshot`: proximo lote a consumir por producto.
- `GET /api/products/{productId}/batches`: lotes de un producto.
- `POST /api/products/{productId}/batches`: crea un lote manualmente.

### Movimientos

- `GET /api/movements`: lista movimientos.
- `GET /api/movements/filter-by-user?userId=`: filtra por usuario.
- `GET /api/movements/entrance`: lista entradas.
- `GET /api/movements/exit`: lista salidas.
- `GET /api/movements/updated`: lista ajustes.
- `GET /api/movements/report/users-activity?role=`: actividad por rol o usuario.
- `POST /api/movements`: registra movimiento directo sobre un lote.
- `POST /api/movements/entries`: registra entrada con creacion automatica de lote.
- `POST /api/movements/exits`: registra salida descontando stock disponible.
- `POST /api/movements/consume-fefo`: consume stock siguiendo FEFO.

Alias heredados:

- `GET /api/motions`
- `GET /api/Motion`

### Reportes

- `GET /api/reports/inventory-batches`: reporte de inventario por lote.
- `GET /api/reports/movements-batches`: reporte de movimientos asociados a lotes.

### Salud del servicio

- `GET /status`
- `GET /actuator/health`
- `GET /actuator/info`

## Seguridad

La autenticacion se realiza por JWT usando el encabezado:

```http
Authorization: Bearer <token>
```

Endpoints publicos:

- `/status`
- `/actuator/health`
- `/actuator/info`
- `/error`

Autorizacion principal por rol:

- `ADMIN`: administra productos, consulta reportes y puede ejecutar movimientos.
- `FARMACEUTICO`: consulta productos, registra entradas y salidas, consulta FEFO.
- `AUDITOR`: consulta productos, movimientos y reportes.

## Reglas de negocio relevantes

- El codigo del producto debe ser unico.
- No se pueden modificar productos retirados.
- El retiro de producto es logico; no elimina fisicamente el registro.
- Al crear un producto se crea un lote inicial `INIT-<codigo>` y un movimiento `Entrance`.
- El stock del producto se deriva de los lotes activos y no vencidos.
- Los lotes vencidos o agotados dejan de aportar al stock operativo.
- Las entradas crean un lote nuevo con cantidad y fecha de vencimiento.
- Las salidas validan stock total disponible y pueden ejecutarse con FEFO.
- Los motivos de entrada y salida estan restringidos por validacion.

## Variables de entorno

Definidas en `inventory-service/src/main/resources/application.yaml`:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

Ejemplo en PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/inventorydb"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="tu_clave"
$env:JWT_SECRET="tu_secreto_jwt"
```

## Ejecucion local

Prerrequisitos:

- Java 17
- PostgreSQL

Desde la carpeta del microservicio:

```powershell
cd inventory-service
.\mvnw.cmd spring-boot:run
```

El servicio inicia en `http://localhost:8082`.

## Docker

Desde `inventory-service`:

```powershell
docker build -t inventory-service .
docker run -p 8082:8082 --env-file .env inventory-service
```

## Pruebas

Hay pruebas en `src/test` para arranque, JWT, productos y movimientos.

Para ejecutarlas:

```powershell
cd inventory-service
.\mvnw.cmd test
```
