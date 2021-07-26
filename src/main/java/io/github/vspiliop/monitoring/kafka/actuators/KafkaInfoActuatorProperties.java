package io.github.vspiliop.monitoring.kafka.actuators;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;


@ConfigurationProperties(prefix = "management.info.kafka", ignoreUnknownFields = false)
@Data
public class KafkaInfoActuatorProperties {

  /**
   * Time to wait for a response from the cluster description operation.
   */
  private Duration responseTimeout = Duration.ofMillis(100);

  private String brokerConfigurationProperty = KafkaInfoActuator.REPLICATION_PROPERTY;

  private boolean enabled = false;

}
