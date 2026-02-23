package namelessju.scathapro.achievements;

import namelessju.scathapro.sounds.SoundData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public enum AchievementType
{
    NORMAL(null, null, LockedVisibility.VISIBLE, null),
    SECRET("Secret", Style.EMPTY.withColor(ChatFormatting.AQUA), LockedVisibility.TITLE_ONLY,
        SoundData.scathaPro("achievements.unlock", 0.9f, 0.749154f)),
    BONUS("BONUS", Style.EMPTY.withColor(ChatFormatting.YELLOW), LockedVisibility.HIDDEN,
        SoundData.vanilla("ui.toast.challenge_complete", 0.75f, 1.259921f)),
    HIDDEN("HIDDEN", Style.EMPTY.withColor(ChatFormatting.RED), LockedVisibility.HIDDEN,
        SoundData.vanilla("ui.toast.challenge_complete", 0.75f, 0.749154f)),
    LEGACY("LEGACY", Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE), LockedVisibility.HIDDEN, null);
    
    private static final SoundData DEFAULT_UNLOCK_SOUND = SoundData.scathaPro("achievements.unlock", 0.9f, 1f);
    
    public final @Nullable String typeName;
    public final @Nullable Style style;
    public final AchievementType.@NonNull LockedVisibility lockedVisibility;
    private final @Nullable SoundData unlockSound;
    
    AchievementType(@Nullable String typeName, @Nullable Style style, AchievementType.@NonNull LockedVisibility lockedVisibility, @Nullable SoundData unlockSound)
    {
        this.typeName = typeName;
        this.style = style;
        this.lockedVisibility = lockedVisibility;
        this.unlockSound = unlockSound;
    }
    
    public @NonNull SoundData getUnlockSound()
    {
        if (unlockSound != null) return unlockSound;
        return DEFAULT_UNLOCK_SOUND;
    }
    
    public @NonNull Component getNameComponent()
    {
        if (typeName == null) return CommonComponents.EMPTY;
        return Component.literal(typeName).setStyle(style != null ? style : Style.EMPTY);
    }
    
    @Override
    public String toString()
    {
        return typeName != null ? typeName : "Unnamed achievement type";
    }
    
    public enum LockedVisibility
    {
        VISIBLE, TITLE_ONLY, HIDDEN;
    }
}