package fr.lunaison

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.lunaison.core.designsystem.LunaisonTheme
import fr.lunaison.ui.LunaisonApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as LunaisonApplication).repository
        setContent {
            LunaisonTheme {
                LunaisonApp(repository)
            }
        }
    }
}
