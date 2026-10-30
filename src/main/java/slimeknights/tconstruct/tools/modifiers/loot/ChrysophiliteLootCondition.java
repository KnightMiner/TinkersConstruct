package slimeknights.tconstruct.tools.modifiers.loot;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.tconstruct.library.TinkerAttributes;
import slimeknights.tconstruct.library.loot.TinkerLoot;

import java.util.Set;

/** Condition to check if the enemy has the chrysophilite modifier */
@Internal
public class ChrysophiliteLootCondition implements LootItemCondition {
  public static final ChrysophiliteLootCondition INSTANCE = new ChrysophiliteLootCondition();
  public static final MapCodec<ChrysophiliteLootCondition> CODEC = MapCodec.unit(INSTANCE);

  private ChrysophiliteLootCondition() {}

  @Override
  public boolean test(LootContext context) {
    return context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity living && living.getAttributeValue(TinkerAttributes.CHRYSOPHILITE) >= 1;
  }

  @Override
  public Set<LootContextParam<?>> getReferencedContextParams() {
    return ImmutableSet.of(LootContextParams.THIS_ENTITY);
  }

  @Override
  public LootItemConditionType getType() {
    return TinkerLoot.chrysophiliteLootCondition.value();
  }
}
