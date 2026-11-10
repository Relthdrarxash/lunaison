package fr.lunaison.ui

import fr.lunaison.core.model.Language
import fr.lunaison.core.model.tr
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Textes communs à l'écran du bilan soignant et à son PDF, dans les six langues de l'appli.
 * Registre sobre d'un document remis à un médecin, une gynécologue ou une sage-femme ; les termes
 * reprennent ceux de `Symptom`, `Pain`, `IntimacyKind`, `PillStatus`, `ReportSection` et des fiches.
 *
 * Pluriels : le polonais et l'ukrainien accordent selon le nombre. Les durées passent toujours par
 * [Format.days] (« dni », « дн. » invariables) et les autres comptes sont écrits « libellé : nombre »
 * plutôt qu'avec un nom accordé.
 */
internal object ReportText {

    /** Un nombre décimal à une décimale, avec la virgule ou le point de la langue courante. */
    fun decimal(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return value.toString()
        val tenths = (value * 10).roundToLong()
        val magnitude = abs(tenths)
        val separator = if (Language.current() == Language.EN) "." else ","
        return (if (tenths < 0) "-" else "") + "${magnitude / 10}$separator${magnitude % 10}"
    }

    /** Met un libellé en minuscules dans une phrase ; l'allemand garde la majuscule de ses noms. */
    fun inline(label: String): String = if (Language.current() == Language.DE) label else label.lowercase()

    fun subtitle(): String = tr(
        fr = "Synthèse pour une consultation médicale",
        en = "Summary for a medical consultation",
        pl = "Podsumowanie na wizytę lekarską",
        uk = "Підсумок для медичної консультації",
        es = "Resumen para una consulta médica",
        de = "Zusammenfassung für ein ärztliches Gespräch",
    )

    fun last90Days(): String = tr(
        fr = "90 derniers jours",
        en = "last 90 days",
        pl = "ostatnie 90 dni",
        uk = "останні 90 днів",
        es = "últimos 90 días",
        de = "letzte 90 Tage",
    )

    fun ninetyDays(): String = tr(
        fr = "90 jours",
        en = "90 days",
        pl = "90 dni",
        uk = "90 днів",
        es = "90 días",
        de = "90 Tage",
    )

    fun lastPeriodStart(): String = tr(
        fr = "Début des dernières règles",
        en = "Last period start",
        pl = "Początek ostatniej miesiączki",
        uk = "Початок останньої менструації",
        es = "Inicio de la última regla",
        de = "Beginn der letzten Periode",
    )

    fun spotting(): String = tr(
        fr = "Saignements hors règles",
        en = "Spotting outside periods",
        pl = "Plamienia poza miesiączką",
        uk = "Мажучі виділення поза менструацією",
        es = "Manchado fuera de la regla",
        de = "Schmierblutungen außerhalb der Periode",
    )

    fun overviewTitle(): String = tr(
        fr = "Synthèse des derniers cycles (repères FIGO)",
        en = "Recent cycles summary (FIGO references)",
        pl = "Podsumowanie ostatnich cykli (wartości referencyjne FIGO)",
        uk = "Підсумок останніх циклів (орієнтири FIGO)",
        es = "Resumen de los últimos ciclos (valores de referencia FIGO)",
        de = "Zusammenfassung der letzten Zyklen (FIGO-Referenzwerte)",
    )

    fun referencesWithin(): String = tr(
        fr = "Repères médicaux (FIGO / ACOG) : durées et régularité dans les valeurs habituelles.",
        en = "Medical references (FIGO / ACOG): lengths and regularity within usual ranges.",
        pl = "Wartości referencyjne (FIGO / ACOG): długość i regularność cykli mieszczą się w typowych zakresach.",
        uk = "Орієнтири (FIGO / ACOG): тривалість і регулярність циклів у межах звичайних значень.",
        es = "Valores de referencia (FIGO / ACOG): duración y regularidad dentro de los intervalos habituales.",
        de = "Referenzwerte (FIGO / ACOG): Längen und Regelmäßigkeit liegen im üblichen Bereich.",
    )

    fun referencesOutside(): String = tr(
        fr = "Repères médicaux (FIGO / ACOG) : au moins un cycle sort des repères habituels.",
        en = "Medical references (FIGO / ACOG): at least one cycle is outside usual ranges.",
        pl = "Wartości referencyjne (FIGO / ACOG): co najmniej jeden cykl wykracza poza typowe zakresy.",
        uk = "Орієнтири (FIGO / ACOG): щонайменше один цикл виходить за межі звичайних значень.",
        es = "Valores de referencia (FIGO / ACOG): al menos un ciclo queda fuera de los intervalos habituales.",
        de = "Referenzwerte (FIGO / ACOG): mindestens ein Zyklus liegt außerhalb des üblichen Bereichs.",
    )

    fun cyclesHistoryTitle(): String = tr(
        fr = "Historique des cycles complets",
        en = "Completed cycles history",
        pl = "Historia pełnych cykli",
        uk = "Історія повних циклів",
        es = "Historial de ciclos completos",
        de = "Verlauf der vollständigen Zyklen",
    )

    fun cycleLength(): String = tr(
        fr = "Durée du cycle",
        en = "Cycle length",
        pl = "Długość cyklu",
        uk = "Тривалість циклу",
        es = "Duración del ciclo",
        de = "Zykluslänge",
    )

    fun periodLength(): String = tr(
        fr = "Durée des règles",
        en = "Period length",
        pl = "Czas trwania miesiączki",
        uk = "Тривалість менструації",
        es = "Duración de la regla",
        de = "Blutungsdauer",
    )

    fun painBothersomeOrSevere(): String = tr(
        fr = "Douleur gênante ou forte",
        en = "Bothersome or severe pain",
        pl = "Ból dokuczliwy lub silny",
        uk = "Помірний або сильний біль",
        es = "Dolor molesto o fuerte",
        de = "Mäßige oder starke Schmerzen",
    )

    fun painCramps(): String = tr(
        fr = "Crampes abdominales",
        en = "Abdominal cramps",
        pl = "Skurcze brzucha",
        uk = "Спазми в животі",
        es = "Cólicos abdominales",
        de = "Bauchkrämpfe",
    )

    fun painPelvic(): String = tr(
        fr = "Douleurs pelviennes (bas-ventre)",
        en = "Pelvic pain",
        pl = "Ból miednicy (podbrzusza)",
        uk = "Тазовий біль (низ живота)",
        es = "Dolor pélvico (bajo vientre)",
        de = "Unterleibsschmerzen",
    )

    fun painDyspareunia(): String = tr(
        fr = "Douleurs aux rapports (dyspareunie)",
        en = "Pain during sex (dyspareunia)",
        pl = "Ból przy stosunku (dyspareunia)",
        uk = "Біль під час статевого акту (диспареунія)",
        es = "Dolor en las relaciones (dispareunia)",
        de = "Schmerzen beim Geschlechtsverkehr (Dyspareunie)",
    )

    fun painDigestive(): String = tr(
        fr = "Douleurs au transit ou à la défécation",
        en = "Pain with bowel movements",
        pl = "Ból przy wypróżnianiu",
        uk = "Біль під час дефекації",
        es = "Dolor al defecar",
        de = "Schmerzen beim Stuhlgang",
    )

    fun painUrinary(): String = tr(
        fr = "Douleurs en urinant (dysurie)",
        en = "Pain when urinating",
        pl = "Ból przy mikcji (dyzuria)",
        uk = "Біль при сечовипусканні (дизурія)",
        es = "Dolor al orinar (disuria)",
        de = "Schmerzen beim Wasserlassen (Dysurie)",
    )

    fun painRadiating(): String = tr(
        fr = "Douleurs irradiantes (dos, lombaires, jambes)",
        en = "Radiating pain (back, legs)",
        pl = "Ból promieniujący (plecy, lędźwie, nogi)",
        uk = "Біль, що віддає (спина, поперек, ноги)",
        es = "Dolor irradiado (espalda, zona lumbar, piernas)",
        de = "Ausstrahlende Schmerzen (Rücken, Lendenbereich, Beine)",
    )

    fun painGuidelines(): String = tr(
        fr = "Repères utiles pour le dialogue soignant selon les recommandations HAS / ESHRE.",
        en = "Helpful indicators for clinician dialogue per HAS / ESHRE guidelines.",
        pl = "Wskaźniki pomocne w rozmowie z lekarzem, zgodne z zaleceniami HAS / ESHRE.",
        uk = "Орієнтири, корисні для розмови з лікарем, за рекомендаціями HAS / ESHRE.",
        es = "Indicadores útiles para la conversación con el profesional sanitario, según las recomendaciones HAS / ESHRE.",
        de = "Hilfreiche Anhaltspunkte für das ärztliche Gespräch gemäß den Empfehlungen von HAS / ESHRE.",
    )

    fun symptomsTitle(): String = tr(
        fr = "Symptômes et humeurs récurrents par phase",
        en = "Recurring symptoms and moods by phase",
        pl = "Powtarzające się objawy i nastroje według faz",
        uk = "Симптоми та настрої, що повторюються, за фазами",
        es = "Síntomas y estados de ánimo recurrentes por fase",
        de = "Wiederkehrende Symptome und Stimmungen je Phase",
    )

    fun defaultMedicationLabel(): String = tr(
        fr = "Pilule / Contraception",
        en = "Pill / Contraception",
        pl = "Tabletka / Antykoncepcja",
        uk = "Таблетка / Контрацепція",
        es = "Píldora / Anticoncepción",
        de = "Pille / Verhütung",
    )

    /** « 3 prises notées » : pour le polonais et l'ukrainien, « libellé : nombre » évite l'accord. */
    fun pillTaken(count: Int): String = tr(
        fr = if (count == 1) "1 prise notée" else "$count prises notées",
        en = if (count == 1) "1 dose logged" else "$count doses logged",
        pl = "zapisane przyjęcia: $count",
        uk = "відмічені прийоми: $count",
        es = if (count == 1) "1 toma registrada" else "$count tomas registradas",
        de = if (count == 1) "1 Einnahme erfasst" else "$count Einnahmen erfasst",
    )

    fun pillMissed(count: Int): String = tr(
        fr = if (count == 1) "1 oubli noté" else "$count oublis notés",
        en = if (count == 1) "1 missed dose logged" else "$count missed doses logged",
        pl = "zapisane pominięcia: $count",
        uk = "відмічені пропуски: $count",
        es = if (count == 1) "1 olvido registrado" else "$count olvidos registrados",
        de = if (count == 1) "1 vergessene Einnahme erfasst" else "$count vergessene Einnahmen erfasst",
    )

    /** « noté 3 jours » : durée déjà formatée par [Format.days]. */
    fun loggedDays(count: Int): String = tr(
        fr = "noté ${Format.days(count)}",
        en = "logged ${Format.days(count)}",
        pl = "zapisano ${Format.days(count)}",
        uk = "відмічено ${Format.days(count)}",
        es = "${Format.days(count)} con registro",
        de = "${Format.days(count)} erfasst",
    )

    fun kg(): String = tr(fr = "kg", en = "kg", pl = "kg", uk = "кг", es = "kg", de = "kg")

    fun lastWeight(): String = tr(
        fr = "Dernier poids noté",
        en = "Last weight logged",
        pl = "Ostatnia zapisana waga",
        uk = "Остання записана вага",
        es = "Último peso registrado",
        de = "Zuletzt erfasstes Gewicht",
    )

    fun weightRange(): String = tr(
        fr = "Plage sur 90 jours",
        en = "90-day range",
        pl = "Zakres z 90 dni",
        uk = "Діапазон за 90 днів",
        es = "Rango en 90 días",
        de = "Spanne über 90 Tage",
    )

    /** « 60,0 à 62,0 kg (3 pesées) ». Les mesures sont toujours au moins deux ici. */
    fun weightRangeValue(min: Double, max: Double, entries: Int): String = tr(
        fr = "${decimal(min)} à ${decimal(max)} kg ($entries pesées)",
        en = "${decimal(min)} to ${decimal(max)} kg ($entries entries)",
        pl = "${decimal(min)} do ${decimal(max)} kg (liczba pomiarów: $entries)",
        uk = "від ${decimal(min)} до ${decimal(max)} кг (кількість вимірювань: $entries)",
        es = "de ${decimal(min)} a ${decimal(max)} kg ($entries registros)",
        de = "${decimal(min)} bis ${decimal(max)} kg ($entries Einträge)",
    )

    fun mucusObservations(): String = tr(
        fr = "Observations de glaire",
        en = "Mucus observations",
        pl = "Obserwacje śluzu szyjkowego",
        uk = "Спостереження цервікального слизу",
        es = "Observaciones de moco cervical",
        de = "Beobachtungen des Zervixschleims",
    )

    fun basalTemperature(): String = tr(
        fr = "Température basale notée",
        en = "Basal temperature logged",
        pl = "Zapisana temperatura podstawowa",
        uk = "Записана базальна температура",
        es = "Temperatura basal registrada",
        de = "Erfasste Basaltemperatur",
    )

    fun basalTemperatureValue(days: Int, average: Double): String = tr(
        fr = "${Format.days(days)} (moyenne : ${decimal(average)} °C)",
        en = "${Format.days(days)} (avg: ${decimal(average)} °C)",
        pl = "${Format.days(days)} (średnia: ${decimal(average)} °C)",
        uk = "${Format.days(days)} (середня: ${decimal(average)} °C)",
        es = "${Format.days(days)} (media: ${decimal(average)} °C)",
        de = "${Format.days(days)} (Durchschnitt: ${decimal(average)} °C)",
    )

    fun intimacyProtected(): String = tr(
        fr = "Rapports protégés",
        en = "Protected intercourse",
        pl = "Stosunki zabezpieczone",
        uk = "Захищений секс",
        es = "Relaciones protegidas",
        de = "Geschützter Verkehr",
    )

    fun intimacyUnprotected(): String = tr(
        fr = "Rapports non protégés",
        en = "Unprotected intercourse",
        pl = "Stosunki niezabezpieczone",
        uk = "Незахищений секс",
        es = "Relaciones sin protección",
        de = "Ungeschützter Verkehr",
    )

    fun intimacyNonPenetrative(): String = tr(
        fr = "Sans pénétration ou solo",
        en = "Non-penetrative or solo",
        pl = "Bez penetracji lub solo",
        uk = "Без проникнення або соло",
        es = "Sin penetración o a solas",
        de = "Ohne Penetration oder solo",
    )

    fun alertsTitle(): String = tr(
        fr = "Points d'attention relevés",
        en = "Points of attention noted",
        pl = "Odnotowane ważne uwagi",
        uk = "Відмічені важливі моменти",
        es = "Puntos de atención detectados",
        de = "Festgehaltene wichtige Hinweise",
    )

    /** Ligne « libellé : [valeur] », avec l'espace avant les deux-points propre au français. */
    fun labelValue(label: String, value: String): String =
        if (Language.current() == Language.FR) "$label : $value" else "$label: $value"
}
