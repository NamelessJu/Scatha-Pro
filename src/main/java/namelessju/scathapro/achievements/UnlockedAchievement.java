package namelessju.scathapro.achievements;

import net.minecraft.util.EnumChatFormatting;

public class UnlockedAchievement
{
    public static final String repeatFormatting = EnumChatFormatting.LIGHT_PURPLE.toString();
    
    
    public final Achievement achievement;
    public final long unlockedAtTimestamp;
    private int repeatCount = 0;

    public UnlockedAchievement(Achievement achievement, long unlockedAtTimestamp)
    {
        this.achievement = achievement;
        this.unlockedAtTimestamp = unlockedAtTimestamp;
    }
    
    public void setRepeatCount(int count)
    {
    	repeatCount = Math.max(count, 0);
    }
    
    public int getRepeatCount()
    {
    	return repeatCount;
    }
    
    public String getRepeatCountUnlockString()
    {
        return repeatFormatting + "[x" + (1 + getRepeatCount()) + "]";
    }
}
