package nano.offtheshelf.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import nano.offtheshelf.OffTheShelf;
import nano.offtheshelf.block.BookcaseBlock;
import nano.offtheshelf.block.ModularShelfBlock;
import nano.offtheshelf.block.entity.BookcaseBlockEntity;
import nano.offtheshelf.block.entity.OffTheShelfBlockEntity;
import nano.offtheshelf.client.OffTheShelfClient;
import nano.offtheshelf.client.renderer.blockentity.state.BookRenderState;
import nano.offtheshelf.client.renderer.blockentity.state.BookcaseRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BookcaseRenderer implements BlockEntityRenderer<BookcaseBlockEntity, BookcaseRenderState> {
    public static final SpriteId BOOK_TEXTURE = Sheets.BLOCKS_MAPPER.apply(Identifier.fromNamespaceAndPath(OffTheShelf.MOD_ID, "book"));
    private final SpriteGetter sprites;
    private final Font font;
    private final ModelPart book;

    public BookcaseRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.font = context.font();
        this.book = context.bakeLayer(OffTheShelfClient.BOOK_LAYER);
    }

    @Override
    public BookcaseRenderState createRenderState() {
        return new BookcaseRenderState();
    }

    @Override
    public void extractRenderState(BookcaseBlockEntity blockEntity, BookcaseRenderState state, float partialTicks, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPos, breakProgress);
        state.direction = blockEntity.getBlockState().getValue(BookcaseBlock.FACING);
        state.model = blockEntity.getBlockState().getValue(BookcaseBlock.MODEL);
        NonNullList<ItemStack> items = blockEntity.getItems();
        Minecraft client = Minecraft.getInstance();

        if(client.hitResult != null && client.hitResult instanceof BlockHitResult) {
            BlockHitResult blockHitResult = (BlockHitResult) client.hitResult;

            if (blockHitResult.getBlockPos().equals(blockEntity.getBlockPos()))
                state.highlight = ((ModularShelfBlock) blockEntity.getBlockState().getBlock()).getInteractionSlot(blockEntity.getBlockState(), blockHitResult);
        }

        for(int i = 0; i < 16; i++) {
            if(blockEntity.getMode() == OffTheShelfBlockEntity.ADVENTURE && blockEntity.getCooldown(i) > 0)
                continue;

            ItemStack itemStack = items.get(i);

            if(!itemStack.isEmpty()) {
                BookRenderState itemStackRenderState = new BookRenderState();
                itemStackRenderState.bookType = 1;
                itemStackRenderState.bookColor = DyedItemColor.getOrDefault(itemStack, -6265536);
                state.books[i] = itemStackRenderState;

                if(state.highlight == i)
                    state.name = itemStack.getStyledHoverName();
            }
            else
                state.books[i] = null;
        }
    }

    @Override
    public void submit(BookcaseRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera) {
        Direction direction = state.direction;
        int size = state.model == BookcaseBlock.CENTER ? 16 : 14;

        for(int i = 0; i < size; i++) {
            if(state.books[i] == null)
                continue;


            double x = (double) ((i / 2) + 1) * 0.125 + 0.0625;
            double y = (double) (1 - i % 2) * 0.5 + 0.0625;
            double z = 0.4375;

            if(state.model == BookcaseBlock.LEFT)
                x += 0.0625;
            else if(state.model == BookcaseBlock.RIGHT || state.model == BookcaseBlock.CENTER)
                x -= 0.0625;

            Vec3 vec = switch (direction) {
                case NORTH -> new Vec3(1.0 - x, y, 1.0 - z);
                case EAST -> new Vec3(z, y, 1.0 - x);
                case SOUTH -> new Vec3(x, y, z);
                case WEST -> new Vec3(1.0 - z, y, x);
                default -> Vec3.ZERO;
            };

            int color = state.books[i].bookColor;

            if(state.highlight == i)
                color = ARGB.average(color, ARGB.white(1.0f));

            matrices.pushPose();
            matrices.translate(vec);
            matrices.mulPose(Axis.YP.rotationDegrees(-direction.getOpposite().toYRot()));
            queue.submitModelPart(
                    this.book,
                    matrices,
                    BOOK_TEXTURE.renderType(RenderTypes::entitySolid),
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    this.sprites.get(BOOK_TEXTURE),
                    color,
                    state.breakProgress
            );

            if(state.highlight == i) {
                matrices.pushPose();
                matrices.translate(0.0625f, 0.125f, 0.0f);
                matrices.scale(-0.0125f, -0.0125f, -0.0125f);
                float nameWidth = this.font.width(state.name);
                queue.submitText(
                        matrices,
                        -nameWidth * 0.5f, -7.5f,
                        state.name.getVisualOrderText(),
                        true,
                        Font.DisplayMode.SEE_THROUGH,
                        state.lightCoords,
                        0xffffffff,
                        0,
                        0
                );
                matrices.popPose();
            }

            matrices.popPose();
        }
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("book", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 2.0F, 6.0F, 5.0F).mirror(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 16, 16);
    }
}