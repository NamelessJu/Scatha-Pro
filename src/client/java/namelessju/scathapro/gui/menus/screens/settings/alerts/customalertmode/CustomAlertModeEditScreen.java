package namelessju.scathapro.gui.menus.screens.settings.alerts.customalertmode;

import com.google.gson.JsonObject;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.alerts.title.AlertTitleTemplate;
import namelessju.scathapro.alerts.title.DynamicAlertTitleTemplate;
import namelessju.scathapro.alerts.title.FullAlertTitleTemplate;
import namelessju.scathapro.gui.menus.framework.screens.LayoutScreen;
import namelessju.scathapro.gui.menus.framework.widgets.lists.ScathaProGuiList;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.FloatSlider;
import namelessju.scathapro.managers.CustomAlertModeManager;
import namelessju.scathapro.util.JsonUtil;
import namelessju.scathapro.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class CustomAlertModeEditScreen extends LayoutScreen
{
    private final CustomAlertModeManager manager;
    private ScathaProGuiList list;
    
    private final @NonNull String modeId;
    private final @Nullable JsonObject modeProperties;
    private final @NonNull File modeFolder;
    private final boolean isNewMode;
    private final @NonNull String currentModeName;
    
    private @Nullable File droppedFile = null;
    
    public CustomAlertModeEditScreen(ScathaPro scathaPro, Screen parentScreen, @NonNull String subModeId)
    {
        super(scathaPro, Component.literal(
            (scathaPro.customAlertModeManager.doesSubModeExist(subModeId) ? "Edit" : "Create") + " Custom Alert Mode"
        ), true, parentScreen);
        
        this.manager = scathaPro.customAlertModeManager;
        
        this.modeId = subModeId;
        this.modeProperties = manager.loadSubModeProperties(subModeId);
        this.modeFolder = manager.submodesDirectory.resolve(subModeId).toFile();
        this.isNewMode = !modeFolder.exists();
        
        this.currentModeName = Objects.requireNonNullElse(manager.getSubModeName(subModeId), "");
    }
    
    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        /* TODO: add note somewhere if FFmpeg is missing
        if (!scathaPro.ffmpegManager.isFFmpegInstalled())
        {
            audioFileButtonDefaultTooltip = EnumChatFormatting.YELLOW + "Note:\n" + EnumChatFormatting.GRAY + "No FFmpeg installation found,\nonly *.ogg files supported";
        }
        */
        
        LinearLayout headerWidgetsLayout = LinearLayout.vertical().spacing(0);
        
        headerWidgetsLayout.addChild(new StringWidget(Component.literal("Mode Name").withStyle(ChatFormatting.GRAY), font),
            LayoutSettings::alignHorizontallyLeft);
        
        LinearLayout headerSubLayout = LinearLayout.horizontal().spacing(10);
        
        EditBox nameEditBox = headerSubLayout.addChild(new EditBox(font, 225, 20, Component.literal("Custom Alert Mode Name")));
        nameEditBox.setValue(currentModeName);
        nameEditBox.setHint(Component.literal("(unnamed)").setStyle(EditBox.DEFAULT_HINT_STYLE.withItalic(true)));
        
        Button exportButton = headerSubLayout.addChild(
            Button.builder(Component.literal("Export..."), button -> {
                // TODO: export
            }).size(75, 20).build()
        );
        if (isNewMode)
        {
            exportButton.active = false;
            exportButton.setTooltip(Tooltip.create(
                Component.literal("Save your new mode before exporting!").withStyle(ChatFormatting.YELLOW)
            ));
        }
        else exportButton.setTooltip(Tooltip.create(
            Component.literal("Unsaved changes will not be exported!").withStyle(ChatFormatting.GRAY)
        ));
        
        headerWidgetsLayout.addChild(headerSubLayout);
        addTitleHeader(headerWidgetsLayout);
        
        
        list = addScrollList(new ScathaProGuiList(minecraft, this, layout, 125));
        for (Alert alert : scathaPro.alertManager)
        {
            list.addEntry(new Entry(list, alert));
        }
        
        
        LinearLayout footerLayout = LinearLayout.horizontal().spacing(10);
        footerLayout.addChild(
            Button.builder(Component.literal("Save"),
                button -> {
                    // TODO: save
                    minecraft.setScreen(parentScreen);
                }
            ).width(150).build(), LayoutSettings::alignHorizontallyCenter
        );
        footerLayout.addChild(doneButton(Component.literal("Cancel"), 150), LayoutSettings::alignHorizontallyCenter);
        addLayoutFooter(footerLayout);
    }
    
    @Override
    public void onFilesDrop(@NonNull List<Path> list)
    {
        droppedFile = list.getFirst().toFile();
    }
    
    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        if (droppedFile != null)
        {
            Entry hoveredEntry = null;
            for (ScathaProGuiList.Entry entry : list.children())
            {
                if (entry.isMouseOver(mouseX, mouseY))
                {
                    if (entry instanceof Entry customizationEntry)
                    {
                        hoveredEntry = customizationEntry;
                    }
                    break;
                }
            }
            
            if (hoveredEntry != null)
            {
                hoveredEntry.fileDropped(droppedFile);
            }
            
            droppedFile = null;
        }
        
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void removed()
    {
        for (Alert alert : scathaPro.alertManager)
        {
            alert.stopSound();
        }
        
        super.removed();
    }
    
    private class Entry extends ScathaProGuiList.Entry
    {
        private final StringWidget fileLabel;
        
        public Entry(ScathaProGuiList list, Alert alert)
        {
            addCenteredChild(
                label(0, 5, Component.literal(alert.alertName).withStyle(ChatFormatting.YELLOW)),
                list.getRowWidth() / 2
            );
            
            // Titles
            
            AlertTitleTemplate titleTemplate = alert.titleTemplate;
            Tooltip titleFormattingTooltip = Tooltip.create(
                Component.literal("Supports formatting\ncodes using '&'").withStyle(ChatFormatting.GRAY)
            );
            
            addChild(label(0, 23, Component.literal("Title").withStyle(ChatFormatting.GRAY)));
            
            EditBox titleEditBox = new EditBox(font, list.getRowWidth() / 2 - 5, 20, Component.literal(alert.alertName + " Title"));
            String title = JsonUtil.getString(modeProperties, "titles." + alert.alertId + ".title");
            if (title != null) titleEditBox.setValue(title);
            if (titleTemplate.titleText != null) titleEditBox.setHint(Component.literal(titleTemplate.titleText));
            titleEditBox.setTooltip(titleFormattingTooltip);
            titleEditBox.addFormatter(new EditBoxFormatter(titleEditBox, titleTemplate.titleStyle));
            addPositionedChild(0, 33, titleEditBox);
            
            EditBox subtitleEditBox = new EditBox(font, list.getRowWidth() / 2 - 5, 20, Component.literal(alert.alertName + " Subtitle"));
            boolean canEditSubtitle = false;
            if (titleTemplate instanceof FullAlertTitleTemplate fullAlertTitleTemplate)
            {
                canEditSubtitle = true;
                
                String subtitle = JsonUtil.getString(modeProperties, "titles." + alert.alertId + ".subtitle");
                if (subtitle != null) subtitleEditBox.setValue(subtitle);
                if (fullAlertTitleTemplate.subtitleText != null)
                    subtitleEditBox.setHint(Component.literal(fullAlertTitleTemplate.subtitleText));
                subtitleEditBox.setTooltip(titleFormattingTooltip);
                subtitleEditBox.addFormatter(new EditBoxFormatter(subtitleEditBox, fullAlertTitleTemplate.subtitleStyle));
            }
            else if (titleTemplate instanceof DynamicAlertTitleTemplate)
            {
                subtitleEditBox.setHint(Component.literal("(automatic)").setStyle(EditBox.DEFAULT_HINT_STYLE.withItalic(true)));
            }
            
            addChild(label(list.getRowWidth() / 2 + 5, 23, Component.literal("Subtitle")
                .withStyle(canEditSubtitle ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY)));
            
            if (!canEditSubtitle)
            {
                subtitleEditBox.active = false;
                subtitleEditBox.setEditable(false);
            }
            addPositionedChild(list.getRowWidth() / 2 + 5, 33, subtitleEditBox);
            
            // Audio
            
            File alertAudioFile = manager.getAlertAudioFile(modeId, alert);
            boolean audioExists = alertAudioFile.exists();
            boolean canPlayAudio = manager.isSubModeActive(modeId) || !audioExists;
            
            addChild(label(0, 62, Component.literal("Audio").withStyle(ChatFormatting.GRAY)));
            
            addChild(label(0, 75, Component.empty()
                .append("Current: ")
                .append(audioExists ? "Custom audio" : "Default")
            ));
            addChild(fileLabel = label(0, 95 - font.lineHeight,
                Component.literal("Drag and drop audio files here")
                    .withStyle(ChatFormatting.GRAY)
            ));
            addChild(Button.builder(Component.literal("Default"), button -> {
                // TODO
            }).bounds(list.getRowWidth()/2 + 5, 75, 70, 20).build());
            addChild(Button.builder(Component.literal("Discard"), button -> {
                // TODO
            }).bounds(list.getRowWidth()/2 + 85, 75, 70, 20).build());
            
            Button playButton = Button.builder(Component.literal("Play Current Audio"), button -> {
                if (alert.isSoundPlaying()) alert.stopSound();
                else alert.playSound();
            }).bounds(0, 100, 150, 20).build();
            if (!canPlayAudio)
            {
                playButton.active = false;
                playButton.setTooltip(Tooltip.create(
                    Component.literal("Mode needs to be selected to play custom audio").withStyle(ChatFormatting.YELLOW)
                ));
            }
            addChild(playButton);
            
            Double audioVolume = JsonUtil.getDouble(modeProperties, "soundVolumes." + alert.alertId);
            addChild(new FloatSlider(
                160, 100, 150, 20,
                Component.literal("Volume"),
                0f, 1f, audioVolume != null ? audioVolume.floatValue() : 1f,
                value -> {
                    // TODO
                }
            ).setStepSize(0.01f).setValueComponentSupplier(FloatSlider.PERCENTAGE_COMPONENT_SUPPLIER_WITH_OFF));
        }
        
        public void fileDropped(File file)
        {
            fileLabel.setMessage(Component.literal("Selected: " + file.getName()));
        }
    }
    
    private record EditBoxFormatter(@NonNull EditBox editBox, @Nullable Style baseStyle) implements EditBox.TextFormatter
    {
        @Override
        public @Nullable FormattedCharSequence format(@NonNull String string, int i)
        {
            // TODO: bold text fucks up click and selection positions
            //  + this formatter is bad performance wise as it's run per render
            
            int stringIndex = editBox.getValue().indexOf(string);
            if (stringIndex < 0) return null;
            
            Component fullFormat = CustomAlertModeManager.formatFormattingCodes(editBox.getValue());
            
            Component formattedSegment = TextUtil.subString(fullFormat, stringIndex, stringIndex + string.length());
            if (baseStyle != null) formattedSegment = Component.empty().setStyle(baseStyle).append(formattedSegment);
            return formattedSegment.getVisualOrderText();
        }
    }
}
