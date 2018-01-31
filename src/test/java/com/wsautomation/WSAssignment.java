package com.wsautomation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.hamcrest.Matchers;
import org.testng.Reporter;
import org.testng.annotations.Test;
import org.testng.Assert;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.Response;

public class WSAssignment 
{
    
//   @Test(priority=1) 
	public void getCatlogue()
	{
                                
		Response res = RestAssured.get("http://10.12.40.220/catalogue");                        
        String response = res.asString();
        Reporter.log(" Status code is --> "+res.statusCode());
        Reporter.log(" Resonse body is --> "+res.asString());    
        Assert.assertEquals(200, res.getStatusCode());                      
        JsonArray jsonarray = (JsonArray) new JsonParser().parse(response);
        for(int i = 0;i < jsonarray.size(); i++)
        {
        	JsonObject object = (JsonObject)jsonarray.get(i);                                    
        	JsonElement id = object.get("id");           
        	Reporter.log("Id's are --> "+id);                                    
        	Assert.assertEquals(true, id != null );
        }
	}
                
	//@Test(priority=2) 
    public void getCatalogueId()
    {
    	String id="03fef6ac-1896-4ce8-bd69-b798f85c6e0b";                         
    	RestAssured.given().contentType(ContentType.JSON).accept(ContentType.JSON).
        when().get("http://10.12.40.220/catalogue/"+id).then().assertThat().statusCode(200);
    }
    
    @Test(priority=1) 
    public void registerUser()
    {                                                               
	     Random rnd = new Random(); // Initialize number generator
	     String firstname = "nikhil"; // Initialize the strings
	     String lastname = "bhalerao";
	     String result; // We'll be building on this string
	     // We'll take the first character in the first name
	     result = Character.toString(firstname.charAt(0)); // First char
	     if (lastname.length() > 5)
	    	 result += lastname.substring(0,5);
	     else
	    	 result += lastname; // You did not specify what to do, if the name is shorter than 5 chars
	     	result += Integer.toString(rnd.nextInt(99));
	
	     	////////////////////////////////////////////////////////////// 
	     	HashMap<String,String> map = new HashMap<String,String>();
	        map.put("firstName", firstname);
	        map.put("lastName",lastname);
	        map.put("password",result);
	        map.put("username",result);
	        RestAssured.given().
	        contentType(ContentType.JSON).
	        accept(ContentType.JSON).
	        body(new Gson().toJson(map)).
	        when().post("http://10.12.40.220/register").then().assertThat().statusCode(200);
	                                  
	        Response res = RestAssured.given().
	        contentType(ContentType.JSON).
	        auth().preemptive().basic(result, result).
	        when().
	        get("http://10.12.40.220/login");
	        Assert.assertEquals(200, res.statusCode());
	        String sign_id = res.getCookies().get("md.sid");
	        String user_id = res.getCookies().get("logged_in");
	        res = RestAssured.given().
	        header("content-type","application/json").
	        accept("application/json").
	        cookie("logged_in",user_id).
	        cookie("md.sid",sign_id).
	        when().get("http://10.12.40.220/customers?"+user_id);
	        Assert.assertEquals(200, res.statusCode());
    }
                   
    @Test(priority=2) 
    public void logIn()
    {
    	RestAssured.given().
        auth().preemptive().basic("nikhil.bhalerao", "nikhil@6699").
        when().get("http://10.12.40.220/login").
        then().assertThat().statusCode(200).and().
        assertThat().body("Cookie is set",Matchers.contains("Cookie is set"));
    }
                     
    @Test(priority=3)
    public void addToCart()
    {
	    //do the log in 
	    Response res = RestAssured.given().
	    auth().preemptive().basic("nikhil.bhalerao", "nikhil@6699").
	    when().get("http://10.12.40.220/login");
	    Assert.assertEquals(200, res.statusCode());
	    //add to the cart 
	    String mdSId = res.getCookies().get("md.sid");
	    String userId = res.getCookies().get("logged_in");
	    String product_id = "3395a43e-2d88-40de-b95f-e00e1502085b";
	    //Double unit_price = 18.0;
	    //put the product id into the map and send it into the body
	    Map<String,Object> map = new HashMap<String,Object>();
	    map.put("id", product_id);
	    RestAssured.given().
	    contentType("application/json").
	    accept("application/json").
	    body(map).
	    cookie("logged_in",userId).
	    cookie("md.sid",mdSId).
	    when().
	    post("http://10.12.40.220/cart").
	    then().
	    assertThat().statusCode(201);
	    //validate the added product from the cart 
	    res = RestAssured.given().
	    header("Content-Type","application/json").
	    body(map).
	    cookie("logged_in",userId).
	    cookie("md.sid",mdSId).
	    when().
	    get("http://10.12.40.220/cart");
	    Assert.assertEquals(200, res.getStatusCode());
	    String add_to_cart_response = res.getBody().asString();
	    Assert.assertEquals(true,add_to_cart_response.contains(product_id));
    }
                    
    @Test(priority=4)
    public void addAddress()
    {
    	//do the log in 
        Response res = RestAssured
        		.given()
        			.auth().preemptive().basic("nikhil.bhalerao", "nikhil@6699")
        		.when().get("http://10.12.40.220/login");
        	
        	Assert.assertEquals(200,res.getStatusCode());
        
        String mdSId = res.getCookies().get("md.sid");
        String userId = res.getCookies().get("logged_in");
        
        //put the adressb as map and post into the body as json array
        HashMap<String,Object> hashmap = new HashMap<String,Object>();
        hashmap.put("street", "Dhanori road");
        hashmap.put("number", "403");
        hashmap.put("country", "India");                      
        hashmap.put("city", "Pune");
        hashmap.put("postcode", "411047");
        hashmap.put("userID", userId);
        //add the address for 
        RestAssured
        .given()
        	.contentType("application/json")
        	.accept("application/json")
        	.cookie("logged_in",userId)
        	.cookie("md.sid",mdSId)
        .when()
        	.post("http://10.12.40.220/addresses")
        .then()
        	.assertThat().statusCode(200);
    }
                      
    @Test(priority=5)
    public void addCardDetails()
    	{
        	//do the log in 
    		Response res = RestAssured
    				.given()
    				.auth()
    					.preemptive()
    					.basic("nikhil.bhalerao", "nikhil@6699")
    				.when()
    					.get("http://10.12.40.220/login");
            	
    		Assert.assertEquals(200,res.getStatusCode());
            
    					String mdSId = res.getCookies().get("md.sid");
    					String userId = res.getCookies().get("logged_in");
            //  System.out.println(" md id "+mdSId+" login id "+userId);
            //put the adressb as map and post into the body as json array
            HashMap<String,String> hashmap = new HashMap<String,String>();
            hashmap.put("longNum", "102030405060708090");
            hashmap.put("expires", "12/20");
            hashmap.put("cvv", "333"); 
            hashmap.put("usreID", userId); 
            //add the card for 
            RestAssured
            .given()
            	.contentType("application/json")
            	.accept("application/json")
            	.cookie("logged_in",userId)
            	.cookie("md.sid",mdSId)
            	.body(hashmap)
            .when()
            	.post("http://10.12.40.220/cards")
            .then()
            	.assertThat().statusCode(200);
    	}
                       
    @Test(priority=6)
    public void placeAndVerifyOrders()
    {
    	//do the log in 
        Response res = RestAssured.given().
        auth().preemptive().basic("nikhil.bhalerao", "nikhil@6699").
        when().get("http://10.12.40.220/login");
        
        Assert.assertEquals(200,res.getStatusCode());
        String mdSId = res.getCookies().get("md.sid");
        String userId = res.getCookies().get("logged_in");
        //add the card for 
        res = RestAssured.given().
        contentType("application/json").
        accept("application/json").
        cookie("logged_in",userId).
        cookie("md.sid",mdSId).
        when().
        get("http://10.12.40.220/orders");
        Assert.assertEquals(201,res.getStatusCode());
        System.out.println(" Response body  of orders --> "+res.getBody().asString());
        Reporter.log(" Response body  of orders --> "+res.getBody().asString());
    }
                     
    public static void baseURI()
    {
    	RestAssured.baseURI = "http://10.12.40.220";
        RestAssured.defaultParser = Parser.JSON; // set the default parser for all response as json
    }
}