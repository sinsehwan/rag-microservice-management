package com.example.log.deployer.service.deploy.service;

import com.example.log.deployer.service.deploy.template.YamlTemplates;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DeployService {

    // log deployment .zip 파일 생성
    public byte[] generateDeploymentZip(Long projectId) throws IOException {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos)) {

            addToZip(zos, "namespace.yaml", YamlTemplates.namespace());
            addToZip(zos, "filebeat-config.yaml", YamlTemplates.filebeatConfig());
            addToZip(zos, "filebeat.yaml", YamlTemplates.filebeat());
            addToZip(zos, "logstash-config.yaml", YamlTemplates.logstashConfig(projectId));
            addToZip(zos, "logstash.yaml", YamlTemplates.logstash());

            zos.close();
            return baos.toByteArray();
        }
    }

    private void addToZip(ZipOutputStream zos, String filename, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(filename));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }
}
