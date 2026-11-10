package fr.lunaison.ui

import fr.lunaison.core.model.Language
import java.util.Locale

/** Locale des dates et des nombres du bilan PDF : celle de la langue de l'appli, comme [Format]. */
internal fun ReportText.locale(): Locale = Locale.forLanguageTag(Language.current().code)
