package slimeknights.tconstruct.tables.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.BlockEntityPacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;

/**
 * Packet to send the current crafting recipe to a player who opens the crafting station.
 */
@Internal
public record UpdateCraftingRecipePacket(BlockPos pos, ResourceLocation recipe) implements BlockEntityPacket<CraftingStationBlockEntity> {
  public static final Type<UpdateCraftingRecipePacket> TYPE = new Type<>(TConstruct.getResource("update_crafting_recipe"));
  public static final StreamCodec<ByteBuf,UpdateCraftingRecipePacket> CODEC = StreamCodec.composite(
    BlockPos.STREAM_CODEC, UpdateCraftingRecipePacket::pos,
    ResourceLocation.STREAM_CODEC, UpdateCraftingRecipePacket::recipe,
    UpdateCraftingRecipePacket::new);

  public UpdateCraftingRecipePacket(BlockPos pos, RecipeHolder<CraftingRecipe> recipe) {
    this(pos, recipe.id());
  }

  @Override
  public Type<UpdateCraftingRecipePacket> type() {
    return TYPE;
  }

  @Override
  public Class<CraftingStationBlockEntity> blockEntityType() {
    return CraftingStationBlockEntity.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, CraftingStationBlockEntity be) {
    Level level = context.player().level();
    RecipeHolder<CraftingRecipe> recipe = level.getRecipeManager().byKeyTyped(RecipeType.CRAFTING, this.recipe);
    if (recipe != null) {
      be.updateRecipe(recipe);
    } else {
      TConstruct.LOG.error("Failed to update Crafting Station Recipe at {}: unknown recipe {}", pos, this.recipe);
    }
  }
}
