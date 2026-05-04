package nano.offtheshelf;

import nano.offtheshelf.block.BookcaseBlock;
import nano.offtheshelf.block.TieredShelfBlock;
import nano.offtheshelf.block.entity.BookcaseBlockEntity;
import nano.offtheshelf.block.entity.OffTheShelfBlockEntity;
import nano.offtheshelf.block.entity.TieredShelfBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public class OffTheShelf implements ModInitializer {

    public static final String MOD_ID = "offtheshelf";

    public static ArrayList<Block> BOOKCASES = new ArrayList<>();
    public static ArrayList<Block> TIERED_SHELVING = new ArrayList<>();
    public static HashMap<Block, Block> BASE_BLOCKS = new HashMap<>();

    public static BlockEntityType<BookcaseBlockEntity> BOOKCASE_BLOCK_ENTITY;
    public static BlockEntityType<TieredShelfBlockEntity> TIERED_SHELF_BLOCK_ENTITY;

    public static CreativeModeTab CREATIVE_TAB = registerCreativeTab("offtheshelf");

    @Override
    public void onInitialize() {
        // Wood Types
        registerShelving("oak", Blocks.OAK_PLANKS);
        registerShelving("spruce", Blocks.SPRUCE_PLANKS);
        registerShelving("birch", Blocks.BIRCH_PLANKS);
        registerShelving("jungle", Blocks.JUNGLE_PLANKS);
        registerShelving("acacia", Blocks.ACACIA_PLANKS);
        registerShelving("dark_oak", Blocks.DARK_OAK_PLANKS);
        registerShelving("mangrove", Blocks.MANGROVE_PLANKS);
        registerShelving("cherry", Blocks.CHERRY_PLANKS);
        registerShelving("pale_oak", Blocks.PALE_OAK_PLANKS);
        registerShelving("bamboo", Blocks.BAMBOO_PLANKS);
        registerShelving("crimson", Blocks.CRIMSON_PLANKS);
        registerShelving("warped", Blocks.WARPED_PLANKS);
        // Mineral Types
        registerShelving("stone", Blocks.STONE);
        registerShelving("stone_brick", Blocks.STONE_BRICKS);
        registerShelving("polished_granite", Blocks.POLISHED_GRANITE);
        registerShelving("polished_diorite", Blocks.POLISHED_DIORITE);
        registerShelving("polished_andesite", Blocks.POLISHED_ANDESITE);
        registerShelving("polished_deepslate", Blocks.POLISHED_DEEPSLATE);
        registerShelving("deepslate_brick", Blocks.DEEPSLATE_BRICKS);
        registerShelving("prismarine_brick", Blocks.PRISMARINE_BRICKS);
        registerShelving("dark_prismarine", Blocks.DARK_PRISMARINE);
        registerShelving("nether_brick", Blocks.NETHER_BRICKS);
        registerShelving("red_nether_brick", Blocks.RED_NETHER_BRICKS);
        registerShelving("polished_blackstone_brick", Blocks.POLISHED_BLACKSTONE_BRICKS);
        registerShelving("end_stone_brick", Blocks.END_STONE_BRICKS);
        registerShelving("purpur", Blocks.PURPUR_BLOCK);
        registerShelving("quartz", Blocks.QUARTZ_BLOCK);
        // Metal Types
        registerShelving("copper", Blocks.CUT_COPPER);
        registerShelving("iron", Blocks.IRON_BLOCK);
        registerShelving("gold", Blocks.GOLD_BLOCK);

        BOOKCASE_BLOCK_ENTITY = registerBlockEntity("bookcase", BookcaseBlockEntity::new, BOOKCASES.toArray(new Block[0]));
        TIERED_SHELF_BLOCK_ENTITY = registerBlockEntity("tiered_shelf", TieredShelfBlockEntity::new, TIERED_SHELVING.toArray(new Block[0]));

        CommandRegistrationCallback.EVENT.register(((dispatcher, buildContext, selection) ->
                dispatcher.register(Commands.literal("shelf").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("from", BlockPosArgument.blockPos())
                                .then(Commands.argument("to", BlockPosArgument.blockPos())
                                        .then(Commands.literal("mode")
                                                .then(Commands.literal("normal").executes(context ->
                                                OffTheShelfBlockEntity.setMode(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to"), OffTheShelfBlockEntity.NORMAL)))
                                                .then(Commands.literal("locked").executes(context ->
                                                OffTheShelfBlockEntity.setMode(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to"), OffTheShelfBlockEntity.LOCKED)))
                                                .then(Commands.literal("adventure").executes(context ->
                                                OffTheShelfBlockEntity.setMode(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to"), OffTheShelfBlockEntity.ADVENTURE)))))))));
    }

    private static void registerShelving(String name, Block baseBlock) {
        registerBookcase(name, baseBlock);
        registerTieredShelf(name, baseBlock);
    }

    private static Block registerBookcase(String name, Block baseBlock) {
        Block bookcase = registerBlock(name + "_bookcase", BookcaseBlock::new, BlockBehaviour.Properties.ofFullCopy(baseBlock));
        BOOKCASES.add(bookcase);
        BASE_BLOCKS.put(bookcase, baseBlock);

        return bookcase;
    }

    private static Block registerTieredShelf(String name, Block baseBlock) {
        Block tieredShelf = registerBlock(name + "_tiered_shelf", TieredShelfBlock::new, BlockBehaviour.Properties.ofFullCopy(baseBlock).isViewBlocking(Blocks::never).noOcclusion());
        TIERED_SHELVING.add(tieredShelf);
        BASE_BLOCKS.put(tieredShelf, baseBlock);
        return tieredShelf;
    }

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, TagKey<Block>... tagKeys) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Block block = factory.apply(settings.setId(blockKey));
        // Block Item
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
        // Block Tags
        //Arrays.stream(tagKeys).forEach(tagKey -> BLOCK_TAGS.add(new Pair<>(Identifier.fromNamespaceAndPath(MODID, name), tagKey)));
        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String name, FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory, Block... blocks) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
    }

    private static CreativeModeTab registerCreativeTab(String name) {
        ResourceKey<CreativeModeTab> creativeTabKey = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(MOD_ID, name));
        CreativeModeTab.Builder builder = FabricCreativeModeTab.builder()
                .icon(() -> new ItemStack(BOOKCASES.get(0)))
                .title(Component.translatable("creativeTab." + name))
                .displayItems((_, output) -> {
                    BOOKCASES.forEach(block -> output.accept(block.asItem()));
                    TIERED_SHELVING.forEach(block -> output.accept(block.asItem()));
                });
        CreativeModeTab creativeTab = builder.build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, creativeTabKey, creativeTab);
        return creativeTab;
    }
}