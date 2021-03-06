[![License](https://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.vspiliop.monitoring/kafka-actuators/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.vspiliop.monitoring/kafka-actuators)

### kafka-actuators

A Spring Boot `info` actuator that displays the health of the Kafka cluster, based on the number of currently running
 kafka broker nodes, and a configurable broker setting (by default `min.insync.replicas` is used). If you use Kafka transactions,
 then it would make sense to use `transaction.state.log.replication.factor` instead.
 
Based on code that used to be part of the Spring Boot project, but was removed. Currently, there is no available Spring Boot Kafka actuator.

Examples:

- The currently running Kafka brokers are enough for the cluster to operate properly.

```
{
    "kafka": {
        "status": "HEALTHY",
        "clusterId": "jefgM2tdT7OjvthaNQK_1Q",
        "brokerId": "0",
        "minNumberOfNodesRequired": 1,
        "nodes": 1
    }
}
```

- The currently running Kafka brokers are less than the required number for the cluster to operate properly.

```
{
    "kafka": {
        "status": "UNHEALTHY",
        "clusterId": "jefgM2tdT7OjvthaNQK_1Q",
        "brokerId": "0",
        "minNumberOfNodesRequired": 2,
        "nodes": 1
    }
}
```

- The actuator failed to connect to the Kafka cluster.

```
{
    "kafka": {
        "status": "DOWN"
    }
}
```
### Usage

#### Spring KafkaAdmin is required

##### Option 1: Standard spring auto-configuration

KafkaAdmin is by default created from Spring `org.springframework.boot.autoconfigure.kafka.EnableAutoConfiguration` via spring properties.

```
spring:
  kafka:
    admin:
      client-id: admin-client-id
    bootstrap-servers: ${config.kafkaBootstrapAddress}
```

##### Option 2: Manual creation

```
@Slf4j
@Configuration
public class KafkaActuatorsConfiguration {
	
	@Autowired
	private KafkaProperties config;
	
	@Primary
	@Bean
	public KafkaAdmin kafkaAdmin() {  
	    return new KafkaAdmin(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, config.kafkaBootstrapAddress()));
	}

}
```

#### Configuration properties

| Parameter | Description | Example | Default Value | Required |
| --- | --- | --- | --- | --- |
| enabled | If the actuator is enabled or not | enabled: true | false | Required |
| responseTimeout | How long to wait for a reply from Kafka | responseTimeout: PT5S | 100 ms | Optional |
| brokerConfigurationProperty | Kafka broker property to compare to number of currently running nodes, for determining if the cluster is healthy or not | brokerConfigurationProperty: min.insync.replicas | min.insync.replicas | Optional |

```
management:
  info:
    kafka:
      enabled: true
      # format https://en.wikipedia.org/wiki/ISO_8601#Durations (P[n]Y[n]M[n]DT[n]H[n]M[n]S)
      responseTimeout: PT5S
      # by default min.insync.replicas is used
      brokerConfigurationProperty: min.insync.replicas
```

### Add as a dependency to your project

From Maven Central as follows:

```
<dependency>
  <groupId>io.github.vspiliop.monitoring</groupId>
  <artifactId>kafka-actuators</artifactId>
  <version>0.0.5</version>
</dependency>
```
