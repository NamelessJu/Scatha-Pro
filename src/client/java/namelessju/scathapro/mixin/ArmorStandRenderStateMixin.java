package namelessju.scathapro.mixin;

import namelessju.scathapro.miscellaneous.data.enums.WormSegmentType;
import namelessju.scathapro.miscellaneous.data.mixindata.IArmorStandRenderStateData;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ArmorStandRenderState.class)
public abstract class ArmorStandRenderStateMixin implements IArmorStandRenderStateData
{
    @Unique
    private float wormLifetimeLeft = -1f;
    @Unique
    private @Nullable Boolean wormType = null;
    @Unique
    private @Nullable WormSegmentType wormSegmentType = null;

    @Override
    public void scathaPro$setWormLifetimeLeft(float lifetimeLeft)
    {
        wormLifetimeLeft = lifetimeLeft;
    }

    @Override
    public float scathaPro$getWormLifetimeLeft()
    {
        return wormLifetimeLeft;
    }

    @Override
    public void scathaPro$setIsWorm(boolean isScatha)
    {
        wormType = isScatha;
    }

    @Override
    public boolean scathaPro$isWorm()
    {
        return wormType != null;
    }

    @Override
    public boolean scathaPro$isScatha()
    {
        return wormType != null && wormType;
    }

    @Override
    public void scathaPro$setWormSegmentType(WormSegmentType segmentType)
    {
        this.wormSegmentType = segmentType;
    }

    @Override
    public @Nullable WormSegmentType scathaPro$getWormSegmentType()
    {
        return wormSegmentType;
    }
}