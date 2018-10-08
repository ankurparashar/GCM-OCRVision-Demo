package com.javainuse.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

@Controller
public class TestController {

	@RequestMapping("/welcome")
	public ModelAndView firstPage(Map<String, Object> model) {
		
	
		return new ModelAndView("welcome");
	}

	@RequestMapping(value = "/analyze", method = RequestMethod.POST)
	public ModelAndView analyzeAadhar(Map<String, Object> model,@RequestParam("fileName") MultipartFile file) throws IllegalStateException, IOException {
		

		
		// Instantiates a client
	    try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {

	     
	      // Reads the image file into memory
	      byte[] data = file.getBytes();
	      //byte[] data = Files.readAllBytes(path);
	      ByteString imgBytes = ByteString.copyFrom(data);

	      // Builds the image annotation request
	      List<AnnotateImageRequest> requests = new ArrayList<>();
	      Image img = Image.newBuilder().setContent(imgBytes).build();
	      Feature feat = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();
	      AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
	          .addFeatures(feat)
	          .setImage(img)
	          .build();
	      requests.add(request);

	      // Performs label detection on the image file
	      BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
	      List<AnnotateImageResponse> responses = response.getResponsesList();

	      for (AnnotateImageResponse res : responses) {
	        if (res.hasError()) {
	          System.out.printf("Error: %s\n", res.getError().getMessage());
	          return new ModelAndView("result");
	        }

	        for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
	        	String responseDescription = (String) annotation.getAllFields().get("description");
	        	
	        }
	      }
	    }
		
	
		return new ModelAndView("result");
	}

	public File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException 
	{
	    File convFile = new File( multipart.getOriginalFilename());
	    multipart.transferTo(convFile);
	    return convFile;
	}
}
