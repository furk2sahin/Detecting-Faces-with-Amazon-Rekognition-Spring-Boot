package com.amazon.rekognition.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface AmazonRekognitionService {
    byte[] detectFaces(MultipartFile multipartFile);
}