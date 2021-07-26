[![License](https://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.vspiliop.monitoring/kafka-actuators/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.vspiliop.monitoring/kafka-actuators)

### kafka-actuators

A Spring Boot `info` actuator that displays the health of the Kafka cluster, based on the number of currently running kafka nodes and the configure broker setting `min.insync.replicas`.

Examples:

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
### Usage

#### Spring KafkaAdmin is required

KafkaAdmin is also created from Spring org.springframework.boot.autoconfigure.kafka.EnableAutoConfiguration. Alternatively, use sth like this:

```
@Slf4j
@Configuration
public class KafkaActuatorsConfiguration {
	
	@Autowired
	private KafkaStreamsProperties kafkaStreamsProperties;
	
	@Primary
	@Bean
	public KafkaAdmin kafkaAdmin() {  
	    return new KafkaAdmin(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaStreamsProperties.getAddress()));
	}

}
```

#### Configuration properties

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
  <version>0.0.1</version>
</dependency>
```
