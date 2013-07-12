package org.raxa.mobile.app;

import hms.sdp.ussd.MchoiceUssdException;
import hms.sdp.ussd.MchoiceUssdMessage;
import hms.sdp.ussd.MchoiceUssdTerminateMessage;
import hms.sdp.ussd.client.MchoiceUssdReceiver;
import hms.sdp.ussd.client.MchoiceUssdSender;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.raxa.mobile.rest.RestCaller;

public class USSDReceiver extends MchoiceUssdReceiver {

    private ConcurrentMap<String, Object> map = new ConcurrentHashMap<String, Object>();
    private static org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(USSDReceiver.class);
    private RestCaller restCaller = new RestCaller();

    private final String[] menus = new String[]
            {"Welcome to Raxa App!\n------\n1) Add new Person\n2) " +
                    "Check Patient\n\n10) Exit\n\nChoose and option",
                    "Enter the Fname, Lname, Gender, Age (followed by spaces)\n\n9) Back\n10) Exit\n\n**Please wait a moment after submission**",
                    "Enter the Person Name\n\n9) Back\n10) Exit\n\n**Please wait a moment after submission**",
                    "Enter the PersonID\n\n9) Back\n10) Exit\n\n**Please wait a moment after submission**"};

    @Override
    public void onMessage(MchoiceUssdMessage ussd) {
        String userMessage = ussd.getMessage();
        String address = ussd.getAddress();
        String conversationId = ussd.getConversationId();
        String correlationId = ussd.getCorrelationId();

        System.out.println("User Message: " + userMessage);
        System.out.println("Address: " + address);
        LOGGER.info("Conversation ID: " + conversationId);
        LOGGER.info("Correlation ID: " + correlationId);

        try {
            MchoiceUssdSender ussdSender = new MchoiceUssdSender("http://127.0.0.1:8000/ussd/", "appid", "password");
            Map<String, Object> tempMap = (Map<String, Object>) map.get(address);
            if (map.containsKey(address)) {
                int selection = (Integer) tempMap.get("selection");
                int level = (Integer) tempMap.get("level");
                if ("1".equals(userMessage)) {
                    ussdSender.sendMessage(menus[1], address, conversationId, false);
                    tempMap.put("selection", 1);
                    tempMap.put("level", 1);

                } else if (selection == 1 && level == 1 || userMessage.length() == 10) {

                    try{

                        String givenName, familyName, gender, age = "";
                        String parts[] = {};
                        parts = userMessage.split(" ");

                        givenName = parts[0];
                        familyName = parts[1];
                        gender = parts[2];
                        age = parts[3];

                        String response = restCaller.addPerson(givenName, familyName, gender, age);
                        ussdSender.sendMessage(response, address, conversationId, false);
                        map.remove(address);

                    }catch (Exception e){
                        Logger.getLogger(USSDReceiver.class.getName()).log(Level.SEVERE, null, e);
                    }

                } else if ("2".equals(userMessage) && level == 0) {
                    ussdSender.sendMessage(menus[2], address, conversationId, false);
                    tempMap.put("selection", 2);
                    tempMap.put("level", 1);

//                } else if (selection == 2 && level == 1) {
//                    ussdSender.sendMessage(menus[3], address, conversationId, false);
//                    tempMap.put("selection", 2);
//                    tempMap.put("level", 2);
//                    tempMap.put("weight", userMessage);

                } else if (selection == 2 && level == 1 && !"9".equals(userMessage) && !"10".equals(userMessage)) {

                    try{
                        String personName = "";
                        String parts[] = {};
                        parts = userMessage.split(" ");
                        personName = parts[0];

                        String response2 = restCaller.getPerson(personName);

                        ussdSender.sendMessage(response2, address, conversationId, true);
                        map.remove(address);

                    }catch (Exception e){
                        Logger.getLogger(USSDReceiver.class.getName()).log(Level.SEVERE, null, e);
                    }

                } else if ("9".equals(userMessage) && selection == 1) {
                    ussdSender.sendMessage(menus[0], address, conversationId, false);
                    tempMap.put("selection", 0);
                    tempMap.put("level", 0);
                } else if ("9".equals(userMessage) && selection == 2 && level == 1) {
                    ussdSender.sendMessage(menus[0], address, conversationId, false);
                    tempMap.put("selection", 0);
                    tempMap.put("level", 0);
                } else if ("9".equals(userMessage) && selection == 2 && level == 2) {
                    ussdSender.sendMessage(menus[2], address, conversationId, false);
                    tempMap.put("selection", 2);
                    tempMap.put("level", 1);
                } else if ("10".equals(userMessage)) {
                    map.remove(address);
                    ussdSender.sendMessage("Thank you for using this Raxa USSD app", address, conversationId, true);
                } else {
                    map.remove(address);
                    ussdSender.sendMessage("Invalid Selection. Good Bye!", address, conversationId, true);
                }
            } else {
                map.put(address, new HashMap(){{
                    put("selection", 0);
                    put("level", 0);
                    put("weight", "");
                    put("height", "");
                }});
                ussdSender.sendMessage(menus[0], address, conversationId, false);
            }
        } catch (MchoiceUssdException ex) {
            Logger.getLogger(USSDReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void onSessionTerminate(MchoiceUssdTerminateMessage mutm) {
        map.remove(mutm.getAddress());
    }

}
