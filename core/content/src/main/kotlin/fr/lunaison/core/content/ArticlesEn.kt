package fr.lunaison.core.content

/**
 * Fiches en anglais, traduites des fiches françaises (mêmes identifiants, mêmes sources, mêmes repères chiffrés).
 * Les numéros d'urgence français sont remplacés par une formule valable partout, avec le numéro français en exemple.
 */
internal object ArticlesEn {

    val cycle = Article(
        id = "cycle",
        title = "Understanding your cycle",
        summary = "What happens during a cycle, and why yours may not look like the one in textbooks.",
        sections = listOf(
            Section(
                "What is a cycle?",
                listOf(
                    "A cycle runs from the first day of one period to the first day of the next. The first day is the first day of real bleeding: spotting alone does not count.",
                    "During a cycle, two hormones made by the ovaries, estrogen and progesterone, rise and fall. They prepare the lining of the uterus for a possible pregnancy. Without a pregnancy, this lining is shed: that is your period.",
                ),
            ),
            Section(
                "The four phases",
                listOf(
                    "Menstruation: the lining of the uterus is shed. Hormone levels are at their lowest.",
                    "The follicular phase: a follicle matures in an ovary and estrogen rises.",
                    "Ovulation: the ovary releases an egg. It does not happen on a fixed date.",
                    "The luteal phase: progesterone dominates until the next period. Premenstrual symptoms often appear at the end of this phase.",
                ),
            ),
            Section(
                "The 28-day cycle is an average, not a rule",
                listOf(
                    "A study of more than 600,000 cycles found an average length of 29.3 days, and only 13% of cycles lasting exactly 28 days. Ovulation does not happen on day 14 for everyone: it is mostly the phase before ovulation that varies from one person to another.",
                    "In adults, a cycle of 24 to 38 days and periods of 8 days or less are within the usual range. From one cycle to the next, a difference of a few days is normal.",
                    "In the teenage years, cycles are often irregular at first: most last between 21 and 45 days.",
                ),
            ),
            Section(
                "How Lunaison makes its estimates",
                listOf(
                    "Lunaison looks at your periods and works out the usual length of your recent cycles. The more cycles you log, the more reliable the estimate becomes. The other phases are deduced: ovulation is estimated about 13 days before the next period, because this last part of the cycle varies little.",
                    "These estimates help you plan. They cannot be used to avoid pregnancy.",
                ),
            ),
        ),
        sources = listOf(Sources.BULL_2019, Sources.FIGO_2018, Sources.ACOG_ADOLESCENTS_2015),
    )

    val pain = Article(
        id = "douleurs",
        title = "Relieving period pain",
        summary = "What has been shown to work, what may help, and when pain should lead you to see someone.",
        sections = listOf(
            Section(
                "Where the pain comes from",
                listOf(
                    "During your period, the uterus produces prostaglandins. These substances make it contract to shed its lining, and these contractions hurt. That is why medicines that block prostaglandins work well.",
                ),
            ),
            Section(
                "Anti-inflammatories",
                listOf(
                    "Non-steroidal anti-inflammatory drugs, such as ibuprofen, relieve pain better than paracetamol (acetaminophen). In trials, they brought clear relief to about half of people, compared with fewer than one in five with a placebo.",
                    "They work best when taken as soon as the pain starts, or even when your period starts. Follow the leaflet. Ask a pharmacist or doctor for advice if you have stomach or kidney problems, asthma, a possible pregnancy or another treatment.",
                ),
            ),
            Section(
                "Heat",
                listOf(
                    "A hot water bottle or a heat patch on the lower belly relieves pain. In studies, the effect was comparable to that of anti-inflammatories, although there are still few trials. Watch out for burns: never put very strong heat directly on the skin.",
                ),
            ),
            Section(
                "Moving",
                listOf(
                    "Regular physical activity, about three sessions of 45 minutes to an hour a week, may clearly reduce the intensity of period pain. The studies are of modest quality, but the risk is low and the other benefits are many. Walking, swimming, yoga: choose what suits you.",
                ),
            ),
            Section(
                "Ginger",
                listOf(
                    "A few small trials suggest that ginger powder, taken during the first days of your period, reduces pain. The evidence is weak. It may be worth a try, without replacing an effective treatment.",
                ),
            ),
            Section(
                "When to see someone",
                listOf(
                    "Painful periods are common, but pain that keeps you from school or work, that does not respond to anti-inflammatories or that gets worse over the years is not normal. It can point to endometriosis or another cause that can be treated.",
                    "Talk to your doctor, a midwife or a gynecologist. Log your pain in Lunaison: a precise history helps a lot during an appointment.",
                ),
            ),
        ),
        sources = listOf(Sources.COCHRANE_NSAIDS_2015, Sources.HEAT_2018, Sources.COCHRANE_EXERCISE_2019, Sources.GINGER_2015, Sources.ACOG_DYSMENORRHEA_2018),
    )

    val food = Article(
        id = "alimentation",
        title = "Food and periods",
        summary = "Iron first. For the rest, modest leads and no miracle diet.",
        sections = listOf(
            Section(
                "Iron",
                listOf(
                    "Periods make you lose iron. People who menstruate need more of it: the European Food Safety Authority sets 16 mg a day as the reference intake. Heavy periods can lead to iron deficiency, which causes tiredness and breathlessness.",
                    "The best-absorbed iron is found in meat, fish and seafood. Legumes (lentils, chickpeas), tofu, whole grains and green vegetables also contain some. Pair them with a source of vitamin C (citrus fruit, bell pepper, kiwi) to absorb it better, and avoid tea or coffee with these meals.",
                    "Do not take iron supplements without medical advice: a simple blood test shows whether you need them.",
                ),
            ),
            Section(
                "Omega-3",
                listOf(
                    "Omega-3 fatty acids, found in oily fish (sardines, mackerel, salmon), walnuts and canola oil, reduced period pain in several trials. The effect is modest and the studies vary in quality. Including them in a varied diet costs nothing.",
                ),
            ),
            Section(
                "Calcium",
                listOf(
                    "One trial showed that taking calcium reduced premenstrual symptoms. Dairy products, calcium-rich mineral water, almonds, green vegetables and tofu are good sources.",
                ),
            ),
            Section(
                "What we do not know",
                listOf(
                    "No food makes painful periods go away, and diets that adapt your whole menu to each phase of the cycle are not backed by solid studies. A varied diet, enough water and regular meals remain the best foundation.",
                ),
            ),
        ),
        sources = listOf(Sources.EFSA_IRON_2015, Sources.OMEGA3_2022, Sources.CALCIUM_PMS_1998, Sources.GINGER_2015),
    )

    val pms = Article(
        id = "spm",
        title = "Premenstrual syndrome",
        summary = "Symptoms in the days before your period, what may help, and when to ask for help.",
        sections = listOf(
            Section(
                "What it is",
                listOf(
                    "In the days before their period, many people have tender breasts, bloating, tiredness, irritability or a lower mood. These symptoms usually go away in the first days of the period.",
                    "Logging them in Lunaison lets you see whether they come back every cycle. The home screen shows you what often comes back during the current phase.",
                ),
            ),
            Section(
                "What may help",
                listOf(
                    "Regular physical activity, enough sleep and regular meals often help. Getting enough calcium reduced symptoms in one trial.",
                    "Knowing the time is coming helps too: planning fewer things on those days, or letting the people around you know, avoids a lot of tension.",
                ),
            ),
            Section(
                "When to ask for help",
                listOf(
                    "If your mood collapses on those days, or if anxiety or anger damage your relationships or your work, talk to a doctor. A severe form, premenstrual dysphoric disorder, can be treated. And if dark thoughts appear, do not wait: contact a crisis line in your country now, such as 3114 in France or 988 in the United States, or your local emergency number.",
                ),
            ),
        ),
        sources = listOf(Sources.CALCIUM_PMS_1998, Sources.COCHRANE_EXERCISE_2019),
    )

    val consult = Article(
        id = "consulter",
        title = "When to see a doctor",
        summary = "The signs that should lead you to ask for medical advice, and the urgent situations.",
        sections = listOf(
            Section(
                "Make an appointment",
                listOf(
                    "With your doctor, a midwife or a gynecologist, if you notice:",
                    "pain that stops you from doing your activities or does not respond to anti-inflammatories;",
                    "periods that last more than 8 days, or so heavy that you change your pad, tampon or cup in less than three hours, or with clots larger than 2.5 cm (1 inch);",
                    "cycles regularly shorter than 24 days or longer than 38 days in adults (21 and 45 days in teenagers);",
                    "more than three months without a period, outside of pregnancy;",
                    "bleeding between periods or after sex;",
                    "unusual tiredness or breathlessness with heavy periods, a possible sign of iron deficiency.",
                ),
            ),
            Section(
                "Endometriosis",
                listOf(
                    "Endometriosis affects about one in ten people who menstruate. Depending on the study, it takes an average of 7 to 12 years to be diagnosed, often because the pain is dismissed. Very painful periods, pain during sex, pain when going to the toilet during your period: these signs are worth raising, and insisting on if you are not listened to.",
                ),
            ),
            Section(
                "Call emergency services or go to the emergency room",
                listOf(
                    "Sudden, very severe pain in the lower belly, especially if a pregnancy is possible.",
                    "Very heavy bleeding with fainting, dizziness or paleness.",
                    "Fever with pain in the lower belly.",
                    "Sudden fever, vomiting, a skin rash or feeling faint while wearing a tampon or a cup: remove it and call emergency services (15 or 112 in France), it may be toxic shock syndrome.",
                ),
            ),
        ),
        sources = listOf(Sources.FIGO_2018, Sources.MSD_PARAMETERS, Sources.ACOG_ADOLESCENTS_2015, Sources.ENDOMETRIOSIS_2024, Sources.ACOG_DYSMENORRHEA_2018),
    )

    val protection = Article(
        id = "protections",
        title = "Period products and toxic shock",
        summary = "Using tampons and cups the right way to limit a rare but serious risk.",
        sections = listOf(
            Section(
                "Menstrual toxic shock syndrome",
                listOf(
                    "It is a rare infection, caused by a toxin made by a bacterium, Staphylococcus aureus. It is linked to wearing an internal product, a tampon or a cup, for too long.",
                ),
            ),
            Section(
                "Good habits",
                listOf(
                    "Wash your hands before and after changing your period product.",
                    "Follow the maximum wearing time given in the leaflet, and do not exceed it at night: use a pad or period underwear for sleeping if needed.",
                    "Choose the lowest absorbency that suits your flow, and only use tampons during your period.",
                ),
            ),
            Section(
                "Warning signs",
                listOf(
                    "Sudden fever, vomiting or diarrhea, a rash that looks like sunburn, feeling faint or dizzy while wearing an internal product: remove it and call emergency services.",
                ),
            ),
        ),
        sources = listOf(Sources.ANSES_TSS_2025),
    )

    val predictions = Article(
        id = "previsions",
        title = "What predictions can tell you",
        summary = "Useful estimates for planning, not a method of contraception.",
        sections = listOf(
            Section(
                "A range, not a date",
                listOf(
                    "Even for someone whose periods run like clockwork, cycle length varies by a few days. So Lunaison shows a likely range of dates, which narrows as you log more cycles.",
                ),
            ),
            Section(
                "Not contraception",
                listOf(
                    "Ovulation cannot be seen: Lunaison estimates it from your periods, with a margin of error. Methods based on the cycle calendar often fail in practice: up to about one in four people become pregnant within a year of typical use. To avoid pregnancy, talk to a health professional or a sexual health clinic.",
                ),
            ),
            Section(
                "A late period",
                listOf(
                    "A late period can have many causes: stress, travel, illness, weight change, intense exercise, breastfeeding, pregnancy. If a pregnancy is possible, a urine test is usually reliable from the day your period was expected, and even more so a few days later.",
                ),
            ),
        ),
        sources = listOf(Sources.BULL_2019, Sources.TRUSSELL_2011),
    )

    val partners = Article(
        id = "entourage",
        title = "For partners, family and friends",
        summary = "Understanding what someone goes through during their cycle, and how to help without being intrusive.",
        sections = listOf(
            Section(
                "What to know",
                listOf(
                    "Periods and the days before them can come with very real pain, tiredness and mood changes. They are neither exaggerated nor \"all in the head\".",
                    "Every cycle is different, and so is every person. The only way to know what helps is to ask.",
                ),
            ),
            Section(
                "What often helps",
                listOf(
                    "Taking over everyday tasks, without waiting to be asked.",
                    "Offering a hot water bottle, a meal, some quiet, or simply your company.",
                    "Not commenting on their mood: \"you must be on your period\" hurts more than it helps.",
                ),
            ),
            Section(
                "What you receive",
                listOf(
                    "If someone sends you a message from Lunaison, it is because they chose to do so, at that moment. Nothing is shared automatically. Also respect the day they stop sharing anything: that is their right.",
                ),
            ),
        ),
        sources = emptyList(),
    )

    val all: List<Article> = listOf(cycle, pain, pms, food, consult, protection, predictions, partners)
}
