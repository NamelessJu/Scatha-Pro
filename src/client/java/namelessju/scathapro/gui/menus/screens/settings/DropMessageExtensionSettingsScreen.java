package namelessju.scathapro.gui.menus.screens.settings;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.miscellaneous.data.enums.DropMessageRarityMode;
import namelessju.scathapro.miscellaneous.data.enums.DropMessageStatMode;
import namelessju.scathapro.miscellaneous.data.enums.Rarity;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.StringDecomposer;
import org.jspecify.annotations.NonNull;

public class DropMessageExtensionSettingsScreen extends ConfigScreen
{
    private StringWidget previewWidget;
    private CycleButton<Boolean> coloredRarityButton;
    private CycleButton<Boolean> emphasizedRarityButton;

    public DropMessageExtensionSettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Drop Message Extension Settings", parentScreen);
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();

        GridBuilder gridBuilder = new GridBuilder();


        previewWidget = gridBuilder.addFullWidth(new StringWidget(Component.empty(), font)
            .setMaxWidth(GridBuilder.WIDTH, StringWidget.TextOverflow.SCROLLING));
        gridBuilder.addFullWidth(new MultiLineTextWidget(
            Component.literal("Use \"/" + scathaPro.mainCommand.getCommandName() + " profileStats\" to\nupdate the used stat values")
                .withColor(TextColor.GRAY),
            font
        ).setCentered(true));

        gridBuilder.addGap();

        gridBuilder.addFullWidth(nullableEnumCycleButton(
            DropMessageRarityMode.class, "Add Scatha Rarity Text",
            config.miscellaneous.dropMessageRarityMode, null, null,
            (_, _) -> {
                updatePreview();
                updateRaritySettingButtons();
            }
        ));
        coloredRarityButton = gridBuilder.addSingleCell(booleanConfigButton(
            "Colored Rarity", config.miscellaneous.dropMessageRarityColored,
            null, (_, _) -> updatePreview()
        ));
        emphasizedRarityButton = gridBuilder.addSingleCell(booleanConfigButton(
            "Emphasized Rarity", config.miscellaneous.dropMessageRarityUppercase,
            null, (_, _) -> updatePreview()
        ));

        gridBuilder.addGap();

        gridBuilder.addSingleCell(nullableEnumCycleButton(
            DropMessageStatMode.class, "Add Magic Find",
            config.miscellaneous.dropMessageMagicFindMode, null, null,
            (_, _) -> updatePreview()
        ));
        gridBuilder.addSingleCell(nullableEnumCycleButton(
            DropMessageStatMode.class, "Add Pet Luck",
            config.miscellaneous.dropMessagePetLuckMode, null, null,
            (_, _) -> updatePreview()
        ));
        gridBuilder.addFullWidth(nullableEnumCycleButton(
            DropMessageStatMode.class, "Add Effective Magic Find",
            config.miscellaneous.dropMessageEmfMode, null,
            _ -> Tooltip.create(
                Component.literal("EMF (\"Effective Magic Find\"):\nMagic Find + Pet Luck combined")
                    .withColor(TextColor.GRAY)
            ),
            (_, _) -> updatePreview()
        ));


        gridBuilder.addToContent(layout);

        addDoneButtonFooter();


        updatePreview();
        updateRaritySettingButtons();
    }

    private void updatePreview()
    {
        Component petDropMessage = Constants.generatePetDropMessage(Rarity.EPIC);
        previewWidget.setMessage(
            scathaPro.chatManager.extendPetDropMessage(
                petDropMessage, StringDecomposer.getPlainText(petDropMessage),
                false, false
            )
        );
        getLayout().arrangeElements();
    }

    private void updateRaritySettingButtons()
    {
        if (config.miscellaneous.dropMessageRarityMode.get() != null)
        {
            coloredRarityButton.active = true;
            coloredRarityButton.setTooltip(null);

            emphasizedRarityButton.active = true;
            emphasizedRarityButton.setTooltip(null);
        }
        else
        {
            Tooltip tooltip = Tooltip.create(
                Component.literal("Applies only when the rarity text is added").withColor(TextColor.YELLOW)
            );

            coloredRarityButton.active = false;
            coloredRarityButton.setTooltip(tooltip);

            emphasizedRarityButton.active = false;
            emphasizedRarityButton.setTooltip(tooltip);
        }
    }
}