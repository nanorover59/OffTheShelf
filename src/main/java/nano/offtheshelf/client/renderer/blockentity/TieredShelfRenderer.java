package nano.offtheshelf.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.HashCommon;
import nano.offtheshelf.block.ModularShelfBlock;
import nano.offtheshelf.block.entity.OffTheShelfBlockEntity;
import nano.offtheshelf.block.entity.TieredShelfBlockEntity;
import nano.offtheshelf.client.renderer.blockentity.state.TieredShelfRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TieredShelfRenderer implements BlockEntityRenderer<TieredShelfBlockEntity, TieredShelfRenderState> {
    private static final float ITEM_SIZE = 0.25F;
    private final ItemModelResolver itemModelResolver;
    private final Font font;
    private final RandomSource random = RandomSource.create();

    public TieredShelfRenderer(final BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.font = context.font();
    }

    @Override
    public TieredShelfRenderState createRenderState() {
        return new TieredShelfRenderState();
    }

    @Override
    public void extractRenderState(TieredShelfBlockEntity blockEntity, TieredShelfRenderState state, final float partialTicks, final Vec3 cameraPos, @Nullable final ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPos, breakProgress);
        state.direction = blockEntity.getBlockState().getValue(ModularShelfBlock.FACING);
        NonNullList<ItemStack> items = blockEntity.getItems();
        Minecraft client = Minecraft.getInstance();
        int seed = HashCommon.long2int(blockEntity.getBlockPos().asLong());

        if(client.hitResult != null && client.hitResult instanceof BlockHitResult) {
            BlockHitResult blockHitResult = (BlockHitResult) client.hitResult;

            if(blockHitResult.getBlockPos().equals(blockEntity.getBlockPos()) && blockEntity.getMode() != OffTheShelfBlockEntity.LOCKED)
                state.highlight = ((ModularShelfBlock) blockEntity.getBlockState().getBlock()).getInteractionSlot(blockEntity.getBlockState(), blockHitResult);
        }

        for(int i = 0; i < 6; i++) {
            if(blockEntity.getMode() == OffTheShelfBlockEntity.ADVENTURE && blockEntity.getCooldown(i) > 0)
                continue;

            ItemStack itemStack = items.get(i);
            ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
            this.itemModelResolver.updateForTopItem(itemStackRenderState, itemStack, ItemDisplayContext.ON_SHELF, blockEntity.getLevel(), null, seed + i);
            state.items[i] = itemStackRenderState;
            state.count[i] = itemStack.getCount();

            if(state.highlight == i)
                state.name = itemStack.getStyledHoverName();
        }
    }

    @Override
    public void submit(TieredShelfRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera) {
        for(int i = 0; i < 6; i++) {
            double x = (double) ((i / 2) + 1) * 0.3 - 0.1;
            double y = (double) (1 - i % 2) * 0.5 + 0.2;
            double z = 0.2;

            if(state.highlight == i)
                y += 0.025;

            this.renderItem(matrices, queue, font, state.direction, state.lightCoords, state.items, state.count, state.name, state.highlight, x, y, z, i);
        }
    }

    public static void renderItem(PoseStack matrices, SubmitNodeCollector queue, Font font, Direction direction, int lightCoords, ItemStackRenderState[] items, int[] count, Component name, int highlight, double x, double y, double z, int index) {
        ItemStackRenderState itemStackRenderState = items[index];

        if(items[index] == null || items[index].isEmpty())
            return;

        Vec3 vec = switch (direction) {
            case NORTH -> new Vec3(1.0 - x, y, 1.0 - z);
            case EAST -> new Vec3(z, y, 1.0 - x);
            case SOUTH -> new Vec3(x, y, z);
            case WEST -> new Vec3(1.0 - z, y, x);
            default -> Vec3.ZERO;
        };

        matrices.pushPose();
        matrices.translate(vec);
        matrices.mulPose(Axis.YP.rotationDegrees(-direction.toYRot()));
        matrices.scale(0.25F, 0.25F, 0.25F);

        itemStackRenderState.submit(matrices, queue, lightCoords, OverlayTexture.NO_OVERLAY,  0);

        if(highlight > -1) {
            matrices.pushPose();
            matrices.translate(0.0f, 0.0f, 0.1f);
            matrices.scale(0.05f, -0.05f, 0.05f);
            // Item Name Text
            if(highlight == index && name != null) {
                float nameWidth = font.width(name);
                queue.submitText(
                        matrices,
                        -nameWidth * 0.5f, -20.0f,
                        name.getVisualOrderText(),
                        true,
                        Font.DisplayMode.SEE_THROUGH,
                        lightCoords,
                        0xffffffff,
                        0,
                        0
                );
            }
            // Item Count Text
            if(count[index] > 1) {
                String countText = String.valueOf(count[index]);
                float countWidth = font.width(countText);
                queue.submitText(
                        matrices,
                        11.0f - countWidth, 4.0f,
                        Component.literal(countText).getVisualOrderText(),
                        true,
                        Font.DisplayMode.SEE_THROUGH,
                        lightCoords,
                        0xffffffff,
                        0,
                        0
                );
            }
            matrices.popPose();
        }

        matrices.popPose();
    }
}