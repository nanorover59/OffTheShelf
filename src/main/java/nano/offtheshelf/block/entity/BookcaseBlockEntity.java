package nano.offtheshelf.block.entity;

import nano.offtheshelf.OffTheShelf;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class BookcaseBlockEntity extends OffTheShelfBlockEntity  {
    public BookcaseBlockEntity(final BlockPos worldPosition, final BlockState blockState) {
        super(OffTheShelf.BOOKCASE_BLOCK_ENTITY, worldPosition, blockState);
    }

    @Override
    public int getInventorySize() {
        return 16;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean acceptsItemType(final ItemStack itemStack) {
        return itemStack.is(ItemTags.BOOKSHELF_BOOKS);
    }
}