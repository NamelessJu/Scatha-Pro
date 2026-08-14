package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public abstract class BowItemMixin
{
    @Inject(
        method = "releaseUsing",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/stats/Stat;)V",
            ordinal = 0
        )
    )
    private void onRelease(ItemStack itemStack, Level level, LivingEntity entity, int remainingTime, CallbackInfoReturnable<Boolean> cir)
    {
        if (!level.isClientSide()) return;
        ScathaPro.getInstance().coreManager.projectileWormHitDetector.drawnBowReleased(itemStack);
    }
}