package senti.controller;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import senti.spider.Spider;

import java.io.*;

@RestController
@CrossOrigin
public class SpiderController {

    @GetMapping("api/repo-name")
    public ResponseEntity<Resource> beginSpiderWithRepo(String name, String beginId, String endId, String option, boolean deleteCode, boolean translate) throws FileNotFoundException {
        try {
            Spider.spiderRepo(name, Integer.parseInt(beginId), Integer.parseInt(endId), option, deleteCode, translate);
        } catch (IOException e) {
            String message = "您输入的仓库不存在";
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(new InputStreamResource(new ByteArrayInputStream(message.getBytes())));
        }

        // 合并id范围内的csv
        try {
            InputStreamResource resource = Spider.mergeCSVFiles(name, Integer.parseInt(beginId), Integer.parseInt(endId), option);
            String mergedFileName = name + "_" + beginId + "_" + endId + "_" + option + ".csv";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + mergedFileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            String message = "合并CSV文件时发生错误";
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(new InputStreamResource(new ByteArrayInputStream(message.getBytes())));
        }
    }

    @GetMapping("api/tag")
    public String getTagStr(String filename) throws FileNotFoundException {
        return Spider.getTags(filename);
    }

}
