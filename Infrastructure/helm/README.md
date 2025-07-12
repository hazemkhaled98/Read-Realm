# Read Realm Helm Charts

This directory contains Helm charts for deploying the Read Realm services to Kubernetes.

## Available Charts

- **keycloak**: Keycloak authentication service with MySQL database
- **kafka**: Apache Kafka message broker
- **schema-registry**: Confluent Schema Registry
- **kafka-ui**: UI for managing Kafka

## Installation

### Installing All Services

To install all services using the umbrella chart:

```bash
helm install read-realm .
```

### Installing Individual Services

To install individual services:

```bash
helm install keycloak ./keycloak
helm install kafka ./kafka
helm install schema-registry ./schema-registry
helm install kafka-ui ./kafka-ui
```

## Configuration

Each chart has its own `values.yaml` file that can be customized. You can override values using the `--set` flag or by providing a custom values file:

```bash
helm install read-realm . -f custom-values.yaml
```

### Important Configuration Options

#### Keycloak

- `mysql.enabled`: Enable/disable MySQL database for Keycloak
- `configMap.data`: Environment variables for Keycloak
- `volumes.realms.enabled`: Enable/disable realm import

#### Kafka

- `configMap.data`: Environment variables for Kafka
- `persistence.enabled`: Enable/disable persistent storage

#### Schema Registry

- `configMap.data`: Environment variables for Schema Registry

#### Kafka UI

- `configMap.data`: Environment variables for Kafka UI

## Dependencies

- Schema Registry depends on Kafka
- Kafka UI depends on Kafka and Schema Registry

When using the umbrella chart, these dependencies are managed automatically.
