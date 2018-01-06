FROM node:boron-alpine

ADD package.json ./
ADD *.js ./

RUN npm install

ENTRYPOINT [ "node", "index.js" ]