# Acceso Seguro — Portero Inteligente

Aplicación Android en Kotlin y Jetpack Compose que presenta y simula el circuito de una barrera de estacionamiento activada por oscuridad.

## Descargar la aplicación

Puedes descargar el APK de prueba desde [`distribucion/AccesoSeguro-debug.apk`](distribucion/AccesoSeguro-debug.apk). En un teléfono Android debes permitir temporalmente la instalación desde la aplicación con la que abras el archivo.

El APK requiere Android 7.0 (API 24) o una versión posterior.

## Abrir en Android Studio

Abre la carpeta `GarajeInteligente` como proyecto. Gradle sincronizará las dependencias y podrás ejecutar la app en un emulador o dispositivo Android (API 24 o superior).

## Probar la simulación

- Cambia entre **Día** y **Noche** para ver cuándo se activa el sensor ultrasónico.
- En modo noche, pulsa **Simular vehículo acercándose**: la barrera se levanta y las luces rojas se encienden.
- Pulsa **Retirar vehículo y reiniciar** para cerrar la barrera y apagar la alerta.
- Abre **Cómo funciona** para ver los componentes y la secuencia del circuito.
- En **Ajustes** puedes repetir la introducción y revisar que esta versión trabaja como simulador local.

En el primer inicio aparece una introducción breve de tres pasos. Puedes omitirla y no volverá a aparecer después de completarla.

La aplicación simula la lógica y las lecturas del prototipo. No se conecta a una placa ni controla hardware real.

## Funciones incluidas

- Pantalla inicial compatible con SplashScreen.
- Introducción de tres pasos que se muestra en el primer inicio.
- Panel para simular luz ambiental, distancia, barrera y luces de alerta.
- Secciones **Cómo funciona** y **Ajustes** mediante navegación inferior.
- Textos y descripciones preparados para accesibilidad.
