# SYM_labo04


## Questions

> Quelle est la cause la plus probable des tramblement de la flèche de la boussole 3D et donner une manière d'y remédier.

La cause est probablement dû au bruit lors de la mesure des capteurs, ce qui fais que la valeur est en perpetuel changement. Pour résoudre cela on pourrait imaginer deux moyens :
1. Mettre en place un filtre passe afin de filtrer les données reçue.
2. Récuperer les données a des intervalles réguliers pour éviter les changement si fréquent, mais ça perderait en fluidité
