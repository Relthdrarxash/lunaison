package fr.lunaison.core.content

import fr.lunaison.core.model.Language

data class Section(val heading: String, val paragraphs: List<String>)

data class Article(
    val id: String,
    val title: String,
    val summary: String,
    val sections: List<Section>,
    val sources: List<Source>,
)

/** Fiches de la rubrique « Comprendre », dans la langue de l'appli. */
object Articles {
    val all: List<Article> get() = forLanguage(Language.current())

    fun forLanguage(language: Language): List<Article> = when (language) {
        Language.FR -> ArticlesFr.all
        Language.EN -> ArticlesEn.all
        Language.ES -> ArticlesEs.all
        Language.DE -> ArticlesDe.all
        Language.PL -> ArticlesPl.all
        Language.UK -> ArticlesUk.all
    }

    fun byId(id: String): Article? = all.firstOrNull { it.id == id }
}

/** Fiches en français. Vouvoiement, phrases courtes, une idée par phrase, pas de tiret cadratin. */
internal object ArticlesFr {

    val cycle = Article(
        id = "cycle",
        title = "Comprendre son cycle",
        summary = "Ce qui se passe pendant un cycle, et pourquoi le vôtre ne ressemble pas forcément à celui des manuels.",
        sections = listOf(
            Section(
                "Un cycle, c'est quoi ?",
                listOf(
                    "Un cycle va du premier jour des règles au premier jour des règles suivantes. Le premier jour, c'est le premier jour de vrai saignement : de simples traces ne comptent pas.",
                    "Pendant un cycle, deux hormones produites par les ovaires, les œstrogènes et la progestérone, montent et descendent. Elles préparent la paroi de l'utérus à une éventuelle grossesse. Sans grossesse, cette paroi s'élimine : ce sont les règles.",
                ),
            ),
            Section(
                "Les quatre phases",
                listOf(
                    "Les règles : la paroi de l'utérus s'élimine. Les hormones sont au plus bas.",
                    "La phase folliculaire : un follicule mûrit dans un ovaire et les œstrogènes montent.",
                    "L'ovulation : l'ovaire libère un ovule. Elle n'a pas lieu à date fixe.",
                    "La phase lutéale : la progestérone domine jusqu'aux règles suivantes. Les symptômes prémenstruels apparaissent souvent à la fin de cette phase.",
                ),
            ),
            Section(
                "Le cycle de 28 jours est une moyenne, pas une norme",
                listOf(
                    "Une étude de plus de 600 000 cycles a trouvé une durée moyenne de 29,3 jours, et seulement 13 % de cycles de 28 jours exactement. L'ovulation n'a pas lieu au 14e jour pour tout le monde : c'est surtout la phase avant l'ovulation qui varie d'une personne à l'autre.",
                    "Chez l'adulte, un cycle de 24 à 38 jours et des règles de 8 jours au plus sont dans les repères habituels. D'un cycle à l'autre, un écart de quelques jours est normal.",
                    "À l'adolescence, les premières années, les cycles sont souvent irréguliers : la plupart durent entre 21 et 45 jours.",
                ),
            ),
            Section(
                "Comment Lunaison fait ses estimations",
                listOf(
                    "Lunaison observe vos règles et calcule la durée habituelle de vos derniers cycles. Plus vous notez de cycles, plus l'estimation devient fiable. Les autres phases sont déduites : l'ovulation est estimée environ 13 jours avant les règles suivantes, car cette dernière partie du cycle varie peu.",
                    "Ces estimations aident à s'organiser. Elles ne permettent pas d'éviter une grossesse.",
                ),
            ),
        ),
        sources = listOf(Sources.BULL_2019, Sources.FIGO_2018, Sources.ACOG_ADOLESCENTS_2015),
    )

    val pain = Article(
        id = "douleurs",
        title = "Soulager les douleurs de règles",
        summary = "Ce qui a fait ses preuves, ce qui peut aider, et quand la douleur doit amener à consulter.",
        sections = listOf(
            Section(
                "D'où vient la douleur",
                listOf(
                    "Pendant les règles, l'utérus produit des prostaglandines. Ces substances le font se contracter pour évacuer sa paroi, et ces contractions font mal. C'est pour ça que les médicaments qui bloquent les prostaglandines sont efficaces.",
                ),
            ),
            Section(
                "Les anti-inflammatoires",
                listOf(
                    "Les anti-inflammatoires non stéroïdiens, comme l'ibuprofène, soulagent mieux que le paracétamol. Dans les essais, ils apportaient un soulagement net à environ la moitié des personnes, contre moins d'une sur cinq avec un placebo.",
                    "Ils marchent mieux pris dès le début des douleurs, voire dès le début des règles. Respectez la notice. Demandez conseil à un pharmacien ou un médecin en cas de problème d'estomac, de rein, d'asthme, de grossesse possible ou d'autre traitement.",
                ),
            ),
            Section(
                "La chaleur",
                listOf(
                    "Une bouillotte ou un patch chauffant sur le bas du ventre soulage les douleurs. Dans les études, l'effet était comparable à celui des anti-inflammatoires, même si les essais restent peu nombreux. Attention aux brûlures : jamais de chaleur trop forte directement sur la peau.",
                ),
            ),
            Section(
                "Bouger",
                listOf(
                    "Une activité physique régulière, environ trois séances de 45 minutes à une heure par semaine, pourrait réduire nettement l'intensité des douleurs de règles. Les études sont de qualité modeste, mais le risque est faible et les autres bienfaits sont nombreux. Marche, natation, yoga : choisissez ce qui vous convient.",
                ),
            ),
            Section(
                "Le gingembre",
                listOf(
                    "Quelques petits essais suggèrent que le gingembre en poudre, pris les premiers jours des règles, réduit la douleur. Les preuves sont faibles. Ça peut valoir un essai, sans remplacer un traitement efficace.",
                ),
            ),
            Section(
                "Quand consulter",
                listOf(
                    "Des règles douloureuses sont fréquentes, mais une douleur qui vous empêche d'aller en cours ou au travail, qui résiste aux anti-inflammatoires ou qui s'aggrave avec les années n'est pas normale. Elle peut révéler une endométriose ou une autre cause qui se soigne.",
                    "Parlez-en à votre médecin, une sage-femme ou un gynécologue. Notez vos douleurs dans Lunaison : un historique précis aide beaucoup en consultation.",
                ),
            ),
        ),
        sources = listOf(
            Sources.COCHRANE_NSAIDS_2015,
            Sources.HEAT_2018,
            Sources.COCHRANE_EXERCISE_2019,
            Sources.GINGER_2015,
            Sources.ACOG_DYSMENORRHEA_2018,
            Sources.ESHRE_ENDOMETRIOSIS_2022,
        ),
    )

    val food = Article(
        id = "alimentation",
        title = "Alimentation et règles",
        summary = "Le fer avant tout. Pour le reste, des pistes modestes et pas de régime miracle.",
        sections = listOf(
            Section(
                "Le fer",
                listOf(
                    "Les règles font perdre du fer. Chez les personnes réglées, les besoins sont plus élevés : l'autorité européenne de sécurité des aliments retient 16 mg par jour comme apport de référence. Des règles abondantes peuvent conduire à un manque de fer, qui fatigue et essouffle.",
                    "Le fer le mieux absorbé se trouve dans la viande, le poisson et les fruits de mer. Les légumineuses (lentilles, pois chiches), le tofu, les céréales complètes et les légumes verts en contiennent aussi. Associez-les à une source de vitamine C (agrumes, poivron, kiwi) pour mieux l'absorber, et évitez le thé ou le café pendant ces repas.",
                    "Ne prenez pas de complément de fer sans avis médical : une simple prise de sang dit s'il en faut.",
                ),
            ),
            Section(
                "Les oméga-3",
                listOf(
                    "Les oméga-3, présents dans les poissons gras (sardine, maquereau, saumon), les noix et l'huile de colza, ont réduit les douleurs de règles dans plusieurs essais. L'effet reste modeste et les études sont de qualité variable. Les inclure dans une alimentation variée ne coûte rien.",
                ),
            ),
            Section(
                "Le calcium",
                listOf(
                    "Un essai a montré qu'un apport de calcium réduisait les symptômes prémenstruels. Produits laitiers, eaux riches en calcium, amandes, légumes verts et tofu en sont de bonnes sources.",
                ),
            ),
            Section(
                "Ce qu'on ne sait pas",
                listOf(
                    "Aucun aliment ne fait disparaître les règles douloureuses, et les régimes qui adaptent tout son menu à chaque phase du cycle ne reposent pas sur des études solides. Une alimentation variée, assez d'eau et des repas réguliers restent la meilleure base.",
                ),
            ),
        ),
        sources = listOf(Sources.EFSA_IRON_2015, Sources.OMEGA3_2022, Sources.CALCIUM_PMS_1998, Sources.GINGER_2015),
    )

    val pms = Article(
        id = "spm",
        title = "Le syndrome prémenstruel",
        summary = "Les symptômes des jours qui précèdent les règles, ce qui peut aider et quand demander de l'aide.",
        sections = listOf(
            Section(
                "De quoi parle-t-on",
                listOf(
                    "Dans les jours avant les règles, beaucoup de personnes ressentent des seins sensibles, un ventre gonflé, de la fatigue, de l'irritabilité ou un moral plus bas. Ces symptômes disparaissent en général dans les premiers jours des règles.",
                    "Les noter dans Lunaison permet de voir s'ils reviennent à chaque cycle. L'accueil vous montre ce qui revient souvent pendant la phase en cours.",
                ),
            ),
            Section(
                "Ce qui peut aider",
                listOf(
                    "Une activité physique régulière, un sommeil suffisant et des repas réguliers aident souvent. Un apport suffisant en calcium a réduit les symptômes dans un essai.",
                    "Savoir que la période arrive aide aussi : prévoir moins de choses ces jours-là, ou prévenir son entourage, évite bien des tensions.",
                ),
            ),
            Section(
                "Quand demander de l'aide",
                listOf(
                    "Si ces jours-là votre moral s'effondre, si l'anxiété ou la colère abîment vos relations ou votre travail, parlez-en à un médecin. Une forme sévère, le trouble dysphorique prémenstruel, se soigne. Et si des idées noires apparaissent, n'attendez pas : appelez le 3114, numéro national de prévention du suicide, gratuit et ouvert jour et nuit.",
                ),
            ),
        ),
        sources = listOf(Sources.CALCIUM_PMS_1998, Sources.COCHRANE_EXERCISE_2019),
    )

    val consult = Article(
        id = "consulter",
        title = "Quand consulter",
        summary = "Les repères qui doivent amener à demander un avis médical, et les situations urgentes.",
        sections = listOf(
            Section(
                "Prendre rendez-vous",
                listOf(
                    "Avec votre médecin, une sage-femme ou un gynécologue, si vous remarquez :",
                    "des douleurs qui vous empêchent de faire vos activités ou qui résistent aux anti-inflammatoires ;",
                    "des règles qui durent plus de 8 jours, ou si abondantes que vous devez changer de protection toutes les 1 à 2 heures, ou avec des caillots de plus de 2,5 cm environ ;",
                    "des cycles régulièrement plus courts que 24 jours ou plus longs que 38 jours chez l'adulte (21 et 45 jours à l'adolescence) ;",
                    "plus de trois mois sans règles, hors grossesse ;",
                    "des saignements entre les règles ou après un rapport ;",
                    "une fatigue inhabituelle ou un essoufflement avec des règles abondantes, signe possible d'un manque de fer.",
                ),
            ),
            Section(
                "L'endométriose",
                listOf(
                    "L'endométriose touche environ une personne réglée sur dix. Selon les études, il faut en moyenne 7 à 12 ans pour la diagnostiquer, souvent parce que la douleur est banalisée. Règles très douloureuses, douleurs pendant les rapports, en allant aux toilettes pendant les règles : ces signes justifient d'en parler et d'insister si on ne vous écoute pas.",
                ),
            ),
            Section(
                "Appeler le 15 ou aller aux urgences",
                listOf(
                    "Une douleur brutale et très forte dans le bas du ventre, surtout si une grossesse est possible.",
                    "Un saignement très abondant avec malaise, vertiges ou pâleur.",
                    "De la fièvre avec des douleurs dans le bas du ventre.",
                    "Une fièvre brutale, des vomissements, une éruption sur la peau ou un malaise pendant le port d'un tampon ou d'une coupe : retirez-le et appelez le 15, c'est peut-être un choc toxique.",
                ),
            ),
        ),
        sources = listOf(
            Sources.FIGO_2018,
            Sources.MSD_PARAMETERS,
            Sources.NHS_HEAVY_PERIODS,
            Sources.ACOG_ADOLESCENTS_2015,
            Sources.ENDOMETRIOSIS_2024,
            Sources.ACOG_DYSMENORRHEA_2018,
            Sources.ESHRE_ENDOMETRIOSIS_2022,
            Sources.PTGIP_ENDOMETRIOSIS_2024,
            Sources.AWMF_DGGG_ENDOMETRIOSIS_2020,
            Sources.SANIDAD_ES_ENDOMETRIOSIS_2013,
            Sources.MOZ_UA_ENDOMETRIOSIS_2016,
        ),
    )

    val protection = Article(
        id = "protections",
        title = "Protections et choc toxique",
        summary = "Bien utiliser tampons et coupes pour limiter un risque rare mais grave.",
        sections = listOf(
            Section(
                "Le choc toxique menstruel",
                listOf(
                    "C'est une infection rare, due à une toxine produite par une bactérie, le staphylocoque doré. Elle est liée au port prolongé d'une protection interne, tampon ou coupe.",
                ),
            ),
            Section(
                "Les bons gestes",
                listOf(
                    "Lavez-vous les mains avant et après avoir changé de protection.",
                    "Respectez la durée de port maximale indiquée sur la notice, et ne la dépassez pas la nuit : préférez une serviette ou une culotte menstruelle pour dormir si besoin.",
                    "Choisissez l'absorption la plus faible adaptée à votre flux, et n'utilisez les tampons que pendant les règles.",
                ),
            ),
            Section(
                "Les signes d'alerte",
                listOf(
                    "Fièvre brutale, vomissements ou diarrhée, éruption qui ressemble à un coup de soleil, malaise ou vertiges pendant le port d'une protection interne : retirez-la et appelez le 15.",
                ),
            ),
        ),
        sources = listOf(Sources.ANSES_TSS_2025),
    )

    val predictions = Article(
        id = "previsions",
        title = "Ce que les prévisions peuvent dire",
        summary = "Des estimations utiles pour s'organiser, pas une méthode de contraception.",
        sections = listOf(
            Section(
                "Une plage, pas une date",
                listOf(
                    "Même chez une personne réglée comme une horloge, la durée du cycle varie de quelques jours. Lunaison affiche donc une plage de dates probable, qui se resserre à mesure que vous notez des cycles.",
                ),
            ),
            Section(
                "Pas une contraception",
                listOf(
                    "L'ovulation ne se voit pas : Lunaison l'estime à partir de vos règles, avec une marge d'erreur. Les méthodes fondées sur le calendrier du cycle échouent souvent en pratique : jusqu'à environ une personne sur quatre tombe enceinte en un an d'utilisation courante. Pour éviter une grossesse, parlez-en à un professionnel de santé ou à un centre de santé sexuelle.",
                ),
            ),
            Section(
                "Un retard",
                listOf(
                    "Un retard peut avoir de nombreuses causes : stress, voyage, maladie, changement de poids, sport intense, allaitement, grossesse. Si une grossesse est possible, un test urinaire est en général fiable à partir du jour où les règles étaient attendues, et encore plus quelques jours après.",
                ),
            ),
        ),
        sources = listOf(Sources.BULL_2019, Sources.TRUSSELL_2011),
    )

    val partners = Article(
        id = "entourage",
        title = "Pour l'entourage",
        summary = "Comprendre ce que vit une personne pendant son cycle, et comment l'aider sans être intrusif.",
        sections = listOf(
            Section(
                "Ce qu'il faut savoir",
                listOf(
                    "Les règles et les jours qui les précèdent peuvent s'accompagner de douleurs, de fatigue et de changements d'humeur bien réels. Ils ne sont ni exagérés ni « dans la tête ».",
                    "Chaque cycle est différent, et chaque personne aussi. La seule façon de savoir ce qui aide, c'est de demander.",
                ),
            ),
            Section(
                "Ce qui aide souvent",
                listOf(
                    "Prendre le relais sur les tâches du quotidien, sans attendre qu'on vous le demande.",
                    "Proposer une bouillotte, un repas, du calme, ou simplement de la présence.",
                    "Ne pas commenter l'humeur : « tu dois avoir tes règles » blesse plus qu'il n'aide.",
                ),
            ),
            Section(
                "Ce que vous recevez",
                listOf(
                    "Si une personne vous envoie un message depuis Lunaison, c'est qu'elle a choisi de le faire, à ce moment-là. Rien n'est partagé automatiquement. Respectez aussi le jour où elle ne partage plus rien : c'est son droit.",
                ),
            ),
        ),
        sources = emptyList(),
    )

    val all: List<Article> = listOf(cycle, pain, pms, food, consult, protection, predictions, partners)
}
