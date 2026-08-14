package namelessju.scathapro.util;

import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Random;

public final class Util
{
    private Util() {}

    /** Utility class for common TextColor integer values with alpha set to 255 (fully opaque) */
    @SuppressWarnings("unused")
    public static final class Color
    {
        public static final int DARK_RED = ARGB.opaque(TextColor.DARK_RED.getValue());
        public static final int RED = ARGB.opaque(TextColor.RED.getValue());
        public static final int GOLD = ARGB.opaque(TextColor.GOLD.getValue());
        public static final int YELLOW = ARGB.opaque(TextColor.YELLOW.getValue());
        public static final int DARK_GREEN = ARGB.opaque(TextColor.DARK_GREEN.getValue());
        public static final int GREEN = ARGB.opaque(TextColor.GREEN.getValue());
        public static final int AQUA = ARGB.opaque(TextColor.AQUA.getValue());
        public static final int DARK_AQUA = ARGB.opaque(TextColor.DARK_AQUA.getValue());
        public static final int DARK_BLUE = ARGB.opaque(TextColor.DARK_BLUE.getValue());
        public static final int BLUE = ARGB.opaque(TextColor.BLUE.getValue());
        public static final int LIGHT_PURPLE = ARGB.opaque(TextColor.LIGHT_PURPLE.getValue());
        public static final int DARK_PURPLE = ARGB.opaque(TextColor.DARK_PURPLE.getValue());
        public static final int WHITE = ARGB.opaque(TextColor.WHITE.getValue());
        public static final int GRAY = ARGB.opaque(TextColor.GRAY.getValue());
        public static final int DARK_GRAY = ARGB.opaque(TextColor.DARK_GRAY.getValue());
        public static final int BLACK = ARGB.opaque(TextColor.BLACK.getValue());

        private Color() {}
    }


    public static final Random random = new Random();


    @SuppressWarnings("all")
    public static <T> boolean optionalValueEquals(@NonNull Optional<T> optional, @NonNull T value)
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
}