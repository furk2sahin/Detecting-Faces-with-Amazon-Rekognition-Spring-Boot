package com.amazon.rekognition.service.impl;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.*;
import com.amazon.rekognition.service.AmazonRekognitionService;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Service
public class AmazonRekognitionServiceImpl implements AmazonRekognitionService {

    private AmazonRekognition amazonClient;

    @Autowired
    public void setAmazonClient(AmazonRekognition amazonClient) {
        this.amazonClient = amazonClient;
    }

    @Override
    public byte[] detectFaces(MultipartFile multipartFile){
        ByteBuffer imageBytes = null;

        try(InputStream inputStream = new ByteArrayInputStream(multipartFile.getBytes())){
            imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
        } catch (Exception e){
            System.err.println("Failed to load file");
            System.exit(1);
        }

        InputStream imageBytesStream = new ByteArrayInputStream(imageBytes.array());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedImage image = null;
        try {
            image = ImageIO.read(imageBytesStream);
            ImageIO.write(image, "jpg", baos);
        } catch (IOException e) {
            System.out.println("Failed to load file");
            System.exit(1);
        }

        int width = image.getWidth();
        int height = image.getHeight();

        List<FaceDetail> faceDetails = getFaceDetails(multipartFile);

        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setColor(Color.red);
        graphics2D.setStroke(new BasicStroke(3.0f));

        for(FaceDetail face : faceDetails){
            BoundingBox boundingBox = face.getBoundingBox();
            graphics2D.drawRect((int)(boundingBox.getLeft() * width), (int)(boundingBox.getTop() * height),
                    (int)(boundingBox.getWidth() * width), (int)(boundingBox.getHeight() * height));
        }

        graphics2D.dispose();
        ByteArrayOutputStream drawedImageBaos = new ByteArrayOutputStream();
        byte[] encodedStr = null;
        try {
            ImageIO.write(image, "jpg",
                    drawedImageBaos);
            byte[] drawedImageBytes = drawedImageBaos.toByteArray();
            encodedStr = Base64.getEncoder().encode(drawedImageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encodedStr;
    }

    public List<FaceDetail> getFaceDetails(MultipartFile multipartFile){
        DetectFacesRequest request;
        DetectFacesResult detectFacesResult;

        try{
            request = new DetectFacesRequest()
                    .withImage(new Image().withBytes(ByteBuffer.wrap(multipartFile.getBytes())))
                    .withAttributes(Attribute.ALL);
            detectFacesResult = amazonClient.detectFaces(request);
            return detectFacesResult.getFaceDetails();
        } catch (Exception e){
            return Collections.emptyList();
        }
    }
}
