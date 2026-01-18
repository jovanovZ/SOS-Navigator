# Poročilo o učinkovitosti

## Meritve na enem vozlišču

Meritve so za 50 najdenih blokov pri diff = 5

| Število niti | Pohitritev      | Porabljen čas |
|:------------:|:---------------:|:-------------:|
| 1            | 1               | 112294ms      |
| 2            | 1.279047782     | 87796ms       |
| 4            | 2.04185759      | 54996ms       |
| 8            | 3.271493081     | 34325ms       |
| 16           | 3.573055874     | 31428ms       |

## Meritve na več vozliščih (MPI)

### Meritve z 2 klient vozliščema

| Število niti na vozlišče | Pohitritev | Porabljen čas |
|:------------------------:|:----------:|:-------------:|
| 1                        | 1.268457437| 88528ms       |
| 2                        | 2.310908979| 48593ms       |
| 4                        | 2.422165182| 46361ms       |
| 8                        | 3.156986224| 35570ms       |
| 16                       | 2.484820322| 45192ms       |

### Meritve s 4 klient vozlišči

| Število niti na vozlišče | Pohitritev | Porabljen čas |
|:------------------------:|:----------:|:-------------:|
| 1                        | 1.755444043| 63969ms       |
| 2                        | 2.715432606| 41354ms       |
| 4                        | 3.134165062| 35829ms       |
| 8                        | 2.999465783| 37438ms       |
| 16                       | 2.664531131| 42144ms       |