package namelessju.scathapro.gui.menus.screens.settings;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.miscellaneous.data.enums.DropMessageRarityMode;
import namelessju.scathapro.miscellaneous.data.enums.DropMessageStatMode;
import namelessju.scathapro.miscellaneous.data.enums.Rarity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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
                .withStyle(ChatFormatting.GRAY),
            font
        ).setCentered(true));
        
        gridBuilder.addGap();
        
        gridBuilder.addFullWidth(nullableEnumCycleButton(
            DropMessageRarityMode.class, "Add Scatha Rarity Text",
            config.miscellaneous.dropMessageRarityMode, null, null,
            (button, value) -> {
                updatePreview();
                updateRaritySettingButtons();
            }
        ));
        coloredRarityButton = gridBuilder.addSingleCell(booleanConfigButton(
            "Colored Rarity", config.miscellaneous.dropMessageRarityColored,
            null, (button, value) -> updatePreview()
        ));
        emphasizedRarityButton = gridBuilder.addSingleCell(booleanConfigButton(
            "Emphasized Rarity", config.miscellaneous.dropMessageRarityUppercase,
            null, (button, value) -> updatePreview()
        ));
        
        gridBuilder.addGap();
        
        gridBuilder.addSingleCell(nullableEnumCycleButton(
            DropMessageStatMode.class, "Add Magic Find",
            config.miscellaneous.dropMessageMagicFindMode, null, null,
            (button, value) -> updatePreview()
        ));
        gridBuilder.addSingleCell(nullableEnumCycleButton(
            DropMessageStatMode.class, "Add Pet Luck",
            config.miscellaneous.dropMessagePetLuckMode, null, null,
            (button, value) -> updatePreview()
        ));
        gridBuilder.addFullWidth(nullableEnumCycleButton(
            DropMessageStatMode.class, "Add Effective Magic Find",
            config.miscellaneous.dropMessageEmfMode, null,
            value -> Tooltip.create(
                Component.literal("EMF (\"Effective Magic Find\"):\nMagic Find + Pet Luck combined")
                    .withStyle(ChatFormatting.GRAY)
            ),
            (button, value) -> updatePreview()
        ));
        
        
        gridBuilder.addToContent(layout);
        
        addDoneButtonFooter();
        
        
        updatePreview();
        updateRaritySettingButtons();
    }
    
    private void updatePreview()
    {
        Component message = Constants.getPetDropMessage(Rarity.EPIC);
        String unformattedText = StringDecomposer.getPlainText(message);
        previewWidget.setMessage(scathaPro.chatManager.extendPetDropMessage(message, unformattedText, false));
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
                Component.literal("Applies only when the rarity text is added")
                    .withStyle(ChatFormatting.YELLOW)
            );
            
            coloredRarityButton.active = false;
            coloredRarityButton.setTooltip(tooltip);
            
            emphasizedRarityButton.active = false;
            emphasizedRarityButton.setTooltip(tooltip);
        }
    }
}
