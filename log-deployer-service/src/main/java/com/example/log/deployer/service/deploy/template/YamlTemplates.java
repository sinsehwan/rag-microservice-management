package com.example.log.deployer.service.deploy.template;

public class YamlTemplates {

    // namespace.yaml
    public static String namespace() {
        return """
                apiVersion: v1
                kind: Namespace
                metadata:
                  name: logging
                """;
    }

    // filebeat-config.yaml
    public static String filebeatConfig() {
        return """
                apiVersion: v1
                kind: ConfigMap
                metadata:
                  name: filebeat-config
                  namespace: logging
                data:
                  filebeat-config.yaml: |
                    filebeat.inputs:
                    - type: log
                      paths:
                        - /var/log/*.log
                      scan_frequency: 5s
                      ignore_older: 1h
                      close_inactive: 5m
                                
                    output.logstash:
                      hosts: ["logstash.logging.svc.cluster.local:5044"]
                """;
    }

    // filebeat.yaml
    public static String filebeat() {
        return """
                apiVersion: apps/v1
                kind: DaemonSet
                metadata:
                  name: filebeat
                  namespace: logging
                spec:
                  selector:
                    matchLabels:
                      app: filebeat
                  template:
                    metadata:
                      labels:
                        app: filebeat
                    spec:
                      containers:
                      - name: filebeat
                        image: docker.elastic.co/beats/filebeat:7.17.9
                        args: [
                          "-c", "/etc/filebeat.yaml",
                          "-e"
                        ]
                        volumeMounts:
                        - name: config
                          mountPath: /etc/filebeat.yaml
                          subPath: filebeat-config.yaml
                        - name: shared-logs
                          mountPath: /var/log
                        securityContext:
                          runAsUser: 0
                      volumes:
                      - name: config
                        configMap:
                          name: filebeat-config
                      - name: shared-logs
                        hostPath:
                          path: /var/log
                """;
    }

    // logstash.yaml
    public static String logstashConfig(Long projectId) {
        return """
                apiVersion: v1
                kind: ConfigMap
                metadata:
                  name: logstash-config
                  namespace: logging
                data:
                  logstash.conf: |
                    input {
                      beats {
                        port => 5044
                      }
                    }
                    output {
                      elasticsearch {
                        hosts => ["http://elasticsearch.logging.svc.cluster.local:9200"]
                        index => "logstash-%d-%%{+YYYY.MM.dd}"
                      }
                    }
                """.formatted(projectId);
    }

    // logstash.yaml
    public static String logstash() {
        return """
                apiVersion: apps/v1
                kind: Deployment
                metadata:
                  name: logstash
                  namespace: logging
                                
                spec:
                  replicas: 1
                  selector:
                    matchLabels:
                      app: logstash
                  template:
                    metadata:
                      labels:
                        app: logstash
                    spec:
                      containers:
                      - name: logstash
                        image: docker.elastic.co/logstash/logstash:7.17.9
                        ports:
                        - containerPort: 5044
                        volumeMounts:
                        - name: config-volume
                          mountPath: /usr/share/logstash/pipeline/
                      volumes:
                      - name: config-volume
                        configMap:
                          name: logstash-config
                                
                ---
                apiVersion: v1
                kind: Service
                metadata:
                  name: logstash
                  namespace: logging
                spec:
                  ports:
                  - port: 5044
                    targetPort: 5044
                  selector:
                    app: logstash
                """;
    }
}
