package nano.offtheshelf.block.entity;

import nano.offtheshelf.OffTheShelf;
import nano.offtheshelf.block.ModularShelfBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Objects;

public class BookcaseBlockEntity extends OffTheShelfBlockEntity  {
    public int[] variants;
    public int[] colors;
    public boolean[] flatten;
    public boolean leftTopEmpty;
    public boolean leftBottomEmpty;
    public boolean rightTopEmpty;
    public boolean rightBottomEmpty;

    public BookcaseBlockEntity(final BlockPos worldPosition, final BlockState blockState) {
        super(OffTheShelf.BOOKCASE_BLOCK_ENTITY, worldPosition, blockState);
        this.variants = new int[this.getInventorySize()];
        this.colors = new int[this.getInventorySize()];
        this.flatten = new boolean[this.getInventorySize()];
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

    @Override
    public void setItem(final int slot, final ItemStack itemStack) {
        super.setItem(slot, itemStack);
        this.checkNeighborForSlot(slot);
    }

    @Override
    public ItemStack removeItem(final int slot, final int count) {
        ItemStack retrievedItem = Objects.requireNonNullElse(this.getItems().get(slot), ItemStack.EMPTY);
        this.getItems().set(slot, ItemStack.EMPTY);
        this.checkNeighborForSlot(slot);
        this.setChanged();
        return retrievedItem;
    }

    @Override
    protected void loadAdditional(final ValueInput input) {
        super.loadAdditional(input);
        this.leftTopEmpty = input.getBooleanOr("leftTopEmpty", false);
        this.leftBottomEmpty = input.getBooleanOr("leftBottomEmpty", false);
        this.rightTopEmpty = input.getBooleanOr("rightTopEmpty", false);
        this.rightBottomEmpty = input.getBooleanOr("rightBottomEmpty", false);
        this.updateBooks();
    }

    @Override
    protected void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("leftTopEmpty", this.leftTopEmpty);
        output.putBoolean("leftBottomEmpty", this.leftBottomEmpty);
        output.putBoolean("rightTopEmpty", this.rightTopEmpty);
        output.putBoolean("rightBottomEmpty", this.rightBottomEmpty);
    }

    /**
     * Update the variant and color arrays used for quick rendering of books.
     */
    public void updateBooks() {
        RandomSource random = new LegacyRandomSource(this.getBlockPos().asLong());

        for(int i = 0; i < this.getInventorySize(); i++) {
            ItemStack itemStack = this.getItems().get(i);
            int variant = random.nextInt(5);

            if(!itemStack.isEmpty()) {
                this.variants[i] = variant;
                this.colors[i] = DyedItemColor.getOrDefault(itemStack, -6265536);
            } else {
                this.variants[i] = -1;
                this.colors[i] = -1;
            }
        }

        this.flattenBooks();
    }

    /**
     * Choose what books will be rendered with only the outwards face showing.
     */
    public void flattenBooks() {
        int inventorySize = this.getBlockState().getValue(ModularShelfBlock.MODEL) == ModularShelfBlock.CENTER ? 16 : 14;

        this.flatten[0] = !this.leftTopEmpty && !this.getItem(2).isEmpty();
        this.flatten[1] = !this.leftBottomEmpty && !this.getItem(3).isEmpty();
        this.flatten[inventorySize - 2] =  !this.rightTopEmpty && !this.getItem(inventorySize - 4).isEmpty();
        this.flatten[inventorySize - 1] =  !this.rightBottomEmpty && !this.getItem(inventorySize - 3).isEmpty();

        for(int i = 2; i < inventorySize - 2; i++)
            this.flatten[i] = !this.getItem(i - 2).isEmpty() && !this.getItem(i + 2).isEmpty();
    }

    public void checkNeighborForSlot(int slot) {
        int inventorySize = this.getBlockState().getValue(ModularShelfBlock.MODEL) == ModularShelfBlock.CENTER ? 16 : 14;

        if(slot < 2)
            checkLeftNeighbor();
        else if(slot > inventorySize - 3)
            checkRightNeighbor();
    }

    public void checkLeftNeighbor() {
        BlockPos leftPos = this.getBlockPos().relative(this.getBlockState().getValue(ModularShelfBlock.FACING).getClockWise());

        if(level.getBlockEntity(leftPos) instanceof BookcaseBlockEntity leftBlockEntity) {
            int inventorySize = leftBlockEntity.getBlockState().getValue(ModularShelfBlock.MODEL) == ModularShelfBlock.CENTER ? 16 : 14;
            this.leftTopEmpty = leftBlockEntity.getItem(inventorySize - 2).isEmpty();
            this.leftBottomEmpty = leftBlockEntity.getItem(inventorySize - 1).isEmpty();
            leftBlockEntity.rightTopEmpty = this.getItem(0).isEmpty();
            leftBlockEntity.rightBottomEmpty = this.getItem(1).isEmpty();
            leftBlockEntity.setChanged();
        }
    }

    public void checkRightNeighbor() {
        BlockPos rightPos = this.getBlockPos().relative(this.getBlockState().getValue(ModularShelfBlock.FACING).getCounterClockWise());

        if(level.getBlockEntity(rightPos) instanceof BookcaseBlockEntity rightBlockEntity) {
            int inventorySize = this.getBlockState().getValue(ModularShelfBlock.MODEL) == ModularShelfBlock.CENTER ? 16 : 14;
            this.rightTopEmpty = rightBlockEntity.getItem(0).isEmpty();
            this.rightBottomEmpty = rightBlockEntity.getItem(1).isEmpty();
            rightBlockEntity.leftTopEmpty = this.getItem(inventorySize - 2).isEmpty();
            rightBlockEntity.leftBottomEmpty = this.getItem(inventorySize - 1).isEmpty();
            rightBlockEntity.setChanged();
        }
    }
}