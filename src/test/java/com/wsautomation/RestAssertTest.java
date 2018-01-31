package com.wsautomation;

import org.ajbrown.namemachine.NameGenerator;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

import org.ajbrown.namemachine.Gender;
import org.ajbrown.namemachine.Name;

public class RestAssertTest 
{
	//@Test(priority=1)
	public void getCatalogue() 
	{
		com.jayway.restassured.response.Response catalogue = RestAssured.get("/catalogue");
		String responce = catalogue.asString();
		System.out.println("Catalogue Responce -----> "+ responce);
		JsonArray jsonArray = (JsonArray) new JsonParser().parse(responce);
		int size = jsonArray.size();
		for (int i = 0; i < size; i++) 
		{
			JsonObject jsonObject = (JsonObject) jsonArray.get(i);
			JsonElement id = jsonObject.get("id");
			Assert.assertEquals(true, id.getAsString()!=null&& !id.getAsString().trim().isEmpty());
		}
	}
	
	@Parameters({"catalogueID"})
	@Test(priority=2)
	public void getIteamFromCatalogueID(@Optional("catalogueID") String catalogueID1) throws ParseException 
	{
		catalogueID1 = "03fef6ac-1896-4ce8-bd69-b798f85c6e0b";
		
		Response response=RestAssured
			.given()
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
			.when()
			.get("/catalogue/"+ catalogueID1);
		
		System.out.println("$$$$$$$$$$$$"+response.getBody().asString().contains(catalogueID1));
		@SuppressWarnings("static-access")
		String str = response.getBody().asString().valueOf(catalogueID1);
		System.out.println("@@@@@@@@@@@@@@" + str);
		response.then()
				.assertThat().statusCode(200);
		
		//response.then().assertThat().body(str, Matchers.equalTo(catalogueID1));
		JSONParser parser = new JSONParser(); 
		str.replaceAll("\"(\\w+)\":", "$1:");
		JSONObject json = (JSONObject) parser.parse(str);
		String id = json.getString("id").toString();
		System.out.println("my id:######"+id);
		response.then().assertThat().body(Matchers.equalTo(str), Matchers.equalTo(catalogueID1));
	}
	
	//@Test(priority=3)
	public void registerUser() 
	{
		NameGenerator generator = new NameGenerator();
		Name name =  generator.generateName(Gender.MALE);
		
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("firstName", name.getFirstName());
		jsonMap.put("lastName",name.getLastName());
		jsonMap.put("password","nikhil@6699");
		jsonMap.put("username", "demo"+name.getFirstName());
		
		RestAssured
		.given()
			.contentType("application/json")
			.accept("application/json")
			.body(new Gson().toJson(jsonMap))
		.when()
		.post("/register")
		.then()
			.assertThat().statusCode(200);
		
		Response loginResponce = RestAssured
			.given()
				.contentType("application/json")
				.auth()
				.preemptive()
				.basic("demo" + name.getFirstName(), "nikhil@6699")
			.when()
			.get("/login");
			
		Assert.assertEquals(true, loginResponce.getStatusCode()==200);
		
		String userID = loginResponce.getCookies().get("logged_in");
		String mdSid = loginResponce.getCookies().get("ms.sid");
		
		RestAssured
			.given()
				.header("content-type","application.json")
				.accept("application/json")
				.cookie("logged_in",userID)
				.cookie("md.sid",mdSid)
			.when()
				.get("/customers/"+ userID)
				.then()
					.assertThat().statusCode(200);
		System.out.println("Responce >>>>>>"+loginResponce.getBody().asString());
					//.assertThat().body("firstName", Matchers.equalTo(name.getFirstName()))
					//.assertThat().body("lastName", Matchers.equalTo(name.getLastName()));
	}
	
	//@Test(priority = 4)
	public void basicAuth()
	{
		RestAssured
			.given()
				.auth()
				.preemptive()
				.basic("nikhil", "nikhil@6699")
			.when()
			.get("/login")
			.then()
				.assertThat().statusCode(200)
				.assertThat().body(Matchers.equalTo("Cookie is set"));
	}
	
	//@Test(priority = 5)
	public void sessionWS()
	{
		String productID = "3395a43e-2d88-40de-b95f-e00e1502085b";
		//Double productUnitPrice = 15.0;
		
		Response loginResponce = RestAssured
				.given()
					.auth()
					.preemptive()
					.basic("nikhil", "nikhil@6699")
				.when()
				.get("/login");
		Assert.assertEquals(true, loginResponce.statusCode()==200);
		
		String userID = loginResponce.getCookies().get("logged_in");
		String mdSid = loginResponce.getCookies().get("md.sid");
		
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("id", productID);
		
		RestAssured
			.given()
				.contentType("application/json")
				.accept("application/json")
					.body(jsonMap)
					.cookie("logged_in",userID)
					.cookie("md.sid",mdSid)
				.when()
					.post("/cart")
					.then()
						.assertThat().statusCode(201);
		
		
		RestAssured
			.given()
				.header("Content-type", "application/json")
				.cookie("logged_in",userID)
				.cookie("md.sid",mdSid)
			.when()
			.get("/cart")
			.then()
				.assertThat().statusCode(200);
				//.assertThat().body("[0].itemId",Matchers.equalTo(productID))
				//.assertThat().body("[0].unitPrice",Matchers.equalTo(productUnitPrice));
			 
	}
	@BeforeClass
	private static void getBaseURI()
	{
		RestAssured.baseURI = "http://10.12.40.220";
		RestAssured.defaultParser = Parser.JSON;
	}
}

