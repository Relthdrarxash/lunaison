package fr.lunaison.core.cycle

import fr.lunaison.core.model.AgeGroup
import fr.lunaison.core.model.tr

/**
 * Signaux qui méritent d'en parler à un professionnel de santé. Jamais un diagnostic :
 * l'appli dit ce qu'elle a remarqué et pourquoi ça vaut un avis.
 */
enum class AlertKind(val articleId: String) {
    SEVERE_PAIN("consulter"),
    LONG_PERIOD("consulter"),
    HEAVY_FLOW("consulter"),
    CYCLE_LENGTH("consulter"),
    IRREGULAR("consulter"),
    NO_PERIOD("consulter"),
    SPOTTING("consulter"),
    ;

    val title: String get() = when (this) {
        SEVERE_PAIN -> tr(
            fr = "Des douleurs fortes à plusieurs reprises",
            en = "Severe pain more than once",
            pl = "Silny ból, który się powtarza",
            uk = "Сильний біль, що повторюється",
            es = "Dolores fuertes en varias ocasiones",
            de = "Wiederholt starke Schmerzen",
        )
        LONG_PERIOD -> tr(
            fr = "Des règles de plus de 8 jours",
            en = "Periods lasting more than 8 days",
            pl = "Miesiączki trwające dłużej niż 8 dni",
            uk = "Менструації тривалістю понад 8 днів",
            es = "Reglas de más de 8 días",
            de = "Perioden von mehr als 8 Tagen",
        )
        HEAVY_FLOW -> tr(
            fr = "Des règles abondantes à plusieurs reprises",
            en = "Heavy periods more than once",
            pl = "Obfite miesiączki, które się powtarzają",
            uk = "Рясні менструації, що повторюються",
            es = "Reglas abundantes en varias ocasiones",
            de = "Wiederholt starke Perioden",
        )
        CYCLE_LENGTH -> tr(
            fr = "Des cycles hors des repères habituels",
            en = "Cycles outside the usual range",
            pl = "Cykle poza typowym zakresem",
            uk = "Цикли поза звичайними межами",
            es = "Ciclos fuera de los intervalos habituales",
            de = "Zyklen außerhalb des üblichen Bereichs",
        )
        IRREGULAR -> tr(
            fr = "Des cycles très variables",
            en = "Very variable cycles",
            pl = "Bardzo zmienne cykle",
            uk = "Дуже нестабільні цикли",
            es = "Ciclos muy variables",
            de = "Stark schwankende Zyklen",
        )
        NO_PERIOD -> tr(
            fr = "Pas de règles depuis plus de trois mois",
            en = "No period for more than three months",
            pl = "Brak miesiączki od ponad trzech miesięcy",
            uk = "Немає менструації понад три місяці",
            es = "Sin regla desde hace más de tres meses",
            de = "Seit mehr als drei Monaten keine Periode",
        )
        SPOTTING -> tr(
            fr = "Des saignements entre les règles",
            en = "Bleeding between periods",
            pl = "Krwawienia między miesiączkami",
            uk = "Кровотечі між менструаціями",
            es = "Sangrados entre reglas",
            de = "Blutungen zwischen den Perioden",
        )
    }

    val message: String get() = when (this) {
        SEVERE_PAIN -> tr(
            fr = "Vous avez noté des douleurs qui vous empêchent de faire vos activités pendant plusieurs règles. " +
                "Ce n'est pas quelque chose qu'il faut supporter : un médecin, une sage-femme ou un gynécologue peut en chercher la cause, " +
                "par exemple une endométriose, et vous proposer un traitement.",
            en = "You have logged pain that stops you from doing your activities during several periods. " +
                "This is not something you have to put up with: a doctor, a midwife or a gynecologist can look for the cause, " +
                "such as endometriosis, and offer you treatment.",
            pl = "W Twoich zapisach pojawia się ból, który uniemożliwia codzienne aktywności podczas kilku miesiączek. " +
                "Nie trzeba go znosić: lekarz, położna lub ginekolog może poszukać przyczyny, " +
                "na przykład endometriozy, i zaproponować leczenie.",
            uk = "Ви відзначили біль, який заважає виконувати звичні справи, під час кількох менструацій. " +
                "Терпіти його не потрібно: лікар, акушерка або гінеколог може пошукати причину, " +
                "наприклад ендометріоз, і запропонувати лікування.",
            es = "Has anotado dolores que te impiden realizar tus actividades durante varias reglas. " +
                "No es algo que haya que aguantar: un médico, una matrona o un ginecólogo puede buscar la causa, " +
                "por ejemplo una endometriosis, y proponerte un tratamiento.",
            de = "Du hast Schmerzen eingetragen, die dich bei mehreren Perioden im Alltag einschränken. " +
                "Das musst du nicht aushalten: Eine Ärztin, ein Arzt, eine Hebamme oder ein Gynäkologe kann nach der Ursache suchen, " +
                "zum Beispiel einer Endometriose, und dir eine Behandlung anbieten.",
        )
        LONG_PERIOD -> tr(
            fr = "Des règles qui durent plus de 8 jours sortent des repères habituels. Ça vaut la peine d'en parler à un professionnel de santé.",
            en = "Periods that last more than 8 days are outside the usual range. It is worth talking to a health professional about it.",
            pl = "Miesiączki trwające dłużej niż 8 dni wykraczają poza typowy zakres. Warto porozmawiać o tym z osobą z personelu medycznego.",
            uk = "Менструації, що тривають понад 8 днів, виходять за межі звичайних значень. Варто поговорити про це з медичним працівником.",
            es = "Unas reglas que duran más de 8 días se salen de los intervalos habituales. Vale la pena hablarlo con un profesional sanitario.",
            de = "Perioden, die länger als 8 Tage dauern, liegen außerhalb des üblichen Bereichs. Es lohnt sich, mit medizinischem Fachpersonal darüber zu sprechen.",
        )
        HEAVY_FLOW -> tr(
            fr = "Des règles abondantes répétées peuvent fatiguer et faire baisser le fer. Si vous devez changer de protection toutes les 1 à 2 heures, " +
                "ou si vous perdez des caillots de plus de 2,5 cm environ, parlez-en à un professionnel de santé.",
            en = "Repeated heavy periods can make you tired and lower your iron levels. If you need to change your pad or tampon every 1 to 2 hours, " +
                "or if you pass clots larger than about 2.5 cm (1 inch), talk to a health professional.",
            pl = "Powtarzające się obfite miesiączki mogą powodować zmęczenie i obniżać poziom żelaza. Jeśli musisz zmieniać podpaskę lub tampon co 1 do 2 godzin " +
                "albo oddajesz skrzepy większe niż około 2,5 cm, porozmawiaj o tym z osobą z personelu medycznego.",
            uk = "Повторювані рясні менструації можуть викликати втому та знижувати рівень заліза. Якщо вам доводиться міняти прокладку чи тампон кожні 1-2 години " +
                "або якщо виходять згустки розміром понад 2,5 см приблизно, поговоріть про це з медичним працівником.",
            es = "Unas reglas abundantes repetidas pueden causar cansancio y bajar el hierro. Si tienes que cambiar de compresa o tampón cada 1 o 2 horas, " +
                "o si expulsas coágulos de más de 2,5 cm aproximadamente, habla con un profesional sanitario.",
            de = "Wiederholt starke Perioden können müde machen und den Eisenspiegel senken. Wenn du Binde oder Tampon alle 1 bis 2 Stunden wechseln musst " +
                "oder Blutgerinnsel von mehr als etwa 2,5 cm abgehen, sprich mit medizinischem Fachpersonal darüber.",
        )
        CYCLE_LENGTH -> tr(
            fr = "Vos deux derniers cycles étaient plus courts ou plus longs que la plupart des cycles. Ce n'est pas forcément inquiétant, " +
                "mais si ça continue, un avis médical permet d'en comprendre la raison.",
            en = "Your last two cycles were shorter or longer than most cycles. This is not necessarily a concern, " +
                "but if it continues, a medical opinion can help understand why.",
            pl = "Twoje dwa ostatnie cykle były krótsze lub dłuższe niż większość cykli. Niekoniecznie jest to powód do niepokoju, " +
                "ale jeśli się to utrzymuje, porada lekarska pomoże zrozumieć przyczynę.",
            uk = "Ваші два останні цикли були коротшими або довшими, ніж більшість циклів. Це не обов'язково привід для тривоги, " +
                "але якщо так триває й далі, консультація лікаря допоможе зрозуміти причину.",
            es = "Tus dos últimos ciclos fueron más cortos o más largos que la mayoría de los ciclos. No tiene por qué ser preocupante, " +
                "pero si continúa, una opinión médica ayuda a entender el motivo.",
            de = "Deine letzten beiden Zyklen waren kürzer oder länger als die meisten Zyklen. Das muss nicht beunruhigend sein, " +
                "aber wenn es anhält, kann eine ärztliche Einschätzung helfen, den Grund zu verstehen.",
        )
        IRREGULAR -> tr(
            fr = "L'écart entre votre cycle le plus court et le plus long dépasse 9 jours. Le stress, un voyage ou une maladie peuvent l'expliquer ; " +
                "si ça dure, parlez-en à un professionnel de santé.",
            en = "The difference between your shortest and longest cycle is more than 9 days. Stress, travel or illness can explain it; " +
                "if it lasts, talk to a health professional.",
            pl = "Różnica między Twoim najkrótszym a najdłuższym cyklem przekracza 9 dni. Może ją tłumaczyć stres, podróż lub choroba; " +
                "jeśli się to utrzymuje, porozmawiaj o tym z osobą z personelu medycznego.",
            uk = "Різниця між вашим найкоротшим і найдовшим циклом перевищує 9 днів. Це можуть пояснювати стрес, подорож або хвороба; " +
                "якщо так триває, поговоріть про це з медичним працівником.",
            es = "La diferencia entre tu ciclo más corto y el más largo supera los 9 días. El estrés, un viaje o una enfermedad pueden explicarlo; " +
                "si se mantiene, habla con un profesional sanitario.",
            de = "Der Unterschied zwischen deinem kürzesten und deinem längsten Zyklus beträgt mehr als 9 Tage. Stress, eine Reise oder eine Krankheit können das erklären; " +
                "wenn es anhält, sprich mit medizinischem Fachpersonal darüber.",
        )
        NO_PERIOD -> tr(
            fr = "Aucune règle n'est notée depuis plus de 90 jours. Si vous les avez simplement oubliées, ajoutez-les dans le calendrier. " +
                "Sinon, un avis médical est conseillé.",
            en = "No period has been logged for more than 90 days. If you simply forgot to log them, add them in the calendar. " +
                "Otherwise, seeing a doctor is advised.",
            pl = "Od ponad 90 dni nie zapisano żadnej miesiączki. Jeśli po prostu nie zostały jeszcze wpisane, uzupełnij je w kalendarzu. " +
                "W przeciwnym razie zalecana jest porada lekarska.",
            uk = "Понад 90 днів не записано жодної менструації. Якщо ви просто забули їх занотувати, додайте їх у календарі. " +
                "В іншому разі рекомендовано звернутися до лікаря.",
            es = "No hay ninguna regla anotada desde hace más de 90 días. Si simplemente las olvidaste, añádelas en el calendario. " +
                "Si no es así, se aconseja una opinión médica.",
            de = "Seit mehr als 90 Tagen ist keine Periode eingetragen. Falls du sie einfach vergessen hast, trage sie im Kalender nach. " +
                "Andernfalls wird eine ärztliche Abklärung empfohlen.",
        )
        SPOTTING -> tr(
            fr = "Vous avez noté plusieurs fois des traces de sang en dehors des règles. C'est souvent bénin, mais ça mérite d'en parler " +
                "à un professionnel de santé, surtout si ça se répète.",
            en = "You have logged spotting outside your periods several times. It is often harmless, but it is worth talking " +
                "to a health professional, especially if it keeps happening.",
            pl = "W Twoich zapisach kilka razy pojawiło się plamienie poza miesiączką. Najczęściej jest nieszkodliwe, ale warto porozmawiać o nim " +
                "z osobą z personelu medycznego, zwłaszcza jeśli się powtarza.",
            uk = "Ви кілька разів відзначили кров'янисті виділення поза менструацією. Найчастіше це нешкідливо, але варто поговорити про це " +
                "з медичним працівником, особливо якщо це повторюється.",
            es = "Has anotado varias veces manchas de sangre fuera de las reglas. A menudo es benigno, pero conviene hablarlo " +
                "con un profesional sanitario, sobre todo si se repite.",
            de = "Du hast mehrmals Schmierblutungen außerhalb der Periode eingetragen. Sie sind oft harmlos, aber es lohnt sich, mit medizinischem Fachpersonal " +
                "darüber zu sprechen, vor allem wenn sie sich wiederholen.",
        )
    }
}

object CycleAlerts {

    fun evaluate(state: CycleState.Known, ageGroup: AgeGroup): List<AlertKind> {
        val alerts = mutableListOf<AlertKind>()
        val recentPeriods = state.periods.takeLast(3)

        if (recentPeriods.count { it.hasSeverePain } >= 2) alerts += AlertKind.SEVERE_PAIN
        if (recentPeriods.any { it.length > CycleConfig.MAX_NORMAL_PERIOD }) alerts += AlertKind.LONG_PERIOD
        if (recentPeriods.count { it.heavyDays >= 3 } >= 2) alerts += AlertKind.HEAVY_FLOW

        val plausible = state.cycles.filter { it.isPlausible }
        val range = CycleConfig.normalCycleRange(ageGroup)
        if (plausible.size >= 2 && plausible.takeLast(2).all { it.length !in range }) alerts += AlertKind.CYCLE_LENGTH

        // À l'adolescence, des cycles irréguliers sont habituels les premières années (ACOG, 2015).
        val lastSix = plausible.takeLast(CycleConfig.CYCLES_FOR_PREDICTION).map { it.length }
        if (ageGroup == AgeGroup.ADULT && lastSix.size >= 4 && lastSix.max() - lastSix.min() > CycleConfig.MAX_NORMAL_VARIATION) {
            alerts += AlertKind.IRREGULAR
        }

        if (state.dayOfCycle - 1 > CycleConfig.LONG_ABSENCE_DAYS) alerts += AlertKind.NO_PERIOD

        // Traces isolées depuis le début de l'avant-dernier cycle.
        val since = state.periods.takeLast(3).firstOrNull()?.start
        if (since != null && state.spotting.count { !it.isBefore(since) } >= 2) alerts += AlertKind.SPOTTING

        return alerts
    }
}
