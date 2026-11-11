#!/bin/sh
# Lo ejecuta el contenedor de MySQL la primera vez que arranca (docker-entrypoint-initdb.d).
# Crea UNA BASE POR MICROSERVICIO, cada una con su propio usuario y contraseña:
# el usuario de catálogo no puede leer la base de pedidos, y así con todos.
set -e

crear() {
  base="$1"; usuario="$2"; clave="$3"
  mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" <<SQL
CREATE DATABASE IF NOT EXISTS ${base} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${usuario}'@'%' IDENTIFIED BY '${clave}';
GRANT ALL PRIVILEGES ON ${base}.* TO '${usuario}'@'%';
SQL
}

crear sanmarcos_usuarios sm_usuarios "${DB_USUARIOS_PASSWORD}"
crear sanmarcos_catalogo sm_catalogo "${DB_CATALOGO_PASSWORD}"
crear sanmarcos_pedidos  sm_pedidos  "${DB_PEDIDOS_PASSWORD}"
crear sanmarcos_citas    sm_citas    "${DB_CITAS_PASSWORD}"

mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" -e "FLUSH PRIVILEGES;"
