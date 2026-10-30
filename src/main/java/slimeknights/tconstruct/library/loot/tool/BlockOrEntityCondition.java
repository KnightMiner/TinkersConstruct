package slimeknights.tconstruct.library.loot.tool;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import slimeknights.tconstruct.library.loot.TinkerLoot;

/** Condition checking that the entity has a block or entity parameter */
public class BlockOrEntityCondition implements LootItemCondition {
  public static final BlockOrEntityCondition INSTANCE = new BlockOrEntityCondition();
  public static final MapCodec<BlockOrEntityCondition> CODEC = MapCodec.unit(INSTANCE);

  private BlockOrEntityCondition() {}

  @Override
  public LootItemConditionType getType() {
    return TinkerLoot.blockOrEntityCondition.value();
  }

  @Override
  public boolean test(LootContext lootContext) {
    return lootContext.hasParam(LootContextParams.THIS_ENTITY) || lootContext.hasParam(LootContextParams.BLOCK_STATE);
  }
}
