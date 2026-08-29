package namelessju.scathapro.managers;

import com.mojang.blaze3d.platform.InputConstants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.miscellaneous.data.enums.SecondaryWormStatsType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.apache.commons.compress.utils.Lists;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class InputManager
{
    private final KeyMapping.Category mainCategory = KeyMapping.Category.register(ScathaPro.getIdentifier("main"));
    private final KeyMapping.Category playerRotationCategory = KeyMapping.Category.register(ScathaPro.getIdentifier("player_rotation"));
    private final KeyMapping.Category miscellaneousCategory = KeyMapping.Category.register(ScathaPro.getIdentifier("miscellaneous"));


    private final List<KeyMapping> keyMappings = Lists.newArrayList();

    private final KeyMapping toggleOverlayKeyMapping = registerKeyMapping(
        "toggleOverlay", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, mainCategory
    );
    private final KeyMapping toggleOverlayVisibilityKeyMapping = registerKeyMapping(
        "toggleOverlayVisibility", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, mainCategory
    );
    private final KeyMapping cycleSelectedWormStatsTypeKeyMapping = registerKeyMapping(
        "cycleSelectedWormStatsType", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, mainCategory
    );

    private final KeyMapping lockRotationKeyMapping = registerKeyMapping(
        "lockRotation", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, playerRotationCategory
    );
    private final KeyMapping toggleRotationAnglesKeyMapping = registerKeyMapping(
        "toggleRotationAngles", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, playerRotationCategory
    );
    private final KeyMapping alternativeSensitivityKeyMapping = registerKeyMapping(
        "alternativeSensitivity", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, playerRotationCategory
    );

    private final KeyMapping cancelScathaDropsSlotMachineKeyMapping = registerKeyMapping(
        "cancelScathaDropsSlotMachine", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, miscellaneousCategory
    );


    private final ScathaPro scathaPro;

    private boolean isRotationLocked = false;
    private int cancelSlotMachineDoubleClickTickTimer = 0;

    public InputManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }

    public List<KeyMapping> getKeyMappings()
    {
        return Lists.newArrayList(keyMappings.iterator());
    }

    public void tick()
    {
        while (lockRotationKeyMapping.consumeClick())
        {
            isRotationLocked = !isRotationLocked;
        }

        while (toggleOverlayKeyMapping.consumeClick())
        {
            scathaPro.mainOverlay.toggleEnabled();
        }

        while (toggleOverlayVisibilityKeyMapping.consumeClick())
        {
            scathaPro.mainOverlay.toggleShown();
        }

        while (toggleOverlayKeyMapping.consumeClick())
        {
            if (scathaPro.mainOverlay.isVisible())
            {
                scathaPro.mainOverlay.toggleEnabled();
            }
        }

        while (toggleRotationAnglesKeyMapping.consumeClick())
        {
            boolean enabled = scathaPro.config.miscellaneous.rotationAnglesEnabled.get();
            scathaPro.config.miscellaneous.rotationAnglesEnabled.set(!enabled);
            scathaPro.config.save();
        }

        while (cycleSelectedWormStatsTypeKeyMapping.consumeClick())
        {
            SecondaryWormStatsType statsType = scathaPro.config.overlay.statsType.get();

            int statsTypeIndex = statsType.ordinal();
            statsTypeIndex = (statsTypeIndex + 1) % SecondaryWormStatsType.values().length;

            statsType = SecondaryWormStatsType.values()[statsTypeIndex];

            scathaPro.config.overlay.statsType.set(statsType);
            scathaPro.config.save();

            scathaPro.chatManager.sendChatMessage(
                Component.literal("Changed overlay worm stats to: Per " + statsType).withStyle(ChatFormatting.GRAY)
            );
        }

        if (cancelSlotMachineDoubleClickTickTimer > 0) cancelSlotMachineDoubleClickTickTimer--;
        while (cancelScathaDropsSlotMachineKeyMapping.consumeClick())
        {
            if (cancelSlotMachineDoubleClickTickTimer > 0)
            {
                scathaPro.scathaDropsSlotMachineManager.reset();
                cancelSlotMachineDoubleClickTickTimer = 0;
            }
            else cancelSlotMachineDoubleClickTickTimer = 20;
        }
    }

    public void disableCameraRotationLock()
    {
        isRotationLocked = false;
    }

    public boolean isCameraRotationLocked()
    {
        return isRotationLocked;
    }

    public boolean isAlternativeSensitivityEnabled()
    {
        return alternativeSensitivityKeyMapping.isDown();
    }

    @SuppressWarnings("SameParameterValue")
    private KeyMapping registerKeyMapping(String keyID, InputConstants.Type type, int defaultKey, KeyMapping.Category category)
    {
        KeyMapping mapping = new KeyMapping(
            "key." + ScathaPro.MOD_ID + "." + keyID,
            type,
            defaultKey,
            category
        );
        keyMappings.add(mapping);
        return mapping;
    }
}