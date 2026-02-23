package namelessju.scathapro.achievements;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.NonNull;

public class UnlockedAchievement
{
    public static final Style repeatStyle = Style.EMPTY.applyFormat(ChatFormatting.LIGHT_PURPLE);
    
    
    public final @NonNull Achievement achievement;
    public final long unlockTimestamp;
    private int repeatCount;
    
    public UnlockedAchievement(@NonNull Achievement achievement, long unlockTimestamp)
    {
        this(achievement, unlockTimestamp, 0);
    }
    
    public UnlockedAchievement(@NonNull Achievement achievement, long unlockTimestamp, int repeatCount)
    {
        this.achievement = achievement;
        this.unlockTimestamp = unlockTimestamp;
        this.repeatCount = repeatCount;
    }
    
    public void setRepeatCount(int count)
    {
    	repeatCount = Math.max(count, 0);
    }
    
    public int getRepeatCount()
    {
    	return repeatCount;
    }
    
    public Component getRepeatCountUnlockComponent()
    {
        return Component.literal("[x" + (1 + getRepeatCount()) + "]").withStyle(repeatStyle);
    }
}
