package fr.lunaison.ui

import androidx.compose.runtime.Composable

/*
 * Frontière avec le système : tout ce que les écrans communs demandent à Android ou iOS passe ici.
 * Les implémentations vivent dans androidMain et iosMain.
 */

/** Contexte du système (Android : `Context`). Les écrans le transmettent sans l'inspecter. */
expect class PlatformContext

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
