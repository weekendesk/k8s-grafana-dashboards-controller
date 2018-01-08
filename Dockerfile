FROM node:boron-alpine

ADD src/*.js* ./

RUN npm install

ENTRYPOINT [ "node", "index.js" ]