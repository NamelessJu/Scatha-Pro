package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.miscellaneous.data.mixindata.IWormArmorStandData;
import namelessju.scathapro.util.TimeUtil;
import namelessju.scathapro.util.Util;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity
{
    public LivingEntityMixin(EntityType<?> type, Level level)
    {
        super(type, level);
    }

    @Inject(
        method = "tick",
        at = @At("TAIL")
    )
    private void afterTick(CallbackInfo ci)
    {
        if (isRemoved()
            || !ScathaPro.instance().config.worms.seasonalWormTexturesEnabled.get()
            || !((Object) this instanceof IWormArmorStandData wormData) || wormData.scathaPro$getWormSegmentType() == null)
        {
            return;
        }

        if (TimeUtil.isHalloween() && tickCount % 8 == 0)
        {
            spawnWormSegmentParticle(ParticleTypes.FLAME, 0, 0.6, 0.6, 0);
        }
        if (TimeUtil.isChristmas() && tickCount % 7 == 0)
        {
            spawnWormSegmentParticle(ParticleTypes.SNOWFLAKE, 1, 0.7, 0.3, -0.1);
        }
    }

    @Unique
    private void spawnWormSegmentParticle(SimpleParticleType particleType, double yOffset, double randomOffsetXZ, double randomOffsetY, double yVelocity)
    {
        level().addParticle(
            particleType,
            Util.random.nextDouble(getX() - randomOffsetXZ, getX() + randomOffsetXZ),
            Util.random.nextDouble(getY() + 1.7 + yOffset - randomOffsetY, getY() + 1.7 + yOffset + randomOffsetY),
            Util.random.nextDouble(getZ() - randomOffsetXZ, getZ() + randomOffsetXZ),
            0, yVelocity, 0
        );
    }
}