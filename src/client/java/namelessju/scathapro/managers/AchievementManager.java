package namelessju.scathapro.managers;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.achievements.AchievementType;
import namelessju.scathapro.achievements.UnlockedAchievement;
import namelessju.scathapro.achievements.UnlockedAchievements;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.gui.menus.screens.FakeBanScreen;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.NonNull;

import java.util.LinkedList;
import java.util.Queue;

public class AchievementManager
{
    private final ScathaPro scathaPro;
    
    private final Queue<DelayedAchievementUnlock> delayedAchievementUnlocks = new LinkedList<>();
    private AchievementType lastUnlockedAchievementType = null;
    private SoundInstance lastUnlockSound = null;
    
    public AchievementManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }
    
    public void tick()
    {
        if (canUnlockAchievement())
        {
            while (!delayedAchievementUnlocks.isEmpty())
            {
                DelayedAchievementUnlock delayedAchievementUnlock = delayedAchievementUnlocks.poll();
                unlockAchievement(delayedAchievementUnlock.achievement, delayedAchievementUnlock.goalReachedCount);
            }
        }
    }
    
    private boolean canUnlockAchievement()
    {
        return scathaPro.minecraft.level != null && !(scathaPro.minecraft.screen instanceof FakeBanScreen);
    }

    public void unlockAchievement(Achievement achievement)
    {
        unlockAchievement(achievement, -1);
    }
    
    public void unlockAchievement(Achievement achievement, int goalReachedCount)
    {
        if (!canUnlockAchievement())
        {
            delayedAchievementUnlocks.add(new DelayedAchievementUnlock(achievement, goalReachedCount));
            return;
        }
        
    	UnlockedAchievement unlockedAchievement = getUnlockedAchievements().getFor(achievement);
    	
        if (goalReachedCount == 0) return;
        
        if (unlockedAchievement == null)
    	{
        	unlockedAchievement = new UnlockedAchievement(achievement, TimeUtil.now());
            getUnlockedAchievements().add(unlockedAchievement);
    	}
        else if (!achievement.isRepeatable) return;
        
        if (achievement.isRepeatable)
        {
        	int repeatCount = unlockedAchievement.getRepeatCount();
        	if (goalReachedCount > 0)
        	{
        	    int newRepeatCount = goalReachedCount - 1;
        	    if (newRepeatCount <= repeatCount) return;
        	    unlockedAchievement.setRepeatCount(newRepeatCount);
        	}
        	else unlockedAchievement.setRepeatCount(repeatCount + 1);
        }
        
        scathaPro.persistentData.save();
        
        sendAchievementUnlockAlert(unlockedAchievement);
        
        ScathaProEvents.achievementUnlockedEvent.trigger(scathaPro,
            new ScathaProEvents.AchievementUnlockedEventData(unlockedAchievement)
        );
    }
    
    private void sendAchievementUnlockAlert(UnlockedAchievement unlockedAchievement)
    {
        if (!scathaPro.config.achievements.playAlerts.get()) return;
        
        Achievement achievement = unlockedAchievement.achievement;
        boolean isRepeated = achievement.isRepeatable && unlockedAchievement.getRepeatCount() > 0;
        
        if (isRepeated && !scathaPro.config.achievements.playRepeatAlerts.get()) return;
        
        final Style unlockTextStyle;
        final String unlockWord;
        if (isRepeated)
        {
            unlockTextStyle = UnlockedAchievement.repeatStyle;
            unlockWord = "repeated";
        }
        else
        {
            unlockTextStyle = Style.EMPTY.withColor(ChatFormatting.GREEN);
            unlockWord = "unlocked";
        }
        
        MutableComponent message = Component.empty()
            .append(Component.empty().setStyle(unlockTextStyle)
                .append(
                    achievement.type.typeName != null
                        ? Component.empty().append(achievement.type.getNameComponent()).append(" achievement")
                        : Component.literal("Achievement")
                )
                .append(" " + unlockWord + ": ")
            )
            .append(Component.literal(achievement.achievementName).setStyle(Style.EMPTY
                .withColor(ChatFormatting.GOLD)
                .withItalic(true)
                .withHoverEvent(new HoverEvent.ShowText(Component.empty()
                    .append(Component.literal(achievement.achievementName + "\n").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .append(Component.literal(achievement.description).withStyle(ChatFormatting.GRAY))
                    .append(achievement.isRepeatable
                        ? Component.empty().append("\n")
                        .append(Component.literal("[Repeatable]").setStyle(UnlockedAchievement.repeatStyle))
                        : Component.empty()
                    )
                    .append("\n\n")
                    .append(Component.literal(achievement.category.categoryName).withStyle(ChatFormatting.GOLD))
                ))
            ));
        if (isRepeated)
        {
            message.append(" ").append(unlockedAchievement.getRepeatCountUnlockComponent());
        }
        scathaPro.chatManager.sendChatMessage(message);
        
        boolean isLastUnlockSoundPlaying = lastUnlockSound != null && scathaPro.soundManager.isPlaying(lastUnlockSound);
        if (!isLastUnlockSoundPlaying
            || lastUnlockedAchievementType == null || lastUnlockedAchievementType.ordinal() <= achievement.type.ordinal())
        {
            if (isLastUnlockSoundPlaying) scathaPro.soundManager.stop(lastUnlockSound);
            if (isRepeated) lastUnlockSound = scathaPro.soundManager.playModSound(
                ScathaPro.getIdentifier("achievements.unlock"),
                0.85f, 1.259921f
            );
            else lastUnlockSound = achievement.type.getUnlockSound().playModSound(scathaPro.soundManager);
        }
        lastUnlockedAchievementType = achievement.type;
    }
    
    public boolean revokeAchievement(Achievement achievement)
    {
        if (getUnlockedAchievements().remove(achievement))
        {
            achievement.setProgress(0f);
            scathaPro.persistentData.save();
            return true;
        }
        return false;
    }
    
    private UnlockedAchievements getUnlockedAchievements()
    {
        return scathaPro.getProfileData().unlockedAchievements;
    }
    
    public static Achievement[] getAllAchievements()
    {
        return Achievement.values();
    }
    
    
    private record DelayedAchievementUnlock(@NonNull Achievement achievement, int goalReachedCount) {}
}
