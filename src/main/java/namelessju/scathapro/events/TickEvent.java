package namelessju.scathapro.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class TickEvent extends Event
{
    public static class FirstIngameTickEvent extends TickEvent {}
    
    public static class FirstWorldTickEvent extends TickEvent
    {
        public final EntityPlayer player;
        
        public FirstWorldTickEvent(EntityPlayer player)
        {
            this.player = player;
        }
    }
    
    public static class FirstCrystalHollowsTickEvent extends TickEvent {}

    public static class CrystalHollowsTickEvent extends TickEvent
    {
        public final long now;
        
        public CrystalHollowsTickEvent(long now)
        {
            this.now = now;
        }
    }
}
