'use strict';

const log = require('./log');
const { CloudEvent, HTTP } = require('cloudevents')
const { Kafka } = require('kafkajs');
const { KAFKA_CLIENT_ID, KAFKA_TOPIC } = require('./config');
const { getBinding } = require('kube-service-bindings');

const kafkaConfig = {
  ...getBinding('KAFKA', 'kafkajs'),

  // Override the clientId property in the returned config
  clientId: KAFKA_CLIENT_ID
};

const kafka = new Kafka(kafkaConfig);
const producer = kafka.producer();

/**
 * Send an array of bonus shot payloads to Kafka.
 * @param {Object} shots
 */
exports.send = async (shots) => {
  await producer.connect();

  log.info('sending shot to kafka: %j', shots);

  await producer.send({
    topic: KAFKA_TOPIC,
    messages: shots.map((shot) => {
      const ce = new CloudEvent({
        type: 'bonus',
        source: 'shipwars-game-server',
        id: `${shot.match}:${shot.by.uuid}`,
        data: shot
      });
    
      const msg = HTTP.binary(ce)
      
      return {
        headers: msg.headers,
        key: `${shot.match}:${shot.by.uuid}`,
        value: msg.body
      };
    })
  });
};
