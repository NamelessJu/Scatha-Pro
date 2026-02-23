package namelessju.scathapro.alerts.alertmodes.customalertmode;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.util.Util;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.BuiltInMetadata;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class CustomAlertModePackResources implements PackResources
{
    public static final String NAMESPACE = "scathapro_customalertmode";
    private static final BuiltInMetadata METADATA = BuiltInMetadata.of(PackMetadataSection.CLIENT_TYPE, new PackMetadataSection(
        Component.empty(),
        SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES).minorRange()
    ));
    
    private final ScathaPro scathaPro;
    private final PackLocationInfo locationInfo;
    
    public CustomAlertModePackResources(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        this.locationInfo = new PackLocationInfo(
            NAMESPACE,
            Component.literal(ScathaPro.MOD_NAME + " Custom Alert Mode Resources"),
            PackSource.DEFAULT,
            Optional.empty()
        );
    }
    
    @Override
    public @NonNull Set<String> getNamespaces(@NonNull PackType type)
    {
        return type == PackType.CLIENT_RESOURCES ? Set.of(NAMESPACE) : Set.of();
    }
    
    @Override
    public @NonNull PackLocationInfo location()
    {
        return locationInfo;
    }
    
    @Override
    public <T> T getMetadataSection(@NonNull MetadataSectionType<T> type)
    {
        return METADATA.get(type);
    }
    
    @Override
    public void listResources(@NonNull PackType type, @NonNull String namespace, @NonNull String prefix, @NonNull ResourceOutput output)
    {
        if (type != PackType.CLIENT_RESOURCES) return;
        if (!namespace.equals(NAMESPACE)) return;
        
        String subMode = scathaPro.customAlertModeManager.getCurrentSubModeId();
        if (subMode == null || subMode.isBlank()) return;
        
        if (prefix.equals("sounds"))
        {
            ScathaPro.LOGGER.debug("Listing custom alert mode sound resources");
            
            // sounds.json
            output.accept(
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "sounds.json"),
                () -> new ByteArrayInputStream(
                    generateSoundsJson().toString()
                        .getBytes(StandardCharsets.UTF_8)
                )
            );
            ScathaPro.LOGGER.debug(" -> accepted sounds.json");
            
            // Sound files
            File soundsDir = getAssetsPath(subMode).resolve("sounds").toFile();
            if (ScathaPro.LOGGER.isDebugEnabled()) ScathaPro.LOGGER.debug("Sounds directory: {}", soundsDir.getAbsolutePath());
            if (!soundsDir.isDirectory()) return;
            
            File[] files = soundsDir.listFiles();
            if (files == null) return;
            for (File file : files)
            {
                if (!file.isFile() || !file.getName().endsWith(".ogg")) continue;
                
                String path = "sounds/" + file.getName();
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
                
                output.accept(id, () -> new FileInputStream(file));
                if (ScathaPro.LOGGER.isDebugEnabled()) ScathaPro.LOGGER.debug(" -> accepted {}", file.getAbsolutePath());
            }
        }
    }
    
    @Override
    public @Nullable IoSupplier<InputStream> getResource(@NonNull PackType type, @NonNull ResourceLocation identifier)
    {
        if (type != PackType.CLIENT_RESOURCES) return null;
        if (!identifier.getNamespace().equals(NAMESPACE)) return null;
        
        String subMode = scathaPro.customAlertModeManager.getCurrentSubModeId();
        if (subMode == null || subMode.isBlank()) return null;
        
        // sounds.json
        if (identifier.getPath().equals("sounds.json"))
        {
            byte[] data = generateSoundsJson().toString().getBytes(StandardCharsets.UTF_8);
            ScathaPro.LOGGER.debug("Generated custom alert mode sounds.json and returned IoSupplier");
            return () -> new ByteArrayInputStream(data);
        }
        
        // Other files
        File file = Util.resolvePath(getAssetsPath(subMode), identifier.getPath()).toFile();
        if (file.isFile())
        {
            ScathaPro.LOGGER.debug("Created IoSupplier for custom alert mode resource {}", identifier.getPath());
            return () -> new FileInputStream(file);
        }
        
        return null;
    }
    
    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String @NonNull ... path)
    {
        return null;
    }
    
    @Override
    public void close() {}
    
    
    private @NonNull JsonObject generateSoundsJson()
    {
        JsonObject root = new JsonObject();
        
        for (Alert alert : scathaPro.alertManager)
        {
            if (!soundFileExists(alert)) continue;
            
            JsonObject entry = new JsonObject();
            entry.addProperty("category", "master");
            
            JsonArray sounds = new JsonArray();
            JsonObject sound = new JsonObject();
            sound.addProperty("name", NAMESPACE + ":" + alert.alertId);
            sound.addProperty("stream", true);
            sounds.add(sound);
            
            entry.add("sounds", sounds);
            root.add(alert.alertId, entry);
        }
        
        return root;
    }
    
    private boolean soundFileExists(Alert alert)
    {
        String subMode = scathaPro.customAlertModeManager.getCurrentSubModeId();
        if (subMode == null || subMode.isBlank()) return false;
        
        File file = Util.resolvePath(getAssetsPath(subMode), "sounds", alert.alertId+".ogg").toFile();
        return file.isFile();
    }
    
    private @NonNull Path getAssetsPath(@NonNull String subMode)
    {
        return Util.resolvePath(scathaPro.customAlertModeManager.submodesDirectory, subMode, "assets");
    }
}