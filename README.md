# Acceso Seguro — Portero Inteligente

Aplicación Android en Kotlin y Jetpack Compose que presenta y simula el circuito de una barrera de estacionamiento activada por oscuridad.

## Descargar la aplicación

Puedes descargar el APK de prueba desde [`distribucion/AccesoSeguro-debug.apk`](distribucion/AccesoSeguro-debug.apk). En un teléfono Android debes permitir temporalmente la instalación desde la aplicación con la que abras el archivo.

El APK requiere Android 7.0 (API 24) o una versión posterior.

## Abrir en Android Studio

Abre la carpeta `GarajeInteligente` como proyecto. Gradle sincronizará las dependencias y podrás ejecutar la app en un emulador o dispositivo Android (API 24 o superior).

## Probar la simulación

- Inicia sesión con **operador** y la contraseña **Acceso2026!**.
- Cambia entre **Día** y **Noche** para ver cuándo se activa el sensor ultrasónico.
- En modo noche, pulsa **Simular vehículo acercándose**: la barrera se levanta y las luces rojas se encienden.
- Pulsa **Retirar vehículo y reiniciar** para cerrar la barrera y apagar la alerta.
- Abre **Cómo funciona** para ver los componentes y la secuencia del circuito.
- En **Ajustes** puedes repetir la introducción y revisar que esta versión trabaja como simulador local.

## Conectar dos dispositivos Android

1. Instala la aplicación en dos teléfonos conectados a la misma red Wi-Fi.
2. En el primer teléfono abre **Conexión**, conserva o cambia el código de vinculación y pulsa **Este teléfono controla**.
3. En el segundo teléfono abre **Conexión**, escribe la IP que muestra el primer dispositivo, usa el mismo código y pulsa **Conectar este teléfono como monitor**.
4. Cambia el escenario desde el panel del teléfono controlador. Las lecturas cifradas aparecerán en el monitor y se guardarán en su historial.

La interconexión usa el puerto TCP `40404` y cifra cada mensaje con AES-GCM. Ambos teléfonos deben permanecer en la misma red local y cualquier aislamiento de clientes configurado en el router debe estar desactivado.

En el primer inicio aparece una introducción breve de tres pasos. Puedes omitirla y no volverá a aparecer después de completarla.

La aplicación simula los sensores y actuadores del prototipo. La autenticación, el almacenamiento SQLite y la comunicación Wi-Fi entre dos dispositivos Android son funcionales.

## Funciones incluidas

- Pantalla inicial compatible con SplashScreen.
- Introducción de tres pasos que se muestra en el primer inicio.
- Panel para simular luz ambiental, distancia, barrera y luces de alerta.
- Autenticación local con credenciales protegidas mediante PBKDF2, sal aleatoria y SQLite.
- Interconexión Wi-Fi entre un teléfono controlador y otro monitor.
- Mensajes cifrados y autenticados con AES-GCM y código de vinculación temporal.
- Historial SQLite de lecturas locales y remotas.
- Secciones **Conexión**, **Datos**, **Sistema** y **Ajustes** mediante navegación inferior.
- Textos y descripciones preparados para accesibilidad.

## Relación con la guía TI3042 Unidad 2

- Aplicación desarrollada en Android Studio y Kotlin.
- Despliegue en dos dispositivos Android mediante Wi-Fi.
- Simulación de control y monitoreo de un sistema IoT.
- Login con usuario y contraseña.
- Persistencia y consulta de datos mediante SQLite.
- Protección de credenciales, control de acceso y cifrado de la comunicación, siguiendo principios de ISO/IEC 27400.
