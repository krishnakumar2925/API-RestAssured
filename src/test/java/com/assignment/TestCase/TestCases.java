package com.assignment.TestCase;

import org.testng.annotations.Test;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import static org.hamcrest.Matchers.equalTo;

import org.hamcrest.Matchers;

import static org.hamcrest.Matchers.containsString;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.assignment.Util.Baseclass;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ResponseOptions;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class TestCases extends Baseclass{
	
	//RequestSpecification httqReq;
	public static JSONObject requestBody;
	public static String userId;
	public static String token;
	public static String isbn;
	public static String bookTitle;
	

	@BeforeClass(alwaysRun=true)
	public void setUp() {
		init();
		
	}
	
	
	
	@Test(priority=1)
	public void userRegistration() {
		requestBody= parameters();
		
		   Response res= (Response) RestAssured.given()
				.contentType(ContentType.JSON)
				.body(requestBody.toString())
			.when()
				.post(User_Registration)
			.then()
				.extract().response();
			
		   int code=res.getStatusCode();
		   if(code==201) {
			   userId=res.path("userID");
			   //normal assert
			   Assert.assertTrue(res.getHeader("Content-Type").contains("application/json"), "Content-Type mismatch!");
			   Assert.assertTrue(res.body().asString().contains(requestBody.get("userName").toString()));
			   System.out.println("User registered successfully. User ID: " + userId);
		   }else if(code==406) {
			   String errorMessage = res.path("message");
	            System.out.println("Registration failed: " + errorMessage);

	            //DSL assert
	            res.then().assertThat().body("message",equalTo( "User exists!"));
		   }
		   else {
	            // Handle unexpected status codes
	            System.out.println("Unexpected response: " + code);
	            System.out.println("Response body: " + res.getBody().asString());
	        }
	}
	
	
	@Test(priority=2)
	public void generateToken() {
		
		requestBody=parameters();
		
		Response request=(Response) RestAssured.given()
				.contentType(ContentType.JSON)
				.body(requestBody.toString())
		.when()
				.post(Generate_Token)
		.then()
				.extract().response();
		
		int code=request.getStatusCode();
		String status=request.path("status");
		if(code==200) {
			token=request.path("token");
			//assertion
			request.then()
				.assertThat()
				.header("Content-Type", equalTo("application/json; charset=utf-8"))
				.body("status",equalTo("Success"))
				.body("result", equalTo("User authorized successfully."));
		}else if(code==400) {
			token=request.path("token");
			//assertion
			request.then()
				.assertThat()
				.header("Content-Type", equalTo("application/json; charset=utf-8"))
				.body("status",equalTo("Failed"))
				.body("result", equalTo("User authorization failed."));
		}
		else {
            // Handle unexpected status codes
            System.out.println("Unexpected response: " + code);
            System.out.println("Response body: " + request.getBody().asString());
        }
		
		System.out.println("Token generation :"+ status);
		
		
	}
	
	@Test(priority=3)
	public void getAllBooks() {
		Response res=(Response) RestAssured.given()
				.contentType(ContentType.JSON)
			.when()
				.get(Books_Store)
			.then()
				.assertThat()
				.header("Content-Type", equalTo("application/json; charset=utf-8"))
				.statusCode(200)
				.extract().response();
		
		isbn=res.path("books[0].isbn");
		bookTitle=res.path("books[0].title");
		System.out.println("ISBN: "+isbn);
		System.out.println("Book title is: "+bookTitle);
		
		
	}
	
	@Test(priority=4)
	public void getBookByIsbn() {
		Response res=(Response) RestAssured.given()
				.contentType(ContentType.JSON)
				.queryParam("ISBN", isbn)
			.when()
				.get(Books_Store)
			.then()
				.extract().response();
		
		int code=res.getStatusCode();
		if(code==200) {
			//assertion
			res.then()
				.assertThat()
				.header("Content-Type", equalTo("application/json; charset=utf-8"))
				.body("isbn", equalTo(isbn))
				.body("title", equalTo(bookTitle));
			
			System.out.println("response code: " + code);
			System.out.println(res.getBody().asPrettyString());
		}else if(code==400) {
			//assertion
			res.then()
				.assertThat()
				.header("Content-Type", equalTo("application/json; charset=utf-8"))
				.body("code",equalTo("1205"))
				.body("message", containsString("not available"));
			
			System.out.println("response code: " + code);
			System.out.println(res.getBody().asPrettyString());
		}
		else {
            // Handle unexpected status codes
            System.out.println("Unexpected response: " + code);
            System.out.println("Response body: " + res.getBody().asString());
        }
		
				
	}
	//,dependsOnMethods= {"userRegistration","generateToken","getAllBooks"}
	
	
	@Test(priority=5)
	public void postRequestAuthentication() {
		JSONObject requestBody = new JSONObject();
        requestBody.put("userId", userId);

        // Creating JSON Array for collectionOfIsbns
        JSONArray isbnArray = new JSONArray();
        JSONObject isbnObject = new JSONObject();
        isbnObject.put("isbn", isbn);
        isbnArray.add(isbnObject);
        

        requestBody.put("collectionOfIsbns", isbnArray);

		
		Response res=(Response) RestAssured.given()
				.auth()
				.oauth2(token)
				.contentType(ContentType.JSON)
				.body(requestBody.toString())
			.when()
				.post(Books_Store)
			.then()
				.extract().response();
		int code=res.getStatusCode();
		if(code==201) {
			//assertion
			res.then()
				.assertThat()
				.header("Content-Type", equalTo("application/json; charset=utf-8"))
				.body("isbn", equalTo(isbn));		
			
			System.out.println("response code: " + code);
			System.out.println(res.getBody().asPrettyString());
		}else if(code==400) {
			//assertion
			res.then()
				.assertThat()
				.header("Content-Type", equalTo("application/json; charset=utf-8"))
				.body("code",equalTo("1205"))
				.body("message", containsString("not available"));
			
			System.out.println("response code: " + code);
			System.out.println(res.getBody().asPrettyString());
		}else if(code==401) {
			//assertion
			res.then()
				.assertThat()
				.header("Content-Type", equalTo("application/json; charset=utf-8"))
				.body("code",equalTo("1207"))
				.body("message", containsString("User"));
			
			System.out.println("response code: " + code);
			System.out.println(res.getBody().asPrettyString());
		}
		else {
            // Handle unexpected status codes
            System.out.println("Unexpected response: " + code);
            System.out.println("Response body: " + res.getBody().asString());
        }
		
				
	}
	
	@Test(priority=6)
	public void putRequestAuthentication() {
		requestBody= parameters();
		
		JSONObject requestBody = new JSONObject();
		requestBody.put("userId", userId);
		requestBody.put("isbn", isbn);
		
		Response res=(Response) RestAssured.given()
				.auth()
				.oauth2(token)
				.contentType(ContentType.JSON)
				.body(requestBody.toString())
			.when()
				.put(Books_Store+"9781449365035")
			.then()
				.extract().response();
		
		
				int code=res.getStatusCode();
				if(code==200) {
					//assertion
					res.then()
						.assertThat()
						.header("Content-Type", equalTo("application/json; charset=utf-8"))
						.body("userId", equalTo(userId))
						.body("username", equalTo(requestBody.get("userName")))
						.body("books[0].isbn", equalTo(isbn))
						.body("books[0].title", equalTo(bookTitle));
					
					System.out.println("response code: " + code);
					System.out.println(res.getBody().asPrettyString());
				}else if(code==400) {
					//assertion
					res.then()
						.assertThat()
						.header("Content-Type", equalTo("application/json; charset=utf-8"))
						.body("code",equalTo("1205"))
						.body("message", containsString("not available"));
					
					System.out.println("response code: " + code);
					System.out.println(res.getBody().asPrettyString());
				}else if(code==401) {
					//assertion
					res.then()
						.assertThat()
						.header("Content-Type", equalTo("application/json; charset=utf-8"))
						.body("code",equalTo("1207"))
						.body("message", containsString("User"));
					
					System.out.println("response code: " + code);
					System.out.println(res.getBody().asPrettyString());
				}
				else {
		            // Handle unexpected status codes
		            System.out.println("Unexpected response: " + code);
		            System.out.println("Response body: " + res.getBody().asString());
		        }
				
	}
	
	@Test(priority=7)
	public void DeleteRequestAuthentication() {
		requestBody= parameters();
		
		JSONObject requestBody = new JSONObject();
		requestBody.put("isbn", isbn);
		requestBody.put("userId", userId);
		
		
		Response res=(Response) RestAssured.given()
				.auth()
				.oauth2(token)
				.contentType(ContentType.JSON)
				.body(requestBody.toString())
			.when()
				.delete()
			.then()
				.extract().response();
		
		int code=res.getStatusCode();
		if(code==204) {
			//assertion
			res.then()
				.assertThat()
				.header("Content-Type", equalTo("application/json; charset=utf-8"))
				.body("userId", equalTo(userId))
				.body("isbn", equalTo(isbn))
				.body("message", Matchers.notNullValue());
			
			System.out.println("response code: " + code);
			System.out.println(res.getBody().asPrettyString());
		}else if(code==400) {
			//assertion
			res.then()
				.assertThat()
				.header("Content-Type", equalTo("application/json; charset=utf-8"))
				.body("code",equalTo("1206"))
				.body("message", containsString("not available"));
			
			System.out.println("response code: " + code);
			System.out.println(res.getBody().asPrettyString());
		}else if(code==401) {
			//assertion
			res.then()
				.assertThat()
				.header("Content-Type", equalTo("application/json; charset=utf-8"))
				.body("code",equalTo("1207"))
				.body("message", containsString("User"));
			
			System.out.println("response code: " + code);
			System.out.println(res.getBody().asPrettyString());
		}
		else {
            // Handle unexpected status codes
            System.out.println("Unexpected response: " + code);
            System.out.println("Response body: " + res.getBody().asString());
        }
				
	}
	
	 @AfterClass(alwaysRun=true)
	    public void tearDown() {
	        // Reset base URI and base path
	        RestAssured.reset();

	        // Additional cleanup (if needed)
	        System.out.println("Teardown Complete");
	    }

}
