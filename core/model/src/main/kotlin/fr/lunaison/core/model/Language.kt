package fr.lunaison.core.model

import java.util.Locale

/**
 * Langue des textes de l'appli : le français pour un téléphone en français, l'anglais sinon.
 * Chaque texte est écrit dans les deux langues côte à côte, pour qu'aucune traduction ne puisse manquer.
 */
enum class Language {
    FR,
    EN,
    ;

    companion object {
        /** Lue à chaque appel : la langue suit le réglage du téléphone sans redémarrer l'appli. */
        fun current(): Language = if (Locale.getDefault().language == "fr") FR else EN
    }
}

/** Le texte dans la langue de l'appli. */
fun tr(fr: String, en: String): String = if (Language.current() == Language.FR) fr else en
