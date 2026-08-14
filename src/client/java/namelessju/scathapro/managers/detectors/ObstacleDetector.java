package namelessju.scathapro.managers.detectors;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ObstacleDetector
{
    private final ScathaPro scathaPro;

    private boolean obstacleFoundBefore = false;
    private int obstacleChestTickTimer = 0;

    public ObstacleDetector(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }

    public void detect(LocalPlayer player)
    {
        Level level = player.level();
        BlockPos playerBlockPos = player.blockPosition();
        Direction playerDirection = player.getDirection();

        BlockPos inFrontPos = playerBlockPos.offset(playerDirection.getUnitVec3i());
        BlockState blockInFront = level.getBlockState(inFrontPos);
        BlockState blockInFrontAbove = level.getBlockState(inFrontPos.above());
        BlockState blockAbove = level.getBlockState(playerBlockPos.above());
        Block obstacleBlockFound = null;
        if ((blockInFront.is(Blocks.BEDROCK) || blockInFrontAbove.is(Blocks.BEDROCK))
            && Constants.crystalHollowsBoundsMin < inFrontPos.getX()
            && inFrontPos.getX() < Constants.crystalHollowsBoundsMax
            && Constants.crystalHollowsBoundsMin < inFrontPos.getZ()
            && inFrontPos.getZ() < Constants.crystalHollowsBoundsMax
        ) {
            obstacleBlockFound = Blocks.BEDROCK;
        }
        if (blockInFront.is(Blocks.CHEST) || blockInFrontAbove.is(Blocks.CHEST)
            || blockAbove.is(Blocks.CHEST))
        {
            // Chests have delayed trigger since you can sometimes walk
            // through them, in which case the alert shouldn't trigger
            if (obstacleChestTickTimer > 20) obstacleBlockFound = Blocks.CHEST;
            else obstacleChestTickTimer ++;
        }
        else obstacleChestTickTimer = 0;
        if (obstacleBlockFound != null && !obstacleFoundBefore)
        {
            scathaPro.alertManager.obstacleAlert.play(scathaPro,
                obstacleBlockFound.getName().withColor(TextColor.GRAY)
            );
        }
        obstacleFoundBefore = obstacleBlockFound != null;
    }

    public void reset()
    {
        obstacleFoundBefore = false;
        obstacleChestTickTimer = 0;
    }
}