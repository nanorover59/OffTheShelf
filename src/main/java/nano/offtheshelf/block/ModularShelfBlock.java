package nano.offtheshelf.block;

import nano.offtheshelf.block.entity.BookcaseBlockEntity;
import nano.offtheshelf.block.entity.OffTheShelfBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public abstract class ModularShelfBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty MODEL = IntegerProperty.create("model", 0, 3);
    public static final int SINGLE = 0;
    public static final int CENTER = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;

    public ModularShelfBlock(final BlockBehaviour.Properties properties) {
        super(properties);
    }

    public abstract VoxelShape getDirectionVoxelShape(BlockState state, Direction direction);

    @Override
    protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
        return this.getDirectionVoxelShape(state, state.getValue(FACING));
    }

    @Override
    protected boolean useShapeForLightOcclusion(final BlockState state) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(MODEL);
    }

    @Override
    protected InteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hitResult) {
        if(level.getBlockEntity(pos) instanceof OffTheShelfBlockEntity blockEntity) {
            int slot = this.getInteractionSlot(state, hitResult);

            if(slot < 0 || blockEntity.getMode() == OffTheShelfBlockEntity.LOCKED)
                return InteractionResult.PASS;

            ItemStack previousStack = blockEntity.getItem(slot);
            boolean same = ItemStack.isSameItem(stack, previousStack) && ItemStack.isSameItemSameComponents(stack, previousStack);

            if((!blockEntity.canPlaceItem(slot, stack) || stack.isEmpty()) && previousStack.isEmpty())
                return InteractionResult.PASS;

            if((!blockEntity.canPlaceItem(slot, stack) && !same) || blockEntity.getMode() == OffTheShelfBlockEntity.ADVENTURE)
                return InteractionResult.TRY_WITH_EMPTY_HAND;

            if(!level.isClientSide()) {
                int maxCount = Math.min(this.getMaxCount(), stack.getMaxStackSize());

                if(same && maxCount > 1 && maxCount - previousStack.count() > 0) {
                    int transfer = Math.min(stack.getCount(), maxCount - previousStack.count());
                    stack.consume(transfer, player);
                    previousStack.setCount(previousStack.count() + transfer);
                    blockEntity.setItem(slot, previousStack);
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                    level.playSound(null, pos, this.getSoundPlaceItem(previousStack), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                else if(previousStack.isEmpty()) {
                    blockEntity.setItem(slot, stack.consumeAndReturn(Math.min(stack.getCount(), maxCount), player));
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                    level.playSound(null, pos, this.getSoundPlaceItem(stack), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos, final Player player, final BlockHitResult hitResult) {
        if(level.getBlockEntity(pos) instanceof OffTheShelfBlockEntity blockEntity) {
            int slot = this.getInteractionSlot(state, hitResult);
            boolean locked = blockEntity.getMode() == OffTheShelfBlockEntity.LOCKED;
            boolean adventureCooldown = blockEntity.getMode() == OffTheShelfBlockEntity.ADVENTURE && blockEntity.getCooldown(slot) > 0;

            if(slot < 0 || blockEntity.getItem(slot).isEmpty() || locked || adventureCooldown)
                return InteractionResult.PASS;
            else {
                ItemStack retrieved = blockEntity.getItem(slot).copy();

                if(blockEntity.getMode() == OffTheShelfBlockEntity.ADVENTURE)
                    blockEntity.setCooldown(slot, 20);
                else
                    blockEntity.removeItem(slot, blockEntity.getItem(slot).getCount());

                if(!player.getInventory().add(retrieved))
                    player.drop(retrieved, false);

                player.getInventory().setChanged();
                level.playSound(null, pos, this.getSoundTakeItem(retrieved), SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    public SoundEvent getSoundPlaceItem(ItemStack stack) {
        return SoundEvents.SHELF_PLACE_ITEM;
    }

    public SoundEvent getSoundTakeItem(ItemStack stack) {
        return SoundEvents.SHELF_TAKE_ITEM;
    }

    public int getMaxCount() {
        return 64;
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(final BlockState state, final Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void onPlace(final BlockState state, final Level level, final BlockPos pos, final BlockState oldState, final boolean movedByPiston) {
        updateConnection(level, state, pos);
    }

    @Override
    protected void affectNeighborsAfterRemoval(final BlockState state, final ServerLevel level, final BlockPos pos, final boolean movedByPiston) {
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }

    @Override
    protected void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block block, @Nullable final Orientation orientation, final boolean movedByPiston) {
        updateConnection(level, state, pos);
    }

    private void updateConnection(Level level, BlockState state, BlockPos pos) {
        if(!state.hasProperty(MODEL))
            return;

        Direction direction = state.getValue(FACING);
        BlockPos leftPos = switch (direction) {
            case NORTH -> pos.west();
            case EAST -> pos.north();
            case SOUTH -> pos.east();
            case WEST -> pos.south();
            default -> pos;
        };
        BlockPos rightPos = switch (direction) {
            case NORTH -> pos.east();
            case EAST -> pos.south();
            case SOUTH -> pos.west();
            case WEST -> pos.north();
            default -> pos;
        };
        BlockState leftState = level.getBlockState(leftPos);
        BlockState rightState = level.getBlockState(rightPos);
        int model = SINGLE;

        if(leftState.is(state.getBlock()) && rightState.is(state.getBlock()))
            model = CENTER;
        else if(leftState.is(state.getBlock()))
            model = LEFT;
        else if(rightState.is(state.getBlock()))
            model = RIGHT;

        // Drop the last two stacks if changing from 16 slots to 14.
        if(model != CENTER && level.getBlockEntity(pos) instanceof BookcaseBlockEntity blockEntity) {
            ItemStack stack14 = blockEntity.removeItem(14, 1);
            ItemStack stack15 = blockEntity.removeItem(15, 1);
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack14);
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack15);
        }

        level.setBlockAndUpdate(pos, state.setValue(MODEL, model));
    }

    public abstract int getInteractionSlot(BlockState state, BlockHitResult hitResult);

    public static Optional<Vec2> getInteractionVec2(BlockHitResult hitResult) {
        Direction hitDirection = hitResult.getDirection();
        BlockPos hitBlockPos = hitResult.getBlockPos().relative(hitDirection);
        Vec3 relativeHit = hitResult.getLocation().subtract(hitBlockPos.getX(), hitBlockPos.getY(), hitBlockPos.getZ());
        float rx = (float) relativeHit.x();
        float ry = (float) relativeHit.y();
        float rz = (float) relativeHit.z();
        return switch (hitDirection) {
            case NORTH -> Optional.of(new Vec2(1.0f - rx, 1.0f - ry));
            case EAST -> Optional.of(new Vec2(1.0f - rz, 1.0f - ry));
            case SOUTH -> Optional.of(new Vec2(rx, 1.0f - ry));
            case WEST -> Optional.of(new Vec2(rz, 1.0f - ry));
            default -> Optional.empty();
        };
    }
}