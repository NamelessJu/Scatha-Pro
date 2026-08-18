package namelessju.scathapro.miscellaneous.data;

import namelessju.scathapro.ScathaPro;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public record CachedComponentProvider(Component component) implements Function<ScathaPro, Component>
{
    @Override
    public Component apply(ScathaPro scathaPro)
    {
        return component;
    }
}