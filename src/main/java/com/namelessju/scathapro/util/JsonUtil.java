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
        
        if (currentElement != null && !currentElement.isJsonNull()) return currentElement;
        return null;
    }
    
    public static JsonObject getJsonObject(JsonElement object, String path) {
        JsonElement element = getJsonElement(object, path);
        if (element != null && element.isJsonObject()) return element.getAsJsonObject();
        return null;
    }
    
    public static JsonArray getJsonArray(JsonElement object, String path) {
        JsonElement element = getJsonElement(object, path);
        if (element != null && element.isJsonArray()) return element.getAsJsonArray();
        return null;
    }
    
    public static JsonPrimitive getJsonPrimitive(JsonElement object, String path) {
        JsonElement element = getJsonElement(object, path);
        if (element != null && element.isJsonPrimitive()) return (JsonPrimitive) element;
        return null;
    }

    public static Number getNumber(JsonElement object, String path) {
        JsonPrimitive primitive = getJsonPrimitive(object, path);
        if (primitive != null && primitive.isNumber()) return primitive.getAsNumber();
        return null;
    }
    
    public static Integer getInt(JsonElement object, String path) {
        Number number = JsonUtil.getNumber(object, path);
        return number != null ? number.intValue() : null;
    }
    
    public static Long getLong(JsonElement object, String path) {
        Number number = JsonUtil.getNumber(object, path);
        return number != null ? number.longValue() : null;
    }
    
    public static Double getDouble(JsonElement object, String path) {
        Number number = JsonUtil.getNumber(object, path);
        return number != null ? number.doubleValue() : null;
    }

    public static String getString(JsonElement object, String path) {
        JsonPrimitive primitive = getJsonPrimitive(object, path);
        if (primitive.isString()) return primitive.getAsString();
        return null;
    }
    
}
