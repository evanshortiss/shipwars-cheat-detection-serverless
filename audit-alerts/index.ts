import express from 'express'
import sendgrid from '@sendgrid/mail'
import log from 'barelog'
import { HTTP } from 'cloudevents'
import { get } from 'env-var'
import { json } from 'body-parser'

const SENDGRID_API_KEY = get('SENDGRID_API_KEY').required().asString()
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
      to: 'eshortis@redhat.com', // Change to your recipient
      from: 'eshortis@redhat.com', // Change to your verified sender
      subject: 'Audit Failure Detected',
      text: `Received an audit failure!`,
    }

    await sendgrid.send(msg)
  } catch (e) {
    next(e)
  }
})

app.listen(HTTP_PORT, () => {
  log(`audit-alerts listening on ${HTTP_PORT}`)
})
