DBA - Instrucciones de uso

Resumen
- `dba.properties` contiene credenciales y URIs de administrador (DBA).
- Este archivo NO es cargado automáticamente por Spring Boot (no es `application-*.properties`).
- `src/main/resources/application.properties` debe contener únicamente las credenciales del usuario de la aplicación (mínimos permisos).

Uso operativo (ejemplos)

1) PostgreSQL (psql)
- Conexión directa usando las credenciales DBA del archivo `src/main/resources/dba.properties`:

  psql "host=aws-1-us-west-2.pooler.supabase.com port=6543 dbname=postgres user=postgres.ubvbsawipfepclxdvglm password=2UsnTWpSPIj9L6J0"

- Para ejecutar tareas administrativas (crear usuarios, backups, ...).

2) MongoDB (mongo shell / mongo client)
- URI de conexión (DBA) disponible en `dba.properties`. Ejemplo de conexión con mongo shell:

  mongo "mongodb+srv://cluster0.lfnesef.mongodb.net/UniPlan" -u giuseppe_db_user -p 123 --authenticationDatabase admin

Buenas prácticas
- Nunca active el perfil DBA en producción ni cargue `dba.properties` desde la aplicación.
- Use variables de entorno o un gestor de secretos (Vault, Azure KeyVault, AWS Secrets Manager) para almacenar credenciales reales.
- No comitear credenciales sensibles en repositorios públicos.

Cómo crear el usuario de la aplicación (ejemplo PostgreSQL)
- Conéctese como DBA y ejecute (adaptar nombres/contrasenas):

  CREATE ROLE event_app_user WITH LOGIN PASSWORD 'change_me_app_user_password' NOSUPERUSER INHERIT NOCREATEROLE NOCREATEDB;
  GRANT CONNECT ON DATABASE postgres TO event_app_user;
  -- Conceder permisos sobre esquemas/tablas según mínimo necesario

Cómo cambiar la aplicación para usar el usuario app
- Edite `src/main/resources/application.properties` y reemplace los placeholders `event_app_user` y `change_me_app_user_password` con las credenciales reales del usuario de la app.

Notas finales
- Este repositorio ahora separa explícitamente las credenciales de DBA (operacionales) de las de la aplicación (runtime).
- Si desea que la aplicación cargue un perfil con privilegios elevados temporalmente, use un mecanismo seguro (NO dejar archivos con credenciales en el árbol de código fuente).
