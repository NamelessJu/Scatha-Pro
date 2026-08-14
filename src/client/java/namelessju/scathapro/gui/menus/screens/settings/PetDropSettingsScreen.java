package namelessju.scathapro.gui.menus.screens.settings;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.IntegerSlider;
import namelessju.scathapro.miscellaneous.data.enums.Rarity;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

public class PetDropSettingsScreen extends ConfigScreen
{
    public PetDropSettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Scatha Pet Drop Settings", parentScreen);
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();

        GridBuilder gridBuilder = new GridBuilder();
        gridBuilder.addSingleCell(booleanConfigButton("Scatha Pet Item Popup", config.petDrop.itemPopupEnabled,
            _ -> Tooltip.create(
                Component.literal("Pops up the Scatha pet item when you drop one, similar to Totems of Undying")
                    .withColor(TextColor.GRAY)
            ), null
        ));
        gridBuilder.addSingleCell(new IntegerSlider(
            0, 0, 150, 20,
            Component.literal("Popup Animation Length"),
            1, 10, Math.round(config.petDrop.itemPopupAnimationTicks.get() / 20f),
            value -> config.petDrop.itemPopupAnimationTicks.set(value * 20)
        ).setValueComponentSupplier(IntegerSlider.SECONDS_COMPONENT_SUPPLIER));
        gridBuilder.addSingleCell(booleanConfigButton("Slower Popup Rotation", config.petDrop.itemPopupUseAltRotAnimCurve,
            _ -> Tooltip.create(
                Component.literal("Rotates the item more evenly instead of rapidly speeding up and slowing down at the start and end")
                    .withColor(TextColor.GRAY)
            ), null
        ));

        Button popupPreviewButton;
        gridBuilder.addSingleCell(popupPreviewButton = Button.builder(Component.literal("Play Preview"),
            _ -> scathaPro.itemPopupRenderer.popup(
                    Constants.generateScathaPetItem(Rarity.LEGENDARY),
                    Mth.clamp(scathaPro.config.petDrop.itemPopupAnimationTicks.get(), 1, 200),
                    scathaPro.config.petDrop.itemPopupUseAltRotAnimCurve.get(),
                    true
                )
            ).build());
        if (!BuiltInRegistries.ITEM.wrapAsHolder(Items.PLAYER_HEAD).areComponentsBound())
        {
            popupPreviewButton.active = false;
            popupPreviewButton.setTooltip(Tooltip.create(
                Component.literal("Items aren't loaded yet,\ncannot render preview").withColor(TextColor.YELLOW)
            ));
        }

        gridBuilder.addGap();
        gridBuilder.addSingleCell(booleanConfigButton("Firework Explosion", config.petDrop.fireworkEnabled,
            _ -> Tooltip.create(
                Component.literal("Explodes a firework in front of you, colored to match the dropped rarity")
                    .withColor(TextColor.GRAY)
            ), null
        ));
        gridBuilder.addSingleCell(booleanConfigButton("Automatic Screenshot", config.miscellaneous.automaticPetDropScreenshotEnabled));
        gridBuilder.addFullWidth(subScreenButton("Drop Message Extension...", DropMessageExtensionSettingsScreen::new));
        gridBuilder.addToContent(layout);

        addDoneButtonFooter();
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    public void removed()
    {
        super.removed();

        scathaPro.itemPopupRenderer.clear();
    }
}