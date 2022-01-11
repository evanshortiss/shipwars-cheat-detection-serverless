import express, { ErrorRequestHandler } from 'express'
import sendgrid from '@sendgrid/mail'
import log from 'barelog'
import { HTTP } from 'cloudevents'
import { get } from 'env-var'
import { json } from 'body-parser'

require('dotenv').config()

const SENDGRID_API_KEY = get('SENDGRID_API_KEY').required().asString()
const EMAIL_FROM = get('EMAIL_FROM').required().asString()
const EMAIL_TO = get('EMAIL_TO').required().asString()
const HTTP_PORT = get('HTTP_PORT').default(8080).asPortNumber()
const app = express()

sendgrid.setApiKey(SENDGRID_API_KEY)

app.post('/*', json(), async (req, res, next) => {
  const receivedEvent = HTTP.toEvent({
    headers: req.headers,
    body: req.body
  });

  log('received cloud event:', receivedEvent);

  try {
    const msg = {
      from: EMAIL_FROM,
      to: EMAIL_TO,
      subject: 'Audit Failure Detected',
      text: `Received an audit failure!`,
    }

    await sendgrid.send(msg)
    
    log(`moderation email sent from ${EMAIL_FROM} to ${EMAIL_TO}`)

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
