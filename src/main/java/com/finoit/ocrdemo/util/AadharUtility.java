package com.finoit.ocrdemo.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.finoit.ocrdemo.model.AadharModel;
@Component
public class AadharUtility {

	public AadharModel convertResponseToAadharModel(String responseDescription) {
		if(responseDescription.contains("Enrolment")){
			return parseFullAadharResponse(responseDescription);
		}else{
			return parseSmallAadharResponse(responseDescription);
		}


	}

	private AadharModel parseSmallAadharResponse(String responseDescription) {


		String[] allDataParsed = responseDescription.split("\\n");
		ArrayList<String>  allData = new ArrayList<String>(Arrays.asList(allDataParsed));

		//Check if Government of India is coming
		if(!allData.get(1).contains(":")){
			allData.remove(0);
		}



		AadharModel model = new AadharModel();

		//Name
		model.setName(allData.get(0));

		//DOB
		String dummyDob = allData.get(1).trim();
		String[] dob = dummyDob.split(":");
		if(dob.length ==2){
			model.setDob(dob[1].trim());	
		}else{
			model.setDob(dummyDob);
		}

		//Gender
		String dummyGender = allData.get(2).trim();
		String[] gender = dummyGender.split("/");
		if(gender.length ==2){
			model.setGender(gender[1].trim());	
		}else{
			String[] genderSpace = dummyGender.split(" ");
			if(genderSpace.length ==2){
				model.setGender(genderSpace[1].trim());
			}else{
				model.setGender(dummyGender);
			}
		}


		//Aadhar Number
		String dummyAadharNumber = allData.get(3).trim();
		if(dummyAadharNumber.length()>=14)
			model.setAadharNo(dummyAadharNumber.substring(0, 14));
		else
			model.setAadharNo(dummyAadharNumber);


		//Address
		Pattern zipPattern = Pattern.compile("(\\d{6})");		
		StringBuffer dummyAddress = new StringBuffer();
		for(int i=4; i<allData.size(); i++){
			if(allData.get(i).contains("Address")){
				int k=i+1;
				while(k != allData.size()-1 ){
					dummyAddress.append(allData.get(k)+" ");
					Matcher zipMatcher = zipPattern.matcher(allData.get(k));
					if (zipMatcher.find()) {
						break;
					}
					else{						
						k++;
					}
				}

				break;
			}


		}
		model.setAddress(dummyAddress.toString());

		return model;

	}

	private AadharModel parseFullAadharResponse(String responseDescription) {



		String[] allDataParsed = responseDescription.split("\\n");
		ArrayList<String>  allData = new ArrayList<String>(Arrays.asList(allDataParsed));

		AadharModel model = new AadharModel();

		int k=0;
		for(int i=0; i< allData.size(); i++){
			if(allData.get(i).contains(":") && !allData.get(i).contains(".")){
				k=i;
			}

		}

		if(k!=0){

			//Name
			model.setName(allData.get(k-1));

			//DOB
			String dummyDob = allData.get(k).trim();
			String[] dob = dummyDob.split(":");
			if(dob.length ==2){
				model.setDob(dob[1].trim());	
			}else{
				model.setDob(dummyDob);
			}

			//Gender
			String dummyGender = allData.get(k+1).trim();
			String[] gender = dummyGender.split("/");
			if(gender.length ==2){
				model.setGender(gender[1].trim());	
			}else{
				String[] genderSpace = dummyGender.split(" ,");
				if(genderSpace.length ==2){
					model.setGender(genderSpace[1].trim());
				}else{
					model.setGender(dummyGender);
				}
			}


			//Aadhar Number
			if(responseDescription.contains("electronically")){
				
				Pattern zipPattern = Pattern.compile("\\d{4}\\s\\d{4}\\s\\d{4}$");
				Matcher zipMatcher = zipPattern.matcher(allData.get(allData.size()-1));
				if (zipMatcher.find()) {
					model.setAadharNo(allData.get(allData.size()-1));
				}else{
					Matcher zipMatcher1 = zipPattern.matcher(allData.get(allData.size()-2));
					if (zipMatcher1.find()) {
						model.setAadharNo(allData.get(allData.size()-2));
					}
				}
				
			}else{

				String dummyAadharNumber = allData.get(k+2).trim();
				if(dummyAadharNumber.length()>=14)
					model.setAadharNo(dummyAadharNumber.substring(0, 14));
				else
					model.setAadharNo(dummyAadharNumber);
			}

			//Address
			int nameFirstIndex = 0;
			for(int i=0; i< allData.size(); i++){
				if(allData.get(i).contains(allData.get(k-1))){
					nameFirstIndex=i;
					break;
				}

			}


			Pattern zipPattern = Pattern.compile("(\\d{6})");		
			StringBuffer dummyAddress = new StringBuffer();
			for(int i=nameFirstIndex+1; i<allData.size(); i++){

				dummyAddress.append(allData.get(i)+" ");
				Matcher zipMatcher = zipPattern.matcher(allData.get(i));
				if (zipMatcher.find()) {
					break;
				}

			}
			model.setAddress(dummyAddress.toString());

			return model;
		}

		return null;

	}

	/**
	 * To convert Pdf File to Image
	 * @param multipartFile
	 * @return
	 */
	public byte[] PdfToImage(MultipartFile multipartFile){
		try{

			File file = convert(multipartFile);
			PDDocument document = PDDocument.load(file);
			PDPage pd;

			PDFRenderer pdfRenderer = new PDFRenderer(document);
			for (int page = 0; page < document.getNumberOfPages(); ++page)
			{


				pd = document.getPage(page);
				//pd.setCropBox(new PDRectangle(100, 100,100,100));
				BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write( bim, "jpg", baos );
				baos.flush();
				byte[] imageInByte = baos.toByteArray();
				//ImageIOUtil.writeImage(bim, "C:\\Users\\emp279\\Desktop\\" + (page+1) + ".png", 300);
				baos.close();

				return imageInByte;



			}


		}catch (Exception ex){
			JOptionPane.showMessageDialog(null, ex.getStackTrace());

		}
		return null;

	}


	/*
	 * To convert MultipartFile to File
	 */
	private File convert(MultipartFile file) throws IOException
	{    
		File convFile = new File(file.getOriginalFilename());
		convFile.createNewFile(); 
		FileOutputStream fos = new FileOutputStream(convFile); 
		fos.write(file.getBytes());
		fos.close(); 
		return convFile;
	}
}
