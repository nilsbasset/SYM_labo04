# SYM labo 04 - Environnement II - capteurs et BLE

Auteurs : Basset Nils, Da Rocha Carvalho Bruno, Thurnherr Gabrielle

Date : 27.01.2022

## 1. Introduction

Ce laboratoire est consacré aux capteurs disponible sur les smartphones (principalement accéléromètre et magnétomètre) ainsi qu'à la communication Bluetooth Low Energy

## 1.1 Manipulation

Cette manipulation a pour but de créer une boussole en 3D permettant d'afficher la direction du nord magnétique. La réalisation de la boussole 3D a été fournie. Nous avons simplement dû utiliser la méthode `float[]` `swapRotMatrix(float[] rotMatrix)` qui permet de passer au moteur de rendu 3D la matrice de rotation. (`sensorManager.getRotationMatrix(this.opglr.swapRotMatrix(rotation), null, gravity, geomagnetic);`). On détecte les changement du capteur "accéléromètre" et du capteur de "champs magnétique" pour passer les nouvelles informations à la matrice.

## 1.2 Question

>  Une fois la manipulation effectuée, vous constaterez que les animations de la flèche ne sont pas fluides, il va y avoir un tremblement plus ou moins important même si le téléphone ne bouge pas. Veuillez expliquer quelle est la cause la plus probable de ce tremblement et donner une manière (sans forcément l’implémenter) d’y remédier.

Les capteurs, étant sensibles, captent tous plein de petites variations même si le smartphone reste immobile. On pourrait remédier à se problème en checkant si la variation de la nouvelle valeur par rapport à l'ancienne est assez significative pour faire la mise à jour de la boussole.

## 2.1 Manipulation

Cette manipulation a pour but de faire une communication Bluetooth Low Energy entre un smartphone et un écran Espruino Pixl.js. 

Pour cette partie aussi, une partie du code a été fournie. La scan et la recherche BLE étant déjà implémentée nous avons dû faire un filtre pour ne voir que l'écran Espruino. Pour se faire nous avons créer un filtre dans la fonction qui scan les devices (`scanLeDevice()`) pour n'afficher que les périphériques diposant du service `3c0a1000-281d-4b48-b2a7-f15579a1c38f`. 

Ensuite nous avons dû mettre en place différent type d'échange : lecture, écriture et notification.

- Afficher la température du périphérique (lecture) ;
- Afficher le nombre de bouton cliqués (notification) ;
- Envoi d’un nombre entier au périphérique (écriture) ;
- Afficher l’heure du périphérique (notification) ;
- Mettre à jour l’heure sur le périphérique (écriture).

## 2.2 Questions

> La caractéristique permettant de lire la température retourne la valeur en degrés Celsius, multipliée par 10, sous la forme d’un entier non-signé de 16 bits. Quel est l’intérêt de procéder de la sorte ? Pourquoi ne pas échanger un nombre à virgule flottante de type float par exemple ?

Le type float est sur 32 bits ce qui est pas du tout nécessaire pour envoyer une information de température. Cela ferait des communication plus conséquente. Un entier non-signé sur 16 bits est largement suffisant. Comme ça la communication peut être plus légère et nous pouvons faire la transformation en float dans notre code.

> Le niveau de charge de la pile est à présent indiqué uniquement sur l’écran du périphérique, mais nous souhaiterions que celui-ci puisse informer le smartphone sur son niveau de charge restante. Veuillez spécifier la(les) caractéristique(s) qui composerai(en)t un tel service, mis à disposition par le périphérique et permettant de communiquer le niveau de batterie restant via Bluetooth Low Energy. Pour chaque caractéristique, vous indiquerez les opérations supportées (lecture, écriture, notification, indication, etc.) ainsi que les données échangées et leur format.
