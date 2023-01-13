#!/bin/bash

echo "Combien de robots voulez-vous créer ?"
read nb_robots

declare -A robots_names

agents=""

for ((i=1;i<=nb_robots;i++))
do
    echo "Entrez le nom du robot $i (il ne faut pas que deux robots aient le même nom):"
    read nom_robot
    while [ ${robots_names[$nom_robot]} ] || [ $nom_robot == "eva" ]
    do
        if [ $nom_robot == "eva" ]
        then
            echo "Ce nom est réservé, veuillez en choisir un autre:"
        else
            echo "Ce nom est déjà utilisé, veuillez en choisir un autre:"
        fi
        read nom_robot
    done
    robots_names[$nom_robot]=1
    agents+="$nom_robot:robot;"
done

echo "Exécution de la commande 'ant'..."
ant

echo "On ce place dans le répertoire 'dist'..."
cd build


echo "On lance le programme avec les robots suivants : $agents"
java -cp '.:../lib/jade.jar' jade.Boot -agents "eva:atelier;$agents"
