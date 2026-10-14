# Sistema Control de Acceso OT — Portero Inteligente

Aplicación Android en Kotlin y Jetpack Compose que presenta y simula el circuito de una barrera de estacionamiento activada por oscuridad.

## Descargar la aplicación

Puedes descargar el APK de prueba desde [`distribucion/AccesoSeguro-debug.apk`](distribucion/AccesoSeguro-debug.apk). En un teléfono Android debes permitir temporalmente la instalación desde la aplicación con la que abras el archivo.

El APK requiere Android 7.0 (API 24) o una versión posterior.

## Abrir en Android Studio

Abre la carpeta `GarajeInteligente` como proyecto. Gradle sincronizará las dependencias y podrás ejecutar la app en un emulador o dispositivo Android (API 24 o superior).

## Probar la simulación

- Inicia sesión con **admin** y la contraseña **Admin123!**.
- En **Monitoreo**, mueve los controles del sensor ultrasónico y de la fotorresistencia LDR.
- Si la luz baja del umbral y la distancia es menor al límite, la barrera se abre y se enciende el LED rojo.
- En **Activador**, selecciona el modo automático o fuerza la barrera abierta o cerrada.
- Ajusta los umbrales dinámicos, activa la anulación de emergencia y envía comandos inalámbricos.
- En **Conexión Inalámbrica**, elige Bluetooth, Wi-Fi o malla móvil y revisa la estabilidad del enlace.
- Pulsa **Auditoría** para consultar los eventos de acceso y operación registrados durante la sesión.

## Probar la conexión inalámbrica

1. Abre **Conexión Inalámbrica**.
2. Selecciona Bluetooth, Wi-Fi UDP o interconexión móvil.
3. Pulsa **Conectar** para iniciar la simulación de enlace.
4. Revisa la latencia, la pérdida, los paquetes y la intensidad RSSI mostrados en pantalla.

La aplicación simula los sensores, actuadores y conexiones del prototipo. La autenticación y el almacenamiento de credenciales en SQLite sí funcionan localmente.

## Funciones incluidas

- Pantalla inicial compatible con SplashScreen.
- Acceso OT con usuario y contraseña de prueba.
- Panel para simular luz ambiental, distancia, barrera y luces de alerta.
- Modos automático y manual del servomotor.
- Anulación de emergencia y calibración de umbrales.
- Selector de conexión Bluetooth, Wi-Fi y malla móvil.
- Registro visible de auditoría alineado con ISO/IEC 27001.
- Autenticación local con credenciales protegidas mediante PBKDF2, sal aleatoria y SQLite.
- Simulación visual de conexiones Bluetooth, Wi-Fi UDP y malla móvil.
- Contadores de paquetes, latencia, pérdida y RSSI para la demostración.
- Secciones **Monitoreo**, **Activador** y **Conexión Inalámbrica** mediante pestañas.
- Textos y descripciones preparados para accesibilidad.

## Relación con la guía TI3042 Unidad 2

- Aplicación desarrollada en Android Studio y Kotlin.
- Despliegue en dos dispositivos Android mediante Wi-Fi.
- Simulación de control y monitoreo de un sistema IoT.
- Login con usuario y contraseña.
- Persistencia y consulta de datos mediante SQLite.
- Protección de credenciales, control de acceso y cifrado de la comunicación, siguiendo principios de ISO/IEC 27400.
