package slimeknights.tconstruct.plugin.jei.casting;

import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.plugin.jei.TConstructJEIConstants;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

/** Category for {@link slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe} with {@link slimeknights.tconstruct.library.recipe.TinkerRecipes#castingBasinType} */
@Internal
public class CastingBasinCategory extends AbstractCastingCategory {
  private static final Component TITLE = TConstruct.makeTranslation("jei", "casting.basin");
  public CastingBasinCategory(IGuiHelper guiHelper) {
    super(guiHelper, TConstructJEIConstants.CASTING_BASIN, TITLE, TinkerSmeltery.searedBasin.get(), guiHelper.createDrawable(BACKGROUND_LOC, 117, 16, 16, 16));
  }
}
