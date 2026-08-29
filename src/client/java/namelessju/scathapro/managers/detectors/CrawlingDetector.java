package namelessju.scathapro.managers.detectors;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.Achievement;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class CrawlingDetector
{
    private @Nullable BlockPos crawlingStartPos = null;
    private int crawlingTicks = 0;

    public void detect(ScathaPro scathaPro)
    {
        if (!scathaPro.coreManager.isInCrystalHollows()) return;
        LocalPlayer player = scathaPro.minecraft.player;
        if (player == null) return;

        if (player.isVisuallyCrawling())
        {
            crawlingTicks++;
            if ((crawlingTicks - scathaPro.config.alerts.crawlingAlertTriggerDelayTicks.get())
                % scathaPro.config.alerts.crawlingAlertTriggerIntervalTicks.get() == 0)
            {
                scathaPro.alertManager.crawlingAlert.play(scathaPro);
            }

            if (crawlingStartPos == null) crawlingStartPos = player.blockPosition();
            else if (player.blockPosition().distSqr(crawlingStartPos) >= Achievement.crawl.goal*Achievement.crawl.goal)
            {
                Achievement.crawl.unlock();
            }
        }
        else
        {
            crawlingStartPos = null;
            crawlingTicks = 0;
        }
    }

    public void reset()
    {
        crawlingStartPos = null;
    }
}