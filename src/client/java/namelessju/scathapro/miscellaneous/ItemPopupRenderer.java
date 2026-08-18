package namelessju.scathapro.miscellaneous;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Renders an item popping up like the Totem of Undying, but rendered above EVERYTHING
 */
public class ItemPopupRenderer
{
    private final Minecraft minecraft;
    private final RandomSource randomSource = RandomSource.create();

    @Nullable
    private ItemStack itemStack;
    private int animationTicks;
    private boolean alternativeRotationAnimationCurve;
    private int animationTicksRemaining;
    private float animationOffsetX;
    private float animationOffsetY;
    private boolean animationOffsetMirroredInSecondHalf;
    private boolean angled;

    public ItemPopupRenderer(Minecraft minecraft)
    {
        this.minecraft = minecraft;
    }

    public void tick()
    {
        if (animationTicksRemaining > 0)
        {
            animationTicksRemaining--;
            if (animationTicksRemaining == 0)
            {
                itemStack = null;
            }
        }
    }

    public void render(ProjectionMatrixBuffer hud3dProjectionMatrixBuffer, Projection hudProjection,
                       SubmitNodeCollector submitNodeCollector, DeltaTracker deltaTracker,
                       FeatureRenderDispatcher featureRenderDispatcher, RenderBuffers renderBuffers)
    {
        if (itemStack == null || animationTicksRemaining <= 0) return;

        RenderSystem.setProjectionMatrix(hud3dProjectionMatrixBuffer.getBuffer(hudProjection), ProjectionType.PERSPECTIVE);
        //noinspection DataFlowIssue
        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(Minecraft.getInstance().getMainRenderTarget().getDepthTexture(), 1D);
        renderItem(submitNodeCollector, deltaTracker.getGameTimeDeltaPartialTick(true));
        featureRenderDispatcher.renderAllFeatures();
        renderBuffers.bufferSource().endBatch();
    }

    private void renderItem(SubmitNodeCollector submitNodeCollector, float partialTicks)
    {
        if (itemStack == null) return;

        PoseStack poseStack = new PoseStack();
        int elapsedTicks = animationTicks - animationTicksRemaining;
        float progress = ((float) elapsedTicks + partialTicks) / animationTicks;
        float progressSquared = progress * progress;
        float progressCubed = progress * progressSquared;
        float animationCurve = 10.25f * progressCubed * progressSquared - 24.95f * progressSquared * progressSquared + 25.5f * progressCubed - 13.8f * progressSquared + 4f * progress;
        float halfRotations = animationCurve * (float) Math.PI;
        float aspectRatio = (float) minecraft.getWindow().getWidth() / (float) minecraft.getWindow().getHeight();
        float offsetX = animationOffsetX * 0.3f * aspectRatio;
        float offsetY = animationOffsetY * 0.3f;
        if (animationOffsetMirroredInSecondHalf)
        {
            if (progress > 0.5f)
            {
                offsetX = -offsetX;
                offsetY = -offsetY;
            }

            // Vanilla animation curve doesn't exactly go through (0.5, 0.5)
            // which makes the sudden inverse of the offsets above noticeable,
            // as a fix we just smoothly force the offsets to 0 at the middle
            // of the animation in order to hide any snapping they would do
            if (progress > 0.3f && progress < 0.7f)
            {
                float multiplier = -Mth.cos(5*(float)Math.PI*(progress-0.5f))*0.5f+0.5f;
                offsetX *= multiplier;
                offsetY *= multiplier;
            }
        }
        poseStack.translate(offsetX * Mth.abs(Mth.sin(halfRotations * 2f)), offsetY * Mth.abs(Mth.sin(halfRotations * 2f)), -10f + 9f * Mth.sin(halfRotations));
        float scale = 0.8f;
        poseStack.scale(scale, scale, scale);
        if (angled)
        {
            poseStack.mulPose(Axis.XP.rotationDegrees(30f));
            poseStack.mulPose(Axis.YP.rotationDegrees(45f));
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(900f * (
            alternativeRotationAnimationCurve
                ? (float) Math.pow(Mth.sin(progress * (float) Math.PI), 0.5D)
                : Mth.abs(Mth.sin(halfRotations))
        )));
        poseStack.mulPose(Axis.XP.rotationDegrees(6f * Mth.cos(progress * 8f)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(6f * Mth.cos(progress * 8f)));
        minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
        ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
        minecraft.getItemModelResolver().updateForTopItem(itemStackRenderState, itemStack, ItemDisplayContext.FIXED, minecraft.level, null, 0);
        itemStackRenderState.submit(poseStack, submitNodeCollector, 15728880, OverlayTexture.NO_OVERLAY, 0);
    }

    public void popup(@NonNull ItemStack itemStack, int animationTicks, boolean alternativeRotationAnimationCurve, boolean angled)
    {
        if (!BuiltInRegistries.ITEM.wrapAsHolder(Items.PLAYER_HEAD).areComponentsBound())
            throw new IllegalStateException("Item popup cannot be rendered yet as components aren't bound!");
        if (animationTicks <= 0) throw new IllegalArgumentException("Item popup animation length must be greater than 0!");

        this.itemStack = itemStack;
        this.angled = angled;
        this.animationTicks = animationTicks;
        this.alternativeRotationAnimationCurve = alternativeRotationAnimationCurve;
        animationTicksRemaining = animationTicks;
        animationOffsetX = randomSource.nextFloat() * 2f - 1f;
        animationOffsetY = randomSource.nextFloat() * 2f - 1f;
        animationOffsetMirroredInSecondHalf = randomSource.nextBoolean();
    }

    @SuppressWarnings("unused")
    public void clear()
    {
        itemStack = null;
    }
}