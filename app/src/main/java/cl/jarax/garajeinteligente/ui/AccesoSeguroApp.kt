package cl.jarax.garajeinteligente.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AccesoSeguroApp(
    initialOnboardingComplete: Boolean,
    onOnboardingComplete: () -> Unit,
    onRestartOnboarding: () -> Unit,
) {
    var isNight by rememberSaveable { mutableStateOf(true) }
    var approaching by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var onboardingComplete by rememberSaveable { mutableStateOf(initialOnboardingComplete) }
    var onboardingStep by rememberSaveable { mutableIntStateOf(0) }

    if (!onboardingComplete) {
        OnboardingScreen(
            step = onboardingStep,
            onStepChange = { onboardingStep = it },
            onSkip = {
                onboardingComplete = true
                onOnboardingComplete()
            },
            onFinish = {
                onboardingComplete = true
                onOnboardingComplete()
            },
        )
        return
    }

    Scaffold(
        containerColor = Ink,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = Panel,
                contentColor = TextMuted,
                windowInsets = WindowInsets.navigationBars,
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Panel") },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.AccountTree, contentDescription = null) },
                    label = { Text("Cómo funciona") },
                    colors = navColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text("Ajustes") },
                    colors = navColors(),
                )
            }
        },
    ) { innerPadding ->
        if (selectedTab == 0) {
            Dashboard(
                isNight = isNight,
                approaching = approaching,
                onNightChange = {
                    isNight = it
                    if (!it) approaching = false
                },
                onApproachChange = { approaching = it },
                modifier = Modifier.padding(innerPadding),
            )
        } else if (selectedTab == 1) {
            HowItWorks(modifier = Modifier.padding(innerPadding))
        } else {
            SettingsScreen(
                onReplayIntro = {
                    onboardingStep = 0
                    onboardingComplete = false
                    onRestartOnboarding()
                },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

private data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val label: String,
)

private val onboardingPages = listOf(
    OnboardingPage(
        title = "Detecta la oscuridad",
        description = "La fotorresistencia reconoce si hay poca luz. De día, los actuadores permanecen apagados para ahorrar energía.",
        icon = Icons.Filled.LightMode,
        label = "LDR · luz ambiente",
    ),
    OnboardingPage(
        title = "Busca obstáculos de noche",
        description = "Cuando oscurece, se activa el sensor ultrasónico para detectar si un auto o una persona se acerca.",
        icon = Icons.Filled.Sensors,
        label = "HC-SR04 · distancia",
    ),
    OnboardingPage(
        title = "Abre y alerta",
        description = "Si algo está cerca, el servomotor levanta la barrera y las luces rojas avisan que el acceso está activo.",
        icon = Icons.Filled.WarningAmber,
        label = "Servomotor + luces rojas",
    ),
)

@Composable
private fun OnboardingScreen(
    step: Int,
    onStepChange: (Int) -> Unit,
    onSkip: () -> Unit,
    onFinish: () -> Unit,
) {
    val page = onboardingPages[step]
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        BrandHeader()
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onSkip) { Text("Omitir", color = TextMuted) }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            onboardingPages.indices.forEach { index ->
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = if (index == step) 26.dp else 8.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(if (index == step) Mint else PanelRaised),
                )
            }
        }
        Surface(
            color = Panel,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier.size(86.dp).clip(RoundedCornerShape(25.dp)).background(Mint.copy(alpha = .12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(page.icon, contentDescription = null, tint = Mint, modifier = Modifier.size(42.dp))
                }
                Spacer(Modifier.height(24.dp))
                Text("PASO ${step + 1} DE ${onboardingPages.size} · ${page.label.uppercase()}", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, letterSpacing = .7.sp)
                Spacer(Modifier.height(12.dp))
                Text(page.title, color = TextMain, fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(page.description, color = TextMuted, fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (step > 0) {
                OutlinedButton(
                    onClick = { onStepChange(step - 1) },
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Atrás")
                }
            }
            Button(
                onClick = if (step == onboardingPages.lastIndex) onFinish else ({ onStepChange(step + 1) }),
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Ink),
            ) {
                Text(if (step == onboardingPages.lastIndex) "Entrar al simulador" else "Siguiente", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Mint,
    selectedTextColor = MintSoft,
    indicatorColor = PanelRaised,
    unselectedIconColor = TextMuted,
    unselectedTextColor = TextMuted,
)

@Composable
private fun Dashboard(
    isNight: Boolean,
    approaching: Boolean,
    onNightChange: (Boolean) -> Unit,
    onApproachChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        BrandHeader()

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Tu entrada,\nsiempre segura.",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TextMain,
                lineHeight = 37.sp,
            )
            Text(
                "La luz decide cuándo activar la protección.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
            )
        }

        GarageStatusCard(isNight, approaching)

        ScenarioControls(isNight, approaching, onNightChange, onApproachChange)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeading("Lecturas del circuito", "DEMO")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SensorCard(
                    title = "Fotorresistencia",
                    value = if (isNight) "Noche" else "Día",
                    detail = if (isNight) "18 lx · oscuro" else "248 lx · luz ambiente",
                    icon = if (isNight) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                    active = true,
                    modifier = Modifier.weight(1f),
                )
                SensorCard(
                    title = "Distancia",
                    value = if (!isNight) "En pausa" else if (approaching) "0,6 m" else "2,8 m",
                    detail = if (!isNight) "Se activa de noche" else if (approaching) "Objeto cercano" else "Zona despejada",
                    icon = Icons.Filled.Sensors,
                    active = isNight,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SensorCard(
                    title = "Servomotor",
                    value = if (approaching && isNight) "Abierta" else "Cerrada",
                    detail = if (approaching && isNight) "Barrera levantada" else "Barrera abajo",
                    icon = Icons.Filled.LocalParking,
                    active = approaching && isNight,
                    modifier = Modifier.weight(1f),
                )
                SensorCard(
                    title = "Luces rojas",
                    value = if (approaching && isNight) "Alerta" else "Apagadas",
                    detail = if (approaching && isNight) "Aviso de acceso" else "Sin alerta activa",
                    icon = Icons.Filled.WarningAmber,
                    active = approaching && isNight,
                    modifier = Modifier.weight(1f),
                    alert = approaching && isNight,
                )
            }
        }

        EnergyCard(isNight)
        FlowSummary(isNight, approaching)
        Text(
            "Simulación educativa · Las lecturas son referenciales y no se conectan a hardware.",
            color = TextMuted,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        )
    }
}

@Composable
private fun BrandHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Mint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.LocalParking, contentDescription = null, tint = Ink, modifier = Modifier.size(22.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("ACCESO SEGURO", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMain, letterSpacing = 1.1.sp)
        Text("PORTERO INTELIGENTE", fontSize = 11.sp, color = TextMuted, letterSpacing = 1.2.sp)
            }
        }
        Row(
            modifier = Modifier.clip(CircleShape).background(PanelRaised).padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(Mint))
            Text("MODO DEMO", fontSize = 10.sp, color = MintSoft, fontWeight = FontWeight.Bold, letterSpacing = .7.sp)
        }
    }
}

@Composable
private fun GarageStatusCard(isNight: Boolean, approaching: Boolean) {
    val raised by animateFloatAsState(
        targetValue = if (isNight && approaching) 1f else 0f,
        animationSpec = tween(600),
        label = "gate position",
    )
    val active = isNight && approaching
    Surface(
        color = Panel,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = .06f), RoundedCornerShape(24.dp)),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (active) "VEHÍCULO DETECTADO" else if (isNight) "NOCHE DETECTADA" else "DÍA DETECTADO", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                    Text(
                        when {
                            active -> "Acceso en alerta"
                            isNight -> "Sistema listo para proteger"
                            else -> "Sistema en ahorro"
                        },
                        fontSize = 18.sp,
                        color = TextMain,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                StatusBadge(
                    text = if (active) "ALERTA" else if (isNight) "ACTIVO" else "AHORRANDO",
                    color = if (active) Red else Mint,
                )
            }

            GarageIllustration(
                raised = raised,
                active = active,
                isNight = isNight,
                description = when {
                    active -> "Vehículo detectado de noche; barrera levantada y luces rojas encendidas."
                    isNight -> "Garaje de noche; ultrasonido activo, barrera cerrada y sin alerta."
                    else -> "Garaje de día; sensores y actuadores en reposo para ahorrar energía."
                },
                modifier = Modifier.fillMaxWidth().height(164.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(PanelRaised).padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Icon(
                    imageVector = when {
                        active -> Icons.Filled.WarningAmber
                        isNight -> Icons.Filled.Sensors
                        else -> Icons.Filled.Bolt
                    },
                    contentDescription = null,
                    tint = when { active -> Red; isNight -> Mint; else -> Amber },
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    when {
                        active -> "Barrera arriba · luces rojas encendidas"
                        isNight -> "Ultrasonido activo · acceso despejado"
                        else -> "Sensores y actuadores en reposo"
                    },
                    color = TextMain,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun GarageIllustration(raised: Float, active: Boolean, isNight: Boolean, description: String, modifier: Modifier = Modifier) {
    val angle by animateFloatAsState(targetValue = -62f * raised, animationSpec = tween(650), label = "barrier angle")
    Box(modifier = modifier.clip(RoundedCornerShape(18.dp)).background(Color(0xFF0D171C)).semantics { contentDescription = description }) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val px = density
            val garageLeft = w * .12f
            val garageTop = h * .09f
            val garageWidth = w * .76f
            val garageHeight = h * .56f
            drawRoundRect(Color(0xFF273940), Offset(garageLeft, garageTop), Size(garageWidth, garageHeight), CornerRadius(12f * px))
            drawRoundRect(Color(0xFF0B1216), Offset(garageLeft + w * .12f, garageTop + h * .13f), Size(w * .52f, h * .50f), CornerRadius(6f * px))
            drawRoundRect(Color(0xFF344951), Offset(garageLeft + 7 * px, garageTop + 6 * px), Size(garageWidth - 14 * px, 5 * px), CornerRadius(3 * px))
            drawRect(Color(0xFF17252B), Offset(0f, h * .70f), Size(w, h * .30f))
            for (i in 0..4) {
                val x = w * (.08f + i * .21f)
                drawLine(Color(0xFF58686C), Offset(x, h * .87f), Offset(x + w * .11f, h * .87f), strokeWidth = 2.5f * px, cap = StrokeCap.Round)
            }
            // Detector above the entrance
            val sensorColor = if (isNight) Mint else Amber
            drawCircle(sensorColor.copy(alpha = .18f), radius = 11 * px, center = Offset(w * .69f, h * .20f))
            drawCircle(sensorColor, radius = 4 * px, center = Offset(w * .69f, h * .20f))
            // Red warning lamps
            val lampAlpha = if (active) 1f else .20f
            for (i in 0..2) {
                drawRoundRect(Red.copy(alpha = lampAlpha), Offset(w * (.74f + i * .055f), h * .34f), Size(8 * px, 5 * px), CornerRadius(3 * px))
            }
            // Barrier arm and pivot. Canvas rotation uses clockwise-positive degrees.
            val pivot = Offset(w * .31f, h * .75f)
            drawCircle(Color(0xFFCEDAD8), radius = 10 * px, center = pivot)
            drawContext.transform.rotate(degrees = angle, pivot = pivot)
            drawRoundRect(Color(0xFFEEF3EF), Offset(pivot.x, pivot.y - 5 * px), Size(w * .48f, 10 * px), CornerRadius(5 * px))
            drawRoundRect(Red, Offset(pivot.x + w * .20f, pivot.y - 5 * px), Size(w * .10f, 10 * px), CornerRadius(4 * px))
            drawContext.transform.rotate(degrees = -angle, pivot = pivot)
            // Approaching car
            val carX = if (active) w * .69f else w * .71f
            val carY = h * .72f
            val carColor = if (active) Color(0xFF3D9D80) else Color(0xFF48626A)
            drawRoundRect(carColor, Offset(carX, carY), Size(w * .20f, h * .13f), CornerRadius(8 * px))
            drawRoundRect(Color(0xFF9CC9BB), Offset(carX + w * .045f, carY - h * .07f), Size(w * .11f, h * .08f), CornerRadius(7 * px))
            drawCircle(Color(0xFF0A1116), radius = 4 * px, center = Offset(carX + w * .045f, carY + h * .13f))
            drawCircle(Color(0xFF0A1116), radius = 4 * px, center = Offset(carX + w * .16f, carY + h * .13f))
            if (active) {
                drawCircle(Red.copy(alpha = .14f), radius = 14 * px, center = Offset(w * .85f, h * .41f))
                drawCircle(Red, radius = 4 * px, center = Offset(w * .85f, h * .41f))
            }
        }
        Row(
            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp).clip(CircleShape).background(Ink.copy(alpha = .72f)).padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(Modifier.size(5.dp).clip(CircleShape).background(if (isNight) Mint else Amber))
            Text(if (isNight) "SENSOR LDR: OSCURIDAD" else "SENSOR LDR: LUZ", fontSize = 10.sp, color = TextMain, letterSpacing = .4.sp)
        }
    }
}

@Composable
private fun ScenarioControls(
    isNight: Boolean,
    approaching: Boolean,
    onNightChange: (Boolean) -> Unit,
    onApproachChange: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeading("Prueba el circuito", "CONTROLES")
        Surface(color = Panel, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                    ScenarioButton(
                        label = "Día",
                        icon = Icons.Filled.LightMode,
                        selected = !isNight,
                        color = Amber,
                        modifier = Modifier.weight(1f),
                        onClick = { onNightChange(false) },
                    )
                    ScenarioButton(
                        label = "Noche",
                        icon = Icons.Filled.DarkMode,
                        selected = isNight,
                        color = Mint,
                        modifier = Modifier.weight(1f),
                        onClick = { onNightChange(true) },
                    )
                }
                if (!approaching) {
                    Button(
                        onClick = { onApproachChange(true) },
                        enabled = isNight,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Mint,
                            contentColor = Ink,
                            disabledContainerColor = PanelRaised,
                            disabledContentColor = TextMuted,
                        ),
                    ) {
                        Icon(Icons.Filled.LocalParking, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isNight) "Simular vehículo acercándose" else "El sensor duerme durante el día", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onApproachChange(false) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MintSoft),
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Retirar vehículo y reiniciar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
                Text(
                    if (isNight) "De noche, el ultrasonido busca obstáculos cercanos." else "Con luz de día, el ultrasonido, el servo y las luces permanecen apagados.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun ScenarioButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val background = if (selected) color.copy(alpha = .13f) else PanelRaised
    val foreground = if (selected) color else TextMuted
    Row(
        modifier = modifier
            .height(43.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(background)
            .border(1.dp, if (selected) color.copy(alpha = .38f) else Color.Transparent, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = foreground, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(7.dp))
        Text(label, color = foreground, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionHeading(title: String, label: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, fontSize = 16.sp, color = TextMain, fontWeight = FontWeight.SemiBold)
        Text(label, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
private fun SensorCard(
    title: String,
    value: String,
    detail: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    modifier: Modifier = Modifier,
    alert: Boolean = false,
) {
    val accent = if (alert) Red else if (active) Mint else TextMuted
    Surface(color = Panel, shape = RoundedCornerShape(18.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.size(31.dp).clip(RoundedCornerShape(10.dp)).background(accent.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(17.dp))
                }
                Box(Modifier.size(6.dp).clip(CircleShape).background(accent))
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontSize = 12.sp, color = TextMuted)
                Text(value, fontSize = 15.sp, color = TextMain, fontWeight = FontWeight.SemiBold)
                Text(detail, fontSize = 11.sp, color = accent, lineHeight = 14.sp)
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Row(
        modifier = Modifier.clip(CircleShape).background(color.copy(alpha = .13f)).padding(horizontal = 9.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.size(5.dp).clip(CircleShape).background(color))
        Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = .5.sp)
    }
}

@Composable
private fun EnergyCard(isNight: Boolean) {
    Surface(color = Panel, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Amber.copy(alpha = .13f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = Amber, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(if (isNight) "Ahorro inteligente" else "Ahorro diurno activo", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    if (isNight) "Solo se activa lo necesario cuando oscurece." else "Barrera, ultrasonido y luces están apagados.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
            Icon(if (isNight) Icons.Filled.TipsAndUpdates else Icons.Filled.CheckCircle, contentDescription = null, tint = if (isNight) Mint else Amber, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun FlowSummary(isNight: Boolean, approaching: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeading("Lógica del sistema", "SECUENCIA")
        Surface(color = Panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                FlowNode("LDR", "luz", active = true, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = if (isNight) Mint else TextMuted, modifier = Modifier.size(13.dp))
                FlowNode("HC-SR04", "distancia", active = isNight, modifier = Modifier.weight(1.15f))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = if (approaching && isNight) Mint else TextMuted, modifier = Modifier.size(13.dp))
                FlowNode("SERVO + LED", "barrera", active = approaching && isNight, modifier = Modifier.weight(1.25f))
            }
        }
    }
}

@Composable
private fun FlowNode(label: String, detail: String, active: Boolean, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(if (active) Mint else TextMuted.copy(alpha = .45f)))
        Text(label, color = if (active) TextMain else TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 12.sp)
        Text(detail, color = TextMuted, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun HowItWorks(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        BrandHeader()
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Así piensa tu\nportero inteligente.", fontSize = 29.sp, lineHeight = 35.sp, color = TextMain, fontWeight = FontWeight.Bold)
            Text("Un circuito que cuida el acceso y evita gastar energía de más.", color = TextMuted, fontSize = 13.sp, lineHeight = 19.sp)
        }
        Surface(color = Panel, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                ProcessStep("01", "Detectar la oscuridad", "La fotorresistencia (LDR) mide la luz ambiente. De día, el sistema deja los actuadores apagados.", Icons.Filled.LightMode, Mint, last = false)
                ProcessStep("02", "Buscar obstáculos", "Al detectar noche, se activa el sensor ultrasónico y mide si un vehículo o una persona se acerca.", Icons.Filled.Sensors, Mint, last = false)
                ProcessStep("03", "Abrir y alertar", "Si algo está cerca, el servomotor levanta la barrera y las luces rojas avisan que hay movimiento.", Icons.Filled.WarningAmber, Red, last = false)
                ProcessStep("04", "Volver a reposo", "Cuando el acceso queda libre, la barrera baja y la alerta se apaga. Con luz de día, el sistema ahorra energía.", Icons.Filled.CheckCircle, Amber, last = true)
            }
        }
        Text("Componentes del prototipo", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
        ComponentRow("LDR", "Fotorresistencia", "Distingue día y noche", Icons.Filled.LightMode)
        ComponentRow("HC-SR04", "Sensor ultrasónico", "Mide distancia a un obstáculo", Icons.Filled.Sensors)
        ComponentRow("SG90", "Servomotor", "Levanta y baja la barrera", Icons.Filled.LocalParking)
        ComponentRow("LED rojo", "Luz de alerta", "Indica que el acceso está activo", Icons.Filled.WarningAmber)
        Surface(color = PanelRaised, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.TipsAndUpdates, contentDescription = null, tint = Mint, modifier = Modifier.size(19.dp))
                Text("Este prototipo se presenta como simulación de la lógica del circuito. Para controlar componentes reales se requiere una conexión compatible con la placa.", color = TextMuted, fontSize = 11.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun SettingsScreen(onReplayIntro: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BrandHeader()
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Ajustes", fontSize = 29.sp, lineHeight = 35.sp, color = TextMain, fontWeight = FontWeight.Bold)
            Text("Preferencias de la demostración del circuito.", color = TextMuted, fontSize = 14.sp, lineHeight = 20.sp)
        }
        Surface(color = Panel, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Modo de funcionamiento", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("La aplicación funciona de manera local como simulador. Las lecturas y acciones se representan en pantalla; no hay una placa conectada.", color = TextMuted, fontSize = 13.sp, lineHeight = 19.sp)
                StatusBadge(text = "SIN HARDWARE VINCULADO", color = Amber)
            }
        }
        Surface(color = Panel, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Introducción", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("Vuelve a ver los tres pasos sobre el LDR, el sensor de distancia y la barrera.", color = TextMuted, fontSize = 13.sp, lineHeight = 19.sp)
                OutlinedButton(
                    onClick = onReplayIntro,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MintSoft),
                ) {
                    Text("Repetir introducción", fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Surface(color = PanelRaised, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                "No se crea una cuenta ni se solicitan permisos de red, ubicación o Bluetooth. Para usar componentes reales, primero se debe agregar una conexión compatible con la placa.",
                modifier = Modifier.padding(15.dp),
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun ProcessStep(number: String, title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, last: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(29.dp)) {
            Box(Modifier.size(29.dp).clip(CircleShape).background(accent.copy(alpha = .13f)), contentAlignment = Alignment.Center) {
                Text(number, color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            if (!last) Canvas(Modifier.width(1.dp).height(51.dp)) { drawLine(TextMuted.copy(alpha = .25f), Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = 1.dp.toPx()) }
        }
        Column(Modifier.weight(1f).padding(bottom = if (last) 0.dp else 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
            }
            Text(description, color = TextMuted, fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun ComponentRow(code: String, title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(color = Panel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(PanelRaised), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Mint, modifier = Modifier.size(18.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(title, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(code, color = Mint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text(description, color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}
