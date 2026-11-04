# Parcours utilisateurs : étude et pistes

Novembre 2026. Étude de travail, pas une liste de décisions. Elle couvre surtout Lunaison, avec un passage plus court sur Respire et Sommelle.

## La question

Comment donner à Lunaison l'impression de « vivre » sans ajouter de contenu de remplissage : des conseils qui changent, au bon moment, sans notifications en plus ni séries à tenir.

## Ce que dit la recherche

**Pourquoi les gens suivent leur cycle.** Une étude sur 687 personnes et 2 000 avis d'applis (Epstein et al., CHI 2017) relève cinq raisons :

- se préparer : protections, organisation de la semaine ;
- comprendre le lien entre son corps, son humeur et la phase du cycle ;
- garder un œil sur sa santé ;
- avoir des informations précises pour son médecin ;
- concevoir.

Les mêmes personnes citent plusieurs problèmes :

- des prévisions fausses quand le cycle est irrégulier, qui font abandonner l'appli ;
- les oublis de saisie, qui faussent ensuite les prévisions ;
- des applis centrées sur la fertilité alors que ce n'est pas leur but ;
- des notifications qui révèlent des informations privées ;
- des besoins qui changent avec le temps, sans que l'appli suive.

**La prévision mal comprise.** Dans 25 entretiens (Hohmann-Marriott et Starling, 2022), les utilisatrices ne savaient pas comment leur appli calculait l'ovulation. Certaines s'en servaient pour éviter une grossesse sans le considérer comme une contraception. Expliquer la prévision fait partie de l'appli, pas seulement du site.

**L'abandon fait partie de l'usage.** Le modèle du suivi « vécu » (Epstein et al., UbiComp 2015) décrit des arrêts et des reprises normaux, pas des échecs. Une appli doit permettre de reprendre facilement, sans punir le trou dans l'historique.

**L'usure des messages.** Un message identique répété perd son effet : c'est l'habituation. Le cadre des interventions adaptatives au bon moment (Nahum-Shani et al., 2018) recommande trois choses :

- varier le contenu en puisant dans une banque de messages ;
- n'intervenir que quand la personne peut en tirer quelque chose ;
- prévoir une option « ne rien montrer » quand rien n'est pertinent.

La fatigue d'une intervention vient du temps et de l'effort qu'elle demande.

## Le parcours de Lunaison aujourd'hui

| Étape | Ce dont la personne a besoin | Ce que fait l'appli | Écart |
|---|---|---|---|
| Premier jour | Savoir vite à quoi sert l'appli et que ses données restent chez elle | Accueil court, date approximative des dernières règles | Aucun |
| Premier cycle, sans historique | Une prévision, même grossière, et savoir qu'elle est grossière | Plage provisoire de ± 4 jours | La raison de la largeur de la plage n'est dite que dans une fiche |
| Jours ordinaires | Une raison d'ouvrir l'appli qui ne soit pas une obligation | Les mêmes 2 à 4 conseils pour toute la phase, chaque jour | Contenu identique pendant des jours : usure rapide |
| Règles qui arrivent | Être prévenue, discrètement | Rappel quelques jours avant, neutre sur l'écran verrouillé | Aucun |
| Après 2 cycles | Comprendre son propre cycle | « Ce qui revient chez vous » | Le meilleur contenu de l'appli arrive sous les conseils génériques |
| Fin d'un cycle | Savoir si ce cycle était dans ses repères | Rien de spécifique | Pas de moment de bilan |
| Oubli de saisie, retard | Corriger facilement, sans culpabilité | Phase « Règles en retard », ajout possible dans le calendrier | Aucun |
| Après une longue pause | Reprendre sans que tout soit faux | Les cycles hors des valeurs plausibles sont écartés | À vérifier sur un vrai retour après plusieurs mois |
| Changement de vie (contraception, grossesse, allaitement) | Que l'appli ne prédise pas des règles qui ne viendront pas | Rien de spécifique | Prévisions et signaux faussés pendant ces périodes |
| Consultation médicale | Un résumé à montrer | Export tableur | Pas de résumé lisible d'un coup d'œil |
| Départ | Garder ses données | Export, effacement en un geste | Aucun |

## Pistes pour une appli qui vit

Le principe : la variété vient des données de la personne et du moment du cycle, pas d'un carrousel d'astuces. Quand rien de pertinent n'est à dire, l'appli ne dit rien.

1. **Un seul conseil par jour, qui change.** Il est tiré de la banque de la phase en cours, avec le jour du cycle comme graine : le même toute la journée, un autre le lendemain. Les autres restent dans la fiche liée. C'est le changement le plus simple et celui qui répond directement à l'usure. La banque actuelle compte 2 à 4 conseils par phase ; il faudrait 5 à 8 conseils sourcés par phase pour qu'un conseil ne revienne pas plus d'une fois par phase.
2. **Le conseil choisi selon ce qui a été noté.** Une douleur « Forte » notée aujourd'hui fait passer le conseil sur la chaleur ou les anti-inflammatoires en premier. Une fatigue notée pendant les règles fait passer le conseil sur le fer. Sans saisie, la rotation simple s'applique. Ces règles tiennent dans `core:content` et se testent comme le reste.
3. **« Ce qui revient chez vous » en premier, dès qu'il existe.** C'est le seul contenu propre à la personne : il doit passer devant les conseils génériques.
4. **Prévenir avant une phase connue.** « Dans 3 jours, la phase où vous notez souvent maux de tête. » Une ligne sur l'accueil, pas une notification. Cela répond au besoin de se préparer, la première raison de suivre son cycle.
5. **Un bilan court à la fin de chaque cycle.** Durée du cycle et des règles, comparées aux repères et aux cycles précédents, en une carte qui disparaît après lecture. C'est un moment naturel, une fois par mois, sans rien ajouter au quotidien.
6. **Un « Pourquoi cette plage ? » sur la prévision.** Une phrase et le lien vers la fiche. C'est l'endroit où la confusion est documentée.
7. **Une pause du suivi des prévisions.** Pour la grossesse, l'allaitement ou un changement de contraception, l'appli continue de noter mais arrête de prévoir et de signaler. Elle reprend quand la personne le choisit.
8. **Un résumé pour le médecin.** Les 6 derniers cycles en une page, comme dans Sommelle. C'est une demande explicite de l'étude de 2017.

À ne pas faire : notification de conseil du jour, série de jours de saisie, badge, rappel quotidien par défaut (le réglage actuel, autour des règles seulement, est le bon), contenu qui dit comment « vivre » chaque phase.

**Ordre conseillé.** D'abord les pistes 1 et 3 : elles ne demandent que de réorganiser l'accueil et d'enrichir la banque de conseils. Ensuite la 2, puis la 5. Les pistes 6 à 8 sont indépendantes.

## Respire et Sommelle, en bref

**Respire.**

- **Activation de l'accessibilité.** C'est le point de friction le plus fort, réglé par l'écran d'activation et l'aide sur les paramètres restreints.
- **L'envie de tout couper revient régulièrement.** Les utilisateurs de one sec font des pauses et rechutent, puis reviennent (Haliburton et al., 2024). Le verrou de 30 secondes couvre l'assouplissement ; rien n'accueille encore la personne qui réactive Respire après une pause.
- **Piste : un retour vers ses propres chiffres.** Une ligne hebdomadaire sur l'accueil, par exemple « Cette semaine, tu as renoncé 12 fois sur 30 », suffirait à faire vivre l'appli sans notification. Les statistiques existent déjà : c'est une question de mise en avant, pas de contenu nouveau.

**Sommelle.**

- **La semaine 1 est le moment critique.** La fenêtre de sommeil commence, la fatigue augmente, et les personnes qui abandonnent disent ne pas savoir combien de temps cela dure (Simon et al., 2025). La page « Pourquoi » du site le couvre ; l'appli ne le rappelle qu'au questionnaire de départ.
- **La fin du programme est à vérifier.** Après la semaine 6, le bilan existe et la fenêtre continue de s'ajuster. Il faut vérifier que l'appli dit clairement si l'on peut arrêter l'agenda ou le continuer : ne pas savoir quand cela s'arrête est une cause d'abandon citée par Simon et al.
- **Le contenu change déjà avec le temps :** un exercice par semaine, un bilan hebdomadaire chiffré. Sommelle n'a pas besoin de rotation de conseils.

## Sources

- Epstein D.A. et al. Examining Menstrual Tracking to Inform the Design of Personal Informatics Tools. *CHI 2017*. [PMC5432133](https://pmc.ncbi.nlm.nih.gov/articles/PMC5432133/)
- Hohmann-Marriott B., Starling L. "What if it's wrong?" Ovulation and fertility understanding of menstrual app users. *SSM: Qualitative Research in Health*, 2022;2:100057. [Notice](https://doaj.org/article/7541db9879434d3c93082a63c7cefa0a)
- Epstein D.A., Ping A., Fogarty J., Munson S.A. A Lived Informatics Model of Personal Informatics. *UbiComp 2015*. [doi:10.1145/2750858.2804250](https://doi.org/10.1145/2750858.2804250)
- Nahum-Shani I. et al. Just-in-Time Adaptive Interventions (JITAIs) in Mobile Health: Key Components and Design Principles for Ongoing Health Behavior Support. *Annals of Behavioral Medicine*, 2018;52(6):446-462. [PMC5364076](https://pmc.ncbi.nlm.nih.gov/articles/PMC5364076/)
- Haliburton L. et al. A Longitudinal In-the-Wild Investigation of Design Frictions to Prevent Smartphone Overuse. *CHI 2024*. [doi:10.1145/3613904.3642370](https://doi.org/10.1145/3613904.3642370)
- Simon L. et al. Barriers and Facilitators of Sleep Restriction Therapy in Internet-Delivered CBT-I. *J Sleep Res*, 2025;34(6):e70018. [PubMed 41014699](https://pubmed.ncbi.nlm.nih.gov/41014699/)
