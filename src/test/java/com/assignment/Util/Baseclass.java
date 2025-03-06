package com.assignment.Util;

import org.json.simple.JSONObject;



import io.restassured.RestAssured;

public class Baseclass {
	
	public static final String User_Registration="Account/v1/User";
	public static final String Generate_Token="Account/v1/GenerateToken";
	public static final String Books_Store="BookStore/v1/Books";
	
	
	
	public static void init() {
		RestAssured.useRelaxedHTTPSValidation();
		RestAssured.baseURI="https://bookstore.toolsqa.com/";
	}
	
	public static JSONObject parameters() {
		JSONObject json=new JSONObject();
		json.put("userName","Krishnakumar2925");
		json.put("password","Kumar@123");
		return json;
	}

}
