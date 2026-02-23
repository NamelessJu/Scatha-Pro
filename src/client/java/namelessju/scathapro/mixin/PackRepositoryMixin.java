package namelessju.scathapro.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import namelessju.scathapro.ScathaPro;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.PackRepository;
import org.apache.commons.compress.utils.Lists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(PackRepository.class)
public class PackRepositoryMixin
{
    @ModifyReturnValue(
        method = "openAllSelected",
        at = @At("RETURN")
    )
    private List<PackResources> injectCustomAlertModePack(List<PackResources> original)
    {
        List<PackResources> extended = Lists.newArrayList(original.iterator());
        extended.add(ScathaPro.getInstance().customAlertModeManager.resourcePack);
        ScathaPro.LOGGER.debug("Custom alert mode pack injected");
        return ImmutableList.copyOf(extended);
    }
}
