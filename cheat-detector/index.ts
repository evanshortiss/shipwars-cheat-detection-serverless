import express, { ErrorRequestHandler } from 'express'
import log from 'barelog'
import { CloudEvent, HTTP } from 'cloudevents'
import { get } from 'env-var'
import { json } from 'body-parser'
import got from 'got'

// Load environment variables from .env file in cwd if found
require('dotenv').config()

const CHEAT_THRESHOLD = get('CHEAT_THRESHOLD').default(20).asIntPositive()
const BROKER_URL = get('BROKER_URL').required().asUrlString()
const HTTP_PORT = get('HTTP_PORT').default(8080).asIntPositive()
const app = express()

// const sample = {
//   "game": "KopNJVxtG9XACStpgCoNz",
//   "match": "cXepex3y1Ebwy655G3AFz",
//   "scoreDelta": 210,
//   "attacker": "OnhtJwmSFR26R4BfVuRZd",
//   "shots": 42
// }

type BonusPayload = {
  data: {
    game: string,
    match: string,
    scoreDelta: number,
    attacker: string,
    shots: number
  }
}

app.post('/*', json(), async (req, res, next) => {
  log('incoming HTTP body %j', req.body)
  log('incoming HTTP headers %j', req.headers)

  let receivedEvent = HTTP.toEvent<BonusPayload>({
    headers: req.headers,
    body: req.body
  }) as CloudEvent<BonusPayload>;

  log('received cloud event:', receivedEvent);

  const { data } = receivedEvent
  
  let ce: CloudEvent<BonusPayload>

  if (!data) {
    return next(new Error('cloudevent contains no JSON shot/bonus data'))
  } else if (data.data.shots >= CHEAT_THRESHOLD) {
    ce = new CloudEvent({
      type: 'audit.fail.bonus',
      source: 'cheat-detector',
      data
    })  
  } else {
    ce = new CloudEvent({
      type: 'audit.pass.bonus',
      source: 'cheat-detector',
      data
    })
  }  
  
  const { body, headers } = HTTP.binary(ce)

  log('posting processed cloud event to broker:', { body, headers })

  await got.post(BROKER_URL, {
    headers: headers,
    body: body as any
  })

  res.status(202).end()
})

const errorHandler: ErrorRequestHandler = (err, req, res, next) => {
  log('error processing request', err)

  res.status(500).end('internal server error')
}
app.use(errorHandler)

app.listen(HTTP_PORT, () => {
  log(`cheat-detector listening on ${HTTP_PORT}. Using broker url ${BROKER_URL}`)
})
