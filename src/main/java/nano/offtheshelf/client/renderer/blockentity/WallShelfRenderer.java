package nano.offtheshelf.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.HashCommon;
import nano.offtheshelf.block.ModularShelfBlock;
import nano.offtheshelf.block.WallShelfBlock;
import nano.offtheshelf.block.entity.OffTheShelfBlockEntity;
import nano.offtheshelf.block.entity.TieredShelfBlockEntity;
import nano.offtheshelf.block.entity.WallShelfBlockEntity;
import nano.offtheshelf.client.renderer.blockentity.state.WallShelfRenderState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class WallShelfRenderer implements BlockEntityRenderer<WallShelfBlockEntity, WallShelfRenderState> {
    private static final float ITEM_SIZE = 0.25F;
    private final ItemModelResolver itemModelResolver;
    private final Font font;
    private final RandomSource random = RandomSource.create();

    public WallShelfRenderer(final BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.font = context.font();
    }

    @Override
    public WallShelfRenderState createRenderState() {
        return new WallShelfRenderState();
    }

    @Override
    public boolean shouldRender(WallShelfBlockEntity blockEntity, Vec3 cameraPosition) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 viewVec = new Vec3(camera.forwardVector());
        Vec3 diffVec = cameraPosition.subtract(blockEntity.getBlockPos().getCenter()).normalize();

        if(diffVec.dot(viewVec) > 0)
            return false;

        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPosition);
    }

    @Override
    public void extractRenderState(WallShelfBlockEntity blockEntity, WallShelfRenderState state, final float partialTicks, final Vec3 cameraPos, @Nullable final ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPos, breakProgress);
        Minecraft client = Minecraft.getInstance();

        state.direction = blockEntity.getBlockState().getValue(ModularShelfBlock.FACING);
        state.bottom = blockEntity.getBlockState().getValue(WallShelfBlock.BOTTOM);
        NonNullList<ItemStack> items = blockEntity.getItems();
        int seed = HashCommon.long2int(blockEntity.getBlockPos().asLong());

        if(client.hitResult != null && client.hitResult instanceof BlockHitResult) {
            BlockHitResult blockHitResult = (BlockHitResult) client.hitResult;

            if(blockHitResult.getBlockPos().equals(blockEntity.getBlockPos()) && blockEntity.getMode() != OffTheShelfBlockEntity.LOCKED)
                state.highlight = ((ModularShelfBlock) blockEntity.getBlockState().getBlock()).getInteractionSlot(blockEntity.getBlockState(), blockHitResult);
        }

        for(int i = 0; i < 3; i++) {
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
    public void submit(WallShelfRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera) {
        for(int i = 0; i < 3; i++) {
            double x = (double) i * 0.3 + 0.2;
            double y = state.bottom ? 0.325 : 0.7;
            double z = 0.2;

            if(state.highlight == i)
                y += 0.025;

            TieredShelfRenderer.renderItem(matrices, queue, font, state.direction, state.lightCoords, state.items, state.count, state.name, state.highlight, x, y, z, i);
        }
    }
}