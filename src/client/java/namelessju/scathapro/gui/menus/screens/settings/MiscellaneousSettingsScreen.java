package namelessju.scathapro.gui.menus.screens.settings;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.widgets.lists.TwoColumnGuiList;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.compress.utils.Lists;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class MiscellaneousSettingsScreen extends ConfigScreen
{
    private CycleButton<Boolean> googlyEyesButton;
    
    public MiscellaneousSettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Miscellaneous Settings", parentScreen);
    }
    
    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();
        
        TwoColumnGuiList list = addScrollList();
        
        list.addDoubleColumn(booleanConfigButton(
            "Automatic Bestiary Parsing",
            config.miscellaneous.automaticStatsParsingEnabled,
            value -> Tooltip.create(
                Component.literal("Automatically reads kills and bestiary Magic Find from the worm bestiary menu")
                    .withStyle(ChatFormatting.GRAY)
            ),
            null
        ));
        
        list.addDoubleColumn(booleanConfigButton("Automatic Pet Drop Screenshot", config.miscellaneous.automaticPetDropScreenshotEnabled));
        
        list.addDoubleColumn(booleanConfigButton(
            "High Contrast Colors", config.accessibility.useHighContrastColors,
            value -> Tooltip.create(
                Component.literal("Turns gray overlay\nand title text to white").withStyle(ChatFormatting.GRAY)
            ),
            (button, value) -> scathaPro.mainOverlay.updateContrast()
        ));
        
        if (scathaPro.getProfileData().lastAprilFoolsJokeShownYear.getOr(-1) >= 0)
        {
            list.addDoubleColumn(booleanConfigButton(
                "April Fools Fake Pet Drop", config.miscellaneous.aprilFoolsFakeDropEnabled
            ));
        }
        
        // Unlockables
        OptionalCategoryBuilder unlockables = new OptionalCategoryBuilder("Unlockables", list);
        
        if (scathaPro.getProfileData().scappaModeUnlocked.get())
        {
            unlockables.addEntry(() -> {
                Button scappaModeButton = Button.builder(Component.empty(), button -> {
                        if (scathaPro.coreManager.scappaModeActiveTemp)
                        {
                            scathaPro.coreManager.scappaModeActiveTemp = false;
                        }
                        else config.unlockables.scappaModeEnabled.set(!config.unlockables.scappaModeEnabled.get());
                        
                        scathaPro.mainOverlay.updateScappaMode();
                        
                        updateScappaModeButtonText(button);
                        updateGooglyEyesButtonEnabled();
                        updateTitleWidget();
                        repositionElements();
                    })
                    .tooltip(Tooltip.create(TextUtil.getRainbowText("Scappa")))
                    .build();
                updateScappaModeButtonText(scappaModeButton);
                list.addDoubleColumn(scappaModeButton);
            });
        }
        
        if (scathaPro.getProfileData().overlayIconGooglyEyesUnlocked.get())
        {
            unlockables.addEntry(() -> {
                googlyEyesButton = booleanConfigButton(
                    "Overlay Icon Googly Eyes",
                    config.unlockables.overlayIconGooglyEyesEnabled,
                    null, (button, value) -> updateGooglyEyesButtonEnabled()
                );
                updateGooglyEyesButtonEnabled();
                list.addDoubleColumn(googlyEyesButton);
            });
        }
        
        unlockables.addAllToList();
        
        addDoneButtonFooter();
    }
    
    private void updateScappaModeButtonText(Button scappaModeButton)
    {
        MutableComponent component = Component.empty().append("Scappa Mode: ");
        if (scathaPro.coreManager.scappaModeActiveTemp)
        {
            component.append("Temporary");
        }
        else
        {
            boolean enabled = config.unlockables.scappaModeEnabled.get();
            component.append(enabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
        }
        scappaModeButton.setMessage(component);
    }
    
    private void updateGooglyEyesButtonEnabled()
    {
        if (googlyEyesButton == null) return;
        
        if (TimeUtil.isAprilFools())
        {
            googlyEyesButton.active = false;
            googlyEyesButton.setTooltip(Tooltip.create(
                Component.literal("Googly eyes are always\nenabled on april fools day")
                    .withStyle(ChatFormatting.YELLOW)
            ));
            return;
        }
        
        if (scathaPro.coreManager.isScappaModeActive())
        {
            googlyEyesButton.active = false;
            googlyEyesButton.setTooltip(Tooltip.create(
                Component.literal("Googly eyes don't apply\nto the Scappa mode icon")
                    .withStyle(ChatFormatting.YELLOW)
            ));
            return;
        }
        
        googlyEyesButton.active = true;
        googlyEyesButton.setTooltip(null);
    }
    
    private static class OptionalCategoryBuilder
    {
        private final TwoColumnGuiList list;
        private final String categoryText;
        private final List<Runnable> entries = Lists.newArrayList();
        
        public OptionalCategoryBuilder(String categoryText, TwoColumnGuiList list)
        {
            this.list = list;
            this.categoryText = categoryText;
        }
        
        public void addEntry(Runnable entry)
        {
            entries.add(entry);
        }
        
        public void addAllToList()
        {
            if (entries.isEmpty()) return;
            list.addTitle(Component.literal(categoryText));
            for (Runnable entry : entries) entry.run();
        }
    }
}
