package slimeknights.tconstruct.tables.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.BlockEntityPacket;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;

/**
 * Packet to send the current crafting recipe to a player who opens the tinker station.
 */
@Internal
public record UpdateTinkerStationRecipePacket(BlockPos pos, ResourceLocation recipe) implements ISimplePacket {
  public static final Type<UpdateTinkerStationRecipePacket> TYPE = new Type<>(TConstruct.getResource("update_tinker_station_recipe"));
  public static final StreamCodec<ByteBuf,UpdateTinkerStationRecipePacket> CODEC = StreamCodec.composite(
    BlockPos.STREAM_CODEC, UpdateTinkerStationRecipePacket::pos,
    ResourceLocation.STREAM_CODEC, UpdateTinkerStationRecipePacket::recipe,
    UpdateTinkerStationRecipePacket::new);

  public UpdateTinkerStationRecipePacket(BlockPos pos, RecipeHolder<ITinkerStationRecipe> recipe) {
    this(pos, recipe.id());
  }

  @Override
  public Type<UpdateTinkerStationRecipePacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    Player player = context.player();
    Level level = context.player().level();
    // start by fetching the recipe, no further work if it's missing
    RecipeHolder<ITinkerStationRecipe> recipe = level.getRecipeManager().byKeyTyped(TinkerRecipeTypes.TINKER_STATION.get(), this.recipe);
    if (recipe == null) {
      TConstruct.LOG.error("Failed to update Tinker Station Recipe at {}: unknown recipe {}", pos, recipe);
      return;
    }
    // if the menu is open, use that to get the TE
    if (player.containerMenu instanceof TinkerStationContainerMenu stationMenu) {
      TinkerStationBlockEntity te = stationMenu.getTile();
      if (te != null && te.getBlockPos().equals(pos)) {
        te.updateRecipe(recipe);
        // update the screen as we want to update the screen too
        UpdateStationScreenPacket.updateScreen();
        return;
      }
    }
    // if the wrong screen is open or no screen, use the tile directly
    if (BlockEntityPacket.getBlockEntity(level, pos, this) instanceof TinkerStationBlockEntity be) {
      be.updateRecipe(recipe);
    } else {
      TConstruct.LOG.error("Failed to update Tinker Station Recipe at {} to {}: unable to find Tinker Station", pos, recipe);
    }
  }
}
