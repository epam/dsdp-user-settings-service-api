{{- define "user-settings-service-api.name" }}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "user-settings-service-api.chart" }}
{{- .Chart.Name }}-{{- .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "user-settings-service-api.labels" }}
helm.sh/chart: {{ include "user-settings-service-api.chart" . | quote }}
{{ include "user-settings-service-api.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
{{- end }}

{{- define "user-settings-service-api.selectorLabels" }}
app.kubernetes.io/name: {{ include "user-settings-service-api.name" . | quote }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
{{- end }}

{{- define "user-settings-service-api.serviceAccountName" }}
{{- if .Values.serviceAccount.create }}
{{- default (include "user-settings-service-api.name" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{- define "keycloak.urlPrefix" }}
{{- printf "%s%s%s" .Values.keycloak.url "/realms/" .Release.Namespace }}
{{- end }}

{{- define "issuer.mygov-biz" }}
{{- printf "%s-%s" (include "keycloak.urlPrefix" .) (index .Values.keycloak.realms "mygov-biz") }}
{{- end }}

{{- define "jwksUri.mygov-biz" }}
{{- printf "%s-%s%s" (include "keycloak.urlPrefix" .) (index .Values.keycloak.realms "mygov-biz") .Values.keycloak.certificatesEndpoint }}
{{- end }}

{{- define "user-settings-service-api.istioResources" }}
{{- if .Values.istio.sidecar.resources.limits.cpu }}
sidecar.istio.io/proxyCPULimit: {{ .Values.istio.sidecar.resources.limits.cpu | quote }}
{{- end }}
{{- if .Values.istio.sidecar.resources.limits.memory }}
sidecar.istio.io/proxyMemoryLimit: {{ .Values.istio.sidecar.resources.limits.memory | quote }}
{{- end }}
{{- if .Values.istio.sidecar.resources.requests.cpu }}
sidecar.istio.io/proxyCPU: {{ .Values.istio.sidecar.resources.requests.cpu | quote }}
{{- end }}
{{- if .Values.istio.sidecar.resources.requests.memory }}
sidecar.istio.io/proxyMemory: {{ .Values.istio.sidecar.resources.requests.memory | quote }}
{{- end }}
{{- end }}

{{- define  "database.name" -}}
{{- if .Values.postgres.databaseOverwrite -}}
{{- .Values.postgres.databaseOverwrite -}}
{{- else -}}
{{- $clusterName := .Values.clusterName | replace "-" "_" -}}
{{- $releaseNameSpace := .Release.Namespace | replace "-" "_" -}}
{{- printf "%s_%s_%s" $clusterName $releaseNameSpace .Values.postgres.database -}}
{{- end -}}
{{- end -}}
