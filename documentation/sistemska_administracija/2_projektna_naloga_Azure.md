# 2. Projektna naloga_Azure

## Docker
V tej nalogi smo aplikacijo, razvito pri prvi projektni nalogi pri predmetu Spletno programiranje, uspešno namestili (deployali) v okolju Docker. Za podatkovno bazo smo uporabili **MongoDB Atlas**, ki ni nameščena lokalno. Celoten projekt je verzioniran z uporabo **git** in vsebuje vse potrebne konfiguracijske datoteke, vključno z Dockerfile in docker-compose.yml.

![hithub image](/documentation/sistemska_administracija/img/github_img.png)

### Postopek namestitve

### 1. Priprava Dockerfile in docker-compose.yml

Za zagon aplikacije v Docker okolju smo naredili Dockerfile za frontend in backend ter datoteko `docker-compose.yml`, ki omogoča zagon celotnega sistema.

**Dockerfile za backend:**
```dockerfile
FROM node:24

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

EXPOSE 3002

CMD ["npm", "start"]

```
**Dockerfile za fronted:**
```dockerfile
FROM node:24-slim

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

EXPOSE 3000

CMD ["npm", "start"]
```
Razlaga komand za Dockerfile backend in frontend:
  - FROM node:24 -> uporabi najnovejšo Node.js sliko (-slim pomeni da gre za manjšo različico slike)
  - WORKDIR /app -> nastavi mapo /app
  - COPY package*.json ./ -> v to /app mapo kopira package.json in package-lock.json
  - RUN npm install -> namesti vse Node.js pakete
  - COPY . . -> skopira vso kodo iz projekta v /app
  - EXPOSE 3002 -> označi vrata (različna vrata na forntedn in backend)
  - CMD ["npm", "start"] -> zažene aplikacijo z npm start (na backendu imamo v package.json scripto "start": "nodemon app.js") 


**Docker-compose.yml:**
```yaml
services:

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    restart: always
    ports:
      - "3000:3000"
    volumes:
      - ./frontend:/app
      - /app/node_modules
    
  backend:
    build: 
      context: ./backend
      dockerfile: Dockerfile
    restart: always
    ports:
      - "3002:3002"
    volumes:
      - ./backend:/app
      - /app/node_modules 
```
Razlaga komand za Docker-compose.yml :
 - services: -> definira vse storitve (kontejnerje) ki jih docker mora zagnati, v našem primeru frontend in backend

Komande razložene na primeru dela za backend, enak pomen imajo za frontend: 
 -  build:  
  context: ./backend  
  dockerfile: Dockerfile -> Docker naj zgradi sliko iz podane mape context
 - restart: always -> Kontejner se bo vedo ponovno zagnal ob napaki
 - ports:
      - "3002:3002" -> poveže vrata 3002 na hostu z vrati 3002 v kontejnerju
 -  volumes:
      - ./backend:/app
      - /app/node_modules  -> sinhronizira lokalno mapo /backend z /app v kontejnerju, /app/node_modules pa zagotovi da se node_modules ne prepišejo

Prav tako iammo narejen .dockeringore file. Tu povemo katere file naj Docker ignorira. Ta ima vsebino:
```dockerignore
node_modules/
package-lock.json
```


### 2. Povezava na oddaljeno podatkovno bazo

Ustvarjeno imamo monogodb atlas bazo, ki ni lokalno nameščena. V njej imamo ustvarjen cluster za naš projekt imenovan SOS-Navigator-Cluster. V `.env` datoteko smo zapisali povezavo do MongoDB Atlas baze in port na katerem posluša:
```
PORT=<port>
MONGO_URI=mongodb+srv://<uporabnik>:<geslo>@<cluster-url>/<baza>
```
![slika mongodb atlas baze](/documentation/sistemska_administracija/img/baza.png)


### 3. Zagon aplikacije

Na začetku moramo le zaganti Docker desktop app katerega lahko kasneje zapremo saj deluje v odzadju. Aplikacijo zaženemo z ukazom:
```bash
docker-compose up --build
```
S tem se zgradita in zaženeta oba kontejnerja (frontend in backend). Če pa želimo zagnati le frontend/backend pa uporabimo:
```bash
docker compose up frontend --build
```
Da ustavimo aplikacijo uporabimo eno izmed naslednjih ukazov:
```bash
docker compose down
docker stop
```
stop zaustavi aplikacijo (ob zagonu je ni več potreba build-at), medtem ko compose down čisto zaustavi aplikacijo.
![razilka med stop in compose down](/documentation/sistemska_administracija/img/stop_compose_down.png)
Dodtani uporabni ukazi :
```bash
docker ps
docker image list
```
![uporabni ukazi](/documentation/sistemska_administracija/img/ukazi.png)

### 4. Težave in rešitve

- **Težava:** Napaka pri povezavi na bazo zaradi napačnega URI-ja. Vsebina v URI naslovu je case sensitive, zato moremo biti pozorni pri zapisu v .env datoteki
  - **Rešitev:** Preverili smo pravilnost podatkov v `.env` datoteki.
- **Težava:** Napaka pri nameščanju odvisnosti v Docker okolju (npr. bcrypt, nodemon).
  - **Rešitev:** Odstranili smo lokalni `node_modules` in `package-lock.json`, ter ponovno zgradili kontejnerje.

### 5. Delovanje aplikacije

Aplikacija je dostopna na naslovu `http://localhost:3000` (frontend) in `http://localhost:3002` (backend).  
Podatkovna baza je dosegljiva preko MongoDB Atlas.

**Primer zaslonskega posnetka delujoče aplikacije:**  
**Forntend:**  
![Zaslonski posnetek frontend](/documentation/sistemska_administracija/img/aplikacija.png)
**Backend:**
![Zaslonski posnetek backend](/documentation/sistemska_administracija/img/backend.png)  
**Baza:**  
![Zaslonski posnetek backend](/documentation/sistemska_administracija/img/cluster.png)

## Azure


### Odogovri na vprašanja 
1. Kje in kako omogočite "port forwarding" ?  
2. Kakšen tip diska je bil dodan vaši navidezni napravi in kakšna je njegova kapaciteta ?
3. Kje preverimo stanje trenutne porabe virov v naši naročnini ("Azure for students") ?  
Namig: stanje porabe bo vidno komaj 24ur po vpostavitvi