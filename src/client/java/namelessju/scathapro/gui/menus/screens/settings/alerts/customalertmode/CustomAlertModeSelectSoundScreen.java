package namelessju.scathapro.gui.menus.screens.settings.alerts.customalertmode;

import com.google.common.collect.Lists;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.alerts.alertmodes.AlertMode;
import namelessju.scathapro.alerts.alertmodes.AlertModeManager;
import namelessju.scathapro.gui.menus.framework.screens.LayoutScreen;
import namelessju.scathapro.gui.menus.framework.widgets.lists.ScathaProGuiList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CustomAlertModeSelectSoundScreen extends LayoutScreen
{
    private final @NonNull String subModeId;
    private final @NonNull Alert alert;
    private final CustomAlertModeEditScreen.@NonNull AlertEditData editData;
    private @NonNull AlertMode soundSourceAlertMode = AlertModeManager.DEFAULT_MODE;
    private @NonNull Alert soundSourceAlert;

    private ScathaProGuiList list;
    private ScathaProGuiList.Entry customFilesNoteEntry;
    private SoundPreviewButton soundPreviewButton;

    private @Nullable SoundInstance lastPlayedPreviewSound = null;

    public CustomAlertModeSelectSoundScreen(ScathaPro scathaPro, Screen parentScreen,
                                            @NonNull String subModeId, @NonNull Alert alert,
                                            CustomAlertModeEditScreen.@NonNull AlertEditData editData)
    {
        super(scathaPro, Component.literal("Select Custom Alert Mode Audio"), false, parentScreen);

        this.subModeId = subModeId;
        this.alert = alert;
        this.editData = editData;
        this.soundSourceAlert = alert;
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();

        list = addScrollList();

        // Inbuilt sounds selection
        list.addEntry(new ScathaProGuiList.Entry(new ScathaProGuiList.Entry.PositionedChild(
            label(0, 0, Component.literal("Inbuilt Sounds")),
            list.getRowWidth()/2, 6, true
        )));
        list.addEntry(new ScathaProGuiList.Entry(
            new CycleButton.Builder<>(value -> Component.literal(value.name + " Mode"), () -> soundSourceAlertMode)
                .withValues(
                    Arrays.stream(scathaPro.alertModeManager.getAllModes())
                        .filter(alertMode -> alertMode != scathaPro.alertModeManager.customMode)
                        .toList()
                )
                .create(0, 0, 150, 20, Component.literal("Inherit From"),
                    (_, value) -> soundSourceAlertMode = value
                ),
            new CycleButton.Builder<>(value -> Component.literal(value.alertName), () -> alert)
                .withValues(Lists.newArrayList(scathaPro.alertManager))
                .create(160, 0, 150, 20, Component.literal("Source"),
                    (_, value) -> soundSourceAlert = value
                )
        ));
        list.addEntry(new ScathaProGuiList.Entry(
            soundPreviewButton = new SoundPreviewButton(0, 0, 150, 20),
            Button.builder(
                Component.literal("Select"),
                _ -> selectAlertModeSound()
            ).bounds(160, 0, 150, 20).build()
        ));

        // Custom sound selection
        list.addEntry(new ScathaProGuiList.Entry(new ScathaProGuiList.Entry.PositionedChild(
            label(0, 0, Component.literal("Custom Sound")),
            list.getRowWidth()/2, 6, true
        )));
        if (scathaPro.customAlertModeManager.getAlertAudioFile(subModeId, alert).exists())
        {
            list.addEntry(new ScathaProGuiList.Entry(
                Button.builder(Component.literal("Saved Custom Sound"), _ -> {
                    editData.newSoundSourceAlertMode = scathaPro.alertModeManager.customMode;
                    onClose();
                }).size(310, 20).tooltip(Tooltip.create(
                    Component.literal("The custom sound that was\nlast used for this alert").withColor(TextColor.GRAY)
                )).build()
            ));
        }
        list.addEntry(customFilesNoteEntry = new ScathaProGuiList.Entry(new ScathaProGuiList.Entry.PositionedChild(
            new MultiLineTextWidget(Component.empty()
                .append("Drag and drop sound files into this window to import them\n").withColor(TextColor.GRAY)
                .append(
                    scathaPro.ffmpegManager.isFFmpegInstalled()
                        ? Component.literal(
                                "Supported file types: "
                                + String.join(", ", scathaPro.ffmpegManager.getSupportedFileExtensions())
                            ).withColor(TextColor.GRAY)
                        : Component.literal("No FFmpeg installation found, only .ogg is supported").withColor(TextColor.YELLOW)
                ),
                font
            ).setCentered(true),
            list.getRowWidth()/2, 4, true
        )));

        addFooter(doneButton(CommonComponents.GUI_CANCEL, 200));
    }

    @Override
    public void onFilesDrop(@NotNull List<Path> filePaths)
    {
        String[] supportedExtensions = scathaPro.ffmpegManager.getSupportedFileExtensions();
        Iterator<Path> iterator = filePaths.iterator();
        while (iterator.hasNext())
        {
            String fileName = iterator.next().toFile().getName();
            boolean isFileSupported = false;
            for (String extension : supportedExtensions)
            {
                if (fileName.endsWith(extension))
                {
                    isFileSupported = true;
                    break;
                }
            }
            if (!isFileSupported) iterator.remove();
        }

        if (filePaths.size() == 1)
        {
            selectCustomSound(filePaths.getFirst().toFile());
            return;
        }

        for (Path path : filePaths)
        {
            File file = path.toFile();
            list.addEntry(new ScathaProGuiList.Entry(
                Button.builder(Component.literal(file.getName()),
                    _ -> CustomAlertModeSelectSoundScreen.this.selectCustomSound(file)
                ).size(310, 20).build()
            ));
        }
        list.sort((entry1, entry2) -> {
            if (entry1 == customFilesNoteEntry) return 1;
            if (entry2 == customFilesNoteEntry) return -1;
            return 0;
        });
    }

    @Override
    public void tick()
    {
        super.tick();

        if (lastPlayedPreviewSound != null)
        {
            if (!scathaPro.soundManager.isPlaying(lastPlayedPreviewSound))
            {
                lastPlayedPreviewSound = null;
                soundPreviewButton.updateMessage(false);
            }
        }
    }

    @Override
    public void removed()
    {
        for (Alert alert : scathaPro.alertManager)
        {
            alert.stopSound(scathaPro.soundManager);
        }

        lastPlayedPreviewSound = null;
    }

    private void selectAlertModeSound()
    {
        editData.newSoundFile = null;
        editData.newSoundSourceAlertMode = soundSourceAlertMode;
        editData.newSoundSourceAlert = soundSourceAlert;

        onClose();
    }

    private void selectCustomSound(File file)
    {
        editData.newSoundFile = file;
        editData.newSoundSourceAlertMode = null;
        editData.newSoundSourceAlert = null;

        onClose();
    }

    protected class SoundPreviewButton extends Button.Plain
    {
        private final Component baseComponent;

        protected SoundPreviewButton(int x, int y, int width, int height)
        {
            super(x, y, width, height, Component.empty(),
                button -> {
                    if (scathaPro.soundManager.isPlaying(lastPlayedPreviewSound))
                    {
                        scathaPro.soundManager.stop(lastPlayedPreviewSound);
                        lastPlayedPreviewSound = null;
                        ((SoundPreviewButton) button).updateMessage(false);
                    }
                    else
                    {
                        lastPlayedPreviewSound = soundSourceAlert.playSound(scathaPro.soundManager, soundSourceAlertMode);
                        ((SoundPreviewButton) button).updateMessage(true);
                    }
                },
                Button.DEFAULT_NARRATION
            );

            baseComponent = Component.literal("Play Preview");
            updateMessage(false);
        }

        private void updateMessage(boolean isPlaying)
        {
            this.setMessage(isPlaying ? Component.literal("Stop Sound") : baseComponent);
        }
    }
}