package io.github.vspiliop.monitoring.kafka.actuators;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.ConfigResource.Type;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

@Slf4j
public class KafkaInfoActuator implements InfoContributor {

  static final String REPLICATION_PROPERTY = "min.insync.replicas";

  public final static String HEALTHY = "HEALTHY";
  public final static String UNHEALTHY = "UNHEALTHY";
  public final static String DOWN = "DOWN";

  private final KafkaAdmin kafkaAdmin;
  private final DescribeClusterOptions describeOptions;
  private final String brokerConfigurationProperty;

  public KafkaInfoActuator(KafkaAdmin kafkaAdmin, long responseTimeout, String brokerConfigurationProperty) {
    Assert.notNull(kafkaAdmin, "KafkaAdmin must not be null");
    this.kafkaAdmin = kafkaAdmin;
    this.describeOptions = new DescribeClusterOptions().timeoutMs((int) responseTimeout);
    this.brokerConfigurationProperty = brokerConfigurationProperty;
  }

  public KafkaInfoActuator(KafkaAdmin kafkaAdmin, long responseTimeout) {
    this(kafkaAdmin, responseTimeout, REPLICATION_PROPERTY);
  }

  @Override
  public void contribute(Info.Builder builder) {
    try (AdminClient adminClient = AdminClient.create(this.kafkaAdmin.getConfigurationProperties())) {

      DescribeClusterResult result = adminClient.describeCluster(this.describeOptions);
      String brokerId = result.controller().get().idString();

      int replicationFactor = getMinimumRequiredReplicationFactor(brokerId, adminClient);
      int nodes = result.nodes().get().size();

      builder.withDetail("kafka", Map.of(
        "status", nodes >= replicationFactor ? HEALTHY : UNHEALTHY,
        "clusterId", result.clusterId().get(),
        "brokerId", brokerId,
        "nodes", nodes,
        "minNumberOfNodesRequired", replicationFactor));

    } catch (Exception ex) {
      log.warn("KafkaInfoIndicator actuator failed to get Kafka cluster information", ex);
      builder.withDetail("kafka", Map.of("status", DOWN));
    }
  }

  @SneakyThrows
  private int getMinimumRequiredReplicationFactor(String brokerId, AdminClient adminClient) {
    ConfigResource configResource = new ConfigResource(Type.BROKER, brokerId);
    Map<ConfigResource, Config> kafkaConfig = adminClient.describeConfigs(List.of(configResource)).all().get();
    Config brokerConfig = kafkaConfig.get(configResource);
    return parseInt(brokerConfig.get(brokerConfigurationProperty).value());
  }

}
