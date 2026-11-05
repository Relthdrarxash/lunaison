# Parcours utilisateurs formalisés : Lunaison

Novembre 2026. Ce document formalise les parcours utilisateurs de **Lunaison** (personne qui suit son cycle, partenaire qui accompagne, et préparation d'une consultation médicale), ainsi que les études qui guident chaque choix.

---

## 1. Parcours principal : suivre et comprendre son cycle

| Étape du parcours | Besoin de l'utilisatrice | Réponse dans Lunaison | Fichier d'implémentation |
|---|---|---|---|
| **1. Bienvenue (30 s)** | Savoir d'emblée que ses données restent sur son téléphone, sans compte ni serveur | Écran d'accueil en 4 étapes courtes (âge `< 18` ou `18+`, date approximative des dernières règles, durée habituelle facultative, rappels discrets) | `OnboardingScreen.kt` |
| **2. Premier cycle (sans historique)** | Obtenir une estimation honnête qui affiche sa marge d'incertitude | Plage provisoire de `±4 jours` autour de la durée déclarée (ou 28 j par défaut), avec lien direct « Pourquoi cette plage ? » | `CycleAnalyzer.kt`, `HomeScreen.kt` |
| **3. Quotidien (15 s ou rien)** | Noter uniquement quand on en a envie, sans série de jours à maintenir | Bouton « Mes règles ont commencé » en 1 appui, ou « Noter ma journée » (pastilles : flux, douleur, symptômes, humeur, contraception/traitement, glaire/température facultatives). Un conseil sourcé différent chaque jour, adapté à ce qui est noté (ex. douleur forte -> chaleur / AINS en premier) | `HomeScreen.kt`, `LogScreen.kt`, `PhaseGuides.kt` |
| **4. Après 2 cycles complets** | Repérer ses propres tendances plutôt que des généralités | Mise en avant de « Ce qui revient chez vous » sur l'accueil et dans *Comprendre*, plus une ligne d'anticipation 4 jours avant une phase où un symptôme revient souvent | `PhasePatterns.kt`, `CycleInsights.kt` |
| **5. Fin d'un cycle** | Savoir si le cycle écoulé était dans ses habitudes | Carte de bilan du cycle terminé (affichée pendant 7 jours au début du nouveau cycle, puis masquée une fois lue) | `CycleInsights.kt`, `HomeScreen.kt` |
| **6. Oubli de saisie ou retard** | Corriger après coup sans fausser les calculs ni recevoir d'alerte rouge | Saisie rétroactive en 1 appui dans le calendrier ; exclusion automatique des écarts `< 15 j` ou `> 90 j` ; encart posé après 5 jours de retard rappelant le délai de fiabilité d'un test urinaire | `CalendarScreen.kt`, `CycleAnalyzer.kt`, `HomeScreen.kt` |
| **7. Changements de vie** | Adapter l'application à une grossesse, un allaitement, une contraception continue, un SOPK ou la périménopause | **Mode Pause des prévisions** (continue le journal sans prédire de règles) et **Mode Cycles naturellement irréguliers** (élargit l'affichage sans alerte inutile) | `SettingsScreen.kt`, `HomeScreen.kt` |
| **8. Consultation médicale** | Montrer un résumé clair d'une page à son médecin, gynécologue ou sage-femme | **Bilan pour un soignant** (synthèse sur une page A4 : repères FIGO/ACOG/NHS, 6 derniers cycles, jours de douleur forte, saignements intermenstruels) + export PDF/impression et export CSV | `HealthReportScreen.kt` |

---

## 2. Parcours Partenaire dédié (sans compte et sans serveur)

Lunaison distingue deux usages partenaires complémentaires :

1. **Depuis le téléphone de la personne qui suit son cycle (`ShareScreen.kt`) :**
   - Préparation en 1 appui d'un message modifiable dans sa propre messagerie (SMS, Signal, WhatsApp).
   - **Un seul QR Code universel** (*« Afficher mon QR Code (partage ou mise à jour) »*) et son équivalent texte `LUNA1:...` qui embarque uniquement les dates et durées des 6 dernières règles (aucune note personnelle, aucun symptôme, aucune humeur).
2. **Depuis le téléphone du ou de la partenaire (`OnboardingScreen.kt`, `HomeScreen.kt`, `QrCode.kt`) :**
   - **Entrée directe dès l'écran de bienvenue :** bouton *« Je suis partenaire (scanner un QR Code ou SMS) »* qui ouvre immédiatement le scanner sans poser les questions sur ses propres règles.
   - **Tableau de bord Partenaire sur l'accueil :** lorsque le partenaire ne suit pas son propre cycle (`partnerOnlyMode`), l'accueil masque les boutons de saisie de règles personnelles et affiche directement en haut :
     - la carte du cycle de sa partenaire (jour du cycle, phase estimée, plage des prochaines règles, fenêtre fertile si partagée) ;
     - la carte complète *« Comprendre sa phase actuelle »* et les conseils concrets de soutien du jour ;
     - le bouton **« Mettre à jour (QR / SMS) »** qui ouvre directement la caméra de scan et fusionne automatiquement les nouveaux cycles sans créer de doublon (`PartnerSnapshot.mergeWith`).

---

## 3. Ce que dit la recherche (sources de conception)

- **Pourquoi les personnes suivent leur cycle (Epstein et al., *CHI 2017*, 687 participantes) :** se préparer, comprendre le lien entre corps/humeur et phase, surveiller sa santé, préparer un rendez-vous médical, ou concevoir.
- **Compréhension de l'incertitude (Hohmann-Marriott et Starling, 2022) :** afficher une fourchette de dates et expliquer pourquoi l'ovulation est estimée à rebours (13 jours avant les prochaines règles, Bull et al., *NPJ Digital Medicine* 2019, > 600 000 cycles) évite les fausses certitudes.
- **Interruptions normales de suivi (Epstein et al., *UbiComp 2015*) :** les pauses et reprises font partie de la vie réelle ; l'application ne remet aucun compteur à zéro et écarte automatiquement les trous de saisie (`> 90 jours`) du calcul de médiane.
- **Rotation sobre des messages (Nahum-Shani et al., *Annals of Behavioral Medicine* 2018) :** un seul conseil sourcé par jour, choisi en priorité selon les symptômes notés le jour même, pour éviter l'usure sans envoyer de notifications intrusives.
