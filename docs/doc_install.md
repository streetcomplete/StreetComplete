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
- Puis déplacez vous sur la bonne branche (probablement quelque chose comme "Sprint1")
  ```bash
  git checkout [branche]
  ```

### Lancez l'émulateur ### 

- Installer Android Studio (de préférence Narval 3, étant notre environnement de dev nous pouvons garantir qu'il fonctionne)
- Lancer le projet sous Android Studio.
- Si vous avez placer votre repo local sous la bonne branche, Android Studio doit-y être aussi. Vous pouvez vérifier en haut à gauche que le bouton à droite inscrive bien le bon Sprint. Si il affiche "master", cliquez dessus pour changer la branche.
- Lancez l'application avec la flèche verte en haut à droite ou en appuyant sur Maj + F10.
(Si la flèche est grisée, une bannière bleue doit être visible en haut de l'interface de dev. Cliquez sur synchroniser. Après quelques minutes la flèche devrait devenir verte et permettre de lancer l'application.)
- Le premier démarrage peut prendre quelques minutes le temps du build. Prenez un café en attendant que l'émulateur lance l'application.
