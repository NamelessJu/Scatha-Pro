package namelessju.scathapro.alerts.alertmodes.customalertmode;

import com.google.common.collect.MapMaker;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.files.customalertmode.CustomAlertModeMeta;
import namelessju.scathapro.files.customalertmode.CustomAlertModeProperties;
import namelessju.scathapro.files.framework.ScathaProFile;
import namelessju.scathapro.util.FileUtil;
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
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomAlertModeManager
{
    private final ScathaPro scathaPro;
    public final Path subModesDirectory;
    
    public final CustomAlertModePackResources resourcePack;
    
    public final SubModeFileManager<CustomAlertModeMeta> subModeMetas;
    public final SubModeFileManager<CustomAlertModeProperties> subModeProperties;
    private @Nullable CustomAlertModeProperties currentSubModeProperties = null;
    
    private @Nullable String currentSubModeId = null;
    
    public CustomAlertModeManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        subModesDirectory = scathaPro.getSaveDirectoryPath().resolve("customAlertModes");
        
        resourcePack = new CustomAlertModePackResources(scathaPro);
        
        subModeMetas = new SubModeFileManager<>(subModeId -> new CustomAlertModeMeta(scathaPro, subModeId));
        subModeProperties = new SubModeFileManager<>(subModeId -> new CustomAlertModeProperties(scathaPro, subModeId));
    }
    
    public void init()
    {
        loadCurrentSubMode();
    }
    
    public @NonNull File getMetaFile(@NonNull String subModeId)
    {
        return Util.resolvePath(subModesDirectory, subModeId, "meta.json").toFile();
    }
    
    public @NonNull File getPropertiesFile(@NonNull String subModeId)
    {
        return Util.resolvePath(subModesDirectory, subModeId, "assets", "properties.json").toFile();
    }
    
    public @NonNull Path getSoundsDirectoryPath(@NonNull String subModeId)
    {
        return Util.resolvePath(subModesDirectory, subModeId, "assets", "sounds");
    }
    
    public @NonNull File getAlertAudioFile(String subModeId, Alert alert)
    {
        return getAlertAudioFile(subModeId, alert.alertId);
    }
    
    public @NonNull File getAlertAudioFile(String subModeId, String alertId)
    {
        return getSoundsDirectoryPath(subModeId).resolve(alertId + ".ogg").toFile();
    }
    
    public @NonNull String[] findAllSubModeIds()
    {
        File submodesDirectoryFile = subModesDirectory.toFile();
        if (!submodesDirectoryFile.exists()) return new String[0];
        String[] ids = submodesDirectoryFile.list(DirectoryFileFilter.DIRECTORY);
        return ids != null ? ids : new String[0];
    }
    
    public boolean doesSubModeExist(@Nullable String subModeId)
    {
        if (subModeId == null) return false;
        for (String id : findAllSubModeIds())
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
        
        long now = TimeUtil.getEpochMilliseconds();
        
        // Update previous mode's last used timestamp
        if (currentSubModeId != null && doesSubModeExist(currentSubModeId))
        {
            CustomAlertModeMeta meta = subModeMetas.getOrLoad(currentSubModeId);
            meta.lastUsedAtTimestamp.set(now);
            meta.save();
        }
        
        setCurrentSubMode(subModeId);
        
        // Update new mode's last used timestamp
        if (subModeId != null && doesSubModeExist(subModeId))
        {
            CustomAlertModeMeta meta = subModeMetas.getOrLoad(subModeId);
            meta.lastUsedAtTimestamp.set(now);
            meta.save();
        }
        
        scathaPro.customAlertModeManager.reloadResourcePack();
    }
    
    private void loadCurrentSubMode()
    {
        String loadedSubmode = scathaPro.config.alerts.customModeSubmode.get();
        if (!doesSubModeExist(loadedSubmode)) loadedSubmode = null;
        setCurrentSubMode(loadedSubmode);
    }
    
    private void setCurrentSubMode(String subModeId)
    {
        currentSubModeId = (subModeId != null && !subModeId.isBlank()) ? subModeId : null;
        currentSubModeProperties = subModeId != null ? subModeProperties.getOrLoad(subModeId) : null;
    }
    
    public @Nullable String getCurrentSubModeId()
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
        
        File modeFolder = subModesDirectory.resolve(customModeId).toFile();
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
    
    public @NonNull Component getSubModeDisplayName(@NonNull String subModeId)
    {
        if (!doesSubModeExist(subModeId)) return Component.literal("(mode not found)").withStyle(ChatFormatting.ITALIC);
        
        CustomAlertModeMeta meta = subModeMetas.getOrLoad(subModeId);
        String submodeName = meta.modeName.get();
        if (submodeName == null || submodeName.isBlank())
        {
            return Component.literal("(unnamed)").withStyle(ChatFormatting.ITALIC);
        }
        else submodeName = submodeName.trim();
        return Component.literal(ChatFormatting.stripFormatting(submodeName));
    }
    
    public Map<String, CustomAlertModeMeta> getAllMeta()
    {
        Map<String, CustomAlertModeMeta> metaMap = new HashMap<>();
        for (String subModeId : findAllSubModeIds())
        {
            metaMap.put(subModeId, subModeMetas.getOrLoad(subModeId));
        }
        return metaMap;
    }
    
    
    public @Nullable CustomAlertModeProperties getCurrentSubModeProperties()
    {
        return currentSubModeProperties;
    }
    
    
    public static @NonNull Component formattedStringToComponent(@NonNull String string)
    {
        MutableComponent component = Component.empty();
        visitFormattedString(string, (formattingCode, formattedSequence) -> component.append(formattedSequence));
        return component;
    }
    
    public static @NonNull Component formattedStringToComponentWithCodes(@NonNull String string)
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
    
    public static class SubModeFileManager<T extends ScathaProFile>
    {
        private final ConcurrentMap<@NonNull String, @NonNull T> weakValuesMap = new MapMaker().weakValues().makeMap();
        private final Function<@NonNull String, @NonNull T> fileFactory;
        
        private SubModeFileManager(Function<@NonNull String, @NonNull T> fileFactory)
        {
            this.fileFactory = fileFactory;
        }
        
        public @NonNull T getOrLoad(@NonNull String subModeId)
        {
            if (weakValuesMap.containsKey(subModeId))
            {
                return weakValuesMap.get(subModeId);
            }
            
            T file = fileFactory.apply(subModeId);
            weakValuesMap.put(subModeId, file);
            if (file.getFile().exists())
            {
                file.load();
            }
            return file;
        }
    }
}
