package fr.lunaison.core.content

import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.cycle.Phase
import fr.lunaison.core.model.Language
import fr.lunaison.core.model.tr

/**
 * Messages proposés pour le partage avec un ou une partenaire. La personne choisit, relit et modifie le texte,
 * puis l'envoie elle-même depuis sa messagerie : rien ne part automatiquement.
 */
enum class ShareKind {
    PERIOD_STARTED,
    PAIN,
    REST,
    HELP,
    PERIOD_SOON,
    PHASE_UPDATE,
    FEELING_BETTER,
    ;

    val label: String get() = when (this) {
        PERIOD_STARTED -> tr(
            fr = "Mes règles ont commencé",
            en = "My period has started",
            pl = "Zaczęła mi się miesiączka",
            uk = "У мене почалася менструація",
            es = "Me ha venido la regla",
            de = "Meine Periode hat begonnen",
        )
        PAIN -> tr(
            fr = "J'ai mal aujourd'hui",
            en = "I'm in pain today",
            pl = "Dziś odczuwam ból",
            uk = "Сьогодні мені боляче",
            es = "Hoy tengo dolor",
            de = "Ich habe heute Schmerzen",
        )
        REST -> tr(
            fr = "J'ai besoin de calme",
            en = "I need some quiet",
            pl = "Potrzebuję spokoju",
            uk = "Мені потрібен спокій",
            es = "Necesito calma",
            de = "Ich brauche Ruhe",
        )
        HELP -> tr(
            fr = "Un coup de main ?",
            en = "Can you help?",
            pl = "Pomożesz mi?",
            uk = "Допоможеш?",
            es = "¿Me echas una mano?",
            de = "Kannst du mir helfen?",
        )
        PERIOD_SOON -> tr(
            fr = "Mes règles arrivent bientôt",
            en = "My period is coming soon",
            pl = "Miesiączka zbliża się",
            uk = "Менструація скоро почнеться",
            es = "Mi regla llegará pronto",
            de = "Meine Periode kommt bald",
        )
        PHASE_UPDATE -> tr(
            fr = "Expliquer ma phase actuelle",
            en = "Explain my current phase",
            pl = "Wyjaśnić moją obecną fazę",
            uk = "Пояснити мою поточну фазу",
            es = "Explicar mi fase actual",
            de = "Meine aktuelle Phase erklären",
        )
        FEELING_BETTER -> tr(
            fr = "Ça va mieux",
            en = "Feeling better",
            pl = "Czuję się lepiej",
            uk = "Мені краще",
            es = "Me encuentro mejor",
            de = "Mir geht es besser",
        )
    }
}

/** Repères pour le ou la partenaire : comprendre la phase en cours et savoir comment soutenir concrètement. */
data class PartnerSupportGuide(
    val phaseTitle: String,
    val whatPartnerShouldKnow: String,
    val supportTips: List<String>,
)

object ShareMessages {

    /** Les modèles proposés aujourd'hui, du plus probable au moins probable. */
    fun suggestions(state: CycleState): List<ShareKind> {
        val known = state as? CycleState.Known
        return when {
            known == null -> listOf(ShareKind.PAIN, ShareKind.REST, ShareKind.HELP, ShareKind.PERIOD_STARTED)
            known.inPeriod -> listOf(ShareKind.PAIN, ShareKind.REST, ShareKind.HELP, ShareKind.PERIOD_STARTED, ShareKind.PHASE_UPDATE, ShareKind.FEELING_BETTER)
            known.daysUntilNext in 0..5 || known.daysLate > 0 ->
                listOf(ShareKind.PERIOD_SOON, ShareKind.PHASE_UPDATE, ShareKind.PERIOD_STARTED, ShareKind.REST, ShareKind.PAIN, ShareKind.HELP)
            else -> listOf(ShareKind.PHASE_UPDATE, ShareKind.REST, ShareKind.PAIN, ShareKind.HELP, ShareKind.FEELING_BETTER)
        }
    }

    fun partnerGuide(phase: Phase): PartnerSupportGuide = when (phase) {
        Phase.MENSTRUAL -> PartnerSupportGuide(
            phaseTitle = tr(
                fr = "Pendant les règles",
                en = "During the period",
                pl = "Podczas miesiączki",
                uk = "Під час менструації",
                es = "Durante la regla",
                de = "Während der Periode",
            ),
            whatPartnerShouldKnow = tr(
                fr = "Les hormones sont au plus bas et l'utérus se contracte. La fatigue et les crampes peuvent être intenses les premiers jours.",
                en = "Hormones are at their lowest and the uterus contracts. Fatigue and cramps can be intense in the first days.",
                pl = "Poziom hormonów jest najniższy, a macica się kurczy. Zmęczenie i skurcze mogą być intensywne w pierwszych dniach.",
                uk = "Рівень гормонів найнижчий, а матка скорочується. Втома й спазми можуть бути інтенсивними в перші дні.",
                es = "Las hormonas están en su nivel más bajo y el útero se contrae. El cansancio y los calambres pueden ser intensos los primeros días.",
                de = "Die Hormone sind auf ihrem Tiefststand und die Gebärmutter zieht sich zusammen. Müdigkeit und Krämpfe können in den ersten Tagen stark sein.",
            ),
            supportTips = listOf(
                tr(
                    fr = "Préparer une bouillotte chaude ou vérifier qu'il reste un antalgique adapté.",
                    en = "Prepare a hot water bottle or check that suitable pain relief is on hand.",
                    pl = "Przygotować ciepły termofor lub sprawdzić, czy jest w domu odpowiedni lek przeciwbólowy.",
                    uk = "Підготувати теплу грілку або перевірити, чи залишилися відповідні знеболювальні.",
                    es = "Preparar una bolsa de agua caliente o comprobar que queda un analgésico adecuado.",
                    de = "Eine Wärmflasche vorbereiten oder nachsehen, ob noch ein passendes Schmerzmittel da ist.",
                ),
                tr(
                    fr = "Prendre spontanément le relais sur les repas, les courses ou les tâches lourdes.",
                    en = "Proactively take over meals, grocery shopping or heavy chores.",
                    pl = "Z własnej inicjatywy przejąć posiłki, zakupy lub cięższe obowiązki.",
                    uk = "З власної ініціативи взяти на себе приготування їжі, покупки чи важкі справи.",
                    es = "Hacerse cargo por iniciativa propia de las comidas, la compra o las tareas pesadas.",
                    de = "Von sich aus Mahlzeiten, Einkäufe oder schwere Aufgaben übernehmen.",
                ),
                tr(
                    fr = "Proposer une soirée calme sans pression sociale si l'énergie est basse.",
                    en = "Offer a quiet evening without social pressure if energy is low.",
                    pl = "Zaproponować spokojny wieczór bez presji towarzyskiej, jeśli brakuje energii.",
                    uk = "Запропонувати спокійний вечір без соціального тиску, якщо сил мало.",
                    es = "Proponer una noche tranquila sin presión social si hay poca energía.",
                    de = "Einen ruhigen Abend ohne gesellschaftlichen Druck vorschlagen, wenn die Energie niedrig ist.",
                ),
            ),
        )
        Phase.FOLLICULAR -> PartnerSupportGuide(
            phaseTitle = tr(
                fr = "Phase folliculaire",
                en = "Follicular phase",
                pl = "Faza folikularna",
                uk = "Фолікулярна фаза",
                es = "Fase folicular",
                de = "Follikelphase",
            ),
            whatPartnerShouldKnow = tr(
                fr = "Après les règles, les œstrogènes remontent progressivement. L'énergie et l'élan reviennent souvent pendant cette période.",
                en = "After the period, estrogen gradually rises. Energy and drive often return during this time.",
                pl = "Po miesiączce poziom estrogenów stopniowo rośnie. W tym okresie często wracają energia i chęć do działania.",
                uk = "Після менструації рівень естрогенів поступово зростає. У цей період часто повертаються енергія та натхнення.",
                es = "Después de la regla, los estrógenos vuelven a subir poco a poco. La energía y el impulso suelen volver durante este periodo.",
                de = "Nach der Periode steigt das Östrogen allmählich wieder an. In dieser Zeit kehren Energie und Antrieb oft zurück.",
            ),
            supportTips = listOf(
                tr(
                    fr = "Bon moment pour planifier des sorties, du sport ou des projets à deux.",
                    en = "A good time to plan outings, exercise or projects together.",
                    pl = "Dobry moment na planowanie wyjść, sportu lub wspólnych projektów.",
                    uk = "Слушний час, щоб планувати виходи, спорт або спільні проєкти.",
                    es = "Buen momento para planificar salidas, deporte o proyectos en pareja.",
                    de = "Eine gute Zeit, um Ausflüge, Sport oder gemeinsame Projekte zu planen.",
                ),
                tr(
                    fr = "Après des règles abondantes, privilégier des repas riches en fer et vitamine C.",
                    en = "After heavy periods, favor meals rich in iron and vitamin C.",
                    pl = "Po obfitych miesiączkach wybierać posiłki bogate w żelazo i witaminę C.",
                    uk = "Після рясних менструацій віддавати перевагу стравам, багатим на залізо та вітамін C.",
                    es = "Tras unas reglas abundantes, dar preferencia a comidas ricas en hierro y vitamina C.",
                    de = "Nach starken Perioden eisen- und vitamin-C-reiche Mahlzeiten bevorzugen.",
                ),
                tr(
                    fr = "Rester à l'écoute : chaque cycle est différent et la fatigue peut parfois persister.",
                    en = "Stay attentive: every cycle is different and tiredness can sometimes linger.",
                    pl = "Zachować uważność: każdy cykl jest inny, a zmęczenie czasem się utrzymuje.",
                    uk = "Зберігати уважність: кожен цикл різний, а втома іноді може зберігатися.",
                    es = "Mantener la atención: cada ciclo es distinto y a veces el cansancio puede persistir.",
                    de = "Aufmerksam bleiben: Jeder Zyklus ist anders, und die Müdigkeit kann manchmal anhalten.",
                ),
            ),
        )
        Phase.OVULATION -> PartnerSupportGuide(
            phaseTitle = tr(
                fr = "Autour de l'ovulation",
                en = "Around ovulation",
                pl = "Wokół owulacji",
                uk = "Навколо овуляції",
                es = "En torno a la ovulación",
                de = "Rund um den Eisprung",
            ),
            whatPartnerShouldKnow = tr(
                fr = "Les œstrogènes atteignent leur pic au milieu du cycle. Une gêne passagère d'un côté du bas-ventre est possible.",
                en = "Estrogen reaches its peak mid-cycle. Mild temporary discomfort on one side of the lower belly is possible.",
                pl = "Estrogeny osiągają szczyt w połowie cyklu. Możliwy jest przemijający dyskomfort po jednej stronie podbrzusza.",
                uk = "Естрогени досягають піку в середині циклу. Можливий короткочасний дискомфорт з одного боку внизу живота.",
                es = "Los estrógenos alcanzan su pico a mitad del ciclo. Es posible una molestia pasajera en un lado del bajo vientre.",
                de = "Das Östrogen erreicht in der Zyklusmitte seinen Höhepunkt. Ein vorübergehendes Unbehagen auf einer Seite des Unterbauchs ist möglich.",
            ),
            supportTips = listOf(
                tr(
                    fr = "C'est la fenêtre fertile du cycle : utile à savoir selon vos projets communs.",
                    en = "This is the fertile window of the cycle: useful to know depending on your shared plans.",
                    pl = "To płodne okno cyklu: warto o tym wiedzieć, zależnie od wspólnych planów.",
                    uk = "Це фертильне вікно циклу: корисно знати залежно від ваших спільних планів.",
                    es = "Es la ventana fértil del ciclo: conviene saberlo según los proyectos en común.",
                    de = "Das ist das fruchtbare Fenster des Zyklus: je nach euren gemeinsamen Plänen gut zu wissen.",
                ),
                tr(
                    fr = "Si une douleur pelvienne ou un mal de tête survient, un peu de repos aide.",
                    en = "If pelvic discomfort or a headache occurs, a bit of rest helps.",
                    pl = "Jeśli pojawi się ból miednicy lub ból głowy, pomaga trochę odpoczynku.",
                    uk = "Якщо з'явиться тазовий біль чи головний біль, допоможе трохи відпочинку.",
                    es = "Si aparece dolor pélvico o dolor de cabeza, un poco de descanso ayuda.",
                    de = "Wenn Beckenschmerzen oder Kopfschmerzen auftreten, hilft etwas Ruhe.",
                ),
                tr(
                    fr = "Rappel : l'estimation par calendrier ne protège pas d'une grossesse.",
                    en = "Reminder: calendar estimates do not protect against pregnancy.",
                    pl = "Przypomnienie: szacowanie na podstawie kalendarza nie chroni przed ciążą.",
                    uk = "Нагадування: календарні оцінки не захищають від вагітності.",
                    es = "Recordatorio: la estimación por calendario no protege de un embarazo.",
                    de = "Zur Erinnerung: Die Schätzung per Kalender schützt nicht vor einer Schwangerschaft.",
                ),
            ),
        )
        Phase.LUTEAL -> PartnerSupportGuide(
            phaseTitle = tr(
                fr = "Phase lutéale (avant les règles)",
                en = "Luteal phase (before the period)",
                pl = "Faza lutealna (przed miesiączką)",
                uk = "Лютеїнова фаза (перед менструацією)",
                es = "Fase lútea (antes de la regla)",
                de = "Lutealphase (vor der Periode)",
            ),
            whatPartnerShouldKnow = tr(
                fr = "La progestérone domine puis chute avant les règles. Fatigue, seins sensibles, sommeil perturbé ou irritabilité (SPM) sont fréquents en fin de cycle.",
                en = "Progesterone dominates and then drops before the period. Fatigue, tender breasts, poor sleep or irritability (PMS) are common late in the cycle.",
                pl = "Dominuje progesteron, a przed miesiączką jego poziom spada. Zmęczenie, tkliwe piersi, zaburzony sen lub drażliwość (PMS) są częste pod koniec cyklu.",
                uk = "Переважає прогестерон, а перед менструацією його рівень падає. Втома, чутливі груди, порушений сон або дратівливість (ПМС) часті наприкінці циклу.",
                es = "La progesterona predomina y luego baja antes de la regla. Cansancio, pechos sensibles, sueño alterado o irritabilidad (SPM) son frecuentes al final del ciclo.",
                de = "Progesteron dominiert und fällt dann vor der Periode ab. Müdigkeit, empfindliche Brüste, gestörter Schlaf oder Reizbarkeit (PMS) sind am Zyklusende häufig.",
            ),
            supportTips = listOf(
                tr(
                    fr = "Alléger la charge mentale du quotidien sans attendre qu'on vous le demande.",
                    en = "Lighten the daily mental load without waiting to be asked.",
                    pl = "Zmniejszyć codzienne obciążenie mentalne, nie czekając na prośbę.",
                    uk = "Зменшити щоденне ментальне навантаження, не чекаючи, поки про це попросять.",
                    es = "Aligerar la carga mental del día a día sin esperar a que lo pidan.",
                    de = "Den Mental Load im Alltag verringern, ohne auf eine Bitte zu warten.",
                ),
                tr(
                    fr = "Faire preuve de patience et de douceur si la sensibilité ou la fatigue augmente.",
                    en = "Show patience and gentleness if sensitivity or tiredness increases.",
                    pl = "Okazać cierpliwość i łagodność, jeśli wzrasta wrażliwość lub zmęczenie.",
                    uk = "Виявляти терпіння та м'якість, якщо зростає чутливість або втома.",
                    es = "Mostrar paciencia y delicadeza si aumentan la sensibilidad o el cansancio.",
                    de = "Geduld und Einfühlungsvermögen zeigen, wenn Empfindlichkeit oder Müdigkeit zunehmen.",
                ),
                tr(
                    fr = "Éviter de minimiser ce qui est ressenti (« c'est juste tes règles »).",
                    en = "Avoid dismissing feelings (\"it's just your period\").",
                    pl = "Nie umniejszać tego, co jest odczuwane („to tylko twoja miesiączka”).",
                    uk = "Не применшувати те, що відчувається («це ж просто менструація»).",
                    es = "Evitar quitar importancia a lo que se siente («solo es tu regla»).",
                    de = "Vermeiden, das Empfundene herunterzuspielen („Das ist doch nur deine Periode“).",
                ),
            ),
        )
        Phase.LATE -> PartnerSupportGuide(
            phaseTitle = tr(
                fr = "Règles en retard",
                en = "Period late",
                pl = "Spóźniająca się miesiączka",
                uk = "Затримка менструації",
                es = "Regla con retraso",
                de = "Verspätete Periode",
            ),
            whatPartnerShouldKnow = tr(
                fr = "Les règles dépassent la plage attendue. Cela peut venir du stress, d'un changement de rythme ou d'une grossesse.",
                en = "The period is past the expected range. This can come from stress, a change in routine or pregnancy.",
                pl = "Miesiączka przekracza oczekiwany przedział. Może to wynikać ze stresu, zmiany trybu życia lub ciąży.",
                uk = "Менструація виходить за межі очікуваного діапазону. Причиною може бути стрес, зміна ритму життя або вагітність.",
                es = "La regla se sale del intervalo esperado. Puede deberse al estrés, a un cambio de ritmo o a un embarazo.",
                de = "Die Periode liegt außerhalb des erwarteten Zeitraums. Das kann an Stress, einer Veränderung des Alltagsrhythmus oder an einer Schwangerschaft liegen.",
            ),
            supportTips = listOf(
                tr(
                    fr = "Être présent et rassurant, sans ajouter de stress ni de questions insistantes.",
                    en = "Be present and reassuring, without adding stress or insistent questions.",
                    pl = "Okazać obecność i wsparcie, nie dokładając stresu ani natarczywych pytań.",
                    uk = "Бути поруч і підтримувати, не додаючи стресу та настирливих запитань.",
                    es = "Estar presente y transmitir calma, sin añadir estrés ni preguntas insistentes.",
                    de = "Da sein und Ruhe vermitteln, ohne zusätzlichen Stress oder bohrende Fragen.",
                ),
                tr(
                    fr = "Proposer d'aller chercher un test en pharmacie si vous le souhaitez tous les deux.",
                    en = "Offer to pick up a test at the pharmacy if you both want to.",
                    pl = "Zaproponować kupienie testu w aptece, jeśli oboje tego chcecie.",
                    uk = "Запропонувати купити тест в аптеці, якщо ви обоє цього хочете.",
                    es = "Proponer ir a comprar un test a la farmacia si ambos lo desean.",
                    de = "Anbieten, in der Apotheke einen Test zu holen, wenn ihr beide das möchtet.",
                ),
                tr(
                    fr = "Laisser de l'espace et du calme en attendant la suite.",
                    en = "Allow space and calm while waiting to see what happens next.",
                    pl = "Zostawić przestrzeń i spokój w oczekiwaniu na dalszy rozwój sytuacji.",
                    uk = "Залишити простір і спокій, поки з'ясується, що буде далі.",
                    es = "Dejar espacio y calma mientras se espera a ver qué pasa.",
                    de = "Raum und Ruhe lassen, bis sich zeigt, wie es weitergeht.",
                ),
            ),
        )
    }

    /** Début de phrase en minuscules, sauf en allemand où les noms et les infinitifs substantivés gardent leur majuscule. */
    private fun lowerFirst(text: String): String =
        if (Language.current() == Language.DE) text else text.replaceFirstChar { it.lowercase() }

    fun text(kind: ShareKind, state: CycleState): String = when (kind) {
        ShareKind.PERIOD_STARTED -> tr(
            fr = "Mes règles ont commencé aujourd'hui. Je risque d'avoir moins d'énergie et d'avoir mal les prochains jours.",
            en = "My period started today. I may have less energy and be in pain over the next few days.",
            pl = "Dziś zaczęła mi się miesiączka. Przez najbliższe dni mogę mieć mniej energii i odczuwać ból.",
            uk = "Сьогодні в мене почалася менструація. Найближчими днями я можу мати менше сил і відчувати біль.",
            es = "Hoy me ha venido la regla. Es posible que tenga menos energía y dolor los próximos días.",
            de = "Meine Periode hat heute angefangen. In den nächsten Tagen habe ich vielleicht weniger Energie und Schmerzen.",
        )
        ShareKind.PAIN -> tr(
            fr = "J'ai mal aujourd'hui (règles). Une bouillotte et un peu de calme me feraient du bien.",
            en = "I'm in pain today (period). A hot water bottle and a bit of quiet would do me good.",
            pl = "Dziś odczuwam ból (miesiączka). Termofor i trochę spokoju by mi pomogły.",
            uk = "Сьогодні мені боляче (менструація). Грілка й трохи спокою мені б допомогли.",
            es = "Hoy tengo dolor (regla). Una bolsa de agua caliente y un poco de calma me vendrían bien.",
            de = "Ich habe heute Schmerzen (Periode). Eine Wärmflasche und etwas Ruhe würden mir guttun.",
        )
        ShareKind.REST -> tr(
            fr = "J'ai besoin d'une soirée calme aujourd'hui. Rien de grave, c'est le cycle.",
            en = "I need a quiet evening today. Nothing serious, it's my cycle.",
            pl = "Potrzebuję dziś spokojnego wieczoru. Nic poważnego, to cykl.",
            uk = "Мені сьогодні потрібен спокійний вечір. Нічого серйозного, це цикл.",
            es = "Hoy necesito una noche tranquila. Nada grave, es el ciclo.",
            de = "Ich brauche heute einen ruhigen Abend. Nichts Schlimmes, das ist der Zyklus.",
        )
        ShareKind.HELP -> tr(
            fr = "Je suis à plat aujourd'hui. Tu pourrais prendre le relais pour le repas ou les courses ?",
            en = "I'm worn out today. Could you take over dinner or the shopping?",
            pl = "Nie mam dziś siły. Czy możesz przejąć gotowanie albo zakupy?",
            uk = "Сьогодні в мене зовсім немає сил. Чи можеш ти взяти на себе приготування їжі чи покупки?",
            es = "Hoy estoy sin fuerzas. ¿Podrías encargarte tú de la comida o de la compra?",
            de = "Ich bin heute ziemlich erschöpft. Könntest du das Essen oder den Einkauf übernehmen?",
        )
        ShareKind.PERIOD_SOON -> {
            val days = (state as? CycleState.Known)?.daysUntilNext
            when {
                days == null || days <= 0 -> tr(
                    fr = "Mes règles devraient arriver d'un jour à l'autre.",
                    en = "My period should arrive any day now.",
                    pl = "Miesiączka powinna zacząć się lada dzień.",
                    uk = "Менструація має початися будь-якого дня.",
                    es = "La regla debería llegar de un día para otro.",
                    de = "Meine Periode müsste jeden Tag kommen.",
                )
                days == 1 -> tr(
                    fr = "Mes règles devraient arriver demain.",
                    en = "My period should arrive tomorrow.",
                    pl = "Miesiączka powinna zacząć się jutro.",
                    uk = "Менструація має початися завтра.",
                    es = "La regla debería llegar mañana.",
                    de = "Meine Periode müsste morgen kommen.",
                )
                else -> tr(
                    fr = "Mes règles devraient arriver dans $days jours environ.",
                    en = "My period should arrive in about $days days.",
                    pl = "Miesiączka powinna zacząć się za około $days dni.",
                    uk = "Менструація має початися приблизно через $days дн.",
                    es = "La regla debería llegar dentro de unos $days días.",
                    de = "Meine Periode müsste in etwa $days Tagen kommen.",
                )
            }
        }
        ShareKind.PHASE_UPDATE -> {
            val phase = (state as? CycleState.Known)?.phase ?: Phase.MENSTRUAL
            val guide = partnerGuide(phase)
            val title = lowerFirst(guide.phaseTitle)
            val know = guide.whatPartnerShouldKnow
            val tip = lowerFirst(guide.supportTips.first())
            tr(
                fr = "Petit point cycle ($title) : $know Ce qui m'aiderait en ce moment : $tip",
                en = "Quick cycle update ($title): $know What would help me right now: $tip",
                pl = "Krótka informacja o cyklu ($title): $know Co by mi teraz pomogło: $tip",
                uk = "Коротко про мій цикл ($title): $know Що мені зараз допомогло б: $tip",
                es = "Pequeña actualización del ciclo ($title): $know Lo que me ayudaría ahora: $tip",
                de = "Kurzes Zyklus-Update ($title): $know Was mir gerade helfen würde: $tip",
            )
        }
        ShareKind.FEELING_BETTER -> tr(
            fr = "Ça va mieux aujourd'hui, merci d'avoir été là.",
            en = "I'm feeling better today, thanks for being there.",
            pl = "Czuję się dziś lepiej, dziękuję za wsparcie.",
            uk = "Сьогодні мені краще, дякую за підтримку.",
            es = "Hoy me encuentro mejor, gracias por haber estado ahí.",
            de = "Mir geht es heute besser, danke, dass du da warst.",
        )
    }
}
