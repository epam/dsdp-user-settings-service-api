/*
 * Copyright 2022 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.settings.api.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SslOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableRedisRepositories(
    enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP,
    keyspaceConfiguration = RedisKeyspaceConfig.class)
@Configuration
@ConditionalOnProperty(value = "spring.redis.enabled", matchIfMissing = true)
@EnableConfigurationProperties(RedisSslProperties.class)
public class RedisConfig {

  private final Logger log = LoggerFactory.getLogger(RedisConfig.class);

  @Bean
  public RedisConnectionFactory redisConnectionFactory(
      RedisConnectionDetails connectionDetails,
      RedisProperties redisProperties,
      RedisSslProperties redisSslProperties,
      ObjectProvider<SslBundles> sslBundlesProvider) {

    var redisSentinelConfig = buildSentinelConfig(connectionDetails);
    var lettuceConfig = buildLettuceClientConfig(redisProperties, redisSslProperties,
        sslBundlesProvider);

    return new LettuceConnectionFactory(redisSentinelConfig, lettuceConfig);
  }

  @Bean
  public RedisTemplate<String, Object> newRedisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    return redisTemplate;
  }

  private RedisSentinelConfiguration buildSentinelConfig(RedisConnectionDetails connectionDetails) {
    var sentinelDetails = connectionDetails.getSentinel();
    if (sentinelDetails == null) {
      throw new IllegalStateException(
          "Redis Sentinel configuration is required but 'spring.data.redis.sentinel' is not set");
    }

    var nodes = sentinelDetails.getNodes();
    if (nodes == null || nodes.isEmpty()) {
      throw new IllegalStateException(
          "At least one Redis Sentinel node must be configured via"
              + " 'spring.data.redis.sentinel.nodes'");
    }

    var redisSentinelConfig = new RedisSentinelConfiguration();
    redisSentinelConfig.master(sentinelDetails.getMaster());
    nodes.forEach(node -> redisSentinelConfig.sentinel(node.host(), node.port()));
    redisSentinelConfig.setUsername(connectionDetails.getUsername());
    redisSentinelConfig.setPassword(connectionDetails.getPassword());

    log.info("Redis Sentinel configured: master='{}', nodes={}",
        sentinelDetails.getMaster(), nodes);
    return redisSentinelConfig;
  }

  private LettuceClientConfiguration buildLettuceClientConfig(
      RedisProperties redisProperties,
      RedisSslProperties redisSslProperties,
      ObjectProvider<SslBundles> sslBundlesProvider) {

    LettuceClientConfiguration.LettuceClientConfigurationBuilder lettuceConfigBuilder =
        LettuceClientConfiguration.builder();

    if (redisProperties.getSsl().isEnabled()) {
      log.info("Redis SSL is enabled, verifyMode={}", redisSslProperties.getVerifyMode());
      lettuceConfigBuilder.useSsl().verifyPeer(redisSslProperties.getVerifyMode());

      String sslBundleName = redisProperties.getSsl().getBundle();
      if (sslBundleName != null && !sslBundleName.isEmpty()) {
        SslBundles sslBundles = sslBundlesProvider.getIfAvailable();
        if (sslBundles == null) {
          throw new IllegalStateException(
              "SSL bundle '" + sslBundleName
                  + "' is configured via 'spring.data.redis.ssl.bundle'"
                  + " but no SslBundles bean is available in the application context");
        }

        SslBundle bundle = sslBundles.getBundle(sslBundleName);
        var sslOptionsBuilder = SslOptions.builder()
            .jdkSslProvider()
            .trustManager(bundle.getManagers().getTrustManagerFactory());

        var keyManagerFactory = bundle.getManagers().getKeyManagerFactory();
        if (keyManagerFactory != null) {
          sslOptionsBuilder.keyManager(keyManagerFactory);
        }

        lettuceConfigBuilder.clientOptions(
            ClientOptions.builder().sslOptions(sslOptionsBuilder.build()).build());

        log.info("Redis SSL bundle '{}' applied successfully", sslBundleName);
      }
    } else {
      log.info("Redis SSL is disabled");
    }

    return lettuceConfigBuilder.build();
  }

}
