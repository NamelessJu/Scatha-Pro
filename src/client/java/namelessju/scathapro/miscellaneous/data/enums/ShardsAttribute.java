package namelessju.scathapro.miscellaneous.data.enums;

import namelessju.scathapro.miscellaneous.data.MagicFindSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NullMarked;

@NullMarked
public enum ShardsAttribute implements MagicFindSource
{
    ELUSIVE_FORTUNE(
        "Elusive Fortune",
        Component.literal("Hideyho Shard").withColor(TextColor.BLUE)
    ),
    SUBTERRANEAN_FORTUNE(
        "Subterranean Fortune",
        Component.literal("Chuckwalla Shard").withColor(TextColor.BLUE)
    );

    public final String attributeName;
    public final Component shardName;

    ShardsAttribute(String attributeName, Component shardName)
    {
        this.attributeName = attributeName;
        this.shardName = shardName;
    }

    @Override
    public float getMagicFind(int level)
    {
        // hard-coded for now since both attributes of interest behave the same
        return Mth.clamp(level, 1, 10) * 0.5f;
    }
}