package slimeknights.tconstruct.library.modifiers.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.util.ModifierLevelDisplay;
import slimeknights.tconstruct.library.modifiers.util.ModifierTooltip;
import slimeknights.tconstruct.library.modifiers.util.ModifierTooltip.ShowInTooltips;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;

/**
 * Basic modifier, having a collection of hooks and the ability to set common modifier properties.
 * In most cases it's better to use {@link ComposableModifier},
 * however as sometimes its not feasible extract code to JSON this can be a good alternative for static modifiers.
 */
public class BasicModifier extends Modifier {
  protected final ModifierLevelDisplay levelDisplay;
  protected final ShowInTooltips showInTooltips;
  @Getter
  protected final int priority;

  public BasicModifier(ModuleHookMap hookMap, ModifierLevelDisplay levelDisplay, ShowInTooltips showInTooltips, int priority) {
    super(hookMap);
    this.levelDisplay = levelDisplay;
    this.showInTooltips = showInTooltips;
    this.priority = priority;
  }

  /**
   * This method is final to prevent overrides as the constructor no longer calls it
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  protected final void registerHooks(ModuleHookMap.Builder hookBuilder) {}

  @Override
  public Component getDisplayName(int level) {
    return levelDisplay.nameForLevel(this, level);
  }

  @Override
  public Component getDisplayName(IToolStackView tool, ModifierEntry entry, @Nullable RegistryAccess access) {
    return getHook(ModifierHooks.DISPLAY_NAME).getDisplayName(tool, entry, entry.getDisplayName(), access);
  }

  @Override
  public boolean shouldDisplay(ModifierTooltip context) {
    return showInTooltips.test(context);
  }


  /**
   * Builder to create simple static modifiers. Similar to {@link ComposableModifier.Builder}, except more efficient as we don't require it be JSON serializable.
   * Generally it's better to just use composable unless there is a good reason.
   */
  @Accessors(fluent = true)
  @Setter
  @RequiredArgsConstructor(staticName = "builder")
  public static class Builder {
    private final ModuleHookMap hookMap;

    /** Method of displaying levels in this modifier */
    private ModifierLevelDisplay levelDisplay = ModifierLevelDisplay.DEFAULT;
    /** Whether to show this modifier in tooltips */
    private ShowInTooltips showInTooltips = ShowInTooltips.ALWAYS;
    /** Priority level for this modifier */
    private int priority = DEFAULT_PRIORITY;

    /** Sets the tooltips this modifier shows in */
    public Builder showInTooltips(ShowInTooltips showInTooltips) {
      this.showInTooltips = showInTooltips;
      return this;
    }

    /** Sets the tooltips this modifier shows in */
    public Builder showInTooltips(ModifierTooltip... tooltips) {
      return showInTooltips(ShowInTooltips.match(tooltips));
    }

    /** Builds the final modifier */
    public BasicModifier build() {
      return new BasicModifier(hookMap, levelDisplay, showInTooltips, priority);
    }
  }
}
