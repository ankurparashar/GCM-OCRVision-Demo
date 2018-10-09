package com.finoit.ocrdemo.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.finoit.ocrdemo.model.AadharModel;
import com.finoit.ocrdemo.util.AadharUtility;
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


	@Autowired
	AadharUtility aadharUtility;

	@RequestMapping("/")
	public ModelAndView firstPage(Map<String, Object> model) {

		return new ModelAndView("welcome");
	}

	/**
	 * To handle parsing of Aadhar card
	 * @param model
	 * @param file
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@RequestMapping(value = "/analyze", method = RequestMethod.POST)
	@ResponseBody
	public AadharModel analyzeAadhar(Map<String, Object> model,@RequestParam("fileName") MultipartFile file) throws IllegalStateException, IOException {

		AadharModel aadharModel = null;
		ByteString imgBytes = null;

		// Instantiates a client
		try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {

			// Reads the image file into memory
			String contentType = file.getContentType();
			if (contentType.equals("image/jpeg") || contentType.equals("image/png")) {
				
				byte[] data = file.getBytes(); 
				imgBytes = ByteString.copyFrom(data);
			
			}else if(contentType.equals("application/pdf")){
				
				byte[] data = aadharUtility.PdfToImage(file);
				imgBytes = ByteString.copyFrom(data);
				
			}else{
				return null;
			}





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
					return null;
				}

				if(res.getTextAnnotationsCount() != 0){



					EntityAnnotation annotation = res.getTextAnnotations(0);

					String responseDescription =  annotation.getDescription();
					aadharModel = aadharUtility.convertResponseToAadharModel(responseDescription);
					model.put("aadharModel", aadharModel);
				}
			}
		}

		return aadharModel;

	}




}
