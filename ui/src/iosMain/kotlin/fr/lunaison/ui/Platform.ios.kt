package fr.lunaison.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual class PlatformContext

@Composable
actual fun rememberPlatformContext(): PlatformContext = remember { PlatformContext() }

actual fun openLink(context: PlatformContext, url: String) {
    val target = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(target, options = emptyMap<Any?, Any>(), completionHandler = null)
}

// À renseigner avec l'identifiant App Store une fois la fiche créée.
actual fun openStore(context: PlatformContext) = Unit

actual val storePageAvailable: Boolean = false

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
