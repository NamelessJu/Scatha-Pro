package com.namelessju.scathapro.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public abstract class JsonUtil {
    
    public static JsonElement getJsonElement(JsonElement object, String path) {
        if (object == null || object.isJsonNull()) return null;
        
        String[] pathSegments = path.split("[\\/]");
        
        JsonElement currentElement = object;
        for (String pathSegment : pathSegments) {
            if (currentElement.isJsonObject()) {
                JsonObject currentObject = currentElement.getAsJsonObject();
                currentElement = currentObject.get(pathSegment);
            }
            else return null;
        }
        
        return currentElement.isJsonNull() ? null : currentElement;
    }
    
    public static JsonObject getJsonObject(JsonElement object, String path) {
        JsonElement element = getJsonElement(object, path);
        if (element.isJsonObject()) return element.getAsJsonObject();
        return null;
    }
    
    public static JsonArray getJsonArray(JsonElement object, String path) {
        JsonElement element = getJsonElement(object, path);
        if (element.isJsonArray()) return element.getAsJsonArray();
        return null;
    }
    
    public static JsonPrimitive getJsonPrimitive(JsonElement object, String path) {
        JsonElement element = getJsonElement(object, path);
        if (element.isJsonPrimitive()) return (JsonPrimitive) element;
        return null;
    }

    public static Number getNumber(JsonElement object, String path) {
        JsonPrimitive primitive = getJsonPrimitive(object, path);
        if (primitive.isNumber()) return primitive.getAsNumber();
        return null;
    }

    public static String getString(JsonElement object, String path) {
        JsonPrimitive primitive = getJsonPrimitive(object, path);
        if (primitive.isString()) return primitive.getAsString();
        return null;
    }
    
}
