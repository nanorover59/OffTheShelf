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
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.CommonColors;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BookcaseRenderer implements BlockEntityRenderer<BookcaseBlockEntity, BookcaseRenderState> {
    public static final SpriteId BOOK_TEXTURE_0 = Sheets.BLOCKS_MAPPER.apply(Identifier.fromNamespaceAndPath(OffTheShelf.MOD_ID, "book0"));
    public static final SpriteId BOOK_TEXTURE_1 = Sheets.BLOCKS_MAPPER.apply(Identifier.fromNamespaceAndPath(OffTheShelf.MOD_ID, "book1"));
    public static final SpriteId BOOK_TEXTURE_2 = Sheets.BLOCKS_MAPPER.apply(Identifier.fromNamespaceAndPath(OffTheShelf.MOD_ID, "book2"));
    public static final SpriteId BOOK_TEXTURE_3 = Sheets.BLOCKS_MAPPER.apply(Identifier.fromNamespaceAndPath(OffTheShelf.MOD_ID, "book3"));
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
        RandomSource random = new LegacyRandomSource(blockEntity.getBlockPos().asLong());
        int[] variant = new int[16];

        for(int i = 0; i < 16; i++)
            variant[i] = random.nextInt(5);

        if(client.hitResult != null && client.hitResult instanceof BlockHitResult) {
            BlockHitResult blockHitResult = (BlockHitResult) client.hitResult;

            if(blockHitResult.getBlockPos().equals(blockEntity.getBlockPos()) && blockEntity.getMode() != OffTheShelfBlockEntity.LOCKED)
                state.highlight = ((ModularShelfBlock) blockEntity.getBlockState().getBlock()).getInteractionSlot(blockEntity.getBlockState(), blockHitResult);
        }

        for(int i = 0; i < 16; i++) {
            if(blockEntity.getMode() == OffTheShelfBlockEntity.ADVENTURE && blockEntity.getCooldown(i) > 0)
                continue;

            ItemStack itemStack = items.get(i);

            if(!itemStack.isEmpty()) {
                BookRenderState bookRenderState = new BookRenderState();
                bookRenderState.bookType = variant[i];
                bookRenderState.bookColor = DyedItemColor.getOrDefault(itemStack, -6265536);
                state.books[i] = bookRenderState;

                if(state.highlight == i) {
                    if(itemStack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
                        WrittenBookContent bookContent = itemStack.get(DataComponents.WRITTEN_BOOK_CONTENT);
                        state.name = Component.literal(bookContent.title().raw());
                        state.author = Component.literal("by " + bookContent.author());
                    } else if(itemStack.has(DataComponents.STORED_ENCHANTMENTS)) {
                        ItemEnchantments enchantments = itemStack.get(DataComponents.STORED_ENCHANTMENTS);
                        Holder<Enchantment> holder = enchantments.keySet().stream().findFirst().orElse(null);

                        if(holder != null)
                            state.name = Component.literal(holder.value().description().getString()).withColor(CommonColors.HIGH_CONTRAST_DIAMOND);
                    }
                }
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

            SpriteId texture = this.getTextureLocation(state.books[i].bookType);
            int color = state.books[i].bookColor;

            if(state.highlight == i)
                color = ARGB.average(color, ARGB.white(1.0f));

            matrices.pushPose();
            matrices.translate(vec);
            matrices.mulPose(Axis.YP.rotationDegrees(-direction.getOpposite().toYRot()));
            queue.submitModelPart(
                    this.book,
                    matrices,
                    texture.renderType(RenderTypes::entitySolid),
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    this.sprites.get(texture),
                    color,
                    state.breakProgress
            );

            if(state.highlight == i) {
                matrices.pushPose();
                matrices.translate(0.0625f, 0.125f, 0.0f);
                matrices.scale(-0.0125f, -0.0125f, -0.0125f);

                if(state.name != null) {
                    float nameWidth = this.font.width(state.name);
                    queue.submitText(
                            matrices,
                            -nameWidth * 0.5f, -15.0f,
                            state.name.getVisualOrderText(),
                            true,
                            Font.DisplayMode.SEE_THROUGH,
                            state.lightCoords,
                            0xffffffff,
                            0,
                            0
                    );
                }

                if(state.author != null) {
                    float authorWidth = this.font.width(state.author);
                    queue.submitText(
                            matrices,
                            -authorWidth * 0.5f, -2.0f,
                            state.author.getVisualOrderText(),
                            true,
                            Font.DisplayMode.SEE_THROUGH,
                            state.lightCoords,
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

    public SpriteId getTextureLocation(int type) {
        return switch(type) {
            case 1 -> BOOK_TEXTURE_1;
            case 2 -> BOOK_TEXTURE_2;
            case 3 -> BOOK_TEXTURE_3;
            default -> BOOK_TEXTURE_0;
        };
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("book", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 2.0F, 6.0F, 5.0F).mirror(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 16, 16);
    }
}