package fr.lunaison.ui

import androidx.compose.runtime.Composable
import fr.lunaison.core.cycle.HealthReport
import fr.lunaison.core.data.Partner
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.PartnerSnapshot

/*
 * Frontière avec le système : tout ce que les écrans communs demandent à Android ou iOS passe ici.
 * Les implémentations vivent dans androidMain et iosMain.
 */

/** Contexte du système (Android : `Context`). Les écrans le transmettent sans l'inspecter. */
expect abstract class PlatformContext

@Composable
expect fun rememberPlatformContext(): PlatformContext

/** Ouvre un lien dans le navigateur de la personne. L'appli elle-même n'a pas accès à Internet. */
expect fun openLink(context: PlatformContext, url: String)

/** Ouvre la fiche de l'application sur le magasin d'applications. */
expect fun openStore(context: PlatformContext)

/** Faux tant que l'appli n'a pas de fiche sur le magasin de la plateforme : on masque alors les invites à la noter. */
expect val storePageAvailable: Boolean

/** Geste ou bouton « retour » du système. Sur iOS, la navigation passe par les boutons de l'appli. */
@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)

/** Nom du système, pour les textes qui renvoient à ses réglages. */
expect val platformName: String

/** Message bref à l'écran (Android : Toast). */
expect fun showToast(context: PlatformContext, message: String)

/** Autorisation d'envoyer des notifications, gérée par le système. */
class NotificationAccess(
    /** Autorisé maintenant ; revérifié au retour dans l'appli. */
    val allowed: Boolean,
    /** Affiche la demande du système ; [rememberNotificationAccess] avertit ensuite via `onAnswered`. */
    val request: () -> Unit,
    /** Ouvre les réglages de l'appli dans le système. */
    val openSettings: () -> Unit,
)

@Composable
expect fun rememberNotificationAccess(onAnswered: (granted: Boolean) -> Unit = {}): NotificationAccess

/**
 * Enregistre un texte dans un fichier choisi par la personne (Android : sélecteur de document ; iOS : feuille de partage).
 * Le contenu n'est produit qu'une fois le fichier choisi.
 */
@Composable
expect fun rememberFileExporter(mimeType: String, content: suspend () -> String): (fileName: String) -> Unit

/** Sélecteur de contact du système (sans autorisation) ; `null` quand la plateforme n'en propose pas. */
@Composable
expect fun rememberContactPicker(onPicked: (name: String, phone: String) -> Unit): (() -> Unit)?

/** Prépare le message et ouvre la messagerie. Aucune autorisation : c'est la messagerie qui envoie. */
expect object ShareLauncher {
    fun send(context: PlatformContext, partner: Partner, text: String)
    fun shareAny(context: PlatformContext, text: String)
}

/** Le bilan de santé peut être exporté en PDF. */
expect val pdfReportAvailable: Boolean

/** Crée le PDF du bilan et ouvre le partage ; lève une exception en cas d'échec. */
expect suspend fun shareHealthReportPdf(
    context: PlatformContext,
    report: HealthReport,
    medicationLabel: String,
    logs: List<DayLog>,
    sections: Set<ReportSection>,
)

/** L'import depuis une autre appli de suivi est proposé (Android seulement). */
expect val importAvailable: Boolean

@Composable
expect fun ImportDialogs(showSource: Boolean, onDismiss: () -> Unit, actions: Actions, onResult: (String) -> Unit)

/** La sauvegarde iCloud de l'iPhone inclut les données ; la personne peut l'exclure (iOS seulement). */
expect val cloudBackupControllable: Boolean

expect fun isCloudBackupEnabled(): Boolean

expect fun setCloudBackupEnabled(enabled: Boolean)

/** Matrice de modules d'un QR Code (vrai = module sombre). */
class QrMatrix(val width: Int, val height: Int, private val modules: BooleanArray) {
    operator fun get(x: Int, y: Int): Boolean = modules[y * width + x]
}

/** Encode [content] en QR Code ; `null` si c'est impossible ou si la plateforme ne sait pas encore le faire. */
expect fun encodeQr(content: String): QrMatrix?

/** Lecture d'un code partenaire par la caméra, fournie par la plateforme. */
class ScannerState(
    val hasCamera: Boolean,
    val preview: @Composable () -> Unit,
)

@Composable
expect fun rememberScanner(onSnapshot: (PartnerSnapshot) -> Unit): ScannerState
