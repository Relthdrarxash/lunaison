# Pistes et réalisations

## Importer l'historique depuis d'autres applications

- **Réalisé en v1.4.0** : import local sans serveur depuis l'application *Calendrier des règles* (Simple Design Ltd), compatible avec les archives ZIP chiffrées par mot de passe (ZipCrypto) et les fichiers JSON bruts, ainsi qu'import/export CSV Lunaison.
- **Pistes futures** : prise en charge d'autres formats d'export (Clue, Flo, Drip) si des spécifications d'export exploitables sont disponibles.

**Principes respectés :**

- import facultatif, lancé par la personne, depuis un fichier qu'elle choisit (sélecteur de documents Android, sans autorisation de stockage) ;
- traitement 100 % local sur l'appareil, sans connexion ni envoi vers aucun serveur ;
- ce qui n'a pas d'équivalent dans Lunaison est ignoré et signalé, jamais inventé.
