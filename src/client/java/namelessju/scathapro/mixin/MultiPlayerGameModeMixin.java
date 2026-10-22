package namelessju.scathapro.mixin;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.managers.ChatManager;
import namelessju.scathapro.sounds.instances.ScathaProSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin
{
    @Shadow
    @Final
    private Minecraft minecraft;
    @Unique private BlockPos bedrockFloorAxisCheckStart = null;

    @Inject(
        method = "useItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;ensureHasSentCarriedItem()V"
        )
    )
    private void onUseItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir)
    {
        if (player instanceof LocalPlayer localPlayer)
        {
            ScathaProEvents.useItemEvent.trigger(new ScathaProEvents.UseItemEventData(
                ScathaPro.instance(), localPlayer, localPlayer.getItemInHand(hand)
            ));
        }
    }

    @Inject(
        method = "startDestroyBlock",
        at = @At("RETURN")
    )
    private void afterStartDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir)
    {
        if (!Boolean.TRUE.equals(cir.getReturnValue())) return;

        ScathaPro scathaPro = ScathaPro.instance();
        if (!scathaPro.hypixelContextManager.isInCrystalHollows()) return;

        if (pos.getY() >= 34)
        {
            bedrockFloorAxisCheckStart = null;
            return;
        }

        if (bedrockFloorAxisCheckStart == null)
        {
            bedrockFloorAxisCheckStart = pos;
        }
        else
        {
            int x = pos.getX();
            int distanceX = x - bedrockFloorAxisCheckStart.getX();
            int distanceZ = pos.getZ() - bedrockFloorAxisCheckStart.getZ();
            if (distanceX != 0 && distanceZ != 0)
            {
                bedrockFloorAxisCheckStart = pos;
            }
            else
            {
                if (Constants.crystalHollowsBoundsMin + 3 < x && x < Constants.crystalHollowsBoundsMax - 3
                    && Math.abs(distanceZ) >= 100)
                {
                    bedrockFloorAxisCheckStart = pos;

                    int shownAmount = scathaPro.getProfileData().wrongAxisNotificationsShown.get();
                    if (shownAmount < 3)
                    {
                        shownAmount ++;
                        scathaPro.getProfileData().wrongAxisNotificationsShown.set(shownAmount);
                        scathaPro.persistentData.save();

                        minecraft.getSoundManager().play(new ScathaProSound(scathaPro,
                            SoundEvents.NOTE_BLOCK_BIT.value().location(), 1f, 0.8f
                        ));

                        scathaPro.chatManager.sendChatDivider();
                        scathaPro.chatManager.sendChatMessage(Component.literal(
                            "Please consider mining along the X axis!"
                        ).withStyle(ChatManager.HIGHLIGHT_STYLE));
                        scathaPro.chatManager.sendChatMessage(Component.literal(
                            "As a Scatha farming community we have a consensus of mining towards West (or East) to minimize the amount of orthogonal tunnels."
                        ), false);
                        scathaPro.chatManager.sendChatMessage(Component.literal(
                            shownAmount < 3
                                ? "This notification will stop sending after it's been shown 3 times: " + shownAmount + "/3"
                                : "This notification will now not show again on this profile! (3/3 times shown)"
                        ).withColor(TextColor.GRAY), false);
                        scathaPro.chatManager.sendChatDivider();
                    }
                }
            }
        }
    }
}