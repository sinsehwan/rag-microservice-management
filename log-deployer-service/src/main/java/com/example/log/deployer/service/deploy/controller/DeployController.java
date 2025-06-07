package com.example.log.deployer.service.deploy.controller;

import com.example.log.deployer.service.deploy.service.DeployService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class DeployController {

    private final DeployService deployService;

    @GetMapping("/download-config")
    public ResponseEntity<byte[]> downloadConfig(@RequestParam Long projectId) throws IOException{
        //projectId에 맞는 log deployment .zip 생성
        byte[] zipBytes = deployService.generateDeploymentZip(projectId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"config-" + projectId + ".zip\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(zipBytes.length)
                .body(zipBytes);
    }
}
