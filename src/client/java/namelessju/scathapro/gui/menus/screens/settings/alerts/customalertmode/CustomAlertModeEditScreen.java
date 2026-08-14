package namelessju.scathapro.gui.menus.screens.settings.alerts.customalertmode;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.alerts.alertmodes.AlertMode;
import namelessju.scathapro.alerts.alertmodes.AlertModeManager;
import namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertModeManager;
import namelessju.scathapro.alerts.title.AlertTitleTemplate;
import namelessju.scathapro.alerts.title.DynamicAlertTitleTemplate;
import namelessju.scathapro.alerts.title.FullAlertTitleTemplate;
import namelessju.scathapro.files.customalertmode.CustomAlertModeMeta;
import namelessju.scathapro.files.customalertmode.CustomAlertModeProperties;
import namelessju.scathapro.gui.menus.framework.screens.LayoutScreen;
import namelessju.scathapro.gui.menus.framework.widgets.lists.ScathaProGuiList;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.FloatSlider;
import namelessju.scathapro.gui.menus.screens.InfoMessageScreen;
import namelessju.scathapro.gui.menus.widgets.ScathaProEditBox;
import namelessju.scathapro.util.FileUtil;
import namelessju.scathapro.util.Util;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CustomAlertModeEditScreen extends LayoutScreen
{
    private final CustomAlertModeManager manager;

    private final @NonNull String modeId;
    private final @NonNull CustomAlertModeMeta modeMeta;
    private final @NonNull CustomAlertModeProperties modeProperties;
    private final boolean isNewMode;

    private @NonNull String modeName;
    private Map<Alert, AlertEditData> alertEditDataMap;

    private boolean isRebuildRequired = false;
    private ScathaProGuiList list;
    private double lastScrollAmount = -1D;
    private SoundInstance lastPlayedAlertSound;
    private SoundPreviewButton currentSoundPreviewButton;

    public CustomAlertModeEditScreen(ScathaPro scathaPro, Screen parentScreen, @NonNull String subModeId)
    {
        super(scathaPro, Component.literal(
            (scathaPro.customAlertModeManager.doesSubModeExist(subModeId) ? "Edit" : "Create") + " Custom Alert Mode"
        ), false, parentScreen);

        this.manager = scathaPro.customAlertModeManager;

        this.modeId = subModeId;
        this.isNewMode = !scathaPro.customAlertModeManager.doesSubModeExist(subModeId);

        this.modeMeta = manager.subModeMetas.getOrLoad(subModeId);
        this.modeProperties = manager.subModeProperties.getOrLoad(subModeId);

        this.modeName = modeMeta.modeName.getOr("");
    }

    private void initAlertDataIfUnset()
    {
        if (alertEditDataMap != null) return;

        alertEditDataMap = new HashMap<>();
        for (Alert alert : scathaPro.alertManager)
        {
            CustomAlertModeProperties.AlertPropertiesValue alertProperties = modeProperties.alertProperties.getPropertiesFor(alert);
            AlertEditData editData;
            if (alertProperties != null)
            {
                editData = new AlertEditData(
                    alertProperties.title.getOr(""),
                    alertProperties.subtitle.getOr(""),
                    alertProperties.soundVolume.get()
                );
            }
            else editData = new AlertEditData("", "", 1f);
            alertEditDataMap.put(alert, editData);
        }
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        initAlertDataIfUnset();

        LinearLayout headerWidgetsLayout = LinearLayout.vertical().spacing(0);

        headerWidgetsLayout.addChild(new StringWidget(Component.literal("Mode Name").withColor(TextColor.GRAY), font),
            LayoutSettings::alignHorizontallyLeft);

        LinearLayout headerSubLayout = LinearLayout.horizontal().spacing(10);

        ScathaProEditBox nameEditBox = headerSubLayout.addChild(new ScathaProEditBox(font, 225, 20, Component.literal("Custom Alert Mode Name")));
        nameEditBox.setValue(modeName);
        nameEditBox.setResponder(newValue -> modeName = newValue);
        nameEditBox.setHint(Component.literal("(unnamed)").setStyle(EditBox.DEFAULT_HINT_STYLE.withItalic(true)));

        Button exportButton = headerSubLayout.addChild(
            Button.builder(Component.literal("Export..."), _ -> {
                File exportDirectory = scathaPro.getConfigDirectoryPath().resolve(ScathaPro.MOD_ID + "_export").toFile();
                //noinspection ResultOfMethodCallIgnored
                exportDirectory.mkdirs();
                File file = FileUtil.getUniqueFile(exportDirectory,
                    modeMeta.modeName.getOr("Unnamed Custom Alert Mode") + ".spmode"
                );
                boolean success = FileUtil.zip(
                    Util.resolvePath(scathaPro.customAlertModeManager.subModesDirectory, modeId).toFile(),
                    file.getAbsolutePath(), false
                );
                if (success)
                {
                    InfoMessageScreen infoScreen = new InfoMessageScreen(scathaPro, CustomAlertModeEditScreen.this,
                        Component.literal("Custom alert mode exported"),
                        Component.literal("Exported as \"" + file.getName() + "\"")
                    );
                    infoScreen.setExtraWidgets(new AbstractWidget[] {
                        Button.builder(Component.literal("Show File"), _ -> {
                            try
                            {
                                FileUtil.openFileInExplorer(file);
                            }
                            catch (IOException e)
                            {
                                ScathaPro.LOGGER.error("Failed to open exported custom alert mode file in explorer", e);
                            }
                        }).size(200, 20).build()
                    });
                    minecraft.gui.setScreen(infoScreen);
                }
                else minecraft.gui.setScreen(new InfoMessageScreen(scathaPro, CustomAlertModeEditScreen.this,
                    Component.literal("Custom alert mode export failed").withColor(TextColor.RED),
                    Component.literal("Couldn't create/write the file")
                ));
            }).size(75, 20).build()
        );
        if (isNewMode)
        {
            exportButton.active = false;
            exportButton.setTooltip(Tooltip.create(
                Component.literal("Save your new mode before exporting!").withColor(TextColor.YELLOW)
            ));
        }
        else exportButton.setTooltip(Tooltip.create(
            Component.literal("Unsaved changes will not be exported!").withColor(TextColor.GRAY)
        ));

        headerWidgetsLayout.addChild(headerSubLayout);
        addTitleHeader(headerWidgetsLayout);


        list = addScrollList(new ScathaProGuiList(minecraft, this, layout, 125));
        for (Alert alert : scathaPro.alertManager)
        {
            AlertEditData data = alertEditDataMap.get(alert);
            if (data == null)
            {
                ScathaPro.LOGGER.error("Couldn't find alert edit data for {}", alert.alertId);
                continue;
            }
            list.addEntry(new Entry(alert, data, list.getRowWidth()));
        }


        LinearLayout footerLayout = LinearLayout.horizontal().spacing(10);
        footerLayout.addChild(
            Button.builder(Component.literal("Save"), _ -> save())
                .width(150).build(),
            LayoutSettings::alignHorizontallyCenter
        );
        footerLayout.addChild(doneButton(Component.literal("Cancel"), 150), LayoutSettings::alignHorizontallyCenter);
        addLayoutFooter(footerLayout);
    }

    @Override
    protected void init()
    {
        super.init();

        if (list != null && lastScrollAmount > 0D)
        {
            list.setScrollAmount(lastScrollAmount);
            lastScrollAmount = -1D;
        }
    }

    @Override
    protected void repositionElements()
    {
        // Hack since there's no onOpened() method:
        // this method is called when a screen is re-opened
        if (isRebuildRequired)
        {
            isRebuildRequired = false;
            rebuildWidgets();
            return;
        }

        super.repositionElements();
    }

    @Override
    public void tick()
    {
        super.tick();

        if (lastPlayedAlertSound != null)
        {
            if (!scathaPro.soundManager.isPlaying(lastPlayedAlertSound))
            {
                lastPlayedAlertSound = null;
                currentSoundPreviewButton.updateMessage(false);
                currentSoundPreviewButton = null;
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

        lastPlayedAlertSound = null;
        currentSoundPreviewButton = null;

        if (list != null) lastScrollAmount = list.scrollAmount();
        list = null;

        isRebuildRequired = true;
        this.clearWidgets();

        super.removed();
    }

    private void save()
    {
        modeMeta.modeName.set(modeName.isBlank() ? null : modeName);
        modeMeta.save();

        CustomAlertModeSoundImportScreen importScreen = new CustomAlertModeSoundImportScreen(scathaPro, parentScreen, modeId);

        for (Map.Entry<Alert, AlertEditData> entry : alertEditDataMap.entrySet())
        {
            CustomAlertModeProperties.AlertPropertiesValue alertProperties
                = modeProperties.alertProperties.initAndGetPropertiesFor(entry.getKey());
            alertProperties.title.set(entry.getValue().titleText.isEmpty() ? null : entry.getValue().titleText);
            alertProperties.subtitle.set(entry.getValue().subtitleText.isEmpty() ? null : entry.getValue().subtitleText);
            alertProperties.soundVolume.set(entry.getValue().soundVolume);

            if (entry.getValue().newSoundFile != null)
            {
                importScreen.addFile(entry.getKey(), entry.getValue().newSoundFile);
                alertProperties.soundSourceAlertMode.set(scathaPro.alertModeManager.customMode);
                alertProperties.soundSourceAlert.set(null);
            }
            else
            {
                if (entry.getValue().newSoundSourceAlertMode != null)
                {
                    alertProperties.soundSourceAlertMode.set(entry.getValue().newSoundSourceAlertMode);
                }
                if (entry.getValue().newSoundSourceAlert != null)
                {
                    alertProperties.soundSourceAlert.set(entry.getValue().newSoundSourceAlert);
                }
            }
        }
        modeProperties.save();

        if (importScreen.hasFiles()) minecraft.gui.setScreen(importScreen);
        else minecraft.gui.setScreen(parentScreen);
    }

    public static class AlertEditData
    {
        public @NonNull String titleText;
        public @NonNull String subtitleText;
        public float soundVolume;

        public @Nullable File newSoundFile = null;
        public @Nullable AlertMode newSoundSourceAlertMode = null;
        public @Nullable Alert newSoundSourceAlert = null;

        public AlertEditData(@NonNull String titleText, @NonNull String subtitleText, float soundVolume)
        {
            this.titleText = titleText;
            this.subtitleText = subtitleText;
            this.soundVolume = soundVolume;
        }
    }

    private class Entry extends ScathaProGuiList.Entry
    {
        public Entry(@NonNull Alert alert, @NonNull AlertEditData editData, int rowWidth)
        {
            CustomAlertModeProperties.AlertPropertiesValue alertProperties = modeProperties.alertProperties.getPropertiesFor(alert);
            if (alertProperties == null) alertProperties = new CustomAlertModeProperties.AlertPropertiesValue();

            int halfRowWidth = rowWidth / 2;

            addCenteredChild(
                label(0, 5, Component.literal(alert.alertName).withColor(TextColor.YELLOW)),
                halfRowWidth
            );

            // Titles

            AlertTitleTemplate titleTemplate = alert.titleTemplate;
            Tooltip titleFormattingTooltip = Tooltip.create(
                Component.literal("Supports formatting\ncodes using '&'").withColor(TextColor.GRAY)
            );

            addChild(label(0, 23, Component.literal("Title").withColor(TextColor.GRAY)));

            ScathaProEditBox titleEditBox = new ScathaProEditBox(font, halfRowWidth - 5, 20, Component.literal(alert.alertName + " Title"));
            if (alert.titleTemplate.titleStyle != null) titleEditBox.setBaseStyle(alert.titleTemplate.titleStyle);
            titleEditBox.setPreviewFormattingSequences(true);
            titleEditBox.setValue(editData.titleText);
            titleEditBox.setResponder(newValue -> editData.titleText = newValue);
            if (titleTemplate.titleText != null) titleEditBox.setHint(Component.literal(titleTemplate.titleText));
            titleEditBox.setTooltip(titleFormattingTooltip);
            addPositionedChild(0, 33, titleEditBox);

            ScathaProEditBox subtitleEditBox = new ScathaProEditBox(font, halfRowWidth - 5, 20, Component.literal(alert.alertName + " Subtitle"));
            subtitleEditBox.setPreviewFormattingSequences(true);
            boolean canEditSubtitle = false;
            if (titleTemplate instanceof FullAlertTitleTemplate fullAlertTitleTemplate)
            {
                canEditSubtitle = true;

                if (fullAlertTitleTemplate.subtitleStyle != null) subtitleEditBox.setBaseStyle(fullAlertTitleTemplate.subtitleStyle);
                subtitleEditBox.setValue(editData.subtitleText);
                if (fullAlertTitleTemplate.subtitleText != null)
                    subtitleEditBox.setHint(Component.literal(fullAlertTitleTemplate.subtitleText));
                subtitleEditBox.setTooltip(titleFormattingTooltip);
            }
            else if (titleTemplate instanceof DynamicAlertTitleTemplate)
            {
                subtitleEditBox.setHint(Component.literal("(determined by alert)").setStyle(EditBox.DEFAULT_HINT_STYLE.withItalic(true)));
            }
            subtitleEditBox.setResponder(newValue -> editData.subtitleText = newValue);

            addChild(label(halfRowWidth + 5, 23, Component.literal("Subtitle")
                .withColor(canEditSubtitle ? TextColor.GRAY : TextColor.DARK_GRAY)));

            if (!canEditSubtitle)
            {
                subtitleEditBox.active = false;
                subtitleEditBox.setEditable(false);
            }
            addPositionedChild(halfRowWidth + 5, 33, subtitleEditBox);

            // Audio

            boolean canPlayAudio = alertProperties.soundSourceAlertMode.get() != scathaPro.alertModeManager.customMode
                                    || manager.isSubModeActive(modeId);

            addChild(label(0, 62, Component.literal("Sound").withColor(TextColor.GRAY)));

            Button playButton = new SoundPreviewButton(0, 75, 150, 20, alert, alertProperties);
            if (!canPlayAudio)
            {
                playButton.active = false;
                playButton.setTooltip(Tooltip.create(
                    Component.literal("Mode needs to be selected to play custom sound").withColor(TextColor.YELLOW)
                ));
            }
            addChild(playButton);

            addChild(new FloatSlider(
                halfRowWidth + 5, 75, 150, 20,
                Component.literal("Volume"),
                0f, 1f, editData.soundVolume,
                value -> editData.soundVolume = value
            ).setStepSize(0.01f).setValueComponentSupplier(FloatSlider.PERCENTAGE_COMPONENT_SUPPLIER_WITH_OFF));

            Component selectSoundComponent = Component.literal("Select New Sound...");
            boolean buttonEnabled = false;

            Component selectSoundButtonComponent;
            if (editData.newSoundFile != null)
            {
                selectSoundButtonComponent = Component.literal(editData.newSoundFile.getName())
                    .withStyle(Style.EMPTY.withItalic(true));
                buttonEnabled = true;
            }
            else if (editData.newSoundSourceAlertMode != null)
            {
                selectSoundButtonComponent = getSoundText(
                    alert,
                    editData.newSoundSourceAlertMode, editData.newSoundSourceAlert,
                    () -> Component.literal("Saved Custom Sound")
                ).withStyle(Style.EMPTY.withItalic(true));
                buttonEnabled = true;
            }
            else selectSoundButtonComponent = selectSoundComponent;
            Button selectSoundButton;
            addChild(selectSoundButton = Button.builder(selectSoundButtonComponent,
                _ -> scathaPro.minecraft.gui.setScreen(
                    new CustomAlertModeSelectSoundScreen(scathaPro, CustomAlertModeEditScreen.this, modeId, alert, editData)
                )
            ).bounds(0, 100, 150, 20).build());

            Button discardButton;
            addChild(discardButton = Button.builder(Component.literal("Discard Selected Sound"), button -> {
                editData.newSoundFile = null;
                editData.newSoundSourceAlertMode = null;
                editData.newSoundSourceAlert = null;
                selectSoundButton.setMessage(selectSoundComponent);
                button.active = false;
            }).bounds(halfRowWidth + 5, 100, 150, 20).build());
            discardButton.active = buttonEnabled;
        }
    }

    private @NonNull MutableComponent getSoundText(@NonNull Alert alert, @NonNull AlertMode sourceAlertMode,
                                                    @Nullable Alert sourceAlert, @NonNull Supplier<MutableComponent> customSoundTextSupplier)
    {
        if (sourceAlertMode == scathaPro.alertModeManager.customMode)
        {
            return customSoundTextSupplier.get();
        }

        String sourceText = sourceAlertMode.name;
        if (sourceAlert != null && sourceAlert != alert)
        {
            sourceText += " (" + sourceAlert.alertName + ")";
        }
        return Component.literal(sourceText);
    }

    protected class SoundPreviewButton extends Button.Plain
    {
        private final Component baseComponent;

        protected SoundPreviewButton(int x, int y, int width, int height, Alert alert,
                                     CustomAlertModeProperties.@NonNull AlertPropertiesValue alertProperties)
        {
            super(x, y, width, height, Component.empty(),
                button -> {
                    CustomAlertModeProperties.AlertPropertiesValue alertPropertiesValue = modeProperties.alertProperties.getPropertiesFor(alert);
                    AlertEditData editData = alertEditDataMap.get(alert);
                    Alert sourceAlert = alertPropertiesValue != null ? alertPropertiesValue.soundSourceAlert.getOr(alert) : alert;

                    if (sourceAlert.isSoundPlaying(scathaPro.soundManager))
                    {
                        sourceAlert.stopSound(scathaPro.soundManager);
                        ((SoundPreviewButton) button).updateMessage(false);
                        if (currentSoundPreviewButton == button)
                        {
                            lastPlayedAlertSound = null;
                            currentSoundPreviewButton = null;
                        }
                    }
                    else
                    {
                        if (lastPlayedAlertSound != null) scathaPro.soundManager.stop(lastPlayedAlertSound);
                        if (currentSoundPreviewButton != null) currentSoundPreviewButton.updateMessage(false);

                        AlertMode sourceAlertMode = alertPropertiesValue != null ? alertPropertiesValue.soundSourceAlertMode.get() : AlertModeManager.DEFAULT_MODE;
                        float volume = editData != null ? editData.soundVolume : 1f;
                        lastPlayedAlertSound = sourceAlertMode == scathaPro.alertModeManager.customMode
                            ? alert.playSound(scathaPro.soundManager, sourceAlertMode, volume)
                            : sourceAlert.playSound(scathaPro.soundManager, sourceAlertMode, volume);
                        currentSoundPreviewButton = (SoundPreviewButton) button;
                        currentSoundPreviewButton.updateMessage(true);
                    }
                },
                Button.DEFAULT_NARRATION
            );

            baseComponent = Component.literal("Play Current: ").append(getSoundText(
                alert,
                alertProperties.soundSourceAlertMode.get(), alertProperties.soundSourceAlert.get(),
                () -> scathaPro.customAlertModeManager.getAlertAudioFile(modeId, alert).exists()
                    ? Component.literal("Custom")
                    : Component.literal("MISSING CUSTOM SOUND FILE").withColor(TextColor.RED)
            ));
            updateMessage(false);
        }

        private void updateMessage(boolean isPlaying)
        {
            this.setMessage(isPlaying ? Component.literal("Stop Sound") : baseComponent);
        }
    }
}