package fr.lunaison

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.lunaison.core.designsystem.LunaisonTheme
import fr.lunaison.ui.LunaisonApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as LunaisonApplication).repository
        setContent {
            val settings by repository.settings.collectAsStateWithLifecycle(initialValue = null)
            val hasPin = settings?.pinHash != null
            LaunchedEffect(hasPin) {
                if (hasPin) {
                    window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
            LunaisonTheme(palette = settings?.themePalette?.name ?: "PLUM") {
                LunaisonApp(repository, BuildConfig.VERSION_NAME)
            }
        }
    }
}
