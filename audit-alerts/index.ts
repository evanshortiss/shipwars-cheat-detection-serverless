import express, { ErrorRequestHandler } from 'express'
import sendgrid from '@sendgrid/mail'
import log from 'barelog'
import { CloudEvent, HTTP } from 'cloudevents'
import { get } from 'env-var'
import { json } from 'body-parser'

require('dotenv').config()

const SENDGRID_API_KEY = get('SENDGRID_API_KEY').required().asString()
const EMAIL_FROM = get('EMAIL_FROM').required().asString()
const EMAIL_TO = get('EMAIL_TO').required().asString()
const HTTP_PORT = get('HTTP_PORT').default(8080).asPortNumber()
const app = express()

sendgrid.setApiKey(SENDGRID_API_KEY)

type BonusPayload = {
  match: string,
  game: string,
  by: {
    username: string
    uuid: string
  },
  shots: number,
  human: boolean
}

app.post('/*', json(), async (req, res, next) => {
  // Need to cast so single object, array should not be
  // possible AFAIK...
  const { data } = HTTP.toEvent<BonusPayload>({
    headers: req.headers,
    body: req.body
  }) as CloudEvent<BonusPayload>;

  log('received cloud event with data:', { data });
  
  try {
    const msg = {
      from: EMAIL_FROM,
      to: EMAIL_TO,
      subject: `Audit Failure Detected`,
      text: `The player named ${data?.by.username} is possibly cheating.`,
    }

    await sendgrid.send(msg)
    
    log(`moderation email sent from ${EMAIL_FROM} to ${EMAIL_TO} for user ${data?.by.username}`)

    res.status(202).end()
  } catch (e) {
    next(e)
  }
})

const errorHandler: ErrorRequestHandler = (err, req, res, next) => {
  log('error processing request', err)

  res.status(500).end('internal server error')
}
app.use(errorHandler)

app.listen(HTTP_PORT, () => {
  log(`audit-alerts listening on ${HTTP_PORT}`)
})
