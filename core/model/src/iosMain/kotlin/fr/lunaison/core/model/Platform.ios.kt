package fr.lunaison.core.model

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

/** `NSLocale.preferredLanguages` donne des balises comme `fr-FR` ou `zh-Hans-CN` : on garde la langue seule. */
internal actual fun systemLanguageCode(): String {
    val tag = NSLocale.preferredLanguages.firstOrNull() as? String ?: return "en"
    return tag.substringBefore('-').substringBefore('_').lowercase()
}
