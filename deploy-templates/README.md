# user-settings-service-api

![Version: 0.0.1](https://img.shields.io/badge/Version-0.0.1-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 0.0.1](https://img.shields.io/badge/AppVersion-0.0.1-informational?style=flat-square)

Helm chart for Java application/service deploying

**Homepage:** <https://www.epam.com>

## Maintainers

| Name | Email | Url |
| ---- | ------ | --- |
| MDDA-BPD |  |  |

## Source Code

* <https://github.com>

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| affinity | object | `{}` |  |
| auditchecker.enabled | bool | `true` |  |
| auditchecker.image.pullPolicy | string | `"IfNotPresent"` |  |
| auditchecker.image.repository | string | `"busybox"` |  |
| auditchecker.image.tag | float | `1.32` |  |
| auditchecker.securityContext.allowPrivilegeEscalation | bool | `false` |  |
| auditchecker.securityContext.runAsGroup | int | `1000` |  |
| auditchecker.securityContext.runAsNonRoot | bool | `true` |  |
| auditchecker.securityContext.runAsUser | int | `1000` |  |
| automountServiceAccountToken | bool | `true` |  |
| autoscaling.hpa.enabled | bool | `false` |  |
| autoscaling.hpa.maxReplicas | int | `3` |  |
| autoscaling.hpa.minReplicas | int | `1` |  |
| clusterName | string | `""` |  |
| config.audit.kafka.schemaRegistryUrl | string | `""` |  |
| config.bpms.url | string | `"http://bpms:8080"` |  |
| config.data-platform.datasource.connectionTimeout | int | `30000` |  |
| config.data-platform.datasource.maxPoolSize | int | `10` |  |
| config.dso.url | string | `"https://digital-signature-ops:8080"` |  |
| config.form-submission-validation.url | string | `"http://form-submission-validation:8080"` |  |
| config.logging.aspect.enabled | bool | `false` |  |
| config.server.max-http-request-header-size | string | `"8KB"` |  |
| config.tracing.samplingProbability | float | `0.1` |  |
| container.extraEnvVars | string | `""` |  |
| container.extraVolumeMounts | string | `""` |  |
| container.extraVolumes | string | `""` |  |
| container.livenessProbe | string | `"httpGet:\n  path: /actuator/health/liveness\n  port: {{ .Values.container.port }}\n  httpHeaders:\n    - name: X-B3-Sampled\n      value: \"0\"\nfailureThreshold: 1\ninitialDelaySeconds: 180\nperiodSeconds: 20\nsuccessThreshold: 1\ntimeoutSeconds: 5\n"` |  |
| container.port | int | `8080` |  |
| container.readinessProbe | string | `"httpGet:\n  path: /actuator/health/readiness\n  port: {{ .Values.container.port }}\n  httpHeaders:\n    - name: X-B3-Sampled\n      value: \"0\"\nfailureThreshold: 30\ninitialDelaySeconds: 50\nperiodSeconds: 10\nsuccessThreshold: 1\ntimeoutSeconds: 1\n"` |  |
| container.resources.limits | object | `{}` |  |
| container.resources.requests | object | `{}` |  |
| container.securityContext.runAsUser | int | `1001` |  |
| container.startupProbe | string | `""` |  |
| dbchecker.enabled | bool | `true` |  |
| dbchecker.image.pullPolicy | string | `"IfNotPresent"` |  |
| dbchecker.image.repository | string | `"busybox"` |  |
| dbchecker.image.tag | float | `1.32` |  |
| dbchecker.securityContext.allowPrivilegeEscalation | bool | `false` |  |
| dbchecker.securityContext.runAsGroup | int | `1000` |  |
| dbchecker.securityContext.runAsNonRoot | bool | `true` |  |
| dbchecker.securityContext.runAsUser | int | `1000` |  |
| dnsPolicy | string | `"ClusterFirst"` |  |
| extraInitContainers | string | `""` |  |
| extraObjects | object | `{}` |  |
| extraTrafficExcludeOutboundPorts | string | `"5432,6379,6380,9093,9092,26379,26380"` |  |
| global.deploymentMode | string | `nil` |  |
| global.deploymentStrategy | string | `"Recreate"` |  |
| global.imagePullSecrets | list | `[]` |  |
| global.imageRegistry | string | `nil` |  |
| global.language | string | `"en"` |  |
| image.pullPolicy | string | `"IfNotPresent"` |  |
| image.pullSecrets[0] | string | `"regcred"` |  |
| image.repository | string | `"user-settings-service-api"` |  |
| image.tag | string | `"latest"` |  |
| istio.sidecar.enabled | bool | `true` |  |
| istio.sidecar.requestsLimitsEnabled | bool | `true` |  |
| istio.sidecar.resources.limits | object | `{}` |  |
| istio.sidecar.resources.requests | object | `{}` |  |
| kafka.bootstrapServers | string | `""` |  |
| kafka.clusterName | string | `"kafka-cluster"` |  |
| kafka.numPartitions | int | `15` |  |
| kafka.replicationFactor | int | `1` |  |
| kafka.schemaRegistrySecretName | string | `""` |  |
| kafka.secretName | string | `""` |  |
| kafka.ssl.enabled | bool | `true` |  |
| kafka.sslCertType | string | `"PEM"` |  |
| kafkachecker.enabled | bool | `true` |  |
| kafkachecker.image.pullPolicy | string | `"IfNotPresent"` |  |
| kafkachecker.image.repository | string | `"busybox"` |  |
| kafkachecker.image.tag | float | `1.32` |  |
| kafkachecker.securityContext.allowPrivilegeEscalation | bool | `false` |  |
| kafkachecker.securityContext.runAsGroup | int | `1000` |  |
| kafkachecker.securityContext.runAsNonRoot | bool | `true` |  |
| kafkachecker.securityContext.runAsUser | int | `1000` |  |
| keycloak.certificatesEndpoint | string | `"/protocol/openid-connect/certs"` |  |
| keycloak.realms.mygov-biz | string | `"mygov-biz-portal"` |  |
| keycloak.url | string | `""` |  |
| lifecycleHooks | object | `{}` |  |
| nameOverride | string | `""` |  |
| nodeSelector | object | `{}` |  |
| otelExporter.endpoint | string | `"http://jaeger-collector.opentelemetry-operator:4318/v1/traces"` |  |
| platform.security.csrf.enabled | bool | `true` |  |
| podAnnotations | object | `{}` |  |
| podSecurityContext.fsGroup | int | `1001` |  |
| postgres.appSecretName | string | `"postgresql-app-secrets"` |  |
| postgres.database | string | `"settings"` |  |
| postgres.databaseOverwrite | string | `""` |  |
| postgres.host | string | `""` |  |
| postgres.integrationSecretName | string | `"postgresql-connection-details"` |  |
| postgres.port | string | `""` |  |
| replicas | int | `1` |  |
| schedulerName | string | `"default-scheduler"` |  |
| sentinel.host | string | `""` |  |
| sentinel.port | string | `""` |  |
| sentinel.secretName | string | `"redis-ssl-auth"` |  |
| sentinel.ssl.enabled | bool | `true` |  |
| sentinel.ssl.verifyMode | string | `"CA"` |  |
| service.nodePort | string | `""` |  |
| service.port | int | `8080` |  |
| service.type | string | `"ClusterIP"` |  |
| serviceAccount.create | bool | `true` |  |
| serviceAccount.name | string | `"user-settings-service-api"` |  |
| serviceMonitor.enabled | bool | `false` |  |
| serviceMonitor.interval | string | `"15s"` |  |
| serviceMonitor.scrapePath | string | `"/actuator/prometheus-text"` |  |
| terminationGracePeriodSeconds | int | `30` |  |
| tolerations | object | `{}` |  |
| verification.otp.timeToLive | int | `300` |  |