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

