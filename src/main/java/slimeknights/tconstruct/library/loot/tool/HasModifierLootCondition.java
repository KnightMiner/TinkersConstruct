package slimeknights.tconstruct.library.loot.tool;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.loot.TinkerLoot;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;

/** Condition to check if a held tool has the given modifier */
public record HasModifierLootCondition(ModifierId modifier) implements LootItemCondition {
  public static final MapCodec<HasModifierLootCondition> CODEC = ModifierId.CODEC.fieldOf("modifier").xmap(HasModifierLootCondition::new, HasModifierLootCondition::modifier);

  @Override
  public LootItemConditionType getType() {
    return TinkerLoot.hasModifierCondition.value();
  }

  @Override
  public boolean test(LootContext context) {
    ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
    return tool != null && tool.is(TinkerTags.Items.MODIFIABLE) && ModifierUtil.getModifierLevel(tool, modifier) > 0;
  }
}
