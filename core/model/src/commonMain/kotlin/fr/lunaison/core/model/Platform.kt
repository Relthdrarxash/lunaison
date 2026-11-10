package fr.lunaison.core.model

/**
 * Code de langue de l'appareil sur deux lettres minuscules (par exemple `fr`), lu à chaque appel :
 * la langue suit le réglage du téléphone sans redémarrer l'appli.
 */
internal expect fun systemLanguageCode(): String
