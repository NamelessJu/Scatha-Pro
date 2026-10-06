package namelessju.scathapro.gui.overlay;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.framework.DataEvent;
import namelessju.scathapro.files.framework.JsonFile;
import namelessju.scathapro.gui.menus.screens.settings.playerrotation.PlayerRotationSettingsScreen;
import namelessju.scathapro.gui.overlay.elements.OverlayContainer;
import namelessju.scathapro.gui.overlay.elements.OverlayElement;
import namelessju.scathapro.gui.overlay.elements.OverlayImage;
import namelessju.scathapro.gui.overlay.elements.OverlayText;
import namelessju.scathapro.miscellaneous.data.Texture;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;

public class CrosshairOverlay
{
    private final ScathaPro scathaPro;

    private OverlayContainer rotationAnglesOverlay;
    private OverlayText yawText;
    private OverlayText pitchText;
    private OverlayImage rotationLockImage;

    public CrosshairOverlay(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }

    public void init()
    {
        Font font = scathaPro.minecraft.font;

        rotationAnglesOverlay = new OverlayContainer(0, 0, 0.75f);
        rotationAnglesOverlay.add(yawText = new OverlayText(font, Util.Color.WHITE, 0, 0, 1f));
        rotationAnglesOverlay.add(pitchText = new OverlayText(font, Util.Color.WHITE, 0, 0, 1f));
        pitchText.setAlignment(OverlayElement.Alignment.CENTER);

        rotationLockImage = new OverlayImage(Texture.scathaPro("lock.png", 16, 16), 0, 0, 1f);

        scathaPro.config.miscellaneous.alternativeCrosshairLayoutEnabled.onValueChanged(_ -> updateLayout());
        DataEvent.Listener<JsonFile.PrimitiveValueWithDefault<Integer>> lockIconOffsetChangeListener = _ -> updateLayout();
        scathaPro.config.miscellaneous.rotationLockIconOffsetX.onValueChanged(lockIconOffsetChangeListener);
        scathaPro.config.miscellaneous.rotationLockIconOffsetY.onValueChanged(lockIconOffsetChangeListener);
        updateLayout();
    }

    private void updateLayout()
    {
        Font font = scathaPro.minecraft.font;

        int lockOffsetX = scathaPro.config.miscellaneous.rotationLockIconOffsetX.get();
        int lockOffsetY = scathaPro.config.miscellaneous.rotationLockIconOffsetY.get();

        if (scathaPro.config.miscellaneous.alternativeCrosshairLayoutEnabled.get())
        {
            yawText.setPosition(11, -Mth.ceil(font.lineHeight / 2f));
            pitchText.setPosition(-1, -10 - font.lineHeight);
            rotationLockImage.setPosition(-22 + lockOffsetX, -8 - lockOffsetY);
        }
        else
        {
            yawText.setPosition(10, -Mth.ceil(font.lineHeight / 2f));
            pitchText.setPosition(-1, 10);
            rotationLockImage.setPosition(-8 + lockOffsetX, -21 - lockOffsetY);
        }
    }

    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker)
    {
        if (scathaPro.minecraft.gui.screen() instanceof PlayerRotationSettingsScreen) return;
        extractRenderState(guiGraphics, deltaTracker, false);
    }

    public void renderPreview(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker)
    {
        extractRenderState(guiGraphics, deltaTracker, true);
    }

    private void extractRenderState(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, boolean forceShowLock)
    {
        guiGraphics.nextStratum();
        guiGraphics.pose().pushMatrix();
        //noinspection IntegerDivisionInFloatingPointContext
        guiGraphics.pose().translate(guiGraphics.guiWidth() / 2, guiGraphics.guiHeight() / 2);

        // Rotation Angles
        if (scathaPro.config.miscellaneous.rotationAnglesEnabled.get())
        {
            updateRotationAngles();
            rotationAnglesOverlay.extractRenderStateIfVisible(guiGraphics, deltaTracker);
        }

        // Rotation Lock
        if (forceShowLock || scathaPro.inputManager.isCameraRotationLocked())
        {
            rotationLockImage.extractRenderStateIfVisible(guiGraphics, deltaTracker);
        }

        guiGraphics.pose().popMatrix();
    }

    private void updateRotationAngles()
    {
        int decimalDigits = scathaPro.config.miscellaneous.rotationAnglesDecimalPlaces.get();
        TextColor contrastableGray = TextUtil.contrastableGray(scathaPro);

        LocalPlayer player = scathaPro.minecraft.player;
        float yRot = player != null ? player.getYRot() : 0f;
        float xRot = player != null ? player.getXRot() : 0f;

        float yaw = yRot % 360f;
        if (yaw < 0f) yaw += 360f;
        String yawString = TextUtil.numberToString(yaw, decimalDigits, true);
        if (scathaPro.config.miscellaneous.rotationAnglesMinimalYawEnabled.get())
        {
            int dotIndex = yawString.indexOf('.');
            if (dotIndex >= 0) yawString = yawString.substring(dotIndex - 1);
        }
        yawText.setText(Component.literal(yawString).withColor(contrastableGray));

        if (scathaPro.config.miscellaneous.rotationAnglesYawOnly.get())
        {
            pitchText.setVisible(false);
        }
        else
        {
            pitchText.setVisible(true);
            pitchText.setText(Component.literal(
                TextUtil.numberToString(xRot, decimalDigits, true)
            ).withColor(contrastableGray));
        }
    }
}