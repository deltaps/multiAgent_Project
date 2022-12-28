#!/bin/bash

echo "Exécution de la commande 'ant'..."
ant

echo "On ce place dans le répertoire 'dist'..."
cd build

echo "On lance le programme..."
java -cp '.:../lib/jade.jar' jade.Boot -agents 'eva:atelier;bob:robot;jean:robot;marc:robot'
