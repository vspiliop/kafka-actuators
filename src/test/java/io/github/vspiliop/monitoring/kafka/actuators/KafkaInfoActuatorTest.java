package io.github.vspiliop.monitoring.kafka.actuators;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.Info;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import java.util.Map;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;

public class KafkaInfoActuatorTest {

  private static final Long RESPONSE_TIME = 1000L;

  private EmbeddedKafkaBroker kafkaEmbedded;

  private KafkaAdmin kafkaAdmin;

  private void startKafka(int numberOfBrokers, int replicationFactor) throws Exception {
    kafkaEmbedded = new EmbeddedKafkaBroker(numberOfBrokers, true);
    kafkaEmbedded.brokerProperties(Map.of(KafkaInfoActuator.REPLICATION_PROPERTY, String.valueOf(replicationFactor)));
    kafkaEmbedded.afterPropertiesSet();
    kafkaAdmin = new KafkaAdmin(Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaEmbedded.getBrokersAsString()));
  }

  @AfterEach
  private void shutdownKafka() throws Exception {
    ofNullable(kafkaEmbedded).ifPresent(EmbeddedKafkaBroker::destroy);
  }

  @Test
  public void kafkaIsHealthy() throws Exception {
    startKafka(1, 1);
    KafkaInfoActuator infoActuator = new KafkaInfoActuator(this.kafkaAdmin, RESPONSE_TIME);

    Info.Builder builder = new Info.Builder();
    infoActuator.contribute(builder);

    Map map = (Map<String, String>) builder.build().getDetails().get("kafka");

    assertThat(map).isNotNull();
    assertThat(map).isNotEmpty();
    assertThat(map.get("status")).isEqualTo(KafkaInfoActuator.HEALTHY);

  }

  @Test
  public void kafkaIsUnhealthy() throws Exception {
    startKafka(1, 2);
    KafkaInfoActuator infoActuator = new KafkaInfoActuator(this.kafkaAdmin, RESPONSE_TIME);

    Info.Builder builder = new Info.Builder();
    infoActuator.contribute(builder);

    Map<String, String> map = (Map<String, String>) builder.build().getDetails().get("kafka");

    assertThat(map).isNotNull();
    assertThat(map).isNotEmpty();
    assertThat(map.get("status")).isEqualTo(KafkaInfoActuator.UNHEALTHY);
  }

  @Test
  public void kafkaIsDown() {
    kafkaAdmin = new KafkaAdmin(Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:34987"));
    KafkaInfoActuator infoActuator = new KafkaInfoActuator(this.kafkaAdmin, RESPONSE_TIME);

    Info.Builder builder = new Info.Builder();
    infoActuator.contribute(builder);

    Map<String, String> map = (Map<String, String>) builder.build().getDetails().get("kafka");

    assertThat(map).isNotNull();
    assertThat(map).isNotEmpty();
    assertThat(map.get("status")).isEqualTo(KafkaInfoActuator.DOWN);
  }

}
