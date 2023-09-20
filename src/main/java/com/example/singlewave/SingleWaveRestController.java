package com.example.singlewave;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Base64;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.annotation.PostConstruct;

import reactor.core.publisher.Mono;

import com.google.common.io.ByteStreams;

@RestController
@CrossOrigin
public class SingleWaveRestController {
    String inputCsvPath;
    String outputCsvPath;
    String outputPngPath;
    
    @PostConstruct
    public void init() {
        inputCsvPath = "/Users/inayoshikoya/Documents/個人的勉強/SpringBoot3/SpringBoot3プログラミング入門/singlewave/src/main/resources/input.csv";
        outputCsvPath = "output.csv";
        outputPngPath = "output.png";
    }
    
    @CrossOrigin
    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Mono<HttpEntity<String>> handlingUpload(@RequestPart("file") FilePart uploadedFile) throws IOException, InterruptedException{
        savePartFile(uploadedFile).subscribe();
        ProcessBuilder processBuilder = new ProcessBuilder("python", "/Users/inayoshikoya/Documents/個人的勉強/SpringBoot3/SpringBoot3プログラミング入門/singlewave/src/main/java/com/example/singlewave/preprocess.py", inputCsvPath);
        Process process = processBuilder.start();
        process.waitFor();
        Thread.sleep(3000);
        ClassPathResource cpr = new ClassPathResource(outputPngPath);
        InputStream image = cpr.getInputStream();
        byte[] imageBarr = ByteStreams.toByteArray(image);
        String base64Data = Base64.getEncoder().encodeToString(imageBarr);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return Mono.just(new HttpEntity<String>(base64Data, headers));
    }

    @CrossOrigin
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Mono<Resource>> handlingDownload() throws IOException{
        Resource resource = new ClassPathResource(outputCsvPath);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(outputCsvPath,Charset.defaultCharset()).build().toString())
            .body(Mono.just(resource));
    }

    @CrossOrigin
    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public Mono<String> test() {
        return Mono.just("this is test.");
    }

    private Mono<Void> savePartFile(FilePart filePart) {
        File file = new File(inputCsvPath);
        return filePart.transferTo(file);
    }
}
