-- Para usar un MySQL instalado en el equipo (sin Docker).
-- Ejecutar como root:  mysql -u root -p < backend/sql/01_crear_bases_mysql.sql
-- Luego iniciar cada servicio con el perfil mysql (ver README).
--
-- Una base por microservicio, cada una con su propio usuario.
-- Cambia las contraseñas y usa las mismas en el archivo .env.

CREATE DATABASE IF NOT EXISTS sanmarcos_usuarios CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS sanmarcos_catalogo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS sanmarcos_pedidos  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS sanmarcos_citas    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'sm_usuarios'@'localhost' IDENTIFIED BY 'usuarios-local-cambiar';
CREATE USER IF NOT EXISTS 'sm_catalogo'@'localhost' IDENTIFIED BY 'catalogo-local-cambiar';
CREATE USER IF NOT EXISTS 'sm_pedidos'@'localhost'  IDENTIFIED BY 'pedidos-local-cambiar';
CREATE USER IF NOT EXISTS 'sm_citas'@'localhost'    IDENTIFIED BY 'citas-local-cambiar';

GRANT ALL PRIVILEGES ON sanmarcos_usuarios.* TO 'sm_usuarios'@'localhost';
GRANT ALL PRIVILEGES ON sanmarcos_catalogo.* TO 'sm_catalogo'@'localhost';
GRANT ALL PRIVILEGES ON sanmarcos_pedidos.*  TO 'sm_pedidos'@'localhost';
GRANT ALL PRIVILEGES ON sanmarcos_citas.*    TO 'sm_citas'@'localhost';
FLUSH PRIVILEGES;

-- Las tablas las crea Hibernate al iniciar cada servicio (spring.jpa.hibernate.ddl-auto=update):
--   sanmarcos_usuarios: usuarios, sesiones_refresh
--   sanmarcos_catalogo: productos, ventas_inventario, venta_lineas
--   sanmarcos_pedidos : pedidos, pedido_items
--   sanmarcos_citas   : citas, mensajes_contacto
