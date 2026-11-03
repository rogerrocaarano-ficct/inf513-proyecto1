# Tasks

## 1. Retirar la colección de Bruno

- [x] 1.1 Quitar `src/test/Mailpit API/` del índice y del disco (`git rm -r --cached` y borrar la carpeta); verificar con `git status` que ningún archivo de la colección queda en staging ni en el disco
- [x] 1.2 Quitar la sección "Pruebas por correo con Bruno" del `README.md`; verificar con `grep -i bruno README.md` que no quedan referencias

## 2. Archivos de comandos

- [x] 2.1 Crear `src/test/http/01-ping.http` y `02-create-user.http` con las variables de D2, el cuerpo mínimo de D3 y los asuntos de D4, con comentarios en inglés (D5) y sin scripts, variables dinámicas ni archivos de entorno (D1). `02-create-user.http` lleva las dos peticiones de D4.1, con 3 y con 6 argumentos; verificar con el stack de `docker-compose.dev.yml` y `mail-app` en ejecución que cada petición devuelve 200, que llega `Re: Ping()` y que llegan las dos respuestas de cuenta creada
- [x] 2.2 Crear `03-view-user.http`, `04-list-users.http`, `05-update-user.http`, `06-deactivate-user.http` y `07-reactivate-user.http` igual que en 2.1; verificar ejecutando la secuencia completa 01 a 07 en orden que cada comando recibe una respuesta de éxito, no de rechazo

## 3. Archivo del buzón

- [x] 3.1 Crear `src/test/http/mailbox.http` con `@baseUrl`, `@mailbox`, la búsqueda de los últimos 10 mensajes de la casilla y el borrado de todos los mensajes (D6); verificar que la búsqueda devuelve solo mensajes dirigidos a `@mailbox` y que después del borrado `GET /api/v1/messages` devuelve una lista vacía

## 4. Documentación

- [x] 4.1 Agregar al `README.md`, en "Desarrollo local", una sección sobre `src/test/http/`: qué IDEs la abren, `POLL_INTERVAL=5s`, el orden de ejecución, `mailbox.http` para ver las respuestas y reiniciar los datos con `docker compose -f docker-compose.dev.yml down -v` para repetir la secuencia; verificar siguiendo la sección tal como está escrita sobre una base de datos vacía
