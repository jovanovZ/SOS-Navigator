FROM node:24

WORKDIR /app

COPY package*.json ./

RUN npm install  --legacy-peer-deps

COPY . .

EXPOSE 3002

CMD ["npm", "start"]
