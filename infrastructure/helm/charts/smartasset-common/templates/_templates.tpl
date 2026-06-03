{{- define "smartasset-common.deployment" -}}
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "smartasset-common.fullname" . }}
  labels:
    {{- include "smartasset-common.labels" . | nindent 4 }}
spec:
  {{- if not .Values.autoscaling.enabled }}
  replicas: {{ .Values.replicaCount }}
  {{- end }}
  selector:
    matchLabels:
      {{- include "smartasset-common.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      {{- with .Values.podAnnotations }}
      annotations:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      labels:
        {{- include "smartasset-common.selectorLabels" . | nindent 8 }}
    spec:
      {{- with .Values.imagePullSecrets }}
      imagePullSecrets:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      containers:
        - name: {{ .Chart.Name }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          ports:
            - name: http
              containerPort: {{ .Values.service.port }}
              protocol: TCP
          {{- if .Values.envFrom }}
          envFrom:
            {{- toYaml .Values.envFrom | nindent 12 }}
          {{- end }}
          {{- if .Values.env }}
          env:
            {{- toYaml .Values.env | nindent 12 }}
          {{- end }}
          livenessProbe:
            httpGet:
              path: {{ .Values.probes.liveness.path | default "/actuator/health/liveness" }}
              port: http
            initialDelaySeconds: {{ .Values.probes.liveness.initialDelaySeconds | default 30 }}
            periodSeconds: {{ .Values.probes.liveness.periodSeconds | default 15 }}
          readinessProbe:
            httpGet:
              path: {{ .Values.probes.readiness.path | default "/actuator/health/readiness" }}
              port: http
            initialDelaySeconds: {{ .Values.probes.readiness.initialDelaySeconds | default 30 }}
            periodSeconds: {{ .Values.probes.readiness.periodSeconds | default 15 }}
          resources:
            {{- toYaml .Values.resources | nindent 12 }}
{{- end -}}

{{- define "smartasset-common.service" -}}
apiVersion: v1
kind: Service
metadata:
  name: {{ include "smartasset-common.fullname" . }}
  labels:
    {{- include "smartasset-common.labels" . | nindent 4 }}
spec:
  type: {{ .Values.service.type }}
  ports:
    - port: {{ .Values.service.externalPort | default .Values.service.port }}
      targetPort: {{ .Values.service.port }}
      protocol: TCP
      name: http
  selector:
    {{- include "smartasset-common.selectorLabels" . | nindent 4 }}
{{- end -}}

{{/*
Expand the name of the chart.
*/}}
{{- define "smartasset-common.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "smartasset-common.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "smartasset-common.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "smartasset-common.labels" -}}
helm.sh/chart: {{ include "smartasset-common.chart" . }}
{{ include "smartasset-common.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "smartasset-common.selectorLabels" -}}
app.kubernetes.io/name: {{ include "smartasset-common.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
