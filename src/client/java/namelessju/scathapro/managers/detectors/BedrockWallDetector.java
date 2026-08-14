package namelessju.scathapro.managers.detectors;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class BedrockWallDetector
{
    private final ScathaPro scathaPro;

    private int distanceToWallPrevious;
    private long lastBedrockDetectionTime;
    private boolean bedrockDetectedThisDirection;
    private Direction bedrockDirectionBefore;

    public BedrockWallDetector(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        reset();
    }

    public void detect(LocalPlayer player, long now)
    {
        BlockPos playerBlockPos = player.blockPosition();
        Direction playerDirection = player.getDirection();

        boolean bedrockDetected = false;
        int distanceToWall = switch (playerDirection)
        {
            case NORTH -> playerBlockPos.getZ() - Constants.crystalHollowsBoundsMin;
            case EAST -> Constants.crystalHollowsBoundsMax - playerBlockPos.getX();
            case SOUTH -> Constants.crystalHollowsBoundsMax - playerBlockPos.getZ();
            case WEST -> playerBlockPos.getX() - Constants.crystalHollowsBoundsMin;
            default -> -1;
        };
        distanceToWall -= 1; // being next to the wall should be 0 distance

        if (bedrockDirectionBefore != null && bedrockDirectionBefore != playerDirection)
        {
            distanceToWallPrevious = -1;
            bedrockDetectedThisDirection = false;
        }
        bedrockDirectionBefore = playerDirection;

        int triggerDistance = scathaPro.config.alerts.bedrockWallAlertTriggerDistance.get();
        if (distanceToWallPrevious >= 0 && distanceToWallPrevious - distanceToWall == 1)
        {
            if (distanceToWall < triggerDistance) bedrockDetected = true;
        }
        if (distanceToWall >= triggerDistance) bedrockDetectedThisDirection = false;
        distanceToWallPrevious = distanceToWall;

        if (bedrockDetected && !bedrockDetectedThisDirection && (lastBedrockDetectionTime < 0 || now - lastBedrockDetectionTime > 1500))
        {
            bedrockDetectedThisDirection = true;
            lastBedrockDetectionTime = now;

            ScathaProEvents.bedrockWallDetectedEvent.trigger(scathaPro);
        }
    }

    public void reset()
    {
        distanceToWallPrevious = -1;
        lastBedrockDetectionTime = -1;
        bedrockDetectedThisDirection = false;
        bedrockDirectionBefore = null;
    }
}