package namelessju.scathapro.gui.menus.screens.settings;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.gui.menus.framework.widgets.lists.TwoColumnGuiList;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.FloatSlider;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.IntegerSlider;
import namelessju.scathapro.miscellaneous.data.enums.DateFormat;
import namelessju.scathapro.miscellaneous.data.enums.MaxSlotMachineFakeScathaRarity;
import namelessju.scathapro.miscellaneous.data.enums.TimeFormat;
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

import java.time.LocalTime;
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
        String modName = scathaPro.getModDisplayName();

        list.addDoubleColumn(booleanConfigButton(
            "Show " + modName + " Menu Buttons",
            config.miscellaneous.showScathaProMenuButtons,
            _ -> Tooltip.create(Component.literal(
                "Whether the " + modName + " settings & achievement buttons should be added to the vanilla menus.\n"
                + "If disabled, the menus can still be accessed using /" + scathaPro.mainCommand.getCommandName() + "."
            ).withStyle(ChatFormatting.GRAY)),
            null
        ));


        CycleButton<Boolean> bestiaryParsingButton = booleanConfigButton(
            "Bestiary Parsing",
            config.miscellaneous.automaticStatsParsingEnabled,
            _ -> Tooltip.create(
                Component.literal("Automatically reads kills and bestiary Magic Find from the worm bestiary menu")
                    .withStyle(ChatFormatting.GRAY)
            ), null
        );

        if (scathaPro.getProfileData().lastAprilFoolsJokeShownYear.getOr(-1) >= 0)
        {
            list.addSingleColumn(bestiaryParsingButton);
            list.addSingleColumn(booleanConfigButton(
                "April Fools Fake Drop", config.miscellaneous.aprilFoolsFakeDropEnabled
            ));
        }
        else list.addDoubleColumn(bestiaryParsingButton);

        list.addTitle(Component.literal("Accessibilty"));

        list.addSingleColumn(enumCycleButton(
            TimeFormat.class, "Time Format", config.accessibility.timeFormat,
            value -> Tooltip.create(
                Component.literal("Example: " + value.format(LocalTime.now(), false))
                    .withStyle(ChatFormatting.GRAY)
            ), null
        ));
        list.addSingleColumn(enumCycleButton(
            DateFormat.class, "Date Format", config.accessibility.dateFormat,
            value -> Tooltip.create(
                Component.literal("Example: " + value.format(TimeUtil.today())).withStyle(ChatFormatting.GRAY)
            ), null
        ));

        list.addDoubleColumn(booleanConfigButton(
            "High Contrast Colors", config.accessibility.useHighContrastColors,
            _ -> Tooltip.create(
                Component.literal("Turns gray overlay\nand title text to white").withStyle(ChatFormatting.GRAY)
            ), null
        ));

        list.addTitle(Component.literal("Scatha Drops Slot Machine"));
        list.addSingleColumn(booleanConfigButton(
            "Slot Machine Enabled", config.miscellaneous.dropsSlotMachineEnabled,
            _ -> Tooltip.create(
                Component.empty().withStyle(ChatFormatting.GRAY)
                    .append("""
                        Shows a slot-machine-esque overlay when killing a Scatha that rolls for drops.
                        (Selects the actual item you dropped!)

                        Note: I discourage from gambling with real money!""")
            ), null
        ));
        list.addSingleColumn(new IntegerSlider(
            0, 0, 150, 20,
            Component.literal("Animation Duration"),
            2, 10, Math.round(config.miscellaneous.dropsSlotMachineAnimationTicks.get() / 20f),
            value -> config.miscellaneous.dropsSlotMachineAnimationTicks.set(value * 20)
        ).setValueComponentSupplier(IntegerSlider.SECONDS_COMPONENT_SUPPLIER));
        list.addSingleColumn(floatConfigSlider(
            "Scale", 0.5f, 1.5f, config.miscellaneous.dropsSlotMachineScaleMultiplier, null
        ).setStepSize(0.01f).setValueComponentSupplier(FloatSlider.PERCENTAGE_COMPONENT_SUPPLIER));
        list.addSingleColumn(enumCycleButton(
            MaxSlotMachineFakeScathaRarity.class, "Max. Fake Pet Rarity",
            config.miscellaneous.dropsSlotMachineMaxFakeScathaRarity,
            _ -> Tooltip.create(Component.literal(
                "The highest rarity of Scatha pets that may be used to fill the non-winning slots that are scrolled past"
            ).withStyle(ChatFormatting.GRAY)), null
        ));
        list.addSingleColumn(booleanConfigButton(
            "Hide Pet Rarity", config.miscellaneous.dropsSlotMachineHidePetRarity,
            _ -> Tooltip.create(Component.literal(
                "Hides the rarities of any pet on the wheel (including the winning slot!)"
            ).withStyle(ChatFormatting.GRAY)), null
        ));
        list.addSingleColumn(booleanConfigButton(
            "Apply Random Offset", config.miscellaneous.dropsSlotMachineApplyRandomOffset,
            _ -> Tooltip.create(
                Component.literal("""
                        When enabled, the selector bar will end up at a random position on the winning slot.
                        When disabled, it will always stop exactly in the middle of the slot."""
                    ).withStyle(ChatFormatting.GRAY)
            ), null
        ));

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
                list.addDoubleColumn(googlyEyesButton = booleanConfigButton(
                    "Overlay Icon Googly Eyes", config.unlockables.overlayIconGooglyEyesEnabled,
                    null, (_, _) -> updateGooglyEyesButtonEnabled()
                ));
                updateGooglyEyesButtonEnabled();
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
                Component.literal("Googly eyes are always\nenabled on april fools day").withStyle(ChatFormatting.YELLOW)
            ));
            return;
        }

        if (scathaPro.coreManager.isScappaModeActive())
        {
            googlyEyesButton.active = false;
            googlyEyesButton.setTooltip(Tooltip.create(
                Component.literal("Googly eyes don't apply\nto the Scappa mode icon").withStyle(ChatFormatting.YELLOW)
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