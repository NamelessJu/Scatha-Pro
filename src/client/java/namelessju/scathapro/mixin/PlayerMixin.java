package namelessju.scathapro.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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
            ScathaProEvents.attackEntityEvent.trigger(ScathaPro.getInstance(),
                new ScathaProEvents.AttackEntityEventData(player, entity, itemStack)
            );
        }
        return itemStack;
    }
}
