'use strict'

const pino = require('pino')
const { LOG_LEVEL } = require('./config');

module.exports = pino({
  level: LOG_LEVEL
});
