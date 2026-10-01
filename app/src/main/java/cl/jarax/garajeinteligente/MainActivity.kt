package cl.jarax.garajeinteligente

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cl.jarax.garajeinteligente.ui.AccesoSeguroApp
import cl.jarax.garajeinteligente.ui.AccesoSeguroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val preferences = getSharedPreferences("acceso_seguro", MODE_PRIVATE)
        val onboardingComplete = preferences.getBoolean("onboarding_complete", false)
        setContent {
            AccesoSeguroTheme {
                AccesoSeguroApp(
                    initialOnboardingComplete = onboardingComplete,
                    onOnboardingComplete = {
                        preferences.edit().putBoolean("onboarding_complete", true).apply()
                    },
                    onRestartOnboarding = {
                        preferences.edit().putBoolean("onboarding_complete", false).apply()
                    },
                )
            }
        }
    }
}
