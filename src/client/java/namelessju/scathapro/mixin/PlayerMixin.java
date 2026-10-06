package namelessju.scathapro.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.managers.detectors.entities.detected.DetectedWorm;
import namelessju.scathapro.miscellaneous.data.mixindata.IWormArmorStandData;
import namelessju.scathapro.util.SkyBlockItemUtil;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin
{
    @ModifyExpressionValue(
        method = "attack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getWeaponItem()Lnet/minecraft/world/item/ItemStack;"
        )
    )
    private ItemStack onAttackEntity(ItemStack itemStack, Entity entity)
    {
        if (((Player) (Object) this) instanceof LocalPlayer player)
        {
            ScathaProEvents.attackEntityEvent.trigger(
                new ScathaProEvents.AttackEntityEventData(ScathaPro.instance(), player, entity, itemStack)
            );
        }
        return itemStack;
    }

    @Inject(
        method = "interactOn",
        at = @At("HEAD")
    )
    private void onInteractOn(Entity entity, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir)
    {
        if (!(entity instanceof IWormArmorStandData wormData)) return;
        DetectedWorm worm = wormData.scathaPro$getWorm();
        if (worm == null || !worm.isScatha) return;

        Player self = (Player) (Object) this;
        ItemStack usedItem = self.getItemInHand(hand);
        if (Constants.ItemID.nothingInABottle.equals(SkyBlockItemUtil.getItemID(usedItem)))
        {
            ScathaPro.instance().coreManager.lastNothingInABottleUseTime = TimeUtil.getEpochMilliseconds();
        }
    }
}