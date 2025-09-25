# 🎮 The Summer I Turned Pretty - Mini Juegos

Una colección de mini juegos para Android inspirados en la serie "The Summer I Turned Pretty". El proyecto incluye tres juegos clásicos adaptados con la temática de la serie, personajes y locaciones icónicas como Cousins Beach.

## 📱 Juegos Incluidos

### 🎾 Tennis Game
Un emocionante juego de tenis donde te enfrentas a la IA en partidas rápidas y desafiantes.

**Características:**
- Sistema de 4 vidas para cada jugador
- Control táctil intuitivo
- IA adaptativa que sigue la pelota
- Dificultad progresiva
- Efectos de sonido
- Sistema de puntuación

### 🐍 Snake Summer
El clásico juego de la serpiente con un toque veraniego de Cousins Beach.

**Características:**
- Tablero de 15x15
- Sistema de puntuación con récord histórico
- Controles direccionales
- Ambientación temática de verano
- Efectos de sonido

### 🃏 Memorama TSITP
Un juego de memoria con los personajes favoritos de la serie.

**Características:**
- 16 cartas (8 pares)
- 10 vidas por partida
- Imágenes de personajes:
  - Belly
  - Conrad
  - Jeremiah
  - Taylor
  - Steven
  - Susannah
  - Y más

## 🏗️ Arquitectura del Proyecto

### Estructura de Actividades
- `MainActivity`: Manejo de autenticación y registro
- `CategoriesActivity`: Menú principal de selección de juegos
- `TennisActivity`: Implementación del juego de tenis
- `SnakeActivity`: Implementación del juego Snake
- `MemoramaActivity`: Implementación del juego de memoria

### Componentes Principales
1. **Sistema de Autenticación**
   - Registro de usuarios
   - Inicio de sesión
   - Almacenamiento de preferencias

2. **Motor de Juegos**
   - Manejo de estados
   - Sistema de colisiones
   - Control de puntuación
   - Gestión de vidas

3. **Sistema de Audio**
   - `MediaPlayer` para efectos de sonido
   - Música de fondo
   - Efectos especiales

4. **Almacenamiento de Datos**
   - `SharedPreferences` para guardar:
     - Puntuaciones más altas
     - Datos de usuario
     - Configuraciones

### Tecnologías Utilizadas
- 💻 Kotlin como lenguaje principal
- 🎨 XML para layouts
- 🎵 MediaPlayer para audio
- 💾 SharedPreferences para persistencia
- 📱 Android SDK

## 🚀 Requisitos del Sistema
- Android 6.0 (API 23) o superior
- ~50MB de espacio en almacenamiento
- 2GB RAM recomendado

## 📥 Instalación
1. Clona el repositorio:
```bash
git clone https://github.com/NaomiMSR/Videojuego.git
