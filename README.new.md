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

### Deploy a Producer Application

The producer application will send data to the `shots` Kafka topic.

Deploy the producer using the following commands:

```
# Build and deploy the Node.js producer application
oc new-app https://github.com/evanshortiss/knative-kafka-demo \
-l "app.openshift.io/runtime=nodejs" \
--docker-image="registry.access.redhat.com/ubi8/nodejs-14:latest" \
--context-dir=producer \
--name server-webapp

# Create a public endpoint to access the producer API
oc expose svc/producer

# Enforce HTTPS on the producer endpoint
oc patch route producer --type=json -p='[{"op":"replace","path":"/spec/tls","value":{"termination":"edge","insecureEdgeTerminationPolicy":"Redirect"}}]'
```

This will trigger a build, and eventually deploy the application Pod. It will
crash loop. This is because it needs to connect to a Kafka instance, but it
hasn't been told how yet!

### Configure the Producer

You can use ServiceBinding to inject the necessary Kafka configuration into
the producer application.

Use the [RHOAS CLI](https://access.redhat.com/documentation/en-us/red_hat_openshift_streams_for_apache_kafka/1/guide/f520e427-cad2-40ce-823d-96234ccbc047) to bind the configuration:

```
rhoas login

# Select the Kafka cluster that's connected to your OpenShift environment
rhoas kafka use

# Bind the kafka configuration to the producer application
rhoas cluster bind
```

The producer will restart and connect to your Kafka instance.

### Configure ACLs

A Service Account was created when you linked your Kafka instance to the OpenShift project.

This Service Account has restricted access to your Kafka instance. Expand the assigned permissions to allow produce/consume operations on the `shots` topic.

```bash
export CLIENT_ID=$(oc get secret rh-cloud-services-service-account -o jsonpath='{.data.client-id}' | base64 --decode)

rhoas kafka acl grant-access --producer --consumer \
--service-account $CLIENT_ID --topic shots --group knative-consumer
```
