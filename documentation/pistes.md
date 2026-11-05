# Pistes envisagées

Fonctions étudiées mais pas encore réalisées. Rien ici n'est promis ni daté.

## Importer l'historique depuis d'autres applications

Pour qu'une personne qui change d'application garde ses années de données. L'import se ferait entièrement sur le téléphone, à partir d'un fichier d'export (CSV ou JSON), sans connexion ni envoi vers aucun serveur.

**Prérequis technique :**
Un format d'échange documenté ou des spécifications d'export standardisées (dates ISO, libellés de flux, symptômes) pour écrire et tester un import fiable et sans perte.

**Principes à respecter :**

- import facultatif, lancé par la personne, depuis un fichier qu'elle choisit (sélecteur de documents Android, sans autorisation de stockage) ;
- aperçu avant import : nombre de jours et de règles reconnus, période couverte ;
- ce qui n'a pas d'équivalent dans Lunaison est ignoré et signalé, jamais inventé ;
- les journées déjà notées dans Lunaison ne sont pas écrasées sans confirmation.
