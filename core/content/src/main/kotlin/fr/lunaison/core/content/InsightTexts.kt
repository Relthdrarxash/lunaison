package fr.lunaison.core.content

import fr.lunaison.core.cycle.Anticipation
import fr.lunaison.core.cycle.CycleConfig
import fr.lunaison.core.cycle.CycleOverview
import fr.lunaison.core.cycle.CycleRecap
import fr.lunaison.core.cycle.Onset
import fr.lunaison.core.cycle.Phase
import fr.lunaison.core.model.Language
import fr.lunaison.core.model.tr
import kotlin.math.abs

/**
 * Phrases des statistiques personnelles, dans les six langues de l'appli. Elles décrivent ce qui a été noté,
 * en comptes simples, sans jugement ni diagnostic. Regroupées ici pour être testées avec les autres textes.
 */
object InsightTexts {

    /** Même règle que `Format.days` de l'écran : « dni » et « дн. » invariables, donc jamais d'accord faux. */
    fun days(count: Int): String {
        val n = abs(count)
        return if (n <= 1) {
            tr(
                fr = "$n jour",
                en = "$n day",
                pl = "$n dzień",
                uk = "$n день",
                es = "$n día",
                de = "$n Tag",
            )
        } else {
            tr(
                fr = "$n jours",
                en = "$n days",
                pl = "$n dni",
                uk = "$n дн.",
                es = "$n días",
                de = "$n Tage",
            )
        }
    }

    /** Un libellé dans une phrase : minuscules, sauf en allemand où les noms gardent leur majuscule. */
    private fun inline(label: String): String = if (Language.current() == Language.DE) label else label.lowercase()

    fun phaseName(phase: Phase): String = when (phase) {
        Phase.MENSTRUAL -> tr(
            fr = "les règles",
            en = "your period",
            pl = "miesiączka",
            uk = "менструація",
            es = "la regla",
            de = "die Periode",
        )
        Phase.FOLLICULAR -> tr(
            fr = "la phase folliculaire",
            en = "the follicular phase",
            pl = "faza folikularna",
            uk = "фолікулярна фаза",
            es = "la fase folicular",
            de = "die Follikelphase",
        )
        Phase.OVULATION -> tr(
            fr = "la période autour de l'ovulation",
            en = "the time around ovulation",
            pl = "okres wokół owulacji",
            uk = "період навколо овуляції",
            es = "el periodo en torno a la ovulación",
            de = "die Zeit um den Eisprung",
        )
        Phase.LUTEAL -> tr(
            fr = "la phase lutéale",
            en = "the luteal phase",
            pl = "faza lutealna",
            uk = "лютеїнова фаза",
            es = "la fase lútea",
            de = "die Lutealphase",
        )
        Phase.LATE -> tr(
            fr = "le retard des règles",
            en = "the late period",
            pl = "opóźnienie miesiączki",
            uk = "затримка менструації",
            es = "el retraso de la regla",
            de = "die verspätete Periode",
        )
    }

    fun recapLines(recap: CycleRecap): List<String> = buildList {
        val length = recap.record.length
        val difference = recap.difference
        add(
            when {
                difference == null -> tr(
                    fr = "Il a duré ${days(length)}.",
                    en = "It lasted ${days(length)}.",
                    pl = "Trwał ${days(length)}.",
                    uk = "Він тривав ${days(length)}.",
                    es = "Duró ${days(length)}.",
                    de = "Er dauerte ${days(length)}.",
                )
                abs(difference) <= CycleConfig.USUAL_TOLERANCE -> tr(
                    fr = "Il a duré ${days(length)}, comme d'habitude.",
                    en = "It lasted ${days(length)}, as usual.",
                    pl = "Trwał ${days(length)}, jak zwykle.",
                    uk = "Він тривав ${days(length)}, як зазвичай.",
                    es = "Duró ${days(length)}, como de costumbre.",
                    de = "Er dauerte ${days(length)}, wie üblich.",
                )
                difference > 0 -> tr(
                    fr = "Il a duré ${days(length)}, ${days(difference)} de plus que d'habitude (${recap.usualLength}).",
                    en = "It lasted ${days(length)}, ${days(difference)} longer than usual (${recap.usualLength}).",
                    pl = "Trwał ${days(length)}, czyli o ${days(difference)} dłużej niż zwykle (${recap.usualLength}).",
                    uk = "Він тривав ${days(length)}, на ${days(difference)} довше, ніж зазвичай (${recap.usualLength}).",
                    es = "Duró ${days(length)}, ${days(difference)} más que de costumbre (${recap.usualLength}).",
                    de = "Er dauerte ${days(length)}, ${days(difference)} länger als üblich (${recap.usualLength}).",
                )
                else -> tr(
                    fr = "Il a duré ${days(length)}, ${days(difference)} de moins que d'habitude (${recap.usualLength}).",
                    en = "It lasted ${days(length)}, ${days(difference)} shorter than usual (${recap.usualLength}).",
                    pl = "Trwał ${days(length)}, czyli o ${days(difference)} krócej niż zwykle (${recap.usualLength}).",
                    uk = "Він тривав ${days(length)}, на ${days(difference)} коротше, ніж зазвичай (${recap.usualLength}).",
                    es = "Duró ${days(length)}, ${days(difference)} menos que de costumbre (${recap.usualLength}).",
                    de = "Er dauerte ${days(length)}, ${days(difference)} kürzer als üblich (${recap.usualLength}).",
                )
            },
        )
        recap.record.periodLength?.let {
            add(
                tr(
                    fr = "Ses règles ont duré ${days(it)}.",
                    en = "The period lasted ${days(it)}.",
                    pl = "Miesiączka trwała ${days(it)}.",
                    uk = "Менструація тривала ${days(it)}.",
                    es = "La regla duró ${days(it)}.",
                    de = "Die Periode dauerte ${days(it)}.",
                ),
            )
        }
        recap.record.predictionError?.let { error ->
            add(
                when {
                    error == 0 -> tr(
                        fr = "Vos règles sont arrivées à la date estimée.",
                        en = "Your period arrived on the estimated date.",
                        pl = "Twoja miesiączka pojawiła się w szacowanym terminie.",
                        uk = "Ваша менструація почалася в передбачену дату.",
                        es = "Tu regla llegó en la fecha estimada.",
                        de = "Deine Periode kam zum geschätzten Datum.",
                    )
                    error > 0 -> tr(
                        fr = "Vos règles sont arrivées ${days(error)} après la date estimée.",
                        en = "Your period arrived ${days(error)} after the estimated date.",
                        pl = "Twoja miesiączka pojawiła się ${days(error)} po szacowanym terminie.",
                        uk = "Ваша менструація почалася на ${days(error)} пізніше за передбачену дату.",
                        es = "Tu regla llegó ${days(error)} después de la fecha estimada.",
                        de = "Deine Periode kam ${days(error)} nach dem geschätzten Datum.",
                    )
                    else -> tr(
                        fr = "Vos règles sont arrivées ${days(error)} avant la date estimée.",
                        en = "Your period arrived ${days(error)} before the estimated date.",
                        pl = "Twoja miesiączka pojawiła się ${days(error)} przed szacowanym terminem.",
                        uk = "Ваша менструація почалася на ${days(error)} раніше за передбачену дату.",
                        es = "Tu regla llegó ${days(error)} antes de la fecha estimada.",
                        de = "Deine Periode kam ${days(error)} vor dem geschätzten Datum.",
                    )
                },
            )
        }
        val usualPain = recap.usualPainDays
        if (usualPain != null && (recap.record.painDays > 0 || usualPain > 0)) {
            add(
                tr(
                    fr = "Jours de douleur gênante ou forte : ${recap.record.painDays}, contre $usualPain d'habitude.",
                    en = "Days of bothersome or severe pain: ${recap.record.painDays}, compared with $usualPain usually.",
                    pl = "Dni z bólem dokuczliwym lub silnym: ${recap.record.painDays}, zwykle: $usualPain.",
                    uk = "Днів із помірним або сильним болем: ${recap.record.painDays}, зазвичай: $usualPain.",
                    es = "Días de dolor molesto o fuerte: ${recap.record.painDays}, frente a $usualPain habitualmente.",
                    de = "Tage mit mäßigen oder starken Schmerzen: ${recap.record.painDays}, gegenüber $usualPain üblicherweise.",
                ),
            )
        }
        if (!recap.withinReferences) {
            add(
                tr(
                    fr = "Cette durée sort des repères habituels. Si cela se répète, la fiche « Quand consulter » dit quoi faire.",
                    en = "This length is outside the usual range. If it happens again, the \"When to see a doctor\" guide says what to do.",
                    pl = "Ta długość wykracza poza typowy zakres. Jeśli się to powtórzy, artykuł „Kiedy udać się do lekarza” podpowiada, co zrobić.",
                    uk = "Ця тривалість виходить за межі звичайних значень. Якщо це повториться, стаття «Коли звертатися до лікаря» підкаже, що робити.",
                    es = "Esta duración se sale de los intervalos habituales. Si se repite, la ficha «Cuándo consultar» explica qué hacer.",
                    de = "Diese Dauer liegt außerhalb des üblichen Bereichs. Wenn das wieder vorkommt, sagt der Artikel „Wann zum Arzt?“, was zu tun ist.",
                ),
            )
        }
    }

    fun anticipationLine(anticipation: Anticipation): String {
        val labels = anticipation.pattern.labels.joinToString(", ") { inline(it) }
        val phase = phaseName(anticipation.phase)
        return if (anticipation.inDays == 1) {
            tr(
                fr = "Demain commence $phase, où vous notez souvent : $labels.",
                en = "Tomorrow $phase begins, when you often log: $labels.",
                pl = "Jutro zaczyna się $phase, kiedy często zapisujesz: $labels.",
                uk = "Завтра починається $phase, коли ви часто записуєте: $labels.",
                es = "Mañana empieza $phase, cuando anotas con frecuencia: $labels.",
                de = "Morgen beginnt $phase, wenn du häufig einträgst: $labels.",
            )
        } else {
            val inDays = days(anticipation.inDays)
            tr(
                fr = "Dans $inDays commence $phase, où vous notez souvent : $labels.",
                en = "In $inDays, $phase begins, when you often log: $labels.",
                pl = "Za $inDays zaczyna się $phase, kiedy często zapisujesz: $labels.",
                uk = "Через $inDays починається $phase, коли ви часто записуєте: $labels.",
                es = "En $inDays empieza $phase, cuando anotas con frecuencia: $labels.",
                de = "In $inDays beginnt $phase, wenn du häufig einträgst: $labels.",
            )
        }
    }

    fun overviewLines(overview: CycleOverview): List<String> = buildList {
        val count = overview.records.size
        add(
            if (overview.shortest == overview.longest) {
                tr(
                    fr = "Vos $count derniers cycles ont tous duré ${days(overview.usualLength)}.",
                    en = "Your last $count cycles all lasted ${days(overview.usualLength)}.",
                    pl = "Wszystkie Twoje ostatnie cykle ($count) trwały ${days(overview.usualLength)}.",
                    uk = "Усі ваші останні цикли ($count) тривали ${days(overview.usualLength)}.",
                    es = "Tus últimos $count ciclos han durado todos ${days(overview.usualLength)}.",
                    de = "Deine letzten $count Zyklen dauerten alle ${days(overview.usualLength)}.",
                )
            } else {
                tr(
                    fr = "Vos cycles durent en général ${days(overview.usualLength)}, de ${overview.shortest} à ${days(overview.longest)} sur les $count derniers.",
                    en = "Your cycles usually last ${days(overview.usualLength)}, from ${overview.shortest} to ${days(overview.longest)} over the last $count.",
                    pl = "Twoje cykle trwają zwykle ${days(overview.usualLength)}, od ${overview.shortest} do ${days(overview.longest)} w ostatnich $count cyklach.",
                    uk = "Ваші цикли зазвичай тривають ${days(overview.usualLength)}, від ${overview.shortest} до ${days(overview.longest)} в останніх $count циклах.",
                    es = "Tus ciclos suelen durar ${days(overview.usualLength)}, de ${overview.shortest} a ${days(overview.longest)} en los últimos $count.",
                    de = "Deine Zyklen dauern in der Regel ${days(overview.usualLength)}, von ${overview.shortest} bis ${days(overview.longest)} in den letzten $count.",
                )
            },
        )
        overview.usualPeriodLength?.let {
            add(
                tr(
                    fr = "Vos règles durent en général ${days(it)}.",
                    en = "Your period usually lasts ${days(it)}.",
                    pl = "Twoja miesiączka trwa zwykle ${days(it)}.",
                    uk = "Ваша менструація зазвичай триває ${days(it)}.",
                    es = "Tu regla suele durar ${days(it)}.",
                    de = "Deine Periode dauert in der Regel ${days(it)}.",
                ),
            )
        }
        add(
            if (overview.withinReferences) {
                tr(
                    fr = "Ces durées sont dans les repères habituels.",
                    en = "These lengths are within the usual range.",
                    pl = "Te długości mieszczą się w typowym zakresie.",
                    uk = "Ці тривалості в межах звичайних значень.",
                    es = "Estas duraciones están dentro de los intervalos habituales.",
                    de = "Diese Längen liegen im üblichen Bereich.",
                )
            } else {
                tr(
                    fr = "Certaines durées sortent des repères habituels. La fiche « Quand consulter » dit quand en parler.",
                    en = "Some lengths are outside the usual range. The \"When to see a doctor\" guide says when to bring it up.",
                    pl = "Niektóre długości wykraczają poza typowy zakres. Artykuł „Kiedy udać się do lekarza” podpowiada, kiedy o tym porozmawiać.",
                    uk = "Деякі тривалості виходять за межі звичайних значень. Стаття «Коли звертатися до лікаря» підкаже, коли про це поговорити.",
                    es = "Algunas duraciones se salen de los intervalos habituales. La ficha «Cuándo consultar» explica cuándo hablarlo.",
                    de = "Einige Längen liegen außerhalb des üblichen Bereichs. Der Artikel „Wann zum Arzt?“ sagt, wann du das ansprechen solltest.",
                )
            },
        )
    }

    fun predictionLine(overview: CycleOverview): String {
        val error = overview.typicalPredictionError
        val checked = overview.predictionsChecked
        return when {
            error == null -> tr(
                fr = "Il faut encore quelques cycles pour mesurer la justesse des prévisions.",
                en = "A few more cycles are needed to measure how accurate the forecasts are.",
                pl = "Potrzeba jeszcze kilku cykli, aby ocenić trafność prognoz.",
                uk = "Потрібно ще кілька циклів, щоб оцінити точність прогнозів.",
                es = "Hacen falta todavía algunos ciclos para medir la precisión de las previsiones.",
                de = "Es braucht noch einige Zyklen, um die Genauigkeit der Vorhersagen zu messen.",
            )
            error == 0 -> tr(
                fr = "Vos règles sont en général arrivées à la date estimée, sur $checked cycles vérifiés.",
                en = "Your period has usually arrived on the estimated date, over $checked cycles checked.",
                pl = "Twoja miesiączka zwykle pojawiała się w szacowanym terminie (sprawdzone cykle: $checked).",
                uk = "Ваша менструація зазвичай починалася в передбачену дату (перевірені цикли: $checked).",
                es = "Tu regla ha llegado por lo general en la fecha estimada, sobre $checked ciclos comprobados.",
                de = "Deine Periode kam in der Regel zum geschätzten Datum, bei $checked geprüften Zyklen.",
            )
            else -> tr(
                fr = "Vos règles sont en général arrivées à ${days(error)} de la date estimée, sur $checked cycles vérifiés. C'est pour cela que Lunaison affiche une plage de dates.",
                en = "Your period has usually arrived within ${days(error)} of the estimated date, over $checked cycles checked. That is why Lunaison shows a range of dates.",
                pl = "Twoja miesiączka zwykle pojawiała się z dokładnością do ${days(error)} względem szacowanego terminu (sprawdzone cykle: $checked). Dlatego Lunaison pokazuje przedział dat.",
                uk = "Ваша менструація зазвичай починалася з точністю до ${days(error)} щодо передбаченої дати (перевірені цикли: $checked). Саме тому Lunaison показує діапазон дат.",
                es = "Tu regla ha llegado por lo general con un margen de ${days(error)} respecto a la fecha estimada, sobre $checked ciclos comprobados. Por eso Lunaison muestra un intervalo de fechas.",
                de = "Deine Periode kam in der Regel mit einer Abweichung von höchstens ${days(error)} vom geschätzten Datum, bei $checked geprüften Zyklen. Deshalb zeigt Lunaison einen Datumsbereich an.",
            )
        }
    }

    fun onsetLine(onset: Onset): String = when (onset.daysBefore) {
        1 -> tr(
            fr = "${onset.label} : en général la veille des règles (${onset.cycles} cycles).",
            en = "${onset.label}: usually the day before your period (${onset.cycles} cycles).",
            pl = "${onset.label}: zwykle dzień przed miesiączką (cykle: ${onset.cycles}).",
            uk = "${onset.label}: зазвичай за день до менструації (цикли: ${onset.cycles}).",
            es = "${onset.label}: normalmente el día antes de la regla (${onset.cycles} ciclos).",
            de = "${onset.label}: meist am Tag vor der Periode (${onset.cycles} Zyklen).",
        )
        else -> {
            val before = days(onset.daysBefore)
            tr(
                fr = "${onset.label} : en général $before avant les règles (${onset.cycles} cycles).",
                en = "${onset.label}: usually $before before your period (${onset.cycles} cycles).",
                pl = "${onset.label}: zwykle $before przed miesiączką (cykle: ${onset.cycles}).",
                uk = "${onset.label}: зазвичай за $before до менструації (цикли: ${onset.cycles}).",
                es = "${onset.label}: normalmente $before antes de la regla (${onset.cycles} ciclos).",
                de = "${onset.label}: meist $before vor der Periode (${onset.cycles} Zyklen).",
            )
        }
    }

    val NOTHING_YET: String
        get() = tr(
            fr = "Vos statistiques apparaîtront quand deux cycles complets auront été notés : il faut deux débuts de règles après le premier.",
            en = "Your statistics will appear once two complete cycles have been logged: two period starts are needed after the first one.",
            pl = "Statystyki pojawią się, gdy zostaną zapisane dwa pełne cykle: potrzebne są dwa początki miesiączki po pierwszym.",
            uk = "Статистика з'явиться, коли буде записано два повні цикли: потрібні два початки менструації після першого.",
            es = "Tus estadísticas aparecerán cuando se hayan anotado dos ciclos completos: hacen falta dos inicios de regla después del primero.",
            de = "Deine Statistiken erscheinen, sobald zwei vollständige Zyklen eingetragen sind: Dafür braucht es zwei Periodenbeginne nach dem ersten.",
        )

    val HOW_IT_WORKS: String
        get() = tr(
            fr = "Calculé sur votre téléphone, à partir de ce que vous avez noté. Seuls les jours remplis comptent, et rien n'est affiché avant deux cycles. Ces chiffres décrivent vos cycles, ils ne posent pas de diagnostic.",
            en = "Calculated on your phone, from what you have logged. Only filled-in days count, and nothing is shown before two cycles. These figures describe your cycles; they do not make a diagnosis.",
            pl = "Obliczone na Twoim telefonie na podstawie Twoich zapisów. Liczą się tylko wypełnione dni, a przed dwoma cyklami nic się nie wyświetla. Te liczby opisują Twoje cykle, nie stanowią diagnozy.",
            uk = "Обчислено на вашому телефоні на основі ваших записів. Враховуються лише заповнені дні, а до двох циклів нічого не показується. Ці цифри описують ваші цикли, вони не є діагнозом.",
            es = "Calculado en tu teléfono, a partir de lo que has anotado. Solo cuentan los días rellenados, y no se muestra nada antes de dos ciclos. Estas cifras describen tus ciclos, no constituyen un diagnóstico.",
            de = "Auf deinem Telefon berechnet, auf Grundlage deiner Einträge. Nur ausgefüllte Tage zählen, und vor zwei Zyklen wird nichts angezeigt. Diese Zahlen beschreiben deine Zyklen, sie stellen keine Diagnose dar.",
        )
}
