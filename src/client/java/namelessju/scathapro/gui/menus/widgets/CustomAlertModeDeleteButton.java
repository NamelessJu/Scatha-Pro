package namelessju.scathapro.gui.menus.widgets;

import namelessju.scathapro.ScathaPro;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class CustomAlertModeDeleteButton extends Button
{
    protected CustomAlertModeDeleteButton(int width, int height, ScathaPro scathaPro, Screen screen, String subModeId)
    {
        super(0, 0, width, height, Component.literal("Delete"), _ -> {
            Component modeName = scathaPro.customAlertModeManager.getSubModeDisplayName(subModeId);
            ConfirmScreen confirmGui = new ConfirmScreen(
                result -> {
                    if (result) scathaPro.customAlertModeManager.deleteSubMode(subModeId);
                    scathaPro.minecraft.setScreen(screen);
                },
                Component.empty().append("Do you really want to delete \"").append(modeName).append("\"?"),
                Component.literal("This cannot be undone!")
            );
            scathaPro.minecraft.setScreen(confirmGui);
            confirmGui.setDelay(40);
        }, Button.DEFAULT_NARRATION);
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphics, int i, int j, float f)
    {
        this.extractDefaultSprite(guiGraphics);
        this.extractScrollingStringOverContents(
            guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE),
            isHoveredOrFocused() ? Component.empty().append(getMessage()).withStyle(ChatFormatting.RED) : getMessage(),
            2
        );
    }
}