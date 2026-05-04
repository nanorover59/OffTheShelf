package nano.offtheshelf.client.renderer.blockentity.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public class WallShelfRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState[] items = new ItemStackRenderState[3];
    public final int[] count = new int[3];
    public Direction direction = Direction.NORTH;
    public boolean bottom;
    public int highlight = -1;
    public Component name = null;
}