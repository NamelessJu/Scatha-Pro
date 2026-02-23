package namelessju.scathapro.files.framework;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.miscellaneous.IteratorWrapperImmutable;
import namelessju.scathapro.util.JsonUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.StringWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public abstract class JsonFile extends ScathaProFile
{
    protected final ObjectValue root = new ObjectValue();
    private final boolean usePrettyJson;
    private boolean shouldInitializeWithDefaultValues = false;
    
    public JsonFile(ScathaPro scathaPro, Path relativeFilePath, boolean usePrettyJson)
    {
        super(scathaPro, relativeFilePath);
        this.usePrettyJson = usePrettyJson;
    }
    
    @Override
    protected void deserialize(@Nullable String content)
    {
        if (content != null) root.loadFromJson(JsonUtil.parseJson(content));
        else reset();
    }
    
    @Override
    protected @NonNull String serialize()
    {
        JsonElement json = root.getAsJson(this);
        String jsonString = JsonUtil.toString(json, usePrettyJson);
        if (jsonString == null) jsonString = "";
        return jsonString;
    }
    
    public void reset()
    {
        root.reset();
    }
    
    public void setShouldInitializeWithDefaultValues(boolean value)
    {
        this.shouldInitializeWithDefaultValues = value;
    }
    
    
    
    public interface JsonValue
    {
        void reset();
        boolean hasValue();
        
        /**
         * Sets the value from a JsonElement.<br>
         * If the JsonElement doesn't represent a valid value for this JsonValue, the value gets reset.
         */
        void loadFromJson(@Nullable JsonElement jsonElement);
        @NonNull JsonElement getAsJson(@NonNull JsonFile jsonFile);
        
        interface Serializer<T, S extends JsonElement>
        {
            @Nullable T jsonToValue(@NonNull S jsonElement);
            @NonNull S valueToJson(@NonNull T value);
        }
    }
    
    public static class ObjectValue implements JsonValue
    {
        private final List<ChildValue> childValues = new ArrayList<>();
        
        public <T extends JsonValue> T addValue(@NonNull String path, @NonNull T value)
        {
            Objects.requireNonNull(path);
            Objects.requireNonNull(value);
            childValues.add(new ChildValue(path, value));
            return value;
        }
        
        public <T> PrimitiveValueNullable<T> addPrimitiveNullable(@NonNull String path, @NonNull Serializer<T, JsonPrimitive> serializer)
        {
            return addValue(path, new PrimitiveValueNullable<>(serializer));
        }
        
        public <T> PrimitiveValueWithDefault<T> addPrimitiveWithDefault(@NonNull String path, @NonNull Serializer<T, JsonPrimitive> serializer, @NonNull T defaultValue)
        {
            return addValue(path, new PrimitiveValueWithDefault<>(serializer, defaultValue));
        }
        
        /**
         * Convenience method to add a boolean with a default value
         */
        public BooleanValue addBoolean(@NonNull String path, boolean defaultValue)
        {
            return addValue(path, new BooleanValue(defaultValue));
        }
        
        @Override
        public void reset()
        {
            for (ChildValue child : childValues) child.jsonValue.reset();
        }
        
        @Override
        public boolean hasValue()
        {
            for (ChildValue child : childValues)
            {
                if (child.jsonValue.hasValue()) return true;
            }
            return false;
        }
        
        @Override
        public void loadFromJson(@Nullable JsonElement jsonElement)
        {
            JsonObject jsonObject = jsonElement instanceof JsonObject ? jsonElement.getAsJsonObject() : null;
            for (ChildValue child : childValues)
            {
                if (jsonObject != null)
                {
                    child.jsonValue.loadFromJson(JsonUtil.getJsonElement(jsonObject, child.path));
                }
                else child.jsonValue.reset();
            }
        }
        
        @Override
        public @NonNull JsonElement getAsJson(@NonNull JsonFile jsonFile)
        {
            JsonObject jsonObject = new JsonObject();
            for (ChildValue child : childValues)
            {
                if (child.jsonValue.hasValue() || jsonFile.shouldInitializeWithDefaultValues)
                {
                    JsonUtil.set(jsonObject, child.path, child.jsonValue.getAsJson(jsonFile));
                }
            }
            return jsonObject;
        }
        
        public void visit(BiConsumer<String, JsonValue> valueConsumer)
        {
            visit(null, valueConsumer);
        }
        private void visit(String currentPath, BiConsumer<String, JsonValue> valueConsumer)
        {
            for (ChildValue childValue : childValues)
            {
                String fullPath = currentPath != null
                    ? currentPath + "." + childValue.path
                    : childValue.path;
                if (childValue.jsonValue instanceof ObjectValue objectValue)
                {
                    objectValue.visit(fullPath, valueConsumer);
                }
                else valueConsumer.accept(fullPath, childValue.jsonValue);
            }
        }
        
        private record ChildValue(String path, JsonValue jsonValue) {}
    }
    
    public abstract static class ArrayValue<T> implements JsonValue, Iterable<T>
    {
        protected final ArrayList<T> list = new ArrayList<>();
        
        public T get(int index)
        {
            return list.get(index);
        }
        
        public void add(T value)
        {
            list.add(value);
        }
        
        public void remove(int index)
        {
            list.remove(index);
        }
        
        public boolean remove(T value)
        {
            return list.remove(value);
        }
        
        public int size()
        {
            return list.size();
        }
        
        public boolean contains(T value)
        {
            return list.contains(value);
        }
        
        @Override
        public void reset()
        {
            list.clear();
        }
        
        @Override
        public boolean hasValue()
        {
            return !list.isEmpty();
        }
        
        @Override
        public @NonNull Iterator<T> iterator()
        {
            return new IteratorWrapperImmutable<>(list.iterator());
        }
    }
    
    public static class JsonValueArrayValue<T extends JsonValue> extends ArrayValue<T>
    {
        private final Supplier<T> valueFactory;
        
        public JsonValueArrayValue(@NonNull Supplier<T> valueFactory)
        {
            this.valueFactory = Objects.requireNonNull(valueFactory);
        }
        
        @Override
        public boolean hasValue()
        {
            if (!super.hasValue()) return false;
            for (T entry : list)
            {
                if (entry.hasValue()) return true;
            }
            return false;
        }
        
        @Override
        public void loadFromJson(@Nullable JsonElement jsonElement)
        {
            list.clear();
            
            if (jsonElement instanceof JsonArray jsonArray)
            {
                for (JsonElement childJsonElement : jsonArray)
                {
                    T child = valueFactory.get();
                    if (child == null) continue;
                    child.loadFromJson(childJsonElement);
                    list.add(child);
                }
            }
        }
        
        @Override
        public @NonNull JsonElement getAsJson(@NonNull JsonFile jsonFile)
        {
            JsonArray array = new JsonArray();
            for (T element : list)
            {
                if (element.hasValue() || jsonFile.shouldInitializeWithDefaultValues)
                {
                    array.add(element.getAsJson(jsonFile));
                }
            }
            return array;
        }
    }
    
    public static class GenericArrayValue<T> extends ArrayValue<T>
    {
        private final @NonNull Serializer<T, JsonElement> valueSerializer;
        
        public GenericArrayValue(@NonNull Serializer<T, JsonElement> valueSerializer)
        {
            this.valueSerializer = Objects.requireNonNull(valueSerializer);
        }
        
        @Override
        public void loadFromJson(@Nullable JsonElement jsonElement)
        {
            list.clear();
            
            if (jsonElement instanceof JsonArray jsonArray)
            {
                for (JsonElement childJsonElement : jsonArray)
                {
                    T value = valueSerializer.jsonToValue(childJsonElement);
                    if (value == null) continue;
                    list.add(value);
                }
            }
        }
        
        @Override
        public @NonNull JsonElement getAsJson(@NonNull JsonFile jsonFile)
        {
            JsonArray array = new JsonArray();
            for (T child : list)
            {
                array.add(valueSerializer.valueToJson(child));
            }
            return array;
        }
    }
    
    public abstract static class PrimitiveValue<T> implements JsonValue
    {
        protected final @NonNull Serializer<T, JsonPrimitive> serializer;
        protected @Nullable T value = null;
        
        public PrimitiveValue(@NonNull Serializer<T, JsonPrimitive> serializer)
        {
            this.serializer = serializer;
        }
        
        public Optional<T> getOptional()
        {
            return Optional.ofNullable(value);
        }
        
        public T getOr(T nullReplacement)
        {
            return value != null ? value : nullReplacement;
        }
        
        public void set(@Nullable T value)
        {
            this.value = value;
        }
        
        @Override
        public void reset()
        {
            set(null);
        }
        
        @Override
        public boolean hasValue()
        {
            return value != null;
        }
        
        @Override
        public void loadFromJson(@Nullable JsonElement jsonElement)
        {
            if (jsonElement instanceof JsonPrimitive jsonPrimitive)
            {
                set(serializer.jsonToValue(jsonPrimitive));
            }
            else reset();
        }
    }
    
    public static class PrimitiveValueNullable<T> extends PrimitiveValue<T>
    {
        public PrimitiveValueNullable(@NonNull Serializer<T, JsonPrimitive> serializer)
        {
            super(serializer);
        }
        
        /**
         * Returns the value - may be null!
         */
        public @Nullable T get()
        {
            return value;
        }
        
        @Override
        public @NonNull JsonElement getAsJson(@NonNull JsonFile jsonFile)
        {
            T currentValue = get();
            if (currentValue == null) return JsonNull.INSTANCE;
            return serializer.valueToJson(currentValue);
        }
    }
    
    public static class PrimitiveValueWithDefault<T> extends PrimitiveValue<T>
    {
        public final @NonNull T defaultValue;
        
        public PrimitiveValueWithDefault(@NonNull Serializer<T, JsonPrimitive> serializer, @NonNull T defaultValue)
        {
            super(serializer);
            this.defaultValue = defaultValue;
        }
        
        /**
         * Returns the current value if one is set, otherwise returns the default value
         */
        public @NonNull T get()
        {
            if (value == null) return defaultValue;
            return value;
        }
        
        @Override
        public @NonNull JsonElement getAsJson(@NonNull JsonFile jsonFile)
        {
            return serializer.valueToJson(get());
        }
    }
    
    public static JsonValue.Serializer<String, JsonPrimitive> STRING_SERIALIZER = new JsonValue.Serializer<>()
    {
        @Override
        public @Nullable String jsonToValue(@NonNull JsonPrimitive jsonPrimitive)
        {
            if (jsonPrimitive.isString()) return jsonPrimitive.getAsString();
            return null;
        }
        
        @Override
        public @NonNull JsonPrimitive valueToJson(@NonNull String value)
        {
            return new JsonPrimitive(value);
        }
    };
    
    public static JsonValue.Serializer<Boolean, JsonPrimitive> BOOLEAN_SERIALIZER = new JsonValue.Serializer<>()
    {
        @Override
        public @Nullable Boolean jsonToValue(@NonNull JsonPrimitive jsonPrimitive)
        {
            if (jsonPrimitive.isBoolean()) return jsonPrimitive.getAsBoolean();
            return null;
        }
        
        @Override
        public @NonNull JsonPrimitive valueToJson(@NonNull Boolean value)
        {
            return new JsonPrimitive(value);
        }
    };
    
    /**
     * Convenience class representing a boolean with a default value
     */
    public static class BooleanValue extends PrimitiveValueWithDefault<Boolean>
    {
        public BooleanValue(boolean defaultValue)
        {
            super(BOOLEAN_SERIALIZER, defaultValue);
        }
    }
    
    public static abstract class NumberSerializer<T extends Number> implements JsonValue.Serializer<T, JsonPrimitive>
    {
        @Override
        public final @Nullable T jsonToValue(@NonNull JsonPrimitive jsonPrimitive)
        {
            if (jsonPrimitive.isNumber()) return getNumberFromJsonPrimitive(jsonPrimitive);
            return null;
        }
        
        protected abstract T getNumberFromJsonPrimitive(@NonNull JsonPrimitive jsonPrimitive);
        
        @Override
        public final @NonNull JsonPrimitive valueToJson(@NonNull T value)
        {
            return new JsonPrimitive(onSerializeValue(value));
        }
        
        protected T onSerializeValue(@NonNull T value)
        {
            return value;
        }
    }
    
    public static JsonValue.Serializer<Integer, JsonPrimitive> INTEGER_SERIALIZER = new NumberSerializer<>()
    {
        @Override
        protected Integer getNumberFromJsonPrimitive(@NonNull JsonPrimitive jsonPrimitive)
        {
            return jsonPrimitive.getAsInt();
        }
    };
    
    public static JsonValue.Serializer<Long, JsonPrimitive> LONG_SERIALIZER = new NumberSerializer<>()
    {
        @Override
        protected Long getNumberFromJsonPrimitive(@NonNull JsonPrimitive jsonPrimitive)
        {
            return jsonPrimitive.getAsLong();
        }
    };
    
    public static JsonValue.Serializer<Float, JsonPrimitive> FLOAT_SERIALIZER = new NumberSerializer<>()
    {
        // Floats get rounded to this accuracy to prevent values like 0.789999996
        private static final float MAX_ACCURACY = 0.000001f;
        
        @Override
        protected Float getNumberFromJsonPrimitive(@NonNull JsonPrimitive jsonPrimitive)
        {
            return jsonPrimitive.getAsFloat();
        }
        
        @Override
        protected Float onSerializeValue(@NonNull Float value)
        {
            return (float) (Math.round((double) value / MAX_ACCURACY) * (double) MAX_ACCURACY);
        }
    };
    
    public static class EnumSerializer<T extends Enum<T>> implements JsonValue.Serializer<T, JsonPrimitive>
    {
        private final @NonNull Class<T> type;
        
        public EnumSerializer(@NonNull Class<T> type)
        {
            this.type = Objects.requireNonNull(type);
        }
        
        @Override
        public @Nullable T jsonToValue(@NonNull JsonPrimitive jsonPrimitive)
        {
            if (jsonPrimitive.isString())
            {
                String stringValue = jsonPrimitive.getAsString();
                if (!stringValue.isEmpty())
                {
                    try
                    {
                        return Enum.valueOf(type, stringValue);
                    }
                    catch (Exception ignored) {}
                }
            }
            return null;
        }
        
        @Override
        public @NonNull JsonPrimitive valueToJson(@NonNull T value)
        {
            return new JsonPrimitive(value.name());
        }
    }
    
    public record ObjectChildSerializer<T>(@NonNull String path, JsonValue.@NonNull Serializer<T, JsonPrimitive> serializer)
    {
        public @Nullable T deserialize(JsonObject jsonObject)
        {
            JsonPrimitive jsonPrimitive = JsonUtil.getJsonPrimitive(jsonObject, path);
            if (jsonPrimitive == null) return null;
            return serializer.jsonToValue(jsonPrimitive);
        }
        
        public void serialize(JsonObject jsonObject, T value)
        {
            JsonUtil.set(jsonObject, path, serializer.valueToJson(value));
        }
    }
}
