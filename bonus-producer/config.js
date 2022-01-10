'use strict';

const { get } = require('env-var');

module.exports = {
  LOG_LEVEL: get('LOG_LEVEL').default('info').asString(),
  HTTP_PORT: get('HTTP_PORT').default(8080).asPortNumber(),
  KAFKA_CLIENT_ID: get('KAFKA_CLIENT_ID').default('shot-producer').asString(),
  KAFKA_TOPIC: get('KAFKA_TOPIC').default('shots').asString()
};
