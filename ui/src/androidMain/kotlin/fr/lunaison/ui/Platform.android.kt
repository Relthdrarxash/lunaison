package fr.lunaison.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual typealias PlatformContext = Context

@Composable
actual fun rememberPlatformContext(): PlatformContext = LocalContext.current

actual fun openLink(context: PlatformContext, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: ActivityNotFoundException) {
        // Aucun navigateur : rien à faire.
    }
}

private const val PACKAGE_NAME = "fr.lunaison"

actual fun openStore(context: PlatformContext) {
    val marketUri = Uri.parse("market://details?id=$PACKAGE_NAME")
    val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$PACKAGE_NAME")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, marketUri))
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        } catch (_: ActivityNotFoundException) {
            // Aucun magasin ni navigateur disponible
        }
    }
}

actual val storePageAvailable: Boolean = true

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}
