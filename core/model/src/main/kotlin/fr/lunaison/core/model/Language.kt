package fr.lunaison.core.model

import java.util.Locale

/**
 * Langue des textes de l'appli : suit automatiquement la langue du téléphone ou le choix par application (Android 13+).
 * Langues prises en charge : français (fr), anglais (en), polonais (pl), ukrainien (uk), espagnol (es), allemand (de).
 */
enum class Language(val code: String) {
    FR("fr"),
    EN("en"),
    PL("pl"),
    UK("uk"),
    ES("es"),
    DE("de"),
    ;

    companion object {
        /** Lue à chaque appel : la langue suit le réglage du téléphone sans redémarrer l'appli. */
        fun current(): Language = when (Locale.getDefault().language) {
            "fr" -> FR
            "pl" -> PL
            "uk" -> UK
            "es" -> ES
            "de" -> DE
            else -> EN
        }
    }
}

/**
 * Le texte dans la langue de l'appli.
 * Les traductions en polonais, ukrainien, espagnol et allemand se replient sur l'anglais si elles ne sont pas précisées.
 */
fun tr(
    fr: String,
    en: String,
    pl: String = en,
    uk: String = en,
    es: String = en,
    de: String = en,
): String = when (Language.current()) {
    Language.FR -> fr
    Language.EN -> en
    Language.PL -> pl
    Language.UK -> uk
    Language.ES -> es
    Language.DE -> de
}
