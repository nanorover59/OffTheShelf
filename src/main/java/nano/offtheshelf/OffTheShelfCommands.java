package nano.offtheshelf;

import nano.offtheshelf.block.entity.OffTheShelfBlockEntity;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ListBackedContainer;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OffTheShelfCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, buildContext, selection) ->
                dispatcher.register(Commands.literal("shelf").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("from", BlockPosArgument.blockPos())
                                .then(Commands.argument("to", BlockPosArgument.blockPos())
                                        .then(Commands.literal("mode")
                                                .then(Commands.literal("normal").executes(context ->
                                                        setMode(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to"), OffTheShelfBlockEntity.NORMAL)))
                                                .then(Commands.literal("locked").executes(context ->
                                                        setMode(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to"), OffTheShelfBlockEntity.LOCKED)))
                                                .then(Commands.literal("adventure").executes(context ->
                                                        setMode(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to"), OffTheShelfBlockEntity.ADVENTURE))))
                                        .then(Commands.literal("loot")
                                                .then(Commands.argument("loot_table", ResourceOrIdArgument.lootTable(buildContext)).executes(context ->
                                                        loot(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to"), ResourceOrIdArgument.getLootTable(context, "loot_table")))))
                                        .then(Commands.literal("scatter")
                                                .then(Commands.argument("source", BlockPosArgument.blockPos()).executes(context ->
                                                        scatter(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to"), BlockPosArgument.getLoadedBlockPos(context, "source")))))
                                        .then(Commands.literal("clear").executes(context ->
                                                clear(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to")))))))));

    }

    public static int setMode(CommandSourceStack source, BlockPos fromPos, BlockPos toPos, int mode) {
        ServerLevel level = source.getLevel();
        int count = 0;

        for(BlockPos pos : BlockPos.betweenClosed(fromPos, toPos)) {
            if(level.getBlockEntity(pos) instanceof OffTheShelfBlockEntity blockEntity) {
                blockEntity.setMode(mode);
                blockEntity.resetCooldowns();
                blockEntity.setChanged();
                count++;
            }
        }

        int finalCount = count;
        String modeString = switch(mode) {
            case 1 -> "Locked";
            case 2 -> "Adventure";
            default -> "Normal";
        };
        source.sendSuccess(() -> Component.translatable("commands.shelf.mode", finalCount, modeString), false);
        return 1;
    }

    public static int loot(CommandSourceStack source, BlockPos fromPos, BlockPos toPos,  Holder<LootTable> lootTable) {
        ServerLevel level = source.getLevel();
        int count = 0;

        for(BlockPos pos : BlockPos.betweenClosed(fromPos, toPos)) {
            if(level.getBlockEntity(pos) instanceof OffTheShelfBlockEntity blockEntity) {
                LootParams lootParams = new LootParams.Builder(source.getLevel())
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, source.getEntity())
                        .withParameter(LootContextParams.ORIGIN, source.getPosition())
                        .create(LootContextParamSets.COMMAND);
                List<ItemStack> loot = lootTable.value().getRandomItems(lootParams);
                List<Integer> slots = IntStream.range(0, blockEntity.getItems().size()).boxed().collect(Collectors.toList());
                Collections.shuffle(slots);

                for(int slot : slots) {
                    if(loot.isEmpty())
                        break;

                    ItemStack stack = loot.getLast();

                    if(blockEntity.getItem(slot).isEmpty() && blockEntity.canPlaceItem(slot, stack)) {
                        blockEntity.setItem(slot, stack);
                        loot.removeLast();
                    }
                }

                blockEntity.resetCooldowns();
                blockEntity.setChanged();
                count++;
            }
        }

        int finalCount = count;
        source.sendSuccess(() -> Component.translatable("commands.shelf.loot", finalCount), false);
        return 1;
    }

    public static int scatter(CommandSourceStack source, BlockPos fromPos, BlockPos toPos,  BlockPos sourcePos) {
        ServerLevel level = source.getLevel();
        List<ItemStack> toScatter = new ArrayList<>();

        if(level.getBlockEntity(sourcePos) instanceof Container sourceContainer) {
            sourceContainer.iterator().forEachRemaining(stack -> {
                if(!stack.isEmpty())
                    toScatter.add(stack);
            });
        } else if(level.getBlockEntity(sourcePos) instanceof ListBackedContainer sourceContainer) {
            for(ItemStack stack : sourceContainer.getItems()) {
                if(!stack.isEmpty())
                    toScatter.add(stack);
            }
        }

        if(toScatter.isEmpty())
            return 0;

        List<BlockPos> posList = new ArrayList<>();

        for(BlockPos pos : BlockPos.betweenClosed(fromPos, toPos)) {
            if(level.getBlockEntity(pos) instanceof OffTheShelfBlockEntity)
                posList.add(new BlockPos(pos));
        }

        if(posList.isEmpty())
            return 0;

        Collections.shuffle(posList);
        List<Tuple<BlockPos, Integer>> slotList = new ArrayList<>();
        int slotCount = Math.min(posList.size(), toScatter.size());

        for(int i = 0; i < slotCount; i++) {
            if(level.getBlockEntity(posList.get(i)) instanceof OffTheShelfBlockEntity shelfBlockEntity) {
                for(int j = 0; j < shelfBlockEntity.getInventorySize(); j++)
                    slotList.add(new Tuple<>(posList.get(i), j));
            }
        }

        Collections.shuffle(slotList);

        for(Tuple<BlockPos, Integer> slot : slotList) {
            if(toScatter.isEmpty())
                break;

            ItemStack stack = toScatter.removeLast();

            if(level.getBlockEntity(slot.getA()) instanceof OffTheShelfBlockEntity shelfBlockEntity) {
                shelfBlockEntity.setItem(slot.getB(), stack);
                shelfBlockEntity.setChanged();
            }
        }

        return 1;
    }

    public static int clear(CommandSourceStack source, BlockPos fromPos, BlockPos toPos) {
        ServerLevel level = source.getLevel();
        int count = 0;

        for(BlockPos pos : BlockPos.betweenClosed(fromPos, toPos)) {
            if(level.getBlockEntity(pos) instanceof OffTheShelfBlockEntity blockEntity) {
                blockEntity.getItems().clear();
                blockEntity.resetCooldowns();
                blockEntity.setChanged();
                count++;
            }
        }

        int finalCount = count;
        source.sendSuccess(() -> Component.translatable("commands.shelf.clear", finalCount), false);
        return 1;
    }
}