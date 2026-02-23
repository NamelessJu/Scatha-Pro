package namelessju.scathapro.gui.overlay;

import com.mojang.blaze3d.platform.Window;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.overlay.elements.GuiContainer;
import namelessju.scathapro.gui.overlay.elements.GuiElement;
import namelessju.scathapro.gui.overlay.elements.GuiImage;
import namelessju.scathapro.gui.overlay.elements.GuiText;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class CrosshairOverlay
{
    private final ScathaPro scathaPro;
    
    private GuiContainer rotationAnglesOverlay;
    private GuiText yawText;
    private GuiText pitchText;
    private GuiImage rotationLockOverlay;
    
    public CrosshairOverlay(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }
    
    public void init()
    {
        Font font = scathaPro.minecraft.font;
        
        rotationAnglesOverlay = new GuiContainer(0, 0, 0.75f);
        rotationAnglesOverlay.add(yawText = new GuiText(font, Util.Color.WHITE, 0, 0, 1f));
        rotationAnglesOverlay.add(pitchText = new GuiText(font, Util.Color.WHITE, 0, 0, 1f));
        pitchText.setAlignment(GuiElement.Alignment.CENTER);
        
        rotationLockOverlay = new GuiImage("lock.png", 16, 16, 0, 0, 1f);
        
        updateLayout();
    }
    
    public void updateLayout()
    {
        Font font = scathaPro.minecraft.font;
        
        if (scathaPro.config.miscellaneous.alternativeCrosshairLayoutEnabled.get())
        {
            yawText.setPosition(11, -Mth.ceil(font.lineHeight / 2f));
            pitchText.setPosition(-1, -10 - font.lineHeight);
            rotationLockOverlay.setPosition(-22, -8);
        }
        else
        {
            yawText.setPosition(10, -Mth.ceil(font.lineHeight / 2f));
            pitchText.setPosition(-1, 10);
            rotationLockOverlay.setPosition(-8, -21);
        }
    }
    
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        LocalPlayer player = scathaPro.minecraft.player;
        if (player == null) return;
        
        Window window = scathaPro.minecraft.getWindow();
        
        guiGraphics.nextStratum();
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(
            (int) (window.getGuiScaledWidth() / 2f),
            (int) (window.getGuiScaledHeight() / 2f)
        );
        
        // Rotation Angles
        if (scathaPro.config.miscellaneous.rotationAnglesEnabled.get())
        {
            updateRotationAngles(player);
            rotationAnglesOverlay.render(guiGraphics, deltaTracker);
        }
        
        // Rotation Lock
        if (scathaPro.inputManager.isCameraRotationLocked())
        {
            rotationLockOverlay.render(guiGraphics, deltaTracker);
        }
        
        guiGraphics.pose().popMatrix();
    }
    
    private void updateRotationAngles(LocalPlayer player)
    {
        int decimalDigits = scathaPro.config.miscellaneous.rotationAnglesDecimalPlaces.get();
        ChatFormatting contrastableGray = TextUtil.contrastableGray(scathaPro);
        
        float yaw = player.getYRot() % 360f;
        if (yaw < 0f) yaw += 360f;
        String yawString = TextUtil.numberToString(yaw, decimalDigits, true);
        if (scathaPro.config.miscellaneous.rotationAnglesMinimalYawEnabled.get())
        {
            int dotIndex = yawString.indexOf('.');
            if (dotIndex >= 0) yawString = yawString.substring(dotIndex - 1);
        }
        yawText.setText(Component.literal(yawString).withStyle(contrastableGray));
        
        if (scathaPro.config.miscellaneous.rotationAnglesYawOnly.get())
        {
            pitchText.setVisible(false);
        }
        else
        {
            pitchText.setVisible(true);
            pitchText.setText(Component.literal(
                TextUtil.numberToString(player.getXRot(), decimalDigits, true)
            ).withStyle(contrastableGray));
        }
    }
}
