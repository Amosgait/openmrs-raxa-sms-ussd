package org.raxa.mobile.app;

import hsenidmobile.sdp.rest.servletbase.MchoiceAventuraMessagingException;
import hsenidmobile.sdp.rest.servletbase.MchoiceAventuraSmsMessage;
import hsenidmobile.sdp.rest.servletbase.MchoiceAventuraSmsMoServlet;
import hsenidmobile.sdp.rest.servletbase.MchoiceAventuraSmsSender;

import java.net.MalformedURLException;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.raxa.mobile.rest.RestCaller;

public class SmsReceiver extends MchoiceAventuraSmsMoServlet {

    private static final String URL = "http://127.0.0.1:8000/sms/";
    private static final String APP_ID = "appid";
    private static final String PASS = "password";
    private RestCaller restCaller = new RestCaller();

    @Override
    protected void onMessage(MchoiceAventuraSmsMessage message) {

        String userMessage = message.getMessage();
        String address = message.getAddress();

        String parts[] = {};
        parts = userMessage.split(" ");
        String givenName, familyName, gender, age = "";

        try{

            if (parts[1].equalsIgnoreCase("addperson"))  {

                givenName = parts[2];
                familyName = parts[3];
                gender = parts[4];
                age = parts[5];

                String response = restCaller.addPerson(givenName, familyName, gender, age);
                try {
                    MchoiceAventuraSmsSender smsSender = new MchoiceAventuraSmsSender(new java.net.URL(URL), APP_ID, PASS);
                    smsSender.sendMessage(response, address);

                } catch (MalformedURLException e) {
                    Logger.getLogger(this.getClass()).error(e);
                } catch (MchoiceAventuraMessagingException e) {
                    Logger.getLogger(this.getClass()).error(e);
                }
            }
            else if (parts[1].equalsIgnoreCase("getperson"))  {

                String personName = "";
                parts = userMessage.split(" ");
                personName = parts[2];

                String response2 = restCaller.getPerson(personName);

                try {
                    MchoiceAventuraSmsSender smsSender = new MchoiceAventuraSmsSender(new java.net.URL(URL), APP_ID, PASS);
                    smsSender.sendMessage(response2, address);

                } catch (MalformedURLException e) {
                    Logger.getLogger(this.getClass()).error(e);
                } catch (MchoiceAventuraMessagingException e) {
                    Logger.getLogger(this.getClass()).error(e);
                }
            }
            else  {
                String response2 = "Incorrect SMS message, pls follow the Syntax";
                try {
                    MchoiceAventuraSmsSender smsSender = new MchoiceAventuraSmsSender(new java.net.URL(URL), APP_ID, PASS);
                    smsSender.sendMessage(response2, address);

                } catch (MalformedURLException e) {
                    Logger.getLogger(this.getClass()).error(e);
                } catch (MchoiceAventuraMessagingException e) {
                    Logger.getLogger(this.getClass()).error(e);
                }
            }

        }catch (Exception e){
            java.util.logging.Logger.getLogger(USSDReceiver.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
