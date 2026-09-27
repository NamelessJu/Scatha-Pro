package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >= 26.2 {
import net.minecraft.client.gui.Gui;

@Mixin(Gui.class)
//? } else {
/*import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
*///? }
public class SetScreenMixin
{
    @Shadow
    private @Nullable Screen screen;

    @Inject(
        method = "setScreen",
        at = @At("RETURN")
    )
    private void afterSetScreen(CallbackInfo ci)
    {
        if (screen == null) return;

        if (ScathaPro.instance().scathaDropsGamblingReelManager.shouldHideScreen(screen))
        {
            screen.onClose();
        }
    }
}