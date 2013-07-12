package org.raxa.mobile.rest;

import org.apache.http.entity.StringEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class RestCaller {

    private final static org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(RestCaller.class.getName());

    public RestCaller(){
        ApiAuthRest.setURLBase("http://23.23.184.197:8080/openmrs/ws/rest/v1/");
        ApiAuthRest.setUsername("admin");
        ApiAuthRest.setPassword("Admin123");
    }

    public String addPerson(String givenName, String familyName, String gender, String age) throws Exception {

        StringEntity inputAddPerson = new StringEntity("{\"names\":[{\"givenName\": \""
                + givenName + "\",\"familyName\": \""+ familyName + "\"}],\"gender\":\"" + gender + "\",\"age\":" + age + "}");

        inputAddPerson.setContentType("application/json");
        Boolean response = ApiAuthRest.getRequestPost("person", inputAddPerson);
        LOGGER.info("AddPerson = " + response);

        if (response == true) {
            return "Person created Successfully on OpenMRS System!\n\n9) Menu\n10) Exit";
        }
        else {
            return "Error occurred while creating a new Person.\n\n9) Menu\n10) Exit\n\n";
        }
    }

    public String getPerson(String personName) throws Exception {

        String response2 = "";
        Object objSessionJson = JSONValue.parse( ApiAuthRest.getRequestGet("session"));
        JSONObject jsonObjectSessionJson= (JSONObject) objSessionJson;
        String sessionId = (String) jsonObjectSessionJson.get("sessionId");
        Boolean authenticated = (Boolean) jsonObjectSessionJson.get("authenticated");
        System.out.println("Session:"+sessionId+" Authenticated:"+authenticated);

        LOGGER.info("Search the persons that have name >> personName");
        Object obj = JSONValue.parse( ApiAuthRest.getRequestGet("person?q=" + personName));
        JSONObject jsonObject = (JSONObject) obj;
        JSONArray arrayResult = (JSONArray) jsonObject.get("results");

        int largoArray = arrayResult.size();
        int contador;
        for(contador=0; contador < largoArray; contador ++){
            JSONObject registro = (JSONObject) arrayResult.get(contador);
            String uuid = (String) registro.get("uuid");
            String display = (String) registro.get("display");
            LOGGER.info("Rows "+ contador + " => Result Persons UUID:" + uuid +" Display:"+display);

            response2 = uuid;
            JSONArray arrayResultLinks = (JSONArray)  registro.get("links");
            int largoArrayLinks = arrayResultLinks.size();
            int contadorLinks;
            for(contadorLinks=0; contadorLinks < largoArrayLinks; contadorLinks ++){
                JSONObject registroLink = (JSONObject) arrayResultLinks.get(contadorLinks);
                String uri = (String) registroLink.get("uri");
                String rel = (String) registroLink.get("rel");
                LOGGER.info("==>Record Row "+ contador + "."+ contadorLinks +" =>  URI:" + uri +" REL:"+rel);
            }
        }

        if (response2.length() > 10) {
            return "Yeap, This Person named: " + personName + " exists in the OpenMRS system! > UUID: " + response2 + "\n\n9) Menu\n10) Exit\n\n";
        }
        else {
            return "Sorry, this Person named: " + personName + "is not exist in the OpenMRS system.\n\n9) Menu\n10) Exit\n\n";
        }
    }
}
