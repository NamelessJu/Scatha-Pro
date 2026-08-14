package namelessju.scathapro.gui.menus.screens.settings.alerts.customalertmode;

import com.google.common.collect.Lists;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.gui.menus.framework.screens.LayoutScreen;
import namelessju.scathapro.gui.menus.screens.InfoMessageScreen;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CustomAlertModeSoundImportScreen extends LayoutScreen
{
    private final @NonNull String subModeId;

    private final List<ImportFile> importFiles = Lists.newArrayList();
    private final List<File> failedFileCopies = Lists.newArrayList();
    private final List<File> failedFileConversions = Lists.newArrayList();

    private boolean resourceReloadRequired = false;

    private StringWidget statusLabel;

    public CustomAlertModeSoundImportScreen(ScathaPro scathaPro, Screen returnScreen, @NonNull String subModeId)
    {
        super(scathaPro, Component.literal("Custom Alert Audio Import"), false, returnScreen);

        this.subModeId = subModeId;
    }

    public void addFile(@NonNull Alert alert, @NonNull File file)
    {
        importFiles.add(new ImportFile(alert, file));
    }

    public boolean hasFiles()
    {
        return !importFiles.isEmpty();
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();

        layout.addToContents(
            statusLabel = label(0, 0, Component.empty()),
            LayoutSettings::alignHorizontallyCenter
        );

        startImport();
    }

    private void setStatus(String message)
    {
        statusLabel.setMessage(Component.literal(message));
        repositionElements();
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return false;
    }

    private void startImport()
    {
        scathaPro.minecraft.getSoundManager().destroy();

        //noinspection ResultOfMethodCallIgnored
        scathaPro.customAlertModeManager.getSoundsDirectoryPath(subModeId).toFile().mkdirs();

        setStatus("Copying files...");

        List<ImportFile> fileConversions = Lists.newArrayList();
        for (ImportFile importFile : importFiles)
        {
            if (scathaPro.ffmpegManager.needsConversion(importFile.file))
            {
                fileConversions.add(importFile);
            }
            else copyFile(importFile);
        }

        Iterator<ImportFile> conversionIterator = fileConversions.iterator();
        AtomicInteger conversionCounter = new AtomicInteger();
        // Array prevents Java from complaining about the self reference
        Runnable[] convertNext = new Runnable[1];
        convertNext[0] = () -> {
            if (conversionIterator.hasNext())
            {
                setStatus("Converting files... (" + conversionCounter.get() + "/" + fileConversions.size() + ")");
                conversionCounter.getAndIncrement();
                convertFile(conversionIterator.next(), convertNext[0]);
            }
            else scathaPro.minecraft.execute(this::returnToParentScreen);
        };
        convertNext[0].run();
    }

    private void copyFile(ImportFile importFile)
    {
        File targetFile = scathaPro.customAlertModeManager.getAlertAudioFile(subModeId, importFile.alert);
        try
        {
            FileUtils.copyFile(importFile.file, targetFile);
            resourceReloadRequired = true;
        }
        catch (IOException e)
        {
            failedFileCopies.add(importFile.file);
            ScathaPro.LOGGER.error("Failed to copy custom alert file {}", importFile.file.getAbsolutePath(), e);
        }
    }

    private void convertFile(ImportFile importFile, Runnable callback)
    {
        File targetFile = scathaPro.customAlertModeManager.getAlertAudioFile(subModeId, importFile.alert);
        scathaPro.ffmpegManager.convertToOgg(
            importFile.file.getAbsolutePath(),
            targetFile.getAbsolutePath(),
            success -> {
                if (success) resourceReloadRequired = true;
                else failedFileConversions.add(targetFile);
                callback.run();
            }
        );
    }

    private void returnToParentScreen()
    {
        scathaPro.minecraft.getSoundManager().reload();

        if (resourceReloadRequired)
        {
            scathaPro.customAlertModeManager.reloadResourcePack();
        }

        Screen returnScreen = parentScreen;
        if (!failedFileCopies.isEmpty() || !failedFileConversions.isEmpty())
        {
            StringBuilder description = new StringBuilder();

            Function<List<File>, String> getFileNames = list -> list.stream()
                .map(File::getName)
                .collect(Collectors.joining(", "));

            if (!failedFileCopies.isEmpty())
            {
                description.append("The following files couldn't be copied into the custom modes folder:\n")
                    .append(getFileNames.apply(failedFileCopies));
            }

            if (!failedFileConversions.isEmpty())
            {
                if (!description.isEmpty()) description.append("\n\n");

                description.append("The following files couldn't be converted to ogg:\n")
                    .append(getFileNames.apply(failedFileConversions));
            }

            returnScreen = new InfoMessageScreen(scathaPro, returnScreen,
                Component.literal("Failed sound imports").withColor(TextColor.RED),
                Component.literal(description.toString())
            );
        }
        minecraft.gui.setScreen(returnScreen);
    }

    private record ImportFile(@NonNull Alert alert, @NonNull File file) {}
}