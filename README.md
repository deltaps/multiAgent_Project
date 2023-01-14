# Projet Multi-Agent 
## M1 Informatique - Université de Caen
### 2022 - 2023
#### PRONOST Sacha 21901956

## Description du projet
Ce projet à pour but d'implémenter à l'aide de la bibliothèque JADE, un système multi-agent permettant de simuler un atelier de production.

## Fichier de configuration
Le fichier de configuration est un fichier texte contenant les informations suivantes :
- Le temps de production d'un produit (qui fluctue en fonction du niveau de compétence des robots)
- La liste des compétences que peuvent avoir les robots
- La liste des produits que doit produire l'atelier

## Lancement du projet
Pour lancer le projet, il vous suffit de lancer le script 'run.sh' situé à la racine du projet.
Il est possible que vous deviez modifier les droits d'exécution du script avec la commande suivante :
```sudo chmod +x run.sh```
Le script vous demandera le nombre de robots que vous souhaitez créer, ainsi que le nom de chaque robot.
Il est indispensable d'avoir ant pour pouvoir lancer le projet.
Si vous voulez lancer le projet avec windows, un fichier scriptWin.bat est aussi à disposition.