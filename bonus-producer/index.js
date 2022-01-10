'use strict';

const { HTTP_PORT } = require('./config');
const generateShot = require('./generate.shot');
const kafka = require('./kafka');
const log = require('./log');
const fastify = require('fastify')({
  logger: log
});

fastify.get('/bonus', async (request, reply) => {
  // Generate a random shot payload
  const shot = generateShot();

  // Send it to the configured kafka cluster and topic
  await kafka.send([shot]);

  // Return the shot information to the calling client
  reply.send(shot);
});

fastify.listen(HTTP_PORT, '0.0.0.0', (err) => {
  if (err) throw err;
});
