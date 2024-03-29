package com.namelessju.scathapro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessju.scathapro.util.ChatUtil;
import com.namelessju.scathapro.util.Util;

import java.net.URL;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

// Unused
public class HypixelApiManager {

    public abstract static class HypixelApiEvent extends Event {
        public final String endpoint;

        public HypixelApiEvent(String endpoint)
        {
            super();
            this.endpoint = endpoint;
        }
    }
    
    public static class HypixelApiResponseEvent extends HypixelApiEvent {
        public final JsonObject json;

        public HypixelApiResponseEvent(String endpoint, JsonObject json)
        {
            super(endpoint);
            this.json = json;
        }
    }
    
    public static class HypixelApiErrorEvent extends HypixelApiEvent {
        public final ErrorType errorType;
        
        public enum ErrorType {
            MALFORMED_URL, IO, MISSING_PARAMS, ACCESS_DENIED, DATA_UNAVAILABLE, INVALID_PARAMS, REQUEST_LIMIT_REACHED, DATA_NOT_AVAILABLE_YET, UNKNOWN; 
        }

        public HypixelApiErrorEvent(String endpoint, ErrorType errorType)
        {
            super(endpoint);
            this.errorType = errorType;
        }
    }
    
    
    private final static String URL = "https://api.hypixel.net/skyblock/";
    
    
    public static void requestProfilesData() {
        String uuid = Util.getPlayerUUIDString();
        
        if (uuid != null) sendRequest("profiles", "uuid=" + uuid);
        else {
            final String apiKey = Config.instance.getString(Config.Key.apiKey);
            if (!apiKey.isEmpty()) sendApiErrorMessage("Your session is offline");
        }
        
        ScathaPro.getInstance().lastProfilesDataRequestTime = Util.getCurrentTime();
    }
    
    public static void sendRequest(final String endpoint, final String parameters) {
        final String apiKey = Config.instance.getString(Config.Key.apiKey);
        
        if (!apiKey.isEmpty()) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        URL url = new URL(URL + endpoint + "?key=" + apiKey + "&" + parameters);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();
            
                        int code = connection.getResponseCode();
                        
                        switch (code) {
                            case 200:
                                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        
                                StringBuilder response = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    response.append(line + "\n");
                                }
                                
                                connection.disconnect();
        
                                JsonObject json = new JsonParser().parse(response.toString()).getAsJsonObject();
                                
                                MinecraftForge.EVENT_BUS.post(new HypixelApiResponseEvent(endpoint, json));
                                break;
                                
                            case 400:
                                sendApiErrorMessage("Missing parameters");
                                MinecraftForge.EVENT_BUS.post(new HypixelApiErrorEvent(endpoint, HypixelApiErrorEvent.ErrorType.MISSING_PARAMS));
                                break;
                            case 403:
                                sendApiErrorMessage("Access denied, make sure the API key is correct");
                                MinecraftForge.EVENT_BUS.post(new HypixelApiErrorEvent(endpoint, HypixelApiErrorEvent.ErrorType.ACCESS_DENIED));
                                break;
                            case 404:
                                sendApiErrorMessage("Data does not exist");
                                MinecraftForge.EVENT_BUS.post(new HypixelApiErrorEvent(endpoint, HypixelApiErrorEvent.ErrorType.DATA_UNAVAILABLE));
                                break;
                            case 422:
                                sendApiErrorMessage("Invalid parameters");
                                MinecraftForge.EVENT_BUS.post(new HypixelApiErrorEvent(endpoint, HypixelApiErrorEvent.ErrorType.INVALID_PARAMS));
                                break;
                            case 429:
                                sendApiErrorMessage("Request limit reached");
                                MinecraftForge.EVENT_BUS.post(new HypixelApiErrorEvent(endpoint, HypixelApiErrorEvent.ErrorType.REQUEST_LIMIT_REACHED));
                                break;
                            case 503:
                                sendApiErrorMessage("Data not available yet");
                                MinecraftForge.EVENT_BUS.post(new HypixelApiErrorEvent(endpoint, HypixelApiErrorEvent.ErrorType.DATA_NOT_AVAILABLE_YET));
                                break;
                                
                            default:
                                sendApiErrorMessage("Unknown error");
                                MinecraftForge.EVENT_BUS.post(new HypixelApiErrorEvent(endpoint, HypixelApiErrorEvent.ErrorType.UNKNOWN));
                        }
                    }
                    catch (MalformedURLException e) {
                        e.printStackTrace();
                        sendApiErrorMessage("Invalid request URL");
                        MinecraftForge.EVENT_BUS.post(new HypixelApiErrorEvent(endpoint, HypixelApiErrorEvent.ErrorType.MALFORMED_URL));
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        sendApiErrorMessage("Data couldn't be read");
                        MinecraftForge.EVENT_BUS.post(new HypixelApiErrorEvent(endpoint, HypixelApiErrorEvent.ErrorType.IO));
                    }
                }
            }).start();
        }
    }
    
    public static void sendApiErrorMessage(String details) {
        ChatUtil.sendModErrorMessage("Hypixel API request failed (" + details + ")");
    }
}
