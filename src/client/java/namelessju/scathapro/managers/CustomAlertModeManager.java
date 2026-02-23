package namelessju.scathapro.managers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertModePackResources;
import namelessju.scathapro.util.FileUtil;
import namelessju.scathapro.util.JsonUtil;
import namelessju.scathapro.util.TimeUtil;
import namelessju.scathapro.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomAlertModeManager
{
    private final ScathaPro scathaPro;
    public final Path submodesDirectory;
    
    public final CustomAlertModePackResources resourcePack;
    
    private String currentSubModeId = null;
    private JsonObject currentProperties = null;
    private final HashMap<String, JsonObject> metas = new HashMap<>();
    
    public CustomAlertModeManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        submodesDirectory = scathaPro.getSaveDirectoryPath().resolve("customAlertModes");
        
        resourcePack = new CustomAlertModePackResources(scathaPro);
    }
    
    public void init()
    {
        loadCurrentSubMode();
    }
    
    public @NonNull File getMetaFile(@NonNull String subModeId)
    {
        return Util.resolvePath(submodesDirectory, subModeId, "meta.json").toFile();
    }
    
    public @NonNull File getPropertiesFile(@NonNull String subModeId)
    {
        return Util.resolvePath(submodesDirectory, subModeId, "assets", "properties.json").toFile();
    }
    
    public @NonNull File getAlertAudioFile(String subModeId, Alert alert)
    {
        return Util.resolvePath(submodesDirectory, subModeId, "assets", "sounds", alert.alertId + ".ogg").toFile();
    }
    
    public @Nullable String[] findAllSubModeIds()
    {
        File submodesDirectoryFile = submodesDirectory.toFile();
        if (!submodesDirectoryFile.exists()) return new String[0];
        return submodesDirectoryFile.list(DirectoryFileFilter.DIRECTORY);
    }
    
    public boolean doesSubModeExist(@Nullable String subModeId)
    {
        if (subModeId == null) return false;
        
        String[] subModeIds = findAllSubModeIds();
        for (String id : subModeIds)
        {
            if (id.equals(subModeId)) return true;
        }
        
        return false;
    }
    
    public @Nullable String getNewSubModeId()
    {
        int tries = 0;
        String newId;
        do
        {
            if (tries > 9999) return null;
            newId = UUID.randomUUID().toString().replace("-", "");
            tries ++;
        }
        while (doesSubModeExist(newId));
        
        return newId;
    }
    
    public void reloadResourcePack()
    {
        scathaPro.minecraft.reloadResourcePacks();
    }
    
    public void changeSubMode(@Nullable String subModeId)
    {
        scathaPro.config.alerts.customModeSubmode.set(subModeId);
        scathaPro.config.save();
        
        // Update previous mode's last used timestamp
        if (doesSubModeExist(currentSubModeId))
        {
            updateSubModeLastUsed(currentSubModeId);
            saveMeta(currentSubModeId);
        }
        
        setCurrentSubMode(subModeId);
        
        // Update new mode's last used timestamp
        updateSubModeLastUsed(subModeId);
        saveMeta(subModeId);
        
        loadCurrentSubModeProperties();
        
        scathaPro.customAlertModeManager.reloadResourcePack();
    }
    
    private void loadCurrentSubMode()
    {
        String loadedSubmode = scathaPro.config.alerts.customModeSubmode.get();
        if (!doesSubModeExist(loadedSubmode)) loadedSubmode = null;
        setCurrentSubMode(loadedSubmode);
        loadCurrentSubModeProperties();
    }
    
    private void setCurrentSubMode(String subModeId)
    {
        currentSubModeId = (subModeId != null && !subModeId.isBlank()) ? subModeId : null;
    }
    
    
    public String getCurrentSubModeId()
    {
        return currentSubModeId;
    }
    
    public boolean isSubModeActive(@Nullable String subModeId)
    {
        if (currentSubModeId == null) return false;
        return currentSubModeId.equals(subModeId);
    }
    
    public void deleteSubMode(@NonNull String customModeId)
    {
        boolean wasActive = isSubModeActive(customModeId);
        
        if (wasActive) scathaPro.minecraft.getSoundManager().destroy();
        
        File modeFolder = submodesDirectory.resolve(customModeId).toFile();
        if (!FileUtil.deleteDirectoryRecursive(modeFolder))
        {
            ScathaPro.LOGGER.error("Couldn't delete custom alert mode - recursively deleting the directory failed");
        }
        
        if (wasActive)
        {
            scathaPro.minecraft.getSoundManager().reload();
            changeSubMode(null);
        }
    }
    
    
    public @Nullable String getSubModeName(@NonNull String subModeId)
    {
        return JsonUtil.getString(metas.get(subModeId), "name");
    }
    
    public void setSubModeName(@NonNull String subModeId, @NonNull String name)
    {
        setMeta(subModeId, "name", new JsonPrimitive(name));
    }
    
    public @NonNull Component getSubModeDisplayName(@NonNull String subModeId)
    {
        if (!doesSubModeExist(subModeId)) return Component.literal("(mode not found)").withStyle(ChatFormatting.ITALIC);
        
        String submodeName = getSubModeName(subModeId);
        if (submodeName == null || submodeName.replace(" ", "").isEmpty())
        {
            return Component.literal("(unnamed)").withStyle(ChatFormatting.ITALIC);
        }
        else submodeName = submodeName.trim();
        return Component.literal(ChatFormatting.stripFormatting(submodeName));
    }
    
    
    public void loadAllMeta()
    {
        unloadAllMeta();
        
        String[] submodeIds = findAllSubModeIds();
        for (String submodeId : submodeIds)
        {
            loadMeta(submodeId);
        }
    }
    
    public void unloadAllMeta()
    {
        metas.clear();
    }
    
    public void loadMeta(@Nullable String subModeId)
    {
        if (subModeId == null) return;
        
        String metaString;
        try
        {
            metaString = FileUtil.readFile(getMetaFile(subModeId));
        }
        catch (IOException e)
        {
            ScathaPro.LOGGER.warn("Failed to read custom alert mode meta file for sub-mode {}", subModeId, e);
            return;
        }
        if (metaString == null || !(JsonUtil.parseJson(metaString) instanceof JsonObject jsonObject))
        {
            ScathaPro.LOGGER.error("Failed to parse custom alert mode meta for sub-mode {}", subModeId);
            return;
        }
        metas.put(subModeId, jsonObject);
    }
    
    public void setMeta(@Nullable String subModeId, String path, JsonElement value)
    {
        if (subModeId == null) return;
        
        JsonObject metaJson = metas.computeIfAbsent(subModeId, k -> new JsonObject());
        JsonUtil.set(metaJson, path, value);
    }
    
    public void saveMeta(@Nullable String subModeId)
    {
        if (subModeId == null) return;
        
        JsonObject metaJson = metas.get(subModeId);
        if (metaJson == null) metaJson = new JsonObject();
        
        try
        {
            FileUtil.writeFile(getMetaFile(subModeId), metaJson.toString());
        }
        catch (IOException e)
        {
            ScathaPro.LOGGER.error("Failed to write custom alert mode meta file for sub-mode ({})", subModeId, e);
        }
    }

    public long getSubModeLastUsed(@Nullable String subModeId)
    {
        JsonObject metaJson = metas.get(subModeId);
        if (metaJson != null)
        {
            Long lastUsed = JsonUtil.getLong(metaJson, "lastUsed");
            if (lastUsed != null) return lastUsed;
        }
        return -1L;
    }
    
    public void updateSubModeLastUsed(@Nullable String subModeId)
    {
        if (!doesSubModeExist(subModeId)) return;
        setMeta(subModeId, "lastUsed", new JsonPrimitive(TimeUtil.now()));
    }
    
    
    public @Nullable JsonObject loadSubModeProperties(@Nullable String subModeId)
    {
        if (subModeId == null) return null;
        
        ScathaPro.LOGGER.debug("Loading custom alert mode properties for sub-mode {}", subModeId);
        String propertiesString;
        try
        {
            propertiesString = FileUtil.readFile(getPropertiesFile(subModeId));
        }
        catch (IOException e)
        {
            ScathaPro.LOGGER.warn("Failed to read custom alert mode properties file for sub-mode {}", subModeId, e);
            return new JsonObject();
        }
        if (propertiesString != null)
        {
            JsonElement properties = JsonUtil.parseJson(propertiesString);
            if (properties instanceof JsonObject propertiesObject)
            {
                if (ScathaPro.LOGGER.isDebugEnabled())
                {
                    ScathaPro.LOGGER.debug("Loaded custom alert mode properties for sub-mode {}:\n{}", subModeId, properties);
                }
                return propertiesObject;
            }
            ScathaPro.LOGGER.error("Failed to parse custom alert mode properties for sub-mode {}", subModeId);
            return null;
        }
        
        ScathaPro.LOGGER.error("Failed to read custom alert mode properties file for sub-mode {}", subModeId);
        return null;
    }
    
    public void loadCurrentSubModeProperties()
    {
        currentProperties = loadSubModeProperties(currentSubModeId);
    }

    public @Nullable JsonElement getCurrentSubmodePropertyJsonElement(@Nullable String path)
    {
        if (currentProperties == null) return null;
        return JsonUtil.getJsonElement(currentProperties, path);
    }

    public @Nullable String getCurrentSubModeUnformattedStringProperty(@Nullable String path)
    {
        if (currentProperties == null) return null;
        String propertyValue = JsonUtil.getString(currentProperties, path);
        if (propertyValue == null) return null;
        return ChatFormatting.stripFormatting(propertyValue);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveSubModeProperties(String subModeId, JsonObject properties)
    {
        if (subModeId == null) return;
        
        File propertiesFile = getPropertiesFile(subModeId);
        propertiesFile.getParentFile().mkdirs();
        try
        {
            FileUtil.writeFile(propertiesFile, properties.toString());
        }
        catch (IOException e)
        {
            ScathaPro.LOGGER.error("Failed to write custom alert mode properties file for sub-mode ({})", subModeId, e);
        }
    }
    
    
    public static @NonNull Component convertFormattingCodes(@NonNull String string)
    {
        MutableComponent component = Component.empty();
        visitFormattedString(string, (formattingCode, formattedSequence) -> component.append(formattedSequence));
        return component;
    }
    
    public static @NonNull Component formatFormattingCodes(@NonNull String string)
    {
        MutableComponent component = Component.empty();
        visitFormattedString(string, (formattingCode, formattedSequence) -> {
            if (formattingCode != null) component.append(Component.literal(formattingCode).withStyle(ChatFormatting.DARK_GRAY));
            component.append(formattedSequence);
        });
        return component;
    }
    
    private static void visitFormattedString(@NonNull String string, FormattedStringConsumer consumer)
    {
        Matcher formattingMatcher = Pattern.compile("(?i)&[0-9a-fk-or]").matcher(string);
        Style style = Style.EMPTY;
        String previousFormattingCode = null;
        int previousStartIndex = 0;
        while (formattingMatcher.find())
        {
            // consume previous segment
            consumer.accept(previousFormattingCode,
                Component.literal(string.substring(previousStartIndex, formattingMatcher.start()))
                    .setStyle(style)
            );
            
            ChatFormatting formatting = ChatFormatting.getByCode(formattingMatcher.group().charAt(1));
            if (formatting != null) style = style.applyFormat(formatting);
            
            previousFormattingCode = formattingMatcher.group();
            previousStartIndex = formattingMatcher.end();
        }
        
        // consume remaining segment
        consumer.accept(previousFormattingCode,
            Component.literal(string.substring(previousStartIndex))
                .setStyle(style)
        );
    }
    
    private interface FormattedStringConsumer
    {
        void accept(String formattingCode, Component formattedSequence);
    }
}
