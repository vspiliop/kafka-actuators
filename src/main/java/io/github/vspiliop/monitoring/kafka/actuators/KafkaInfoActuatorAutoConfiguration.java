package io.github.vspiliop.monitoring.kafka.actuators;

import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.time.Duration;


@Configuration
@ConditionalOnProperty(prefix = "management.info.kafka", name = "enabled", matchIfMissing = false)
@AutoConfigureAfter(KafkaAutoConfiguration.class)
@ConditionalOnBean(KafkaAdmin.class)
@EnableConfigurationProperties(KafkaInfoActuatorProperties.class)
public class KafkaInfoActuatorAutoConfiguration {

  private final KafkaAdmin admin;

  private final KafkaInfoActuatorProperties properties;

  KafkaInfoActuatorAutoConfiguration(KafkaAdmin admin, KafkaInfoActuatorProperties properties) {
    this.admin = admin;
    this.properties = properties;
  }

  @Bean
  @ConditionalOnMissingBean(name = "kafkaHealthActuator")
  public InfoContributor kafkaHealthActuator() {
    return createInfoActuator(admin);
  }

  protected InfoContributor createInfoActuator(KafkaAdmin source) {
    Duration responseTimeout = properties.getResponseTimeout();

    return new KafkaInfoActuator(source,
      responseTimeout == null ? 100L : responseTimeout.toMillis(),
      properties.getBrokerConfigurationProperty());
  }

}
