package fr.lunaison.core.content

/** Une référence publiée, citée dans un contenu de l'appli. */
data class Source(val label: String, val url: String)

/** Toutes les références de l'appli, réunies pour être relues d'un coup. Détail dans documentation/sources.md. */
object Sources {
    val FIGO_2018 = Source(
        "Munro MG et al. (FIGO), The two FIGO systems for normal and abnormal uterine bleeding symptoms, Int J Gynaecol Obstet, 2018",
        "https://doi.org/10.1002/ijgo.12666",
    )
    val BULL_2019 = Source(
        "Bull JR et al., Real-world menstrual cycle characteristics of more than 600,000 menstrual cycles, npj Digital Medicine, 2019",
        "https://doi.org/10.1038/s41746-019-0152-7",
    )
    val ACOG_ADOLESCENTS_2015 = Source(
        "ACOG, Committee Opinion n° 651, Menstruation in Girls and Adolescents: Using the Menstrual Cycle as a Vital Sign, 2015",
        "https://www.acog.org/clinical/clinical-guidance/committee-opinion/articles/2015/12/menstruation-in-girls-and-adolescents-using-the-menstrual-cycle-as-a-vital-sign",
    )
    val ACOG_DYSMENORRHEA_2018 = Source(
        "ACOG, Committee Opinion n° 760, Dysmenorrhea and Endometriosis in the Adolescent, 2018",
        "https://www.acog.org/clinical/clinical-guidance/committee-opinion/articles/2018/12/dysmenorrhea-and-endometriosis-in-the-adolescent",
    )
    val COCHRANE_NSAIDS_2015 = Source(
        "Marjoribanks J et al., Nonsteroidal anti-inflammatory drugs for dysmenorrhoea, Cochrane, 2015",
        "https://doi.org/10.1002/14651858.CD001751.pub3",
    )
    val COCHRANE_EXERCISE_2019 = Source(
        "Armour M et al., Exercise for dysmenorrhoea, Cochrane, 2019",
        "https://doi.org/10.1002/14651858.CD004142.pub4",
    )
    val HEAT_2018 = Source(
        "Jo J, Lee SH, Heat therapy for primary dysmenorrhea: a systematic review and meta-analysis, Scientific Reports, 2018",
        "https://doi.org/10.1038/s41598-018-34303-z",
    )
    val GINGER_2015 = Source(
        "Daily JW et al., Efficacy of ginger for alleviating the symptoms of primary dysmenorrhea: a systematic review and meta-analysis, Pain Medicine, 2015",
        "https://pubmed.ncbi.nlm.nih.gov/26177393/",
    )
    val OMEGA3_2022 = Source(
        "Mohammadi MM et al., The impact of omega-3 polyunsaturated fatty acids on primary dysmenorrhea, Eur J Clin Pharmacol, 2022",
        "https://pubmed.ncbi.nlm.nih.gov/35059756/",
    )
    val EFSA_IRON_2015 = Source(
        "EFSA, Scientific Opinion on Dietary Reference Values for iron, EFSA Journal, 2015",
        "https://www.efsa.europa.eu/en/efsajournal/pub/4254",
    )
    val CALCIUM_PMS_1998 = Source(
        "Thys-Jacobs S et al., Calcium carbonate and the premenstrual syndrome, Am J Obstet Gynecol, 1998",
        "https://doi.org/10.1016/S0002-9378(98)70377-1",
    )
    val ENDOMETRIOSIS_2024 = Source(
        "Kvaskoff M (Inserm), Endométriose : état des connaissances épidémiologiques, Questions de santé publique n° 48, 2024",
        "https://doi.org/10.1051/qsp/2024048",
    )
    val MSD_PARAMETERS = Source(
        "Manuel MSD, Paramètres menstruels normaux (d'après la FIGO)",
        "https://www.msdmanuals.com/professional/multimedia/table/normal-menstrual-parameters",
    )
    val ANSES_TSS_2025 = Source(
        "Anses, Protections intimes : composition et choc toxique, toutes nos recommandations, 2025",
        "https://www.anses.fr/fr/content/protections-intimes-composition-et-choc-toxique-toutes-nos-recommandations",
    )
    val TRUSSELL_2011 = Source(
        "Trussell J, Contraceptive failure in the United States, Contraception, 2011",
        "https://doi.org/10.1016/j.contraception.2011.01.021",
    )

    val all: List<Source> = listOf(
        FIGO_2018, BULL_2019, ACOG_ADOLESCENTS_2015, ACOG_DYSMENORRHEA_2018, COCHRANE_NSAIDS_2015,
        COCHRANE_EXERCISE_2019, HEAT_2018, GINGER_2015, OMEGA3_2022, EFSA_IRON_2015, CALCIUM_PMS_1998,
        ENDOMETRIOSIS_2024, MSD_PARAMETERS, ANSES_TSS_2025, TRUSSELL_2011,
    )
}
