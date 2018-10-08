package com.finoit.ocrdemo.util;

import org.springframework.stereotype.Component;

import com.finoit.ocrdemo.model.AadharModel;
@Component
public class AadharUtility {

	public AadharModel convertResponseToAadharModel(String responseDescription) {
		String[] allData = responseDescription.split("\\n");
		if(allData.length == 5){
			AadharModel model = new AadharModel();
			model.setName(allData[1]);
			String[] dob = allData[2].split(":");
			if(dob.length ==2){
				model.setDob(dob[1]);	
			}else{
				model.setDob(allData[2]);
			}
			
			String[] gender = allData[3].split(" ");
			if(gender.length ==2){
				model.setGender(gender[1]);	
			}else{
				model.setGender(allData[3]);
			}
			
			model.setAadharNo(allData[4]);
			return model;
		}else{
			AadharModel model = new AadharModel();
			model.setName(allData[0]);
			String[] dob = allData[1].split(":");
			if(dob.length ==2){
				model.setDob(dob[1]);	
			}else{
				model.setDob(allData[1]);
			}
			
			String[] gender = allData[2].split(" ");
			if(gender.length ==2){
				model.setGender(gender[1]);	
			}else{
				model.setGender(allData[2]);
			}
			
			model.setAadharNo(allData[3]);
			return model;
		}

		
	}

}
