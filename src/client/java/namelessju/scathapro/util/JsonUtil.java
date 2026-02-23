package namelessju.scathapro.util;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.StringWriter;
import java.util.Objects;

public final class JsonUtil
{
    private static final String jsonPathNodeSeparatorRegex = "\\.";
    
    private JsonUtil() {}
    
    
    public static @Nullable JsonElement parseJson(String jsonString)
    {
        try
        {
            return JsonParser.parseString(jsonString);
        }
        catch (JsonSyntaxException ignored) {}
        
        return null;
    }
    
    public static @Nullable JsonElement getJsonElement(@Nullable JsonElement object, String path)
    {
        if (object == null || object.isJsonNull()) return null;
        
        if (path == null) return object;
        
        String[] pathNodes = path.split(jsonPathNodeSeparatorRegex);
        
        JsonElement currentElement = object;
        for (String pathSegment : pathNodes)
        {
            if (currentElement != null && currentElement.isJsonObject())
            {
                JsonObject currentObject = currentElement.getAsJsonObject();
                currentElement = currentObject.get(pathSegment);
            }
            else return null;
        }
        
        if (currentElement != null && !currentElement.isJsonNull()) return currentElement;
        return null;
    }
    
    public static @Nullable JsonObject getJsonObject(@Nullable JsonElement object, String path)
    {
        JsonElement element = getJsonElement(object, path);
        if (element != null && element.isJsonObject()) return element.getAsJsonObject();
        return null;
    }
    
    public static @Nullable JsonArray getJsonArray(@Nullable JsonElement object, String path)
    {
        JsonElement element = getJsonElement(object, path);
        if (element != null && element.isJsonArray()) return element.getAsJsonArray();
        return null;
    }
    
    public static @Nullable JsonPrimitive getJsonPrimitive(@Nullable JsonElement object, String path)
    {
        JsonElement element = getJsonElement(object, path);
        if (element != null && element.isJsonPrimitive()) return (JsonPrimitive) element;
        return null;
    }

    public static @Nullable Number getNumber(@Nullable JsonElement object, String path)
    {
        JsonPrimitive primitive = getJsonPrimitive(object, path);
        if (primitive != null && primitive.isNumber()) return primitive.getAsNumber();
        return null;
    }
    
    public static @Nullable Integer getInt(@Nullable JsonElement object, String path)
    {
        Number number = JsonUtil.getNumber(object, path);
        return number != null ? number.intValue() : null;
    }
    
    public static @Nullable Long getLong(@Nullable JsonElement object, String path)
    {
        Number number = JsonUtil.getNumber(object, path);
        return number != null ? number.longValue() : null;
    }
    
    public static @Nullable Double getDouble(@Nullable JsonElement object, String path)
    {
        Number number = JsonUtil.getNumber(object, path);
        return number != null ? number.doubleValue() : null;
    }

    public static @Nullable String getString(@Nullable JsonElement object, String path)
    {
        JsonPrimitive primitive = getJsonPrimitive(object, path);
        return primitive != null && primitive.isString() ? primitive.getAsString() : null;
    }

    public static @Nullable Boolean getBoolean(@Nullable JsonElement object, String path)
    {
        JsonPrimitive primitive = getJsonPrimitive(object, path);
        return primitive != null && primitive.isBoolean() ? primitive.getAsBoolean() : null;
    }
    
    public static void set(@NonNull JsonObject object, String path, @Nullable JsonElement value)
    {
        Objects.requireNonNull(object, "JsonObject cannot be null");
        
        String[] pathNodes = path.split(jsonPathNodeSeparatorRegex);
        
        JsonObject currentObject = object;
        
        for (int i = 0; i < pathNodes.length; i ++)
        {
            if (i == pathNodes.length - 1)
            {
                currentObject.getAsJsonObject().add(pathNodes[i], value);
            }
            else
            {
                JsonElement nextElement = currentObject.get(pathNodes[i]);
                JsonObject nextObject = nextElement != null && nextElement.isJsonObject() ? nextElement.getAsJsonObject() : null;
                if (nextObject == null)
                {
                    nextObject = new JsonObject();
                    currentObject.add(pathNodes[i], nextObject);
                }
                currentObject = nextObject;
            }
        }
    }
    
    public static JsonElement remove(@NonNull JsonObject object, String path)
    {
        if (object.isJsonNull() || path == null) return null;
        
        String[] pathNodes = path.split(jsonPathNodeSeparatorRegex);
        
        JsonObject currentObject = object;
        for (int i = 0; i < pathNodes.length; i ++)
        {
            if (i == pathNodes.length - 1)
            {
                return currentObject.remove(pathNodes[i]);
            }
            else
            {
                JsonElement nextElement = currentObject.get(pathNodes[i]);
                if (nextElement != null && nextElement.isJsonObject())
                {
                    currentObject = nextElement.getAsJsonObject();
                }
                else return null;
            }
        }
        
        return null;
    }
    
    public static @Nullable String toString(JsonElement jsonElement, boolean usePrettyJson)
    {
        String jsonString = null;
        try
        {
            GsonBuilder jsonStringBuilder = new GsonBuilder();
            jsonStringBuilder.serializeNulls();
            Gson gson = jsonStringBuilder.create();
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = gson.newJsonWriter(stringWriter);
            if (usePrettyJson) jsonWriter.setIndent("    ");
            else jsonWriter.setFormattingStyle(FormattingStyle.COMPACT);
            gson.toJson(jsonElement, jsonWriter);
            jsonString = stringWriter.toString();
        }
        catch (Exception ignored) {}
        return jsonString;
    }
}
