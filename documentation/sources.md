# Sources et choix de conception

L'appli reste au strict nécessaire : ce document explique ce qu'elle ne dit pas elle-même, à savoir d'où viennent ses repères et pourquoi elle est faite ainsi. Il est publié sur le site : [Sources et choix de conception](https://relthdrarxash.github.io/lunaison/sources.html).

Chaque affirmation de santé de l'appli renvoie à l'une de ces références. Elles sont aussi déclarées dans [`Sources.kt`](../core/content/src/main/kotlin/fr/lunaison/core/content/Sources.kt), et un test vérifie que chaque fiche en cite au moins une.

## Le cycle et ses repères

| Référence | Utilisée pour |
|---|---|
| Munro MG, Critchley HOD, Fraser IS (FIGO). *The two FIGO systems for normal and abnormal uterine bleeding symptoms and classification of causes of abnormal uterine bleeding in the reproductive years: 2018 revisions.* Int J Gynaecol Obstet, 2018;143(3):393-408. [doi:10.1002/ijgo.12666](https://doi.org/10.1002/ijgo.12666) | Repères adultes : cycle de 24 à 38 jours, règles de 8 jours au plus, écart entre cycles de 7 à 9 jours au plus |
| Manuel MSD, *Normal Menstrual Parameters* (d'après la FIGO). [Lien](https://www.msdmanuals.com/professional/multimedia/table/normal-menstrual-parameters) | Signes de règles abondantes : protection changée en moins de 3 heures, caillots de plus de 2,5 cm |
| Bull JR et al. *Real-world menstrual cycle characteristics of more than 600,000 menstrual cycles.* npj Digital Medicine, 2019;2:83. [doi:10.1038/s41746-019-0152-7](https://doi.org/10.1038/s41746-019-0152-7) | Cycle moyen de 29,3 jours, 13 % de cycles de 28 jours, phase lutéale moyenne de 12,4 jours, variation moyenne de 2,6 jours d'un cycle à l'autre. Étude financée par Natural Cycles |
| ACOG, Committee Opinion n° 651. *Menstruation in Girls and Adolescents: Using the Menstrual Cycle as a Vital Sign*, 2015. [Lien](https://www.acog.org/clinical/clinical-guidance/committee-opinion/articles/2015/12/menstruation-in-girls-and-adolescents-using-the-menstrual-cycle-as-a-vital-sign) | Repères à l'adolescence : cycles de 21 à 45 jours, avis après 90 jours sans règles |

## Douleurs

| Référence | Utilisée pour |
|---|---|
| Marjoribanks J et al. *Nonsteroidal anti-inflammatory drugs for dysmenorrhoea.* Cochrane, 2015. [doi:10.1002/14651858.CD001751.pub3](https://doi.org/10.1002/14651858.CD001751.pub3) | Anti-inflammatoires plus efficaces que le placebo (45 à 53 % de soulagement contre 18 %) et que le paracétamol |
| Jo J, Lee SH. *Heat therapy for primary dysmenorrhea.* Scientific Reports, 2018;8:16252. [doi:10.1038/s41598-018-34303-z](https://doi.org/10.1038/s41598-018-34303-z) | Chaleur efficace, effet comparable aux anti-inflammatoires, preuves limitées |
| Armour M et al. *Exercise for dysmenorrhoea.* Cochrane, 2019. [doi:10.1002/14651858.CD004142.pub4](https://doi.org/10.1002/14651858.CD004142.pub4) | Activité physique régulière (45 à 60 minutes, trois fois par semaine) |
| Daily JW et al. *Efficacy of ginger for alleviating the symptoms of primary dysmenorrhea.* Pain Medicine, 2015;16(12):2243-55. [PubMed 26177393](https://pubmed.ncbi.nlm.nih.gov/26177393/) | Gingembre, preuves faibles |
| ACOG, Committee Opinion n° 760. *Dysmenorrhea and Endometriosis in the Adolescent*, 2018. [Lien](https://www.acog.org/clinical/clinical-guidance/committee-opinion/articles/2018/12/dysmenorrhea-and-endometriosis-in-the-adolescent) | Douleur qui empêche les activités ou résiste aux traitements : consulter |
| Kvaskoff M (Inserm). *Endométriose : état des connaissances épidémiologiques.* Questions de santé publique n° 48, 2024. [doi:10.1051/qsp/2024048](https://doi.org/10.1051/qsp/2024048) | Environ 10 % des personnes réglées, délai de diagnostic de 7 à 12 ans |

## Alimentation et syndrome prémenstruel

| Référence | Utilisée pour |
|---|---|
| EFSA. *Scientific Opinion on Dietary Reference Values for iron.* EFSA Journal, 2015;13(10):4254. [Lien](https://www.efsa.europa.eu/en/efsajournal/pub/4254) | Apport de référence en fer de 16 mg par jour avant la ménopause |
| Mohammadi MM et al. *The impact of omega-3 polyunsaturated fatty acids on primary dysmenorrhea.* Eur J Clin Pharmacol, 2022. [PubMed 35059756](https://pubmed.ncbi.nlm.nih.gov/35059756/) | Oméga-3, effet modeste |
| Thys-Jacobs S et al. *Calcium carbonate and the premenstrual syndrome.* Am J Obstet Gynecol, 1998;179(2):444-52. [doi:10.1016/S0002-9378(98)70377-1](https://doi.org/10.1016/S0002-9378(98)70377-1) | Calcium et symptômes prémenstruels |

## Protections et contraception

| Référence | Utilisée pour |
|---|---|
| Anses. *Protections intimes : composition et choc toxique, toutes nos recommandations*, 2025. [Lien](https://www.anses.fr/fr/content/protections-intimes-composition-et-choc-toxique-toutes-nos-recommandations) | Bons gestes avec tampons et coupes |
| Trussell J. *Contraceptive failure in the United States.* Contraception, 2011;83(5):397-404. [doi:10.1016/j.contraception.2011.01.021](https://doi.org/10.1016/j.contraception.2011.01.021) | Méthodes fondées sur le calendrier : environ une grossesse sur quatre en un an d'usage courant |

## Ce qui n'est pas tiré d'une étude

- La fiche « Pour l'entourage » donne des conseils de bon sens, sans référence.
- Quelques conseils du jour relèvent du bon sens ou de l'usage de l'appli : prévoir une protection en fin de cycle, alléger les jours chargés, parler à un médecin si le moral s'effondre avant les règles, noter son humeur, ajouter des règles oubliées, faire un test de grossesse en cas de retard. Ils sont marqués `withoutStudy` dans [`PhaseGuides.kt`](../core/content/src/main/kotlin/fr/lunaison/core/content/PhaseGuides.kt) ; un test vérifie que tous les autres citent une source.
- Les seuils des signaux (deux règles sur les trois dernières avec des douleurs fortes, trois jours abondants ou plus) sont des choix prudents de l'appli, pas des critères cliniques. Ils servent à suggérer un avis, jamais à poser un diagnostic.

## Sources par fiche

Dans l'appli, chaque fiche renvoie vers cette partie, sans liste de références dans le texte.

### Comprendre son cycle {#fiche-cycle}

- [Bull JR et al., 2019](https://doi.org/10.1038/s41746-019-0152-7)
- [Munro MG et al. (FIGO), 2018](https://doi.org/10.1002/ijgo.12666)
- [ACOG n° 651, 2015](https://www.acog.org/clinical/clinical-guidance/committee-opinion/articles/2015/12/menstruation-in-girls-and-adolescents-using-the-menstrual-cycle-as-a-vital-sign)

### Soulager les douleurs de règles {#fiche-douleurs}

- [Marjoribanks J et al., 2015](https://doi.org/10.1002/14651858.CD001751.pub3)
- [Jo J et Lee SH, 2018](https://doi.org/10.1038/s41598-018-34303-z)
- [Armour M et al., 2019](https://doi.org/10.1002/14651858.CD004142.pub4)
- [Daily JW et al., 2015](https://pubmed.ncbi.nlm.nih.gov/26177393/)
- [ACOG n° 760, 2018](https://www.acog.org/clinical/clinical-guidance/committee-opinion/articles/2018/12/dysmenorrhea-and-endometriosis-in-the-adolescent)

### Alimentation et règles {#fiche-alimentation}

- [EFSA, 2015](https://www.efsa.europa.eu/en/efsajournal/pub/4254)
- [Mohammadi MM et al., 2022](https://pubmed.ncbi.nlm.nih.gov/35059756/)
- [Thys-Jacobs S et al., 1998](https://doi.org/10.1016/S0002-9378(98)70377-1)
- [Daily JW et al., 2015](https://pubmed.ncbi.nlm.nih.gov/26177393/)

### Le syndrome prémenstruel {#fiche-spm}

- [Thys-Jacobs S et al., 1998](https://doi.org/10.1016/S0002-9378(98)70377-1)
- [Armour M et al., 2019](https://doi.org/10.1002/14651858.CD004142.pub4)

### Quand consulter {#fiche-consulter}

- [Munro MG et al. (FIGO), 2018](https://doi.org/10.1002/ijgo.12666)
- [Manuel MSD](https://www.msdmanuals.com/professional/multimedia/table/normal-menstrual-parameters)
- [ACOG n° 651, 2015](https://www.acog.org/clinical/clinical-guidance/committee-opinion/articles/2015/12/menstruation-in-girls-and-adolescents-using-the-menstrual-cycle-as-a-vital-sign)
- [Kvaskoff M (Inserm), 2024](https://doi.org/10.1051/qsp/2024048)
- [ACOG n° 760, 2018](https://www.acog.org/clinical/clinical-guidance/committee-opinion/articles/2018/12/dysmenorrhea-and-endometriosis-in-the-adolescent)

### Protections et choc toxique {#fiche-protections}

- [Anses, 2025](https://www.anses.fr/fr/content/protections-intimes-composition-et-choc-toxique-toutes-nos-recommandations)

### Ce que les prévisions peuvent dire {#fiche-previsions}

- [Bull JR et al., 2019](https://doi.org/10.1038/s41746-019-0152-7)
- [Trussell J, 2011](https://doi.org/10.1016/j.contraception.2011.01.021)

## Sources des conseils du jour

L'accueil affiche un conseil par jour, sans sa source. Les voici toutes, phase par phase.

### Règles {#conseils-regles}

| Conseil | Source |
|---|---|
| La chaleur sur le bas du ventre soulage autant qu'un anti-inflammatoire dans les études. Attention aux brûlures : jamais de chaleur trop forte directement sur la peau. | [Jo J et Lee SH, 2018](https://doi.org/10.1038/s41598-018-34303-z) |
| Un anti-inflammatoire comme l'ibuprofène soulage mieux que le paracétamol, surtout pris dès le début des douleurs. Respectez la notice. | [Marjoribanks J et al., 2015](https://doi.org/10.1002/14651858.CD001751.pub3) |
| Une douleur qui vous empêche de faire vos activités n'est pas à supporter : elle mérite un avis médical. | [ACOG n° 760, 2018](https://www.acog.org/clinical/clinical-guidance/committee-opinion/articles/2018/12/dysmenorrhea-and-endometriosis-in-the-adolescent) |
| Les règles font perdre du fer. Légumineuses, viande ou poisson, avec une source de vitamine C comme un agrume ou un kiwi, aident à le compenser. | [EFSA, 2015](https://www.efsa.europa.eu/en/efsajournal/pub/4254) |
| Le thé et le café gênent l'absorption du fer : mieux vaut les prendre à distance des repas. | [EFSA, 2015](https://www.efsa.europa.eu/en/efsajournal/pub/4254) |
| Si vous changez de protection en moins de trois heures, ou si vous voyez des caillots de plus de 2,5 cm, parlez-en à un professionnel de santé. | [Manuel MSD](https://www.msdmanuals.com/professional/multimedia/table/normal-menstrual-parameters) |
| Tampon ou coupe : respectez la durée de port indiquée sur la notice. Pour la nuit, une serviette ou une culotte menstruelle évite de la dépasser. | [Anses, 2025](https://www.anses.fr/fr/content/protections-intimes-composition-et-choc-toxique-toutes-nos-recommandations) |
| Quelques petits essais suggèrent que le gingembre en poudre réduit la douleur les premiers jours. Les preuves sont faibles. | [Daily JW et al., 2015](https://pubmed.ncbi.nlm.nih.gov/26177393/) |

### Phase folliculaire {#conseils-folliculaire}

| Conseil | Source |
|---|---|
| Une activité physique régulière, environ trois séances par semaine, peut réduire les douleurs des règles suivantes. Les études sont modestes, mais le risque est faible. | [Armour M et al., 2019](https://doi.org/10.1002/14651858.CD004142.pub4) |
| Marche, natation, yoga : l'effet sur les douleurs de règles a été observé avec des activités variées. Choisissez celle qui vous convient. | [Armour M et al., 2019](https://doi.org/10.1002/14651858.CD004142.pub4) |
| Les poissons gras, les noix et l'huile de colza apportent des oméga-3, qui ont un peu réduit les douleurs de règles dans plusieurs essais. | [Mohammadi MM et al., 2022](https://pubmed.ncbi.nlm.nih.gov/35059756/) |
| Après des règles abondantes, des repas riches en fer aident à refaire ses réserves. Un complément ne se prend que sur avis médical. | [EFSA, 2015](https://www.efsa.europa.eu/en/efsajournal/pub/4254) |
| Le cycle de 28 jours est une moyenne : dans une étude de plus de 600 000 cycles, seuls 13 % duraient exactement 28 jours. | [Bull JR et al., 2019](https://doi.org/10.1038/s41746-019-0152-7) |
| Notez votre humeur et votre énergie quelques jours : Lunaison vous montrera ce qui revient vraiment d'un cycle à l'autre. | Sans étude : bon sens ou usage de l’appli |

### Autour de l'ovulation {#conseils-ovulation}

| Conseil | Source |
|---|---|
| Cette période est estimée à partir de vos règles, avec une marge d'erreur : elle ne permet pas d'éviter une grossesse. | [Trussell J, 2011](https://doi.org/10.1016/j.contraception.2011.01.021) |
| L'ovulation n'a pas lieu au 14e jour pour tout le monde : elle arrive en général 12 à 13 jours avant les règles suivantes. | [Bull JR et al., 2019](https://doi.org/10.1038/s41746-019-0152-7) |
| La durée du cycle varie en moyenne de 2 à 3 jours d'un mois à l'autre, même chez une personne régulière. L'estimation de l'ovulation varie d'autant. | [Bull JR et al., 2019](https://doi.org/10.1038/s41746-019-0152-7) |
| Les méthodes fondées sur le calendrier du cycle échouent souvent : jusqu'à environ une personne sur quatre tombe enceinte en un an. Un centre de santé sexuelle peut vous aider à choisir une contraception. | [Trussell J, 2011](https://doi.org/10.1016/j.contraception.2011.01.021) |
| Une activité physique régulière peut réduire les douleurs des règles à venir. Il n'est jamais trop tard dans le cycle pour s'y mettre. | [Armour M et al., 2019](https://doi.org/10.1002/14651858.CD004142.pub4) |

### Phase lutéale {#conseils-luteale}

| Conseil | Source |
|---|---|
| Produits laitiers, eaux riches en calcium ou amandes : un apport suffisant en calcium a réduit les symptômes prémenstruels dans un essai. | [Thys-Jacobs S et al., 1998](https://doi.org/10.1016/S0002-9378(98)70377-1) |
| Si vos règles sont douloureuses, un anti-inflammatoire marche mieux pris dès les premières douleurs : prévoyez-en un à portée de main. | [Marjoribanks J et al., 2015](https://doi.org/10.1002/14651858.CD001751.pub3) |
| Une activité physique régulière peut aussi atténuer les douleurs des règles qui arrivent. | [Armour M et al., 2019](https://doi.org/10.1002/14651858.CD004142.pub4) |
| Les oméga-3 des poissons gras, des noix ou de l'huile de colza ont un peu réduit les douleurs de règles dans plusieurs essais. | [Mohammadi MM et al., 2022](https://pubmed.ncbi.nlm.nih.gov/35059756/) |
| Si les jours qui viennent s'annoncent chargés, allégez ce qui peut l'être, ou prévenez votre entourage. | Sans étude : bon sens ou usage de l’appli |
| Si ces jours-là votre moral s'effondre ou si l'anxiété abîme vos relations, parlez-en à un médecin : une forme sévère du syndrome prémenstruel se soigne. | Sans étude : bon sens ou usage de l’appli |
| Vos règles sont attendues dans une plage de dates : gardez une protection sur vous les derniers jours. | Sans étude : bon sens ou usage de l’appli |

### Règles en retard {#conseils-retard}

| Conseil | Source |
|---|---|
| Si elles ont commencé et que vous l'avez oublié, ajoutez-les dans le calendrier. | Sans étude : bon sens ou usage de l’appli |
| Si une grossesse est possible, un test urinaire est en général fiable dès maintenant. | Sans étude : bon sens ou usage de l’appli |
| Sans règles depuis plus de trois mois, demandez un avis médical. | [ACOG n° 651, 2015](https://www.acog.org/clinical/clinical-guidance/committee-opinion/articles/2015/12/menstruation-in-girls-and-adolescents-using-the-menstrual-cycle-as-a-vital-sign) |

## Choix de conception

Certains s'appuient sur une des références ci-dessus, d'autres sur le bon sens. Aucun n'a été évalué dans l'appli elle-même, et ils peuvent changer si les retours montrent qu'ils gênent.

- **Une plage plutôt qu'une date.** La durée du cycle varie de quelques jours d'un mois à l'autre, même chez une personne régulière (Bull et al., 2019). L'appli affiche donc une plage, calculée sur les six derniers cycles, qui se resserre quand l'historique grandit.
- **L'ovulation estimée à partir des règles suivantes, pas au 14e jour.** La phase après l'ovulation est assez stable alors que celle d'avant varie beaucoup (Bull et al., 2019). L'appli indique cette période pour comprendre son cycle, jamais comme des jours « sans risque » : les méthodes fondées sur le calendrier échouent souvent (Trussell, 2011).
- **La douleur décrite par son effet, pas par une note sur 10.** « Légère », « Gênante », « Forte : elle m'empêche de faire mes activités ». Le niveau « Forte » reprend le critère qui justifie un avis médical : une douleur qui empêche les activités (ACOG, 2018). Une note chiffrée varie d'une personne à l'autre et ne dit pas ce que la douleur empêche.
- **Des conseils par phase sans injonction.** L'appli dit ce qui se passe et ce qui aide. Elle ne dit pas qu'une phase « doit » se vivre d'une certaine façon : adapter tout son quotidien à chaque phase ne repose pas sur des études solides.
- **Des repères adaptés avant 18 ans.** Les cycles sont plus variables les premières années (ACOG, 2015) : les seuils des signaux en tiennent compte.
- **Le partage réservé aux 18 ans et plus, et envoyé par la personne.** Il s'agit d'informations intimes : l'appli prépare un message, la personne le relit et appuie elle-même sur Envoyer. Il n'y a ni serveur, ni envoi automatique.
- **Pas de sauvegarde dans le nuage.** Les données de santé ne quittent pas le téléphone du fait de l'appli. Un export au format tableur permet de les garder ailleurs, à l'endroit choisi par la personne.
- **Des rappels discrets.** Sur l'écran verrouillé, seul le nom de l'appli s'affiche.
- **Un seul conseil par jour.** L'accueil montre un conseil, tiré de la banque de la phase en cours (5 à 8 conseils sourcés par phase). Il change chaque jour et ne revient qu'après tous les autres : un message répété perd son effet (Nahum-Shani et al., 2018). Quand la personne a noté une douleur, une fatigue ou un symptôme prémenstruel, un conseil qui y répond passe en premier.

## Statistiques personnelles

Lunaison calcule, sur le téléphone, ce que les journées notées disent du cycle de la personne. Ces statistiques décrivent ; elles ne posent pas de diagnostic. Les règles sont dans [`CycleInsights.kt`](../core/cycle/src/main/kotlin/fr/lunaison/core/cycle/CycleInsights.kt) et [`PhasePatterns.kt`](../core/cycle/src/main/kotlin/fr/lunaison/core/cycle/PhasePatterns.kt), les seuils dans [`CycleConfig.kt`](../core/cycle/src/main/kotlin/fr/lunaison/core/cycle/CycleConfig.kt), le tout couvert par des tests.

**Ce qui est calculé**

- Durée habituelle des cycles et des règles, plus court et plus long cycle, sur les six derniers cycles, et comparaison aux repères FIGO (ou ACOG avant 18 ans).
- Justesse des prévisions : pour chaque cycle, la durée que l'appli aurait prévue avec les cycles précédents, comparée à la durée réelle. L'écart habituel n'est annoncé qu'après trois prévisions vérifiables.
- Ce qui revient pendant une phase : symptômes et humeurs, affichés en comptes (« 3 cycles sur 4 »), jamais en pourcentages.
- Moment d'apparition des symptômes de fin de cycle, compté à rebours depuis les règles suivantes, parce que cette partie du cycle est la plus stable (Bull et al., 2019).
- Un bilan du cycle terminé, visible les sept premiers jours du cycle suivant, et une ligne qui annonce la phase suivante quand quelque chose y revient d'habitude.

**Les garde-fous**

- Rien avant deux cycles complets. C'est aussi le minimum des critères cliniques du syndrome prémenstruel, qui demandent un suivi au jour le jour sur au moins deux cycles.
- Un jour vide n'est pas un jour sans symptôme : seuls les jours où un symptôme, une humeur ou une douleur a été noté comptent.
- Un élément n'est lié à une phase que s'il revient dans au moins la moitié des cycles où cette phase a été renseignée, et, quand assez de jours sont renseignés ailleurs, s'il y est au moins deux fois plus fréquent qu'en dehors. Une fatigue présente tout le mois n'est pas un symptôme prémenstruel.
- Aucune interprétation : quand une durée sort des repères, l'appli renvoie vers la fiche « Quand consulter ».

Ces seuils sont des choix de conception prudents, pas des critères validés.

| Référence | Utilisée pour |
|---|---|
| Nahum-Shani I. et al. *Just-in-Time Adaptive Interventions (JITAIs) in Mobile Health: Key Components and Design Principles for Ongoing Health Behavior Support.* Ann Behav Med, 2018;52(6):446-462. [PMC5364076](https://pmc.ncbi.nlm.nih.gov/articles/PMC5364076/) | Varier les messages contre l'usure, ne rien afficher quand rien n'est pertinent |
| Epstein D.A. et al. *Examining Menstrual Tracking to Inform the Design of Personal Informatics Tools.* CHI 2017. [PMC5432133](https://pmc.ncbi.nlm.nih.gov/articles/PMC5432133/) | Raisons du suivi (se préparer, comprendre son corps, informer son médecin) et attentes envers les prévisions |

