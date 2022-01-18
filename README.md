## About

A demo for Red Hat OpenShift Streams for Apache Kafka, and Red Hat OpenShift
Serverless. Provides a "cheat detection" service for the [Shipwars game](https://arcade.redhat.com/shipwars/) used at Red Hat Summit 2021.

![Simplified Architecture](/images/architecture-simple.png)

This demo uses OpenShift Serverless (based on the upstream Knative project) to
process events generated by a game server. These events are stored in a Kafka
Topic. A Kafka Source, provided by OpenShift Serverless, is used to fetch
events from the Kafka Topic and route them to a cheat detection function. The
cheat detection function can scale to zero when no events are available, thanks
to OpenShift Serverless.

The cheat detection function will emit a new event if it suspects a player is
cheating. This new event is routed to an email serverless function that uses
SendGrid to notify game admins about the suspected cheater.

![Detailed Architecture](/images/architecture-detailed.png)

## Requirements

* A free Red Hat account, to access [console.redhat.com](https://console.redhat.com/)
* Access to an OpenShift cluster, e.g via the free [OpenShift DevSandbox](https://developers.redhat.com/developer-sandbox).
* OpenShift Operators (these are pre-installed on DevSandbox):
  * RHOAS 0.9.5
  * OpenShift Serverless 1.19
* [RHOAS CLI](https://access.redhat.com/documentation/en-us/red_hat_openshift_streams_for_apache_kafka/1/guide/f520e427-cad2-40ce-823d-96234ccbc047)
* [OpenShift CLI](https://docs.openshift.com/container-platform/4.9/cli_reference/openshift_cli/getting-started-cli.html).
* [SendGrid Account](https://app.sendgrid.com) with a verified sender address and API Key.

## Deployment Process

### Create an OpenShift Streams Instance

OpenShift Streams for Apache Kafka provides access to hosted and managed
Apache Kafka clusters. Limited trial instances are available for 48 hours.

1. Navigate to [console.redhat.com/application-services/streams/kafkas](https://console.redhat.com/application-services/streams/kafkas) in your browser.
2. Login if you already have an account, or create a free account to access the service.
3. Click the *Create Kafka instance* button and complete the dialog to provision a Kafka instance.

The Kafka instance will take a few minutes to provision. You can continue following these instructions while it is provisioning.

### Access OpenShift DevSandbox (Optional)

The OpenShift DevSandbox is a free, time-limited, OpenShift environment. 

1. Visit the [DevSandbox Getting Started page](https://developers.redhat.com/developer-sandbox/get-started).
2. Follow the prompts, and login using the same account you used to create your Kafka instance.

After following login process you'll have access to an OpenShift environment.

### Link your Managed Kafka Instance to the OpenShift Environment

1. Select **Add +** from the side-menu in OpenShift DevSandbox.
1. Scroll down and **Managed Services**.
1. Choose the **Red Hat OpenShift Application Services** option. It will have an `Unlock with token` label.
1. Click the link in the on-screen instructions to obtain a token from [console.redhat.com/openshift/token/](https://console.redhat.com/openshift/token/), and paste it into the dialog then submit the form. You'll be returned to the **Managed Services** screen.
1. On the **Managed Services** screen, choose the **Red Hat OpenShift Streams for Apache Kafka** tile and click **Next**.
1. When prompted to *Select a Kafka Instance*, choose the instance you created at [https://console.redhat.com/application-services/streams/kafkas/](https://console.redhat.com/application-services/streams/kafkas/).

Your OpenShift project can now access the details for your OpenShift Streams for Apache Kafka instance!

### Configure Kafka ACLs

A [Service Account](https://access.redhat.com/documentation/en-us/red_hat_openshift_streams_for_apache_kafka/1/guide/2f4bf7cf-5de2-4254-8274-6bf71673f407) was created when you linked your Kafka instance to the
OpenShift project. The Service Account provides a username (Client ID) and
password (Client Secret) used for SASL authentication against your managed
Kafka.

Service Accounts have limited access to Kafka instances by default. Update
the assigned permissions to allow produce/consume operations on the `shots`
topic.

The Service Account ID and Secret are stored in your OpenShift project in a
**Secret** named *rh-cloud-services-service-account*.

Use the the [RHOAS CLI](https://access.redhat.com/documentation/en-us/red_hat_openshift_streams_for_apache_kafka/1/guide/f520e427-cad2-40ce-823d-96234ccbc047) to update the assigned permissions.

```bash
# Login to your OpenShift cluster
oc login --token=<your-token> --server=<your-cluster-api-url>

# Login to RHOAS
rhoas login

# Select the Kafka cluster that's connected to your OpenShift environment
rhoas kafka use

# Obtain the service account ID from the OpenShift cluster
export CLIENT_ID=$(oc get secret rh-cloud-services-service-account -o jsonpath='{.data.client-id}' | base64 --decode)

# Provide consume permissions to this service account for applications
# in the "knative-consumer" consumer group
rhoas kafka acl grant-access--consumer \
--service-account $CLIENT_ID --topic shots --group knative-consumer
```

### Deploy Serverless Broker

A [Broker](https://knative.dev/docs/eventing/broker/) is required to transport events within the OpenShift cluster.

The cheat detection service uses the broker URL to emit events that contain the
results of the auditing rules it applies. It emits CloudEvent format messages
to the Broker. Downstream services can register their interest in these events
using a *Trigger*, and process them. In this example, an email alerting service
will be subscribed to events of the type `audit.fail.bonus`. 

Create the Broker by applying the *broker.yml*:

```bash
oc apply -f openshift/broker.yml
```

You can confirm the Broker was created and entered the using the `READY`
state using the `oc get brokers` command.

## Deploy Serverless Functions

Deploy the Knative Serving Functions. These can process events generated by
the game server, and other functions that emit events.

The source code for both of these Serverless Functions is included in this 
repository. Pre-built images are deployed to save time.

```bash
# The cheat detection service will HTTP POST events to this URL
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
-p EMAIL_TO=$EMAIL_TO | oc create -f -
```

## Send Events to Cheat Detection using a KafkaSource

1. Select **Add +** from the side-menu in OpenShift DevSandbox.
    ![Kafka Source in Catalog](/images/kafka-source-list.png)
1. Find the **KafkaSource** using search, or under the **Event Sources** section. Select it and choose **Create**.
1. Using the *Form view* set the following options for the **KafkaSource**:
    . **Bootstrap Servers** - This can be auto-completed to your linked managed Kafka bootstrap URL.
    . **Topics**: Enter the name `shots`.
    . **SASL**: Enable SASL and use the *rh-cloud-services-service-account*. Use the *client-id* for *User*, and the *client-secret* as the *Password*.
    . **TLS**: Enable TLS. Leave the TLS options at the defaults.
    . **Sink**: Choose the *cheat-detection* Knative Service.
    ![Kafka Source Config](/images/kafka-source-config.png)
1. Click **Create** to deploy the **KafkaSource**.

## Subscribe the Email Service to Events

Finally, apply a Trigger that will subscribe the email alerting service to any
audit failure events.

```
oc apply -f openshift/audit.trigger.yml
```

This *Trigger* will cause events of type `audit.fail.bonus` in the Broker to be
sent to the email alerting service.

## Sending "Shot" Events to Kafka

Instead of running the entire [Shipwars game](https://arcade.redhat.com/shipwars/), use the *bonus-producer* included in this repository.

This requires Node.js 14 or later and a Service Account with producer
permissions:

```bash
# Login to RHOAS
rhoas login

# Create a service account and store details in /tmp/producer-sa file
rhoas service-account create \
--short-description bonus-producer \
--output-file /tmp/producer-sa \
--file-format env

# Get the Client ID
export CLIENT_ID=$(cat /tmp/producer-sa | grep CLIENT_ID | awk -F '=' '{print $2}')

# Apply produce and consume permissions to the service account
rhoas kafka acl grant-access --producer --consumer \
--service-account $CLIENT_ID --topic shots --group all

# Provide credentials to Node.js application config dir
cat /tmp/producer-sa | grep CLIENT_ID | awk -F '=' '{print $2}' > /bonus-producer/.bindings/kafka/user

cat /tmp/producer-sa | grep CLIENT_SECRET | awk -F '=' '{print $2}' > /bonus-producer/.bindings/kafka/password

rhoas kafka describe | jq .bootstrap_server_host -r > /bonus-producer/.bindings/kafka/bootstrapServers
```

Start the producer locally:

```
cd bonus-producer
npm install

npm run dev:with-bindings
```

You can now send payloads to Kafka for cheat detection by sending HTTP GET
requests to `http://localhost:8080/bonus`.
