package slimeknights.tconstruct.library.tools.stat.impl;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.library.resource.ResourceId;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.IToolStat;

import javax.annotation.Nullable;

/**
 * Same as {@link FloatToolStat} but displays stat values as integers. Used for stats that are always read as ints.
 * @see slimeknights.tconstruct.library.tools.nbt.StatsNBT#getInt(IToolStat)
 */
public class IntegerToolStat extends FloatToolStat {
  public IntegerToolStat(ResourceId<IToolStat<?>> id, int color, float defaultValue, float minValue, float maxValue, IJsonPredicate<Item> items) {
    super(id, color, defaultValue, minValue, maxValue, items);
  }

  public IntegerToolStat(ResourceId<IToolStat<?>> id, int color, float defaultValue, float minValue, float maxValue, @Nullable TagKey<Item> tag) {
    super(id, color, defaultValue, minValue, maxValue, tag);
  }

  public IntegerToolStat(ResourceId<IToolStat<?>> id, int color, float defaultValue, float minValue, float maxValue) {
    super(id, color, defaultValue, minValue, maxValue);
  }

  /** Formats the integer value */
  public Component formatValue(int value) {
    return IToolStat.formatNumber(getTranslationKey(), getColor(), value);
  }

  @Override
  public Component formatValue(Float value) {
    return formatValue(value.intValue());
  }

  @Override
  public Component formatValue(float value) {
    return formatValue((int) value);
  }
}
