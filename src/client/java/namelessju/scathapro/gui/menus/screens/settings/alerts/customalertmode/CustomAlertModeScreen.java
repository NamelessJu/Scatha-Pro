package namelessju.scathapro.gui.menus.screens.settings.alerts.customalertmode;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.LayoutScreen;
import namelessju.scathapro.gui.menus.screens.InfoMessageScreen;
import namelessju.scathapro.gui.menus.widgets.CustomAlertModeList;
import namelessju.scathapro.util.FileUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.compress.utils.Lists;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class CustomAlertModeScreen extends LayoutScreen
{
    private DirectoryWatcher directoryWatcher;
    private long refreshTickTimer = 0L;
    
    public CustomAlertModeScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, Component.literal("Custom Alert Modes"), true, parentScreen);
        directoryWatcher = DirectoryWatcher.create(scathaPro.customAlertModeManager.submodesDirectory);
    }
    
    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader(new StringWidget(
            Component.literal("Drag and drop custom mode files into this window to import them")
                .withStyle(ChatFormatting.GRAY), font
        ));
        addScrollList(new CustomAlertModeList(scathaPro, this, layout));
        addDoneButtonFooter();
    }
    
    public void refresh()
    {
        refreshTickTimer = 0L;
        rebuildWidgets();
    }
    
    @Override
    public void tick()
    {
        if (directoryWatcher != null)
        {
            try
            {
                if (directoryWatcher.pollForChanges())
                {
                    refreshTickTimer = 20L;
                }
            }
            catch (IOException e)
            {
                ScathaPro.LOGGER.warn("Failed to poll for custom alert mode directory changes, stopping");
                closeWatcher();
            }
        }
        
        if (refreshTickTimer > 0L && --refreshTickTimer == 0L)
        {
            refresh();
        }
    }
    
    @Override
    public void onClose()
    {
        closeWatcher();
        super.onClose();
    }
    
    @Override
    public void onFilesDrop(@NonNull List<Path> list)
    {
        List<FailedImport> failedImports = Lists.newArrayList();
        boolean anyFileImported = false;
        
        for (Path path : list)
        {
            File file = path.toFile();
            if (!file.exists())
            {
                failedImports.add(new FailedImport(file, Component.literal("File doesn't exist")));
                continue;
            }
            if (!file.isFile())
            {
                failedImports.add(new FailedImport(file, Component.literal("Not a file")));
                continue;
            }
            
            String modeFolderName = file.getName();
            
            // validate and remove file extension
            int dotIndex = modeFolderName.lastIndexOf('.');
            if (dotIndex < 0 || !modeFolderName.substring(dotIndex + 1).equals("spmode"))
            {
                failedImports.add(new FailedImport(file, Component.literal("File isn't a custom mode")));
                continue;
            }
            modeFolderName = modeFolderName.substring(0, dotIndex);
            
            File importDirectory = FileUtil.getUniqueFile(scathaPro.customAlertModeManager.submodesDirectory.toFile(), modeFolderName);
            
            if (!FileUtil.unzip(file, importDirectory.toPath(), null))
            {
                failedImports.add(new FailedImport(file, Component.literal("Failed to extract file")));
                continue;
            }
            
            String subModeId = importDirectory.getName();
            scathaPro.customAlertModeManager.loadMeta(subModeId);
            scathaPro.customAlertModeManager.updateSubModeLastUsed(subModeId);
            scathaPro.customAlertModeManager.saveMeta(subModeId);
            
            anyFileImported = true;
        }
        
        if (!failedImports.isEmpty())
        {
            MutableComponent description = Component.empty();
            for (FailedImport failedImport : failedImports)
            {
                if (!description.equals(CommonComponents.EMPTY)) description.append("\n");
                description.append(failedImport.file().getName() + ": ").append(failedImport.reason);
            }
            
            scathaPro.minecraft.setScreen(new InfoMessageScreen(scathaPro, this,
                Component.literal(
                    failedImports.size() > 1
                        ? "Failed custom alert mode imports"
                        : "Custom alert mode import failed"
                ).withStyle(ChatFormatting.RED),
                description
            ));
            return;
        }
        
        if (anyFileImported) refresh();
    }
    
    private void closeWatcher()
    {
        if (directoryWatcher != null)
        {
            try
            {
                directoryWatcher.close();
                directoryWatcher = null;
            }
            catch (Exception ignored) {}
        }
    }
    
    private record FailedImport(File file, Component reason) {}
    
    private static class DirectoryWatcher implements AutoCloseable
    {
        private final WatchService watcher;
        private final Path packPath;
        
        private DirectoryWatcher(Path path) throws IOException
        {
            this.packPath = path;
            this.watcher = path.getFileSystem().newWatchService();
            
            try
            {
                this.watch(path);
                DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);
                
                try
                {
                    for (Path childPath : directoryStream)
                    {
                        if (Files.isDirectory(childPath, LinkOption.NOFOLLOW_LINKS))
                        {
                            this.watch(childPath);
                        }
                    }
                }
                catch (Throwable e)
                {
                    try
                    {
                        directoryStream.close();
                    }
                    catch (Throwable e2)
                    {
                        e.addSuppressed(e2);
                    }
                    
                    throw e;
                }
                
                directoryStream.close();
            }
            catch (Exception e)
            {
                this.watcher.close();
                throw e;
            }
        }
        
        public static CustomAlertModeScreen.@Nullable DirectoryWatcher create(Path path)
        {
            try
            {
                return new DirectoryWatcher(path);
            }
            catch (IOException e)
            {
                ScathaPro.LOGGER.warn("Failed to initialize custom alert mode directory {} monitoring", path, e);
                return null;
            }
        }
        
        private void watch(Path path) throws IOException
        {
            path.register(this.watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY
            );
        }
        
        public boolean pollForChanges() throws IOException
        {
            boolean changesFound = false;
            
            WatchKey watchKey;
            while ((watchKey = this.watcher.poll()) != null)
            {
                for (WatchEvent<?> watchEvent : watchKey.pollEvents())
                {
                    changesFound = true;
                    if (watchKey.watchable() == this.packPath && watchEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE)
                    {
                        Path path = this.packPath.resolve((Path) watchEvent.context());
                        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS))
                        {
                            this.watch(path);
                        }
                    }
                }
                
                watchKey.reset();
            }
            
            return changesFound;
        }
        
        public void close() throws IOException
        {
            this.watcher.close();
        }
    }
}
