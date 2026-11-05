package fr.lunaison.core.model

import java.time.LocalDate

/** Flux du jour. Les traces seules ne marquent pas le début des règles. */
enum class Flow(
    private val fr: String,
    private val en: String,
    private val pl: String,
    private val uk: String,
    private val es: String,
    private val de: String,
) {
    SPOTTING("Traces", "Spotting", "Plamienie", "Мажучі виділення", "Manchado", "Schmierblutung"),
    LIGHT("Léger", "Light", "Skąpe", "Легкі", "Ligero", "Leicht"),
    MEDIUM("Moyen", "Medium", "Umiarkowane", "Помірні", "Medio", "Mittel"),
    HEAVY("Abondant", "Heavy", "Obfite", "Рясні", "Abundante", "Stark"),
    ;

    val label: String get() = tr(fr, en, pl, uk, es, de)

    /** Un vrai saignement, par opposition à de simples traces. */
    val isBleeding: Boolean get() = this != SPOTTING
}

/** Douleur ressentie, décrite par son effet plutôt que par un chiffre. */
enum class Pain(
    private val fr: String,
    private val en: String,
    private val frDescription: String,
    private val enDescription: String,
    private val pl: String,
    private val uk: String,
    private val es: String,
    private val de: String,
    private val plDescription: String,
    private val ukDescription: String,
    private val esDescription: String,
    private val deDescription: String,
) {
    NONE(
        "Aucune", "None",
        "Pas de douleur", "No pain",
        "Brak", "Немає", "Ninguno", "Keine",
        "Brak bólu", "Без болю", "Sin dolor", "Keine Schmerzen",
    ),
    MILD(
        "Légère", "Mild",
        "Je la sens, sans que ça me gêne", "I can feel it, but it doesn't bother me",
        "Łagodny", "Легкий", "Leve", "Leicht",
        "Odczuwalny, ale nie przeszkadza", "Відчувається, але не заважає", "Lo noto, pero no me impide nada", "Spürbar, aber nicht störend",
    ),
    MODERATE(
        "Gênante", "Bothersome",
        "Elle me gêne, mais je fais ce que j'avais prévu", "It bothers me, but I still do what I had planned",
        "Dokuczliwy", "Помірний", "Molesto", "Mäßig",
        "Przeszkadza, ale robię swoje", "Заважає, але я роблю заплановане", "Molesta, pero sigo con mis planes", "Störend, aber Alltag möglich",
    ),
    SEVERE(
        "Forte", "Severe",
        "Elle m'empêche de faire mes activités", "It stops me from doing my usual activities",
        "Silny", "Сильний", "Fuerte", "Stark",
        "Uniemożliwia codzienne zajęcia", "Заважає звичайним справам", "Me impide hacer mis actividades", "Verhindert normale Aktivitäten",
    ),
    ;

    val label: String get() = tr(fr, en, pl, uk, es, de)
    val description: String get() = tr(frDescription, enDescription, plDescription, ukDescription, esDescription, deDescription)
}

enum class Symptom(
    private val fr: String,
    private val en: String,
    private val pl: String,
    private val uk: String,
    private val es: String,
    private val de: String,
) {
    HEADACHE("Maux de tête", "Headache", "Ból głowy", "Головний біль", "Dolor de cabeza", "Kopfschmerzen"),
    BACK_PAIN("Mal de dos", "Back pain", "Ból pleców", "Біль у спині", "Dolor de espalda", "Rückenschmerzen"),
    BLOATING("Ventre gonflé", "Bloating", "Wzdęcia", "Здуття живота", "Hinchazón", "Blähbauch"),
    BREAST_TENDERNESS("Seins sensibles", "Tender breasts", "Tkliwość piersi", "Чутливість грудей", "Sensibilidad mamaria", "Brustspannen"),
    DIGESTIVE("Troubles digestifs", "Digestive issues", "Problemy trawienne", "Травний дискомфорт", "Molestias digestivas", "Verdauungsbeschwerden"),
    NAUSEA("Nausées", "Nausea", "Nudności", "Нудота", "Náuseas", "Übelkeit"),
    FATIGUE("Fatigue", "Fatigue", "Zmęczenie", "Втома", "Fatiga", "Müdigkeit"),
    SLEEP("Sommeil perturbé", "Poor sleep", "Zaburzenia snu", "Порушений сон", "Sueño alterado", "Schlafstörungen"),
    CRAVINGS("Fringales", "Cravings", "Zachcianki", "Підвищений апетит", "Antojos", "Heißhunger"),
    ACNE("Boutons", "Acne", "Trądzik", "Висипання", "Acné", "Hautunreinheiten"),
    PELVIC_PAIN("Douleur pelvienne", "Pelvic pain", "Ból miednicy", "Тазовий біль", "Dolor pélvico", "Unterleibsschmerzen"),
    HOT_FLASHES("Bouffées de chaleur", "Hot flashes", "Uderzenia gorąca", "Припливи жару", "Sofocos", "Hitzewallungen"),
    NIGHT_SWEATS("Sueurs nocturnes", "Night sweats", "Nocne poty", "Нічна пітливість", "Sudores nocturnos", "Nachtschweiß"),
    BRAIN_FOG("Brouillard mental", "Brain fog", "Mgła mózgowa", "Туман у голові", "Niebla mental", "Konzentrationsprobleme"),
    ;

    val label: String get() = tr(fr, en, pl, uk, es, de)
}

/** Observation facultative de la glaire cervicale (suivi symptothermique / signes d'ovulation). */
enum class CervicalMucus(
    private val fr: String,
    private val en: String,
    private val pl: String,
    private val uk: String,
    private val es: String,
    private val de: String,
) {
    DRY("Sec ou absent", "Dry or none", "Suchy lub brak", "Сухо або немає", "Seco o ausente", "Trocken oder kein"),
    STICKY("Collant ou épais", "Sticky or thick", "Lepki lub gęsty", "Липкий або густий", "Pegajoso o espeso", "Klebrig oder dicklich"),
    CREAMY("Crémeux ou laiteux", "Creamy or lotion-like", "Kremowy lub mleczny", "Кремоподібний", "Cremoso o lechoso", "Cremig oder milchig"),
    EGG_WHITE("Fluide, transparent (très fertile)", "Clear, stretchy (very fertile)", "Przejrzysty, rozciągliwy (płodny)", "Прозорий, тягучий (плідний)", "Transparente y elástico (fértil)", "Klar, spinnbar (fruchtbar)"),
    ;

    val label: String get() = tr(fr, en, pl, uk, es, de)
}

/** Humeurs du jour, plusieurs possibles, notées à part des symptômes physiques. Libellés non genrés. */
enum class Mood(
    private val fr: String,
    private val en: String,
    val positive: Boolean,
    private val pl: String,
    private val uk: String,
    private val es: String,
    private val de: String,
) {
    CALM("Calme", "Calm", true, "Spokój", "Спокій", "Calma", "Ruhig"),
    HAPPY("De bonne humeur", "In a good mood", true, "Dobry nastrój", "Гарний настрій", "Buen humor", "Gute Laune"),
    ENERGETIC("Énergie haute", "High energy", true, "Dużo energii", "Висока енергія", "Mucha energía", "Viel Energie"),
    MOTIVATED("Motivation", "Motivated", true, "Motywacja", "Мотивація", "Motivación", "Motiviert"),
    SENSITIVE("À fleur de peau", "Sensitive", false, "Wrażliwość", "Чутливість", "Sensible", "Empfindlich"),
    IRRITABLE("Irritable", "Irritable", false, "Drażliwość", "Дратівливість", "Irritable", "Gereizt"),
    ANXIOUS("Anxiété", "Anxious", false, "Niepokój", "Тривожність", "Ansiedad", "Ängstlich"),
    STRESSED("Stress", "Stressed", false, "Stres", "Стрес", "Estrés", "Gestresst"),
    SAD("Triste", "Sad", false, "Smutek", "Смуток", "Tristeza", "Traurig"),
    LOW_ENERGY("Énergie basse", "Low energy", false, "Mało energii", "Низька енергія", "Poca energía", "Wenig Energie"),
    ;

    val label: String get() = tr(fr, en, pl, uk, es, de)
}

/** Ce que la personne a noté pour une journée. Tout est facultatif. */
data class DayLog(
    val date: LocalDate,
    val flow: Flow? = null,
    val pain: Pain? = null,
    val symptoms: Set<Symptom> = emptySet(),
    val moods: Set<Mood> = emptySet(),
    val note: String = "",
    /** Prise de contraception, traitement ou complément notée ce jour-là. */
    val medicationTaken: Boolean = false,
    /** Aspect de la glaire cervicale, si le suivi des signes d'ovulation est activé. */
    val mucus: CervicalMucus? = null,
    /** Température basale au réveil en °C (ex. 36.6f), si le suivi des signes d'ovulation est activé. */
    val basalTemperature: Float? = null,
) {
    val isEmpty: Boolean
        get() = flow == null && pain == null && symptoms.isEmpty() && moods.isEmpty() &&
            note.isBlank() && !medicationTaken && mucus == null && basalTemperature == null
}

/** Palette de couleurs de l'interface, toutes conformes au contraste WCAG AA. */
enum class ThemePalette(private val fr: String, private val en: String) {
    PLUM("Nuit de lune (Prune)", "Moonlight (Plum)"),
    SAGE("Forêt calme (Sauge)", "Calm forest (Sage)"),
    OCEAN("Marée (Océan)", "Tide (Ocean)"),
    TERRACOTTA("Terre chaude (Terracotta)", "Warm earth (Terracotta)"),
    ;

    val label: String get() = tr(fr, en)
}

/** Tranche d'âge déclarée : elle règle les repères de cycle et l'accès au partage. */
enum class AgeGroup {
    /** Moins de 18 ans : repères adaptés aux premières années de cycle, partage désactivé. */
    UNDER_18,
    ADULT,
}

/** Ce que la personne a indiqué au démarrage, avant d'avoir un historique. */
data class Baseline(
    val ageGroup: AgeGroup,
    val lastPeriodStart: LocalDate? = null,
    val usualCycleLength: Int? = null,
    val usualPeriodLength: Int? = null,
)
