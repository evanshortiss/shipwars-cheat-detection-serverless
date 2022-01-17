## Deployment Process

### Create an OpenShift Streams Instance

1. Navigate to [console.redhat.com/application-services/streams/kafkas](https://console.redhat.com/application-services/streams/kafkas) in your browser.
2. Login if you already have an account, or create a free account to access the service.
3. Click the *Create Kafka instance* button and complete the dialog to provision a Kafka instance.

The Kafka instance will take a few minutes to provision. You can continue following these instructions while it is provisioning.

### Access OpenShift DevSandbox

The OpenShift DevSandbox is a free, time-limited, OpenShift environment.

1. Visit the [DevSandbox Getting Started page](https://developers.redhat.com/developer-sandbox/get-started).
2. Follow the prompts, and login using the same account you used to create your Kafka instance.

After following login process you'll have access to an OpenShift environment.

### Link your Managed Kafka Instance to the OpenShift Environment

1. Select **Add +** from the side-menu in OpenShift DevSandbox.
1. Scroll down and **Managed Services**.
1. Choose the **Red Hat OpenShift Application Services** option. It will have an `Unlock with token` label.
1. Click the link in the on-screen instructions to obtain a token from [console.redhat.com/openshift/token/](https://console.redhat.com/openshift/token/), and paste it into the dialog then submit the form.
1. You'll be returned to the **Managed Services** screen. Choose the **Red Hat OpenShift Streams for Apache Kafka** tile and click **Next**.
1. When prompted to *Select a Kafka Instance*, choose the instance you created earlier.

Your OpenShift project can now access the details for your OpenShift Streams for Apache Kafka instance!

### Configure Kafka ACLs

A Service Account was created when you linked your Kafka instance to the OpenShift project.

This Service Account has restricted access to your Kafka instance. Expand the assigned permissions to allow produce/consume operations on the `shots` topic.

```bash
rhoas login

# Select the Kafka cluster that's connected to your OpenShift environment
rhoas kafka use

export CLIENT_ID=$(oc get secret rh-cloud-services-service-account -o jsonpath='{.data.client-id}' | base64 --decode)

rhoas kafka acl grant-access --producer --consumer \
--service-account $CLIENT_ID --topic shots --group knative-consumer
```

### Deploy Serverless Broker & Services/Functions

A broker is required to transport events within the OpenShift cluster.

The cheat detection service uses the broker URL to emit events that contain the
results of the auditing rules it applies. It emits CloudEvent format messages
to the Broker. Downstream services can register their interest in these events
using a *Trigger*, and process them. In this example, an email alerting service
will be subscribed to events of the type `audit.fail.bonus`. 

Create the Broker by applying the *broker.yml*:

```bash
oc apply -f openshift/broker.yml
```

Deploy the Knative serving functions:

```bash
# The cheat detection service will send results to this URL
export BROKER_URL=oc get brokers -o jsonpath='{.items[0].status.address.url}'

# If the cheat detection detects a potential cheating player, a notification
# can be sent via email, using SendGrid, to an email address of your choice
export SENDGRID_API_KEY='replace-with-your-free-api-key'
export EMAIL_FROM=audit-alerts@foobar.com
export EMAIL_TO=audit-department@foobar.com

# Deploy the serverless functions
oc process -f openshift/knative.services.yml \
-p BROKER_URL=$BROKER_URL \
-p SENDGRID_API_KEY=$SENDGRID_API_KEY \
-p EMAIL_FROM=$EMAIL_FROM \
-p EMAIL_TO=$EMAIL_TO | oc create -f -clear
```

### Apply a Trigger to Subscribe to Events

Finally, apply a Trigger that will subscribe the email alerting service to any
audit failure events.

```
oc apply -f openshift/audit.trigger.yml
```

This *Trigger* will cause events of type `audit.fail.bonus` in the Broker to be
sent to the email alerting service.
