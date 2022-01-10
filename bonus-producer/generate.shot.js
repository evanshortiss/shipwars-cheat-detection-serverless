'use strict'

const {nanoid} = require('nanoid')
const getUsername = require('./username.generator')

/**
 * Generates a random bonus shot object.
 * @returns {Object}
 */
module.exports = function generateShot () {
  return {
    match: nanoid(),
    game: nanoid(),
    by: {
      username: getUsername(),
      uuid: nanoid()
    },
    shots: Math.round(40 * Math.random()),
    // 50:50 chance that it's an AI or human player
    human: Math.round(Math.random()) === 1
  }
}
