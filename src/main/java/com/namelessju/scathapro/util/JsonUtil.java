package com.namelessju.scathapro.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public abstract class JsonUtil {
	
	public static JsonObject parseObject(String jsonString) {
		JsonElement json = new JsonParser().parse(jsonString);
    	if (json != null && json instanceof JsonObject) {
    		return json.getAsJsonObject();
    	}
    	else return null;
	}
    
    public static JsonElement getJsonElement(JsonElement object, String path) {
        if (object == null || object.isJsonNull()) return null;
        
        String[] pathSegments = path.split("[\\/]");
        
        JsonElement currentElement = object;
        for (String pathSegment : pathSegments) {
            if (currentElement != null && currentElement.isJsonObject()) {
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
        return primitive != null && primitive.isString() ? primitive.getAsString() : null;
    }

    public static Boolean getBoolean(JsonElement object, String path) {
        JsonPrimitive primitive = getJsonPrimitive(object, path);
        return primitive != null && primitive.isBoolean() ? primitive.getAsBoolean() : null;
    }
    
    public static void set(JsonObject object, String path, JsonElement value) {
        if (object == null) throw new NullPointerException("Object cannot be null");
        
        String[] pathNodes = path.split("/");
        
        JsonObject currentObject = object;
        
        for (int i = 0; i < pathNodes.length; i ++) {
            if (i == pathNodes.length - 1)
                currentObject.getAsJsonObject().add(pathNodes[i], value);
            else {
                JsonElement nextElement = currentObject.get(pathNodes[i]);
                JsonObject nextObject = nextElement != null && nextElement.isJsonObject() ? nextElement.getAsJsonObject() : null;
                
                if (nextObject == null) {
                    nextObject = new JsonObject();
                    currentObject.add(pathNodes[i], nextObject);
                }
                
                currentObject = nextObject;
            }
        }
    }
    
}
