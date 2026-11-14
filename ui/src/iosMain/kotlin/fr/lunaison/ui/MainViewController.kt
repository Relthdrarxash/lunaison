package fr.lunaison.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.lunaison.core.data.LunaisonRepository
import fr.lunaison.core.data.create
import fr.lunaison.core.designsystem.LunaisonTheme
import platform.UIKit.UIViewController

private var requestedLink by mutableStateOf<String?>(null)

/** Point d'entrée appelé par la coque SwiftUI (`iosApp`) : toute l'appli est dans `:ui`. */
fun MainViewController(appVersion: String = "1.5.0"): UIViewController {
    val repository = LunaisonRepository.create()
    return ComposeUIViewController {
        val repo = remember { repository }
        val settings by repo.settings.collectAsStateWithLifecycle(initialValue = null)
        LunaisonTheme(palette = settings?.themePalette?.name ?: "PLUM") {
            LunaisonApp(
                repository = repo,
                appVersion = appVersion,
                link = requestedLink,
                onLinkHandled = { requestedLink = null },
            )
        }
    }
}

fun openLink(link: String) {
    requestedLink = link
}
