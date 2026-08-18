package namelessju.scathapro.util;

import net.minecraft.ChatFormatting;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Random;

@NullMarked
public final class Util
{
    private Util() {}

    /** Utility class for ChatFormatting color integer values with alpha set to 255 (fully opaque) */
    @SuppressWarnings({"unused", "DataFlowIssue"})
    public static final class Color
    {
        public static final int DARK_RED = ARGB.opaque(ChatFormatting.DARK_RED.getColor());
        public static final int RED = ARGB.opaque(ChatFormatting.RED.getColor());
        public static final int GOLD = ARGB.opaque(ChatFormatting.GOLD.getColor());
        public static final int YELLOW = ARGB.opaque(ChatFormatting.YELLOW.getColor());
        public static final int DARK_GREEN = ARGB.opaque(ChatFormatting.DARK_GREEN.getColor());
        public static final int GREEN = ARGB.opaque(ChatFormatting.GREEN.getColor());
        public static final int AQUA = ARGB.opaque(ChatFormatting.AQUA.getColor());
        public static final int DARK_AQUA = ARGB.opaque(ChatFormatting.DARK_AQUA.getColor());
        public static final int DARK_BLUE = ARGB.opaque(ChatFormatting.DARK_BLUE.getColor());
        public static final int BLUE = ARGB.opaque(ChatFormatting.BLUE.getColor());
        public static final int LIGHT_PURPLE = ARGB.opaque(ChatFormatting.LIGHT_PURPLE.getColor());
        public static final int DARK_PURPLE = ARGB.opaque(ChatFormatting.DARK_PURPLE.getColor());
        public static final int WHITE = ARGB.opaque(ChatFormatting.WHITE.getColor());
        public static final int GRAY = ARGB.opaque(ChatFormatting.GRAY.getColor());
        public static final int DARK_GRAY = ARGB.opaque(ChatFormatting.DARK_GRAY.getColor());
        public static final int BLACK = ARGB.opaque(ChatFormatting.BLACK.getColor());

        private Color() {}
    }


    public static final Random random = new Random();


    @SuppressWarnings("all")
    public static <T> boolean optionalValueEquals(Optional<T> optional, T value)
    {
        return optional.map(t -> t.equals(value)).orElse(false);
    }

    public static Path resolvePath(Path parent, String... pathNodes)
    {
        Path currentNode = parent;
        for (String node : pathNodes)
        {
            currentNode = currentNode.resolve(node);
        }
        return currentNode;
    }

    public static Path resolvePath(Path parent, String forwardSlashSeparatedPath)
    {
        return resolvePath(parent, forwardSlashSeparatedPath.split("/"));
    }

    public static Vec2 getHorizontal(Vec3 vec3)
    {
        return new Vec2((float) vec3.x, (float) vec3.z);
    }
}