package fr.lunaison.core.content

import fr.lunaison.core.cycle.Phase

/** Conseils par phase en anglais, traduits de PhaseGuidesFr. */
internal object PhaseGuidesEn {

    val menstrual = PhaseGuide(
        phase = Phase.MENSTRUAL,
        name = "Period",
        whatHappens = "The lining of the uterus is shed and hormone levels are at their lowest. Tiredness and pain are common in the first days.",
        tips = listOf(
            "In studies, heat on the lower belly relieves pain as well as an anti-inflammatory.",
            "An anti-inflammatory works better taken as soon as the pain starts, following the leaflet.",
            "Think about iron: legumes, meat, fish, with a source of vitamin C.",
            "Tampon or cup: follow the wearing time given in the leaflet.",
        ),
        articleId = "douleurs",
    )

    val follicular = PhaseGuide(
        phase = Phase.FOLLICULAR,
        name = "Follicular phase",
        whatHappens = "Estrogen rises again while a follicle matures in an ovary. Many people feel more energetic, others notice no difference at all.",
        tips = listOf(
            "It is often a good time to get back into regular physical activity: it also reduces the pain of your next periods.",
            "Note your mood and energy for a few days: you will see what really changes over your cycle.",
        ),
        articleId = "cycle",
    )

    val ovulation = PhaseGuide(
        phase = Phase.OVULATION,
        name = "Around ovulation",
        whatHappens = "The ovary releases an egg. Some people feel a slight pain on one side of the belly or notice clearer, stretchier discharge.",
        tips = listOf(
            "This window is estimated from your periods, with a margin of error: it cannot be used to avoid pregnancy.",
            "A brief, slight pain on one side is common. Strong or lasting pain deserves medical advice.",
        ),
        articleId = "previsions",
    )

    val luteal = PhaseGuide(
        phase = Phase.LUTEAL,
        name = "Luteal phase",
        whatHappens = "Progesterone dominates. In the last days, tender breasts, bloating, tiredness or changing moods are common.",
        tips = listOf(
            "Regular physical activity and good sleep often ease premenstrual symptoms.",
            "Dairy products, calcium-rich mineral water or almonds provide calcium, which reduced these symptoms in one trial.",
            "If the coming days look busy, lighten what you can.",
        ),
        articleId = "spm",
    )

    val late = PhaseGuide(
        phase = Phase.LATE,
        name = "Period late",
        whatHappens = "Your period has not started within the estimated range. Stress, travel, illness, weight change, intense exercise or pregnancy can explain it.",
        tips = listOf(
            "If it has started and you forgot to log it, add it in the calendar.",
            "If a pregnancy is possible, a urine test is usually reliable from now on.",
            "With no period for more than three months, ask for medical advice.",
        ),
        articleId = "previsions",
    )

    val all: List<PhaseGuide> = listOf(menstrual, follicular, ovulation, luteal, late)
}
