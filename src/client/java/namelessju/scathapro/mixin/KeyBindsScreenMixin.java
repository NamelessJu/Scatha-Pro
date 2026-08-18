package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.miscellaneous.data.mixindata.IKeyBindsListExtraData;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(KeyBindsScreen.class)
public abstract class KeyBindsScreenMixin implements IKeyBindsListExtraData
{
    @Shadow
    private KeyBindsList keyBindsList;
    @Unique
    private boolean isScathaProOnly = false;

    @Override
    public void scathapro$setScathaProOnly()
    {
        isScathaProOnly = true;
    }

    @Override
    public boolean scathapro$isScathaProOnly()
    {
        return isScathaProOnly;
    }

    @ModifyArg(
        method = "addFooter",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;",
            ordinal = 0
        ),
        index = 1
    )
    private Button.OnPress replaceResetButtonOnPress(Button.OnPress onPress)
    {
        if (!this.scathapro$isScathaProOnly())
            return onPress;

        return _ -> {
            for (KeyMapping key : ScathaPro.getInstance().inputManager.getKeyMappings())
            {
                key.setKey(key.getDefaultKey());
            }

            keyBindsList.resetMappingAndUpdateButtons();
        };
    }
}