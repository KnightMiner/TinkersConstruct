package slimeknights.tconstruct.library.tools.stat;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.library.resource.ResourceId;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;
import java.util.function.IntFunction;

/**
 * Tool stat that keeps the largest integer value given
 */
@AllArgsConstructor
public class MaxToolStat implements IToolStat<Integer> {
  @Getter
  private final ResourceId<IToolStat<?>> id;
  private final int defaultValue;
  private final IntFunction<Component> displayName;
  @Nullable
  private final TagKey<Item> tag;

  public MaxToolStat(ResourceId<IToolStat<?>> id, int defaultValue, IntFunction<Component> displayName) {
    this(id, defaultValue, displayName, null);
  }

  @Override
  public Codec<Integer> codec() {
    return Codec.INT;
  }

  @Override
  public StreamCodec<ByteBuf, Integer> streamCodec() {
    return ByteBufCodecs.VAR_INT;
  }

  @Override
  public boolean supports(Item item) {
    return tag == null || RegistryHelper.contains(tag, item);
  }

  @Override
  public Integer getDefaultValue() {
    return defaultValue;
  }

  @Override
  public Integer clamp(Integer value) {
    return Mth.clamp(value, 0, Integer.MAX_VALUE);
  }

  @Override
  public TierBuilder makeBuilder() {
    return new TierBuilder();
  }

  /**
   * Sets the tier to the new tier, keeping the largest
   * @param builder  Builder
   * @param value    Value
   */
  @Override
  public void update(ModifierStatsBuilder builder, Integer value) {
    builder.<TierBuilder>updateStat(this, b -> b.tier = Math.max(b.tier, value));
  }

  @Override
  public Integer build(ModifierStatsBuilder parent, Object builder) {
    return ((TierBuilder)builder).tier;
  }

  @Override
  public Component formatValue(Integer number) {
    return Component.translatable(Util.makeTranslationKey("tool_stat", getId().id())).append(displayName.apply(number));
  }

  @Override
  public String toString() {
    return "TierToolStat{" + id + '}';
  }

  /** Internal int storage, basically just a int wrapper */
  protected static class TierBuilder {
    private int tier = 0;
  }
}
