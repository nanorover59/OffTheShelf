package nano.offtheshelf.block.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ListBackedContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Objects;

public abstract class OffTheShelfBlockEntity extends BlockEntity implements ListBackedContainer {
    public static int NORMAL = 0;
    public static int LOCKED = 1;
    public static int ADVENTURE = 2;
    private final NonNullList<ItemStack> items;
    private int[] cooldowns;
    private int mode;

    public OffTheShelfBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
        this.items = NonNullList.withSize(getInventorySize(), ItemStack.EMPTY);
        this.cooldowns = new int[this.getInventorySize()];
        this.mode = NORMAL;
    }

    public abstract int getInventorySize();

    @Override
    public void setItem(final int slot, final ItemStack itemStack) {
        if(this.acceptsItemType(itemStack))
            this.getItems().set(slot, itemStack);
        else if(itemStack.isEmpty())
            this.removeItem(slot, this.getMaxStackSize());

        this.setChanged();
    }

    @Override
    public ItemStack removeItem(final int slot, final int count) {
        ItemStack retrievedItem = Objects.requireNonNullElse(this.getItems().get(slot), ItemStack.EMPTY);
        this.getItems().set(slot, ItemStack.EMPTY);
        this.setChanged();
        return retrievedItem;
    }

    @Override
    public boolean canTakeItem(final Container into, final int slot, final ItemStack itemStack) {
        return into.hasAnyMatching(
                toItem -> toItem.isEmpty()
                        ? true
                        : ItemStack.isSameItemSameComponents(itemStack, toItem) && toItem.getCount() + itemStack.getCount() <= into.getMaxStackSize(toItem)
        );
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return this.mode;
    }

    public void setCooldown(int slot, int cooldown) {
        this.cooldowns[slot] = cooldown;
        this.setChanged();
        this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 1);
    }

    public void resetCooldowns() {
        this.cooldowns = new int[this.getInventorySize()];
    }

    public int getCooldown(int slot) {
        return this.cooldowns[slot];
    }

    public boolean tickCooldown() {
        boolean hasCooldown = false;

        for(int i = 0; i < this.getInventorySize(); i++) {
            if(this.cooldowns[i] > 0) {
                hasCooldown = true;
                this.cooldowns[i]--;

                if(this.cooldowns[i] == 0)
                    this.setChanged();
            }
        }

        return hasCooldown;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setChanged() {
        super.setChanged();

        if(level != null)
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider registries) {
        ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), LogUtils.getLogger());
        TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
        this.saveAdditional(output);
        CompoundTag compound = output.buildResult();
        return compound;
    }

    @Override
    protected void loadAdditional(final ValueInput input) {
        super.loadAdditional(input);
        this.items.clear();
        ContainerHelper.loadAllItems(input, this.items);
        this.mode = input.getIntOr("mode", NORMAL);
        this.cooldowns = input.getIntArray("cooldowns").orElse(new int[this.getInventorySize()]);
    }

    @Override
    protected void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items, true);
        output.putInt("mode", this.mode);
        output.putIntArray("cooldowns", this.cooldowns);
    }
}