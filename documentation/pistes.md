# Pistes envisagées

Fonctions étudiées mais pas encore réalisées. Rien ici n'est promis ni daté.

## Importer l'historique depuis une autre appli

Pour qu'une personne qui quitte Flo ou Clue garde ses années de données. L'import se ferait entièrement sur le téléphone, à partir du fichier d'export, sans connexion ni envoi.

| Appli | Comment obtenir ses données | Format |
|---|---|---|
| Clue | Réglages > « Download my data » : lien envoyé par e-mail (valable 72 h) vers un dossier .zip protégé par un mot de passe affiché dans l'appli | JSON |
| Flo | Pas de bouton d'export : demande au support depuis l'appli (Aide > Nous contacter) ; en mode anonyme, il faut d'abord créer un compte | Texte ou JSON |

**Bloquant pour l'instant :** aucun des deux formats n'est documenté publiquement. Il faut au moins un vrai fichier d'export de chaque appli, fourni avec l'accord de la personne et anonymisé, pour écrire et tester un import fiable.

**Principes à respecter :**

- import facultatif, lancé par la personne, depuis un fichier qu'elle choisit (sélecteur de documents Android, sans autorisation de stockage) ;
- aperçu avant import : nombre de jours et de règles reconnus, période couverte ;
- ce qui n'a pas d'équivalent dans Lunaison est ignoré et signalé, jamais inventé ;
- les journées déjà notées dans Lunaison ne sont pas écrasées sans confirmation.

Sources : [aide Clue](https://support.helloclue.com/hc/en-us/articles/17320910724125-How-do-I-get-a-copy-of-my-Clue-data), [aide Flo](https://help.flo.health/hc/en-us/articles/360054973811-How-do-I-export-my-data) (consultées le 4 novembre 2026).
