# Chat Service

PositionPal chat service repository

## Pre-requisites

For the correct operation of the service, it is necessary the following environment variables are set and available at startup:

| Variable Name             | Description                                                                                                                                                                                                                                   |
|---------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `AKKA_LICENSE_KEY`        | [Akka license token](https://akka.io/blog/akka-license-keys-and-no-spam-promise) used to running the akka system in production. Ensure that the license is stored securely, for example, by using Dokcer or Kubernetes secrets.               |
| `RABBITMQ_HOST`           | The host address of the RabbitMQ server where the message broker is running (e.g. `localhost`).                                                                                                                                               |
| `RABBITMQ_VIRTUAL_HOST`   | The [virtual host](https://www.rabbitmq.com/docs/vhosts) in RabbitMQ used for logical separation of resources (e.g. `/`).                                                                                                                     |
| `RABBITMQ_PORT`           | The port on which the RabbitMQ server is listening for incoming connections.                                                                                                                                                                  |
| `RABBITMQ_USERNAME`       | The username used to authenticate with the RabbitMQ server. This should be an account with sufficient permissions to interact with the necessary queues and exchanges in the virtual host.                                                    |
| `RABBITMQ_PASSWORD`       | The password associated with the RABBITMQ_USERNAME. This password is used in conjunction with the username for authentication purposes. Ensure the password is stored and used securely, for example, by using Docker or Kubernetes secrets.  |
| `USER_SERVICE_EVENT_QUEUE`| The RabbitMQ event queue name where the user and group events (created by the user service) are published.                                                                                                                                    |
| `CASSANDRA_CONTACT_POINT` | The URL (formatted as hostname:port) of Cassandra database used by the service.                                                                                                                                                               |
| `CASSANDRA_USERNAME`      | The username used to authenticate with the Cassandra server.                                                                                                                                                                                  |
| `CASSANDRA_PASSWORD`      | The password associated with the CASSANDRA_USERNAME. This password is used in conjunction with the username for authentication purposes. Ensure the password is stored and used securely, for example, by using Docker or Kubernetes secrets. |
| `GRPC_PORT`               | The port on which the gRPC server listens for incoming requests.                                                                                                                                                                              |
| `HTTP_PORT`               | The port on which the HTTP server listens for incoming HTTP requests, including WebSocket connections for real-time communications.                                                                                                           |

An example of valid environment setup is shown below:

```env
AKKA_LICENSE_KEY=...
RABBITMQ_HOST=localhost
RABBITMQ_VIRTUAL_HOST=/
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=admin
CASSANDRA_CONTACT_POINT=localhost:9042
CASSANDRA_USERNAME=admin
CASSANDRA_PASSWORD=password
GRPC_PORT=50051
HTTP_PORT=8080
```

Moreover, the service requires a Cassandra database that is accessible based on the environment variables specified above.
Before starting the service, ensure the database is correctly configured with all the required tables.
The necessary CQL scripts for creating these tables can be found here:
- [`./infrastructure/src/main/resources/db-scripts/cassandra-schema-creation.cql`](./infrastructure/src/main/resources/db-scripts/cassandra-schema-creation.cql)
