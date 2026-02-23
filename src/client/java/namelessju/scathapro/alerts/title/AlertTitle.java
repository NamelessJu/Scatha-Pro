package namelessju.scathapro.alerts.title;

public abstract class AlertTitle
{
    public final int fadeInTicks;
    public final int stayTicks;
    public final int fadeOutTicks;

    public AlertTitle(int fadeInTicks, int stayTicks, int fadeOutTicks)
    {
        this.fadeInTicks = fadeInTicks;
        this.stayTicks = stayTicks;
        this.fadeOutTicks = fadeOutTicks;
    }
}
