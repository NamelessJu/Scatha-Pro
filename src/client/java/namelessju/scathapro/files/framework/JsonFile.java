package namelessju.scathapro.files.framework;

import com.google.gson.*;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.framework.DataEvent;
import namelessju.scathapro.events.framework.ScathaProEvent;
import namelessju.scathapro.miscellaneous.IteratorWrapperImmutable;
import namelessju.scathapro.util.JsonUtil;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@NullMarked
public abstract class JsonFile<T extends JsonFile.JsonValue> extends ScathaProFile
{
    public final T root;
    public boolean prettyPrintEnabled;
    public boolean savesDefaultValues = false;
    public @Nullable Consumer<@Nullable JsonElement> postLoadConsumer = null;

    public JsonFile(ScathaPro scathaPro, File file, boolean prettyPrintEnabled)
    {
        super(scathaPro, file);
        this.root = initializeRoot();
        this.prettyPrintEnabled = prettyPrintEnabled;
    }

    protected abstract T initializeRoot();

    @Override
    protected void deserialize(@Nullable String content)
    {
        if (content != null)
        {
            JsonElement jsonElement = JsonUtil.parseJson(content);
            root.loadFromJson(jsonElement);
            if (postLoadConsumer != null) postLoadConsumer.accept(jsonElement);
        }
        else reset();
    }

    @Override
    protected String serialize()
    {
        JsonElement json = root.getAsJson(this);
        return JsonUtil.toString(json, prettyPrintEnabled);
    }

    public void reset()
    {
        root.reset();
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
        JsonElement getAsJson(JsonFile<?> jsonFile);

        interface Serializer<T, S extends JsonElement>
        {
            @Nullable T jsonToValue(S jsonElement);
            S valueToJson(T value);
        }

        static <T, S extends ScathaProEvent<T>> @Nullable S handleNullableEventRemoveListener(@Nullable S event, T listener)
        {
            if (event == null) return null;
            event.removeListener(listener);
            if (event.getListenerCount() <= 0) return null;
            return event;
        }
    }

    public static class ObjectValue implements JsonValue
    {
        private final List<ChildValue> childValues = new ArrayList<>();

        public <T extends JsonValue> T addValue(String path, T value)
        {
            Objects.requireNonNull(path);
            Objects.requireNonNull(value);
            childValues.add(new ChildValue(path, value));
            return value;
        }

        public <T> PrimitiveValueNullable<T> addPrimitiveNullable(String path, Serializer<T, JsonPrimitive> serializer)
        {
            return addValue(path, new PrimitiveValueNullable<>(serializer));
        }

        public <T> PrimitiveValueWithDefault<T> addPrimitiveWithDefault(String path, Serializer<T, JsonPrimitive> serializer, T defaultValue)
        {
            return addValue(path, new PrimitiveValueWithDefault<>(serializer, defaultValue));
        }

        /**
         * Convenience method to add a boolean with a default value
         */
        public BooleanValue addBoolean(String path, boolean defaultValue)
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
        public JsonElement getAsJson(JsonFile<?> jsonFile)
        {
            JsonObject jsonObject = new JsonObject();
            for (ChildValue child : childValues)
            {
                if (child.jsonValue.hasValue() || jsonFile.savesDefaultValues)
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

        private void visit(@Nullable String currentPath, BiConsumer<String, JsonValue> valueConsumer)
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
        private final ArrayList<T> list = new ArrayList<>();

        private @Nullable DataEvent<T> onEntryAddedEvent = null;
        private @Nullable DataEvent<T> onEntryRemovedEvent = null;

        public T get(int index)
        {
            return list.get(index);
        }

        public void add(T value)
        {
            list.add(value);
            if (onEntryAddedEvent != null) onEntryAddedEvent.trigger(value);
        }

        public T remove(int index)
        {
            T value = list.remove(index);
            if (onEntryRemovedEvent != null) onEntryRemovedEvent.trigger(value);
            return value;
        }

        public boolean remove(T value)
        {
            if (list.remove(value))
            {
                if (onEntryRemovedEvent != null) onEntryRemovedEvent.trigger(value);
                return true;
            }
            return false;
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
            if (onEntryRemovedEvent != null)
            {
                for (T value : this) onEntryRemovedEvent.trigger(value);
            }
            list.clear();
        }

        @Override
        public boolean hasValue()
        {
            return !list.isEmpty();
        }

        @Override
        public Iterator<T> iterator()
        {
            return new IteratorWrapperImmutable<>(list.iterator());
        }

        public void onEntryAdded(DataEvent.Listener<T> listener, boolean triggerOnExistingEntries)
        {
            if (onEntryAddedEvent == null) onEntryAddedEvent = new DataEvent<>();
            onEntryAddedEvent.addListener(listener);
            if (triggerOnExistingEntries) for (T entry : this) listener.onTriggered(entry);
        }

        public void removeOnEntryAdded(DataEvent.Listener<T> listener)
        {
            onEntryAddedEvent = JsonValue.handleNullableEventRemoveListener(onEntryAddedEvent, listener);
        }

        public void onEntryRemoved(DataEvent.Listener<T> listener)
        {
            if (onEntryRemovedEvent == null) onEntryRemovedEvent = new DataEvent<>();
            onEntryRemovedEvent.addListener(listener);
        }

        public void removeOnEntryRemoved(DataEvent.Listener<T> listener)
        {
            onEntryRemovedEvent = JsonValue.handleNullableEventRemoveListener(onEntryRemovedEvent, listener);
        }
    }

    public static class JsonValueArrayValue<T extends JsonValue> extends ArrayValue<T>
    {
        private final Supplier<T> valueFactory;

        public JsonValueArrayValue(Supplier<T> valueFactory)
        {
            this.valueFactory = Objects.requireNonNull(valueFactory);
        }

        @Override
        public boolean hasValue()
        {
            if (!super.hasValue()) return false;
            for (T entry : this)
            {
                if (entry.hasValue()) return true;
            }
            return false;
        }

        @Override
        public void loadFromJson(@Nullable JsonElement jsonElement)
        {
            reset();

            if (jsonElement instanceof JsonArray jsonArray)
            {
                for (JsonElement childJsonElement : jsonArray)
                {
                    T child = valueFactory.get();
                    add(child);
                    child.loadFromJson(childJsonElement);
                }
            }
        }

        @Override
        public JsonElement getAsJson(JsonFile<?> jsonFile)
        {
            JsonArray array = new JsonArray();
            for (T element : this)
            {
                if (element.hasValue() || jsonFile.savesDefaultValues)
                {
                    array.add(element.getAsJson(jsonFile));
                }
            }
            return array;
        }
    }

    public static class GenericArrayValue<T> extends ArrayValue<T>
    {
        private final Serializer<T, JsonElement> valueSerializer;

        public GenericArrayValue(Serializer<T, JsonElement> valueSerializer)
        {
            this.valueSerializer = Objects.requireNonNull(valueSerializer);
        }

        @Override
        public void loadFromJson(@Nullable JsonElement jsonElement)
        {
            reset();

            if (jsonElement instanceof JsonArray jsonArray)
            {
                for (JsonElement childJsonElement : jsonArray)
                {
                    T value = valueSerializer.jsonToValue(childJsonElement);
                    if (value == null) continue;
                    add(value);
                }
            }
        }

        @Override
        public JsonElement getAsJson(JsonFile<?> jsonFile)
        {
            JsonArray array = new JsonArray();
            for (T child : this)
            {
                array.add(valueSerializer.valueToJson(child));
            }
            return array;
        }
    }

    public abstract static class PrimitiveValue<T, S extends PrimitiveValue<T, S>> implements JsonValue
    {
        protected final Serializer<T, JsonPrimitive> serializer;
        protected @Nullable T value = null;

        private @Nullable DataEvent<S> onValueChangedEvent = null;

        public PrimitiveValue(Serializer<T, JsonPrimitive> serializer)
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

        @SuppressWarnings("unchecked")
        public void set(@Nullable T value)
        {
            T valueBefore = this.value;
            this.value = value;
            if (onValueChangedEvent != null && !Objects.equals(this.value, valueBefore))
            {
                onValueChangedEvent.trigger((S) this);
            }
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

        public void onValueChanged(DataEvent.Listener<S> listener)
        {
            if (onValueChangedEvent == null) onValueChangedEvent = new DataEvent<>();
            onValueChangedEvent.addListener(listener);
        }

        public void removeOnValueChanged(DataEvent.Listener<S> listener)
        {
            onValueChangedEvent = JsonValue.handleNullableEventRemoveListener(onValueChangedEvent, listener);
        }
    }

    public static class PrimitiveValueNullable<T> extends PrimitiveValue<T, PrimitiveValueNullable<T>>
    {
        public PrimitiveValueNullable(Serializer<T, JsonPrimitive> serializer)
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
        public JsonElement getAsJson(JsonFile<?> jsonFile)
        {
            T currentValue = get();
            if (currentValue == null) return JsonNull.INSTANCE;
            return serializer.valueToJson(currentValue);
        }
    }

    public static class PrimitiveValueWithDefault<T> extends PrimitiveValue<T, PrimitiveValueWithDefault<T>>
    {
        public final T defaultValue;

        public PrimitiveValueWithDefault(Serializer<T, JsonPrimitive> serializer, T defaultValue)
        {
            super(serializer);
            this.defaultValue = defaultValue;
        }

        /**
         * Returns the current value if one is set, otherwise returns the default value
         */
        public T get()
        {
            if (value == null) return defaultValue;
            return value;
        }

        @Override
        public JsonElement getAsJson(JsonFile<?> jsonFile)
        {
            return serializer.valueToJson(get());
        }
    }

    public static JsonValue.Serializer<String, JsonPrimitive> STRING_SERIALIZER = new JsonValue.Serializer<>()
    {
        @Override
        public @Nullable String jsonToValue(JsonPrimitive jsonPrimitive)
        {
            if (jsonPrimitive.isString()) return jsonPrimitive.getAsString();
            return null;
        }

        @Override
        public JsonPrimitive valueToJson(String value)
        {
            return new JsonPrimitive(value);
        }
    };

    public static JsonValue.Serializer<Boolean, JsonPrimitive> BOOLEAN_SERIALIZER = new JsonValue.Serializer<>()
    {
        @Override
        public @Nullable Boolean jsonToValue(JsonPrimitive jsonPrimitive)
        {
            if (jsonPrimitive.isBoolean()) return jsonPrimitive.getAsBoolean();
            return null;
        }

        @Override
        public JsonPrimitive valueToJson(Boolean value)
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
        public final @Nullable T jsonToValue(JsonPrimitive jsonPrimitive)
        {
            if (jsonPrimitive.isNumber()) return getNumberFromJsonPrimitive(jsonPrimitive);
            return null;
        }

        protected abstract T getNumberFromJsonPrimitive(JsonPrimitive jsonPrimitive);

        @Override
        public final JsonPrimitive valueToJson(T value)
        {
            return new JsonPrimitive(onSerializeValue(value));
        }

        protected T onSerializeValue(T value)
        {
            return value;
        }
    }

    public static final JsonValue.Serializer<Integer, JsonPrimitive> INTEGER_SERIALIZER = new NumberSerializer<>()
    {
        @Override
        protected Integer getNumberFromJsonPrimitive(JsonPrimitive jsonPrimitive)
        {
            return jsonPrimitive.getAsInt();
        }
    };

    public static final JsonValue.Serializer<Long, JsonPrimitive> LONG_SERIALIZER = new NumberSerializer<>()
    {
        @Override
        protected Long getNumberFromJsonPrimitive(JsonPrimitive jsonPrimitive)
        {
            return jsonPrimitive.getAsLong();
        }
    };

    public static final JsonValue.Serializer<Float, JsonPrimitive> FLOAT_SERIALIZER = new NumberSerializer<>()
    {
        // Floats get rounded to this accuracy to prevent values like 0.789999996
        private static final float MAX_ACCURACY = 0.000001f;

        @Override
        protected Float getNumberFromJsonPrimitive(JsonPrimitive jsonPrimitive)
        {
            return jsonPrimitive.getAsFloat();
        }

        @Override
        protected Float onSerializeValue(Float value)
        {
            return (float) (Math.round((double) value / MAX_ACCURACY) * (double) MAX_ACCURACY);
        }
    };

    public static final JsonValue.Serializer<UUID, JsonPrimitive> UUID_SERIALIZER = new JsonValue.Serializer<>()
    {
        @Override
        public @Nullable UUID jsonToValue(JsonPrimitive jsonPrimitive)
        {
            if (jsonPrimitive.isString())
            {
                String uuidString = jsonPrimitive.getAsString();
                if (uuidString.length() == 32)
                {
                    uuidString = uuidString.substring(0, 8) + "-" + uuidString.substring(8, 12) + "-" + uuidString.substring(12, 16)
                        + "-" + uuidString.substring(16, 20) + "-" + uuidString.substring(20, 32);
                }
                try
                {
                    return UUID.fromString(uuidString);
                }
                catch (IllegalArgumentException ignored) {}
            }
            return null;
        }

        @Override
        public JsonPrimitive valueToJson(UUID value)
        {
            return new JsonPrimitive(value.toString());
        }
    };

    public static class EnumSerializer<T extends Enum<T>> implements JsonValue.Serializer<T, JsonPrimitive>
    {
        private final Class<T> type;

        public EnumSerializer(Class<T> type)
        {
            this.type = Objects.requireNonNull(type);
        }

        @Override
        public @Nullable T jsonToValue(JsonPrimitive jsonPrimitive)
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
        public JsonPrimitive valueToJson(T value)
        {
            return new JsonPrimitive(value.name());
        }
    }

    public record ObjectChildSerializer<T>(String path, JsonValue.Serializer<T, JsonPrimitive> serializer)
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