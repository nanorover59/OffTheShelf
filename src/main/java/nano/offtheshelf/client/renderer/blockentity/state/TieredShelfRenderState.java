package nano.offtheshelf.client.renderer.blockentity.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public class TieredShelfRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState[] items = new ItemStackRenderState[6];
    public final int[] count = new int[6];
    public Direction direction = Direction.NORTH;
    public int highlight = -1;
    public Component name = null;
}