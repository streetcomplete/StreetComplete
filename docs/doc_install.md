# Installer la release #

## Android ##

### Récuperer l'apk ###

L'apk se situe sur ce git. Dans la catégorie Release chaque sprint dispose de son APK correspondant. 
Vous pouvez utilisez les liens si dessous pour accéder aux APK de chaque sprint. Si les liens ne fonctionnent pas pour quelque raison que ce soit, n'hésitez pas à aller chercher les APK aux endroits indiqués.

### Télécharger l'apk ###

[APK Sprint 1](https://github.com/HugoTHOLLON/StreetCompleteSAE_S5/releases/download/Sprint1/NonOfficial-StreetComplete.apk)

### Installer l'apk ###

- Une fois l'apk téléchargée, il faut soit la transferer sur son téléphone si il n'a pas directement été télécharger dessus, pensez à choisir un dossier adapté pour le retrouver facilement lors de l'étape suivante.
- Ensuite il faut trouver l'apk dans ses fichiers puis tapper dessus, comme pour l'ouvrir.
- Valider ensuite la fênetre pop-up qui demande confirmation (sauf si vous pensez qu'on a de mauvaises intentions envers votre téléphone)
- Vous avez désormais l'application installée, il faut désormais lancer directement cette application de la même manière que n'importe quelle autre application de votre téléphone.

## Autre appareil mobile ##

Malheureusement StreetComplete n'est pas encore compatible avec d'autres appareils. Si vous ne pouvez pas vous procurez un appareil android voici la marche à suivre pour tester la release sur votre ordinateur.

- Cloner ce repo github et se placer sur la branche correspondante à la release. Voir à la fin de ce document si besoin d'aide pour cette étape.
- Ouvrir le projet dans android studio et s'assurer dans l'icône de branche github en haut à gauche que vous êtes sur la branche de la release souhaitée.
- Executer l'application et explorer la release dans l'émulateur.

### Cloner le repo ###

Si vous avez besoin de consulter la release vous savez probablement cloner un repo, mais pour fournir le lien et juste au cas où, voici un petit rappel.

- Copier [ce lien](https://github.com/HugoTHOLLON/StreetCompleteSAE_S5)
- Ouvrir un terminal git (comme Git Bash par exemple)
- Vous placer dans un dossier approprié à l'aide de la commande
  ```bash
  cd [chemin vers le dossier]
  ```
- Cloner le dépot à l'aide de la commande
  ```bash
  git clone [le lien du repo]
  ```
- Puis déplacez vous sur la bonne branche (probablement quelque chose comme "Release0")
  ```bash
  git checkout [branche]
  ```
