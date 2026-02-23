package namelessju.scathapro.gui.menus.widgets;

import namelessju.scathapro.ScathaPro;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class CustomAlertModeDeleteButton extends Button
{
    protected CustomAlertModeDeleteButton(int width, int height, ScathaPro scathaPro, Screen screen, String subModeId)
    {
        super(0, 0, width, height, Component.literal("Delete"), button -> {
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
    protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int i, int j)
    {
        int k = this.getX() + i;
        int l = this.getX() + this.getWidth() - i;
        renderScrollingString(guiGraphics, font,
            isHoveredOrFocused() ? Component.empty().append(getMessage()).withStyle(ChatFormatting.RED) : getMessage(),
            k, this.getY(), l, this.getY() + this.getHeight(), j
        );
    }
}
