package nano.offtheshelf.client.datagen;

import nano.offtheshelf.OffTheShelf;
import nano.offtheshelf.block.BookcaseBlock;
import nano.offtheshelf.block.ModularShelfBlock;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

public class OffTheShelfModelProvider extends FabricModelProvider {

    public static ModelTemplate BOOKCASE = blockTemplate("bookcase", TextureSlot.ALL);
    public static ModelTemplate BOOKCASE_CENTER = blockTemplate("bookcase_center", TextureSlot.ALL);
    public static ModelTemplate BOOKCASE_LEFT = blockTemplate("bookcase_left", TextureSlot.ALL);
    public static ModelTemplate BOOKCASE_RIGHT = blockTemplate("bookcase_right", TextureSlot.ALL);

    public static ModelTemplate TIERED_SHELF = blockTemplate("tiered_shelf", TextureSlot.ALL);
    public static ModelTemplate TIERED_SHELF_CENTER = blockTemplate("tiered_shelf_center", TextureSlot.ALL);
    public static ModelTemplate TIERED_SHELF_LEFT = blockTemplate("tiered_shelf_left", TextureSlot.ALL);
    public static ModelTemplate TIERED_SHELF_RIGHT = blockTemplate("tiered_shelf_right", TextureSlot.ALL);

    public OffTheShelfModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public String getName() {
        return "OffTheShelfModelProvider";
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        OffTheShelf.BOOKCASES.forEach(block -> this.registerBookcase(blockStateModelGenerator, block, this.blockAll(OffTheShelf.BASE_BLOCKS.get(block))));
        OffTheShelf.TIERED_SHELVING.forEach(block -> this.registerTieredShelf(blockStateModelGenerator, block, this.blockAll(OffTheShelf.BASE_BLOCKS.get(block))));
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
    }

    public static void registerBookcase(BlockModelGenerators generator, Block block, TextureMapping textures) {
        Identifier model = BOOKCASE.create(block, textures, generator.modelOutput);
        Identifier modelCenter = BOOKCASE_CENTER.createWithSuffix(block, "_center", textures, generator.modelOutput);
        Identifier modelLeft = BOOKCASE_LEFT.createWithSuffix(block, "_left", textures, generator.modelOutput);
        Identifier modelRight = BOOKCASE_RIGHT.createWithSuffix(block, "_right", textures, generator.modelOutput);
        generator.blockStateOutput.accept(createModularShelfStates(block, model, modelCenter, modelLeft, modelRight));
        generator.registerSimpleItemModel(block, model);
    }

    public static void registerTieredShelf(BlockModelGenerators generator, Block block, TextureMapping textures) {
        Identifier model = TIERED_SHELF.create(block, textures, generator.modelOutput);
        Identifier modelCenter = TIERED_SHELF_CENTER.createWithSuffix(block, "_center", textures, generator.modelOutput);
        Identifier modelLeft = TIERED_SHELF_LEFT.createWithSuffix(block, "_left", textures, generator.modelOutput);
        Identifier modelRight = TIERED_SHELF_RIGHT.createWithSuffix(block, "_right", textures, generator.modelOutput);
        generator.blockStateOutput.accept(createModularShelfStates(block, model, modelCenter, modelLeft, modelRight));
        generator.registerSimpleItemModel(block, model);
    }

    public static BlockModelDefinitionGenerator createModularShelfStates(Block block, Identifier single, Identifier center, Identifier left, Identifier right) {
        MultiVariant model = BlockModelGenerators.plainVariant(single);
        MultiVariant modelCenter = BlockModelGenerators.plainVariant(center);
        MultiVariant modelLeft = BlockModelGenerators.plainVariant(left);
        MultiVariant modelRight = BlockModelGenerators.plainVariant(right);
        return MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(ModularShelfBlock.FACING, ModularShelfBlock.MODEL)
                        .select(Direction.NORTH, ModularShelfBlock.SINGLE, model.with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, ModularShelfBlock.SINGLE, model.with(BlockModelGenerators.UV_LOCK).with(BlockModelGenerators.Y_ROT_90))
                        .select(Direction.SOUTH, ModularShelfBlock.SINGLE, model.with(BlockModelGenerators.UV_LOCK).with(BlockModelGenerators.Y_ROT_180))
                        .select(Direction.WEST, ModularShelfBlock.SINGLE, model.with(BlockModelGenerators.UV_LOCK).with(BlockModelGenerators.Y_ROT_270))
                        .select(Direction.NORTH, ModularShelfBlock.CENTER, modelCenter.with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, ModularShelfBlock.CENTER, modelCenter.with(BlockModelGenerators.UV_LOCK).with(BlockModelGenerators.Y_ROT_90))
                        .select(Direction.SOUTH, ModularShelfBlock.CENTER, modelCenter.with(BlockModelGenerators.UV_LOCK).with(BlockModelGenerators.Y_ROT_180))
                        .select(Direction.WEST, ModularShelfBlock.CENTER, modelCenter.with(BlockModelGenerators.UV_LOCK).with(BlockModelGenerators.Y_ROT_270))
                        .select(Direction.NORTH, ModularShelfBlock.LEFT, modelLeft.with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, ModularShelfBlock.LEFT, modelLeft.with(BlockModelGenerators.UV_LOCK).with(BlockModelGenerators.Y_ROT_90))
                        .select(Direction.SOUTH, ModularShelfBlock.LEFT, modelLeft.with(BlockModelGenerators.UV_LOCK).with(BlockModelGenerators.Y_ROT_180))
                        .select(Direction.WEST, ModularShelfBlock.LEFT, modelLeft.with(BlockModelGenerators.UV_LOCK).with(BlockModelGenerators.Y_ROT_270))
                        .select(Direction.NORTH, ModularShelfBlock.RIGHT, modelRight.with(BlockModelGenerators.UV_LOCK))
                        .select(Direction.EAST, ModularShelfBlock.RIGHT, modelRight.with(BlockModelGenerators.UV_LOCK).with(BlockModelGenerators.Y_ROT_90))
                        .select(Direction.SOUTH, ModularShelfBlock.RIGHT, modelRight.with(BlockModelGenerators.UV_LOCK).with(BlockModelGenerators.Y_ROT_180))
                        .select(Direction.WEST, ModularShelfBlock.RIGHT, modelRight.with(BlockModelGenerators.UV_LOCK).with(BlockModelGenerators.Y_ROT_270))
                );
    }

    private static ModelTemplate blockTemplate(String parent, TextureSlot... requiredTextureKeys) {
        return new ModelTemplate(Optional.of(Identifier.fromNamespaceAndPath(OffTheShelf.MOD_ID, "block/" + parent)), Optional.empty(), requiredTextureKeys);
    }

    public static TextureMapping blockAll(Block block) {
        return new TextureMapping().put(TextureSlot.ALL, new Material(ModelLocationUtils.getModelLocation(block)));
    }
}