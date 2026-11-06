package slimeknights.tconstruct.common.data;

import net.minecraft.advancements.AdvancementRequirements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Implements {@link AdvancementRequirements.Strategy} for the given counts of each group. */
public class CountRequirementsStrategy implements AdvancementRequirements.Strategy {
  private final int[] sizes;
  public CountRequirementsStrategy(int... sizes) {
    this.sizes = sizes;
  }

  @Override
  public AdvancementRequirements create(Collection<String> strings) {
    List<List<String>> requirements = new ArrayList<>(sizes.length);
    List<String> list = new ArrayList<>(strings);
    int nextIndex = 0;
    for (int size : sizes) {
      List<String> sub = new ArrayList<>(size);
      for (int j = 0; j < size; j++) {
        sub.add(list.get(nextIndex));
        nextIndex++;
      }
      requirements.add(sub);
    }
    return new AdvancementRequirements(requirements);
  }
}
