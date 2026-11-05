# Direction artistique et justifications ergonomiques

Ce document fige la direction artistique et les principes ergonomiques de **Lunaison** (et plus largement de la suite Pilmil Studio avec **Sommelle** et **Respire**). Chaque choix visuel ou fonctionnel répond à un usage réel documenté par la recherche en interaction humain-machine et en santé publique.

---

## 1. Direction artistique : « Chaleur éditoriale et calme organique »

### Ce que nous refusons
- **L'imagerie infantilisante ou stéréotypée** : pas de rose bonbon imposé, pas de fleurs décoratives, pas de mascottes, pas de tutoiement intrusif dans un contexte de santé.
- **La froideur hospitalière** : suivre son cycle ou son sommeil n'est pas remplir un dossier clinique austère.
- **L'économie de l'attention** : pas de pastilles rouges d'alerte anxiogènes, pas de compteurs de séries (« streaks »), pas de jauges de performance quotidienne ni de faux cadenas « premium ».

### Les partis pris visuels
1. **Des surfaces « papier chaud » plutôt que du blanc pur (`#FFFFFF`)** :
   - Le blanc pur éblouit au réveil ou le soir. Les fonds clairs utilisent des tons crème, lin ou brume (`#FBF5F2`, `#F5F8F4`, `#F3F7FA`, `#FBF6F1`) qui adoucissent le contraste lumineux tout en garantissant un rapport de contraste WCAG AA supérieur à **4,5:1** (et supérieur à **10:1** pour le texte principal).
   - En thème sombre, le noir pur (`#000000`) est remplacé par des encres profondes teintées (`#18111A`, `#121814`, `#10171D`, `#1A1310`) qui évitent le scintillement (« halation ») des écrans OLED pour les personnes astigmates.
2. **Quatre palettes naturelles au choix (clair et sombre)** :
   - **Prune et terre cuite (`PLUM`)** : l'identité historique de Lunaison, chaleureuse et intime.
   - **Sauge et forêt (`SAGE`)** : apaisement végétal, pensé pour les personnes qui préfèrent une esthétique neutre et botanique.
   - **Océan et brume (`OCEAN`)** : fraîcheur minérale, propice au calme et à la respiration.
   - **Terre et ocre (`TERRACOTTA`)** : tonalités solaires et argileuses.
3. **Formes franches et hiérarchie inspirées de Material 3 Expressive (2025)** :
   - Cartes aux angles doux (`20 dp`), boutons pleins ou tonaux (`16 dp`, hauteur minimale `52 dp`), puces de sélection (`12 dp`).
   - Chaque écran comporte **une seule action primaire** immédiatement identifiable par sa couleur pleine, tandis que les actions secondaires utilisent un contour ou un style discret.

---

## 2. Justifications ergonomiques et scientifiques

### A. Saisie en moins de 15 secondes, sans clavier obligatoire
- **Constat scientifique** : la charge de saisie est la première cause d'abandon des outils de suivi personnel (*Nahum-Shani et al., 2018* ; *Epstein et al., CHI 2017*). Au réveil ou lors d'un soir de fatigue ou de douleurs menstruelles, la mémoire de travail et la précision motrice sont réduites.
- **Choix ergonomiques** :
  - Toute la journée (flux, douleur, symptômes, humeur, prise de contraception ou de traitement) se note par simples appuis sur des puces larges (`ChoiceChips`), sans ouvrir le clavier.
  - Les réglages chiffrés (heure d'un rappel, nombre de jours d'avance, durée habituelle du cycle) utilisent des boutons `−` et `+` (`Stepper`) d'au moins `48 dp × 48 dp` plutôt que des cadrans horaires ou des champs texte.

### B. Honnêteté des prévisions et prévention des risques
- **Constat scientifique** : beaucoup d'utilisatrices ignorent comment leur application estime l'ovulation ou les prochaines règles, et une fausse précision au jour près génère de l'anxiété dès que le cycle varie naturellement de quelques jours (*Hohmann-Marriott & Starling, 2022*).
- **Choix ergonomiques** :
  - **Plage probable plutôt que date figée** : Lunaison affiche toujours la plage probable (par exemple ± 3 à 4 jours selon la régularité observée sur les 6 derniers cycles) et un bouton direct *« Pourquoi cette plage ? »* dès la carte d'accueil.
  - **Fenêtre fertile facultative et explicite** : désactivée par défaut pour ne pas imposer un prisme procréatif aux personnes qui n'en ont pas besoin (*Epstein et al., 2017*), elle peut être activée dans les réglages et rappelle systématiquement qu'un repère de calendrier ne remplace pas une contraception.
  - **Indice de retard prolongé bienveillant** : au-delà de 5 jours de retard sur la fin de plage estimée, une carte neutre explique les causes fréquentes (stress, changement de rythme, maladie, grossesse), indique à partir de quel moment un test urinaire est fiable et propose en un appui de mettre les prévisions en pause.

### C. Respect du « suivi vécu » : oublis, pauses et changements de vie
- **Constat scientifique** : le suivi du cycle n'est pas linéaire ; il connaît des oublis, des pauses volontaires, des grossesses, des périodes d'allaitement ou de contraception continue (*Epstein et al., UbiComp 2015*).
- **Choix ergonomiques** :
  - **Aucune série (« streak ») ni reproche** : un jour non rempli n'est jamais considéré comme un jour « sans symptôme » dans les statistiques (`PhasePatterns`), ce qui évite de fausser les analyses en cas d'oubli.
  - **Mode « Mettre les prévisions en pause » et mode « Cycles naturellement irréguliers »** : accessibles en un geste, ils suspendent les comptes à rebours (grossesse, allaitement, contraception continue) ou élargissent la plage de prévision en désactivant les alertes de durée hors repères pour les personnes en périménopause, avec un SOPK ou en post-partum.
  - **Suivi symptothermique facultatif (glaire cervicale et température basale)** : activable dans les réglages, la température se règle par pas de 0,1 °C (`−` / `+`) sans ouvrir le clavier.
  - **Import et restauration de sauvegarde CSV** : permet de restaurer son historique sur un nouveau téléphone sans passer par un nuage tiers.
  - **Écartement automatique des cycles atypiques** : si l'utilisatrice reprend l'application après plusieurs mois sans saisie, l'intervalle > 90 jours est reconnu comme une interruption de saisie et ne dérègle pas la médiane des cycles suivants.

### D. Lutter contre l'usure tout en donnant du sens aux données notées
- **Constat scientifique** : afficher tous les jours la même liste de conseils génériques provoque une habituation rapide (*Nahum-Shani et al., 2018*). À l'inverse, croiser ses propres symptômes avec les phases du cycle et préparer une consultation médicale figurent parmi les attentes majeures (*Epstein et al., 2017*).
- **Choix ergonomiques** :
  - **Priorité aux motifs personnels (« Ce qui revient chez vous »)** : dès que deux cycles permettent de dégager une récurrence personnelle, celle-ci s'affiche *au-dessus* des explications générales de la phase.
  - **Analyse complète des 4 phases** : l'écran *« Mes cycles »* détaille l'évolution des symptômes et de l'humeur sur les quatre phases (règles, phase folliculaire, autour de l'ovulation, phase lutéale) ainsi que le compte à rebours d'apparition des signes prémenstruels.
  - **Conseil du jour adaptatif** : un seul conseil sourcé par jour, renouvelé quotidiennement par permutation déterministe, avec priorité aux conseils de soulagement lorsqu'une douleur gênante ou forte est notée le jour même.
  - **Exercice de respiration guidée intégré (4 s / 6 s)** : accessible directement depuis l'accueil pour apaiser les crampes pelviennes ou la tension prémenstruelle par expiration longue (activation parasympathique), avec annonces `LiveRegionMode.Polite` pour les lecteurs d'écran.
  - **Bilan de santé complet pour un soignant** : synthèse structurée sur une page (repères FIGO/ACOG, durées des 6 derniers cycles, jours de douleur gênante ou forte, saignements intermenstruels sur 90 jours, prises de traitement sur 30 jours, motifs par phase), partageable ou imprimable en un appui.

### E. Partage partenaire éclairé et sous contrôle exclusif
- **Choix ergonomiques** :
  - L'écran *Partager* propose non seulement des messages prêts à relire et modifier avant envoi dans la messagerie choisie (SMS, Signal, WhatsApp, autre), mais aussi une carte **« Repères pour votre partenaire »** adaptée à la phase en cours (ce qui se passe physiologiquement et 3 gestes de soutien concrets).
  - Rien n'est jamais envoyé automatiquement : c'est toujours l'utilisatrice qui appuie sur *Envoyer* dans sa messagerie.

### F. Intimité, discrétion sur l'écran d'accueil et sécurité locale
- **Constat scientifique** : les notifications ou aperçus qui dévoilent l'arrivée des règles ou un retard sur un téléphone posé sur une table constituent une atteinte fréquente à la vie privée (*Epstein et al., 2017*).
- **Choix ergonomiques** :
  - **Zéro permission `INTERNET`** : aucune donnée ne peut quitter le téléphone à l'insu de la personne.
  - **Notifications privées (`VISIBILITY_PRIVATE`)** : sur l'écran verrouillé, toute notification (règles, journal ou prise de contraception/traitement) affiche uniquement le mot « Lunaison ».
  - **Verrouillage facultatif par code PIN à 4 chiffres + `FLAG_SECURE`** : lorsqu'il est activé, un clavier numérique accessible protège l'ouverture de l'application, l'aperçu dans le sélecteur d'applications récentes d'Android est masqué (`WindowManager.LayoutParams.FLAG_SECURE`), et le widget d'accueil passe automatiquement en affichage neutre (« Lunaison · Toucher pour ouvrir »).

---

## 3. Synthèse des repères techniques d'accessibilité

| Critère | Standard appliqué dans Lunaison, Sommelle et Respire |
|---|---|
| Contraste texte / fond | ≥ 4,5:1 pour tous les textes secondaires ; ≥ 10:1 pour les textes principaux (clair et sombre) |
| Taille des cibles tactiles | Minimum `48 dp × 48 dp` pour toute icône ou flèche ; `52 dp` à `64 dp` pour les boutons d'action |
| Lecteur d'écran (TalkBack) | Titres marqués `heading()`, barres et jauges décrites en une phrase complète (`clearAndSetSemantics`), exercices guidés en `LiveRegionMode.Polite` |
| Lisibilité typographique | Aucune taille inférieure à `12 sp` ; corps de texte à `16 sp`–`18 sp` ; respect intégral de la mise à l'échelle de police d'Android |
| Ponctuation et ton | Vouvoiement respectueux (Lunaison, Sommelle), phrases courtes sans tiret cadratin (`—`), bilingue français / anglais vérifié par tests automatisés |

---

## 4. Évolutions face aux leaders du marché (Lunaison, Sommelle, Respire)

| Application | Évolutions implémentées (100 % hors ligne et gratuites) |
|---|---|
| **Lunaison** | • **Import/restauration CSV** dans les réglages (compatible exports `;` et `,`).<br>• **Suivi symptothermique facultatif** : glaire cervicale (4 repères) et température basale au réveil par pas de `0,1 °C` sans clavier.<br>• **Mode cycles naturellement irréguliers** (périménopause, SOPK, post-partum) + symptômes dédiés (*Douleurs pelviennes*, *Bouffées de chaleur*, *Sueurs nocturnes*, *Brouillard mental*).<br>• **Partage partenaire décentralisé par QR Code** (affiché et scanné dans l'appli) et menu de partage Android.<br>• **Bilan de santé soignant**, **analyse des 4 phases**, **respiration guidée 4 s / 6 s**, **4 thèmes**, **code PIN (`FLAG_SECURE`)** et **widget d'accueil**. |
| **Sommelle** | • **Feuille de route de 6 semaines dès l'accueil** (semaine 1 d'observation, semaines 2 à 6 de consolidation, maintien).<br>• **Mode « Écouter les étapes les yeux fermés »** : synthèse vocale hors ligne à débit ralenti + cloche acoustique douce générée mathématiquement (`432 Hz`, 0 Mo d'audio embarqué).<br>• **Carnet du soir interactif** dans l'exercice *Mettre sa journée au repos* (préoccupation + prochaine petite action, rangeable et effaçable en un geste).<br>• **Import d'agenda CSV** dans les réglages et **widget discret d'écran d'accueil** affichant la fenêtre de sommeil du soir. |
| **Respire** | • **Repères d'intention sur l'écran de pause** (*Message précis*, *Chercher une info*, *Simple réflexe*).<br>• **Choix de session courte « 2 min seulement »** à côté de la durée de session habituelle pour éviter de dériver lors d'une vérification rapide.<br>• **Temps estimé épargné** (aujourd'hui et sur 7 jours) + **partage du bilan hebdomadaire**.<br>• **Widget discret d'écran d'accueil** affichant les renoncements du jour, de la semaine et le temps épargné. |
