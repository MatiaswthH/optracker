# PLG-OPTracker

Sistema de planificación y monitoreo de operaciones para la empresa PLG, diseñado para optimizar rutas de distribución de GLP utilizando un algoritmo genético con A\*.

## Requisitos Previos

- [Git](https://git-scm.com/)
- [Bun](https://bun.sh/) (para construir el frontend)
- [Just](https://just.systems/) (para comandos del backend)
- Java 21+
- MySQL (opcional; hoy no se usa. Para desarrollo local: `docker compose --env-file .env up` en `plg_back`)

## Clonar el Repositorio

```
git clone https://github.com/MatiaswthH/optracker.git
cd optracker
```

## Backend (`plg_back`)

1. Navegar al directorio del backend:
	```
	cd plg_back
	```
2. Ejecutar los siguientes comandos para compilar y correr el servidor:
	```
	just build
	just run-server
	```
	Esto iniciará el backend en el puerto `8080`, accesible desde cualquier IP (`0.0.0.0`).

> La base de datos no se usa actualmente (la autoconfiguración de DataSource está desactivada). Si se habilita, las credenciales se leen de variables de entorno (ver `plg_back/.env.example`); nunca se versionan.

## Frontend (`plg_front`)

El frontend se *sirve de manera estática mediante Nginx*.

1. Con `bun` instalado, ejecutar:
	```
	bun install
	bun run build
	```
2. El frontend estará disponible en:

## Notas

- Validar que los puertos necesarios estén abiertos para correr el sistema en otro servidor.