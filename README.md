# ⚽ FútbolApp — Sistema de Gestión de Equipo de Fútbol

Proyecto académico completo: Backend Spring Boot + Frontend Android Jetpack Compose.

---

## 📐 Arquitectura

```
Backend  →  Spring Boot (Java) + JPA + Supabase (PostgreSQL)  →  Render.com
Frontend →  Android Studio (Kotlin) + Jetpack Compose          →  APK
```

### Modelo relacional
| Tabla | Columnas clave |
|---|---|
| `equipo` | id_equipo, nombre, ciudad, fundacion |
| `jugador` | id_jugador, nombre, posicion, dorsal, fecha_nac, nacionalidad, id_equipo FK |
| `entrenador` | id_entrenador, nombre, especialidad, id_equipo FK |
| `partido` | id_partido, fecha, estadio, equipo_local FK, equipo_visita FK, goles_local, goles_visita |
| `estadisticas_jugador` | id_estadistica, id_jugador FK, id_partido FK, minutos_jugados, goles, asistencias, tarjetas_amarillas, tarjetas_rojas |

---

## 🚀 Deploy del Backend en Render.com

### Paso 1 — Preparar el JAR de Spring Boot

En IntelliJ IDEA, abre la terminal y ejecuta:

```bash
mvn clean package -DskipTests
```

El JAR se genera en `target/nombre-del-proyecto-0.0.1-SNAPSHOT.jar`.

---

### Paso 2 — Subir el código a GitHub

```bash
git init
git add .
git commit -m "Initial commit - FútbolApp Backend"
git remote add origin https://github.com/tu-usuario/futbol-backend.git
git push -u origin main
```

> ⚠️ Agrega `target/` a `.gitignore` para no subir el JAR compilado. Render lo compila él mismo.

---

### Paso 3 — Crear el Web Service en Render.com

1. Ve a [https://render.com](https://render.com) y crea una cuenta (o inicia sesión).
2. Haz clic en **"New +"** → **"Web Service"**.
3. Conecta tu repositorio de GitHub y selecciona el repo del backend.
4. Configura los campos:

| Campo | Valor |
|---|---|
| **Name** | `futbol-backend` |
| **Environment** | `Java` |
| **Build Command** | `mvn clean package -DskipTests` |
| **Start Command** | `java -jar target/*.jar` |
| **Instance Type** | Free (para pruebas) |

---

### Paso 4 — Variables de entorno en Render

En la sección **"Environment Variables"** de tu Web Service en Render, agrega las siguientes variables. Obtendrás los valores desde el dashboard de Supabase (Settings → Database):

| Variable | Descripción | Ejemplo |
|---|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC de Supabase | `jdbc:postgresql://db.xxxx.supabase.co:5432/postgres` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la BD | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de Supabase | `tu_password_aqui` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Estrategia de esquema | `update` |
| `SERVER_PORT` | Puerto del servidor | `8080` |

Tu `application.properties` en Spring Boot debe leer estas variables así:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.jpa.hibernate.ddl-auto=${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
server.port=${SERVER_PORT:8080}
```

---

### Paso 5 — Obtener la URL pública y configurar Android

Una vez que Render despliega el servicio (tarda ~3-5 min la primera vez), verás una URL pública como:

```
https://futbol-backend.onrender.com
```

**Abre `ApiClient.kt` en Android Studio** y reemplaza la línea del `BASE_URL`:

```kotlin
// app/src/main/java/com/futbol/app/data/network/ApiClient.kt

private const val BASE_URL = "https://futbol-backend.onrender.com/api/"
//                            ↑ Reemplaza con tu URL real de Render
```

> **Nota importante:** En el plan gratuito de Render, el servicio se "duerme" después de 15 minutos de inactividad. La primera petición puede tardar ~30 segundos en "despertar". Esto es normal en modo de prueba.

---

### Paso 6 — Verificar el deploy

1. Abre el navegador y ve a: `https://futbol-backend.onrender.com/swagger-ui.html`
2. Deberías ver la documentación Swagger de tu API.
3. Prueba el endpoint `GET /api/equipos` — debería devolver `[]` (lista vacía al inicio).

---

### Paso 7 — Configurar Dockerfile (opcional, más control)

Si prefieres usar Docker en lugar del buildpack de Java, crea un `Dockerfile` en la raíz del proyecto backend:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

En Render, cambia **Environment** a `Docker` y Render usará el Dockerfile automáticamente.

---

## 📱 Estructura del Frontend Android

```
app/src/main/java/com/futbol/app/
├── MainActivity.kt                  ← Entry point, tema dinámico, NavHost
├── data/
│   ├── model/Models.kt              ← Data classes (Equipo, Jugador, etc.)
│   ├── network/
│   │   ├── ApiClient.kt             ← Retrofit singleton ⚠️ cambia BASE_URL
│   │   └── FutbolApiService.kt      ← Todos los endpoints REST
│   └── repository/Repositories.kt  ← Capa de datos con Result<T>
├── navigation/NavGraph.kt           ← Rutas de navegación
├── ui/
│   ├── components/Components.kt     ← LoadingScreen, ErrorScreen, ShimmerItem…
│   ├── forms/
│   │   └── AgregarJugadorForm.kt    ← Formulario con validación inline
│   ├── screens/
│   │   ├── HomeScreen.kt
│   │   ├── JugadoresScreen.kt       ← Pull-to-refresh, búsqueda, FAB animado
│   │   ├── JugadorDetailScreen.kt   ← Detalle + estadísticas + editar/eliminar
│   │   ├── EquiposScreen.kt         ← Lista + detalle con jugadores y entrenadores
│   │   ├── PartidosScreen.kt        ← TabRow resultados/próximos, score animado
│   │   ├── EstadisticasScreen.kt    ← Leaderboard medallas, filtros, stagger
│   │   └── EntrenadoresScreen.kt    ← CRUD con BottomSheet y Snackbar
│   └── theme/
│       ├── Color.kt                 ← Paleta verde campo
│       ├── Type.kt                  ← Tipografía
│       └── Theme.kt                 ← DarkColorScheme + LightColorScheme
└── viewmodel/
    ├── DarkModeViewModel.kt         ← Estado global de tema oscuro/claro
    ├── JugadoresViewModel.kt        ← StateFlow + Loading/Success/Error
    └── ViewModels.kt                ← Equipos, Partidos, Entrenadores, Estadísticas
```

---

## 🛠️ Configuración rápida

### 1. Abrir el proyecto en Android Studio
```
File → Open → selecciona la carpeta futbol-android/
```
Espera que Gradle sincronice (requiere conexión a internet la primera vez).

### 2. Cambiar la URL del backend
Edita `app/src/main/java/com/futbol/app/data/network/ApiClient.kt`:
```kotlin
private const val BASE_URL = "https://TU-APP.onrender.com/api/"
```

### 3. Ejecutar
Conecta un dispositivo Android (API 26+) o usa el emulador y presiona **Run ▶**.

---

## 🔗 Endpoints principales

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/equipos` | Listar todos los equipos |
| POST | `/api/equipos` | Crear equipo |
| GET | `/api/jugadores` | Listar jugadores |
| GET | `/api/jugadores/equipo/{id}` | Jugadores por equipo |
| GET | `/api/partidos` | Listar partidos |
| GET | `/api/partidos/resultados` | Solo resultados (fecha pasada) |
| GET | `/api/estadisticas/jugador/{id}` | Stats de un jugador |
| GET | `/swagger-ui.html` | Documentación Swagger |

---

## ✅ Dependencias clave (app/build.gradle)

| Librería | Uso |
|---|---|
| Jetpack Compose BOM 2024.02 | UI declarativo |
| Navigation Compose 2.7.6 | Navegación entre pantallas |
| Retrofit 2.9.0 + Gson | Llamadas HTTP al backend |
| Coil Compose 2.5.0 | Carga de imágenes |
| Accompanist SystemUI | Control de status bar |
| Material3 Pull-to-Refresh | Refrescar listas |

---

*Proyecto académico — Gestión de Equipo de Fútbol · Spring Boot + Supabase + Android Compose*
#   f u t b o l - f r o n t e n d  
 