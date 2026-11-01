package slimeknights.tconstruct.plugin.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;
import slimeknights.mantle.plugin.jei.NamedRecipeCategory;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuelLookup;
import slimeknights.tconstruct.plugin.jei.melting.MeltingFuelHandler;
import slimeknights.tconstruct.plugin.jei.util.CategoryUtil;
import slimeknights.tconstruct.plugin.jei.util.FluidTooltipCallback;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import java.awt.Color;
import java.util.Arrays;

/** Category to display {@link AlloyRecipe}. */
@Internal
public class AlloyRecipeCategory extends NamedRecipeCategory<AlloyRecipe> {
  private static final ResourceLocation BACKGROUND_LOC = TConstruct.getResource("textures/gui/jei/alloy.png");
  private static final Component TITLE = TConstruct.makeTranslation("jei", "alloy.title");
  private static final Component CATALYST = TConstruct.makeTranslation("jei", "alloy.catalyst").withStyle(ChatFormatting.ITALIC);
  private static final String KEY_TEMPERATURE = TConstruct.makeTranslationKey("jei", "temperature");

  /** Tooltip for fluid inputs */
  private static final FluidTooltipCallback CATALYST_TOOLTIP = (fluid, slot, tooltip) -> {
    tooltip.add(CATALYST);
    FluidTooltipHandler.appendMaterial(fluid, tooltip);
  };

  /** Tooltip for fuel display */
  public static final FluidTooltipCallback FUEL_TOOLTIP = (fluid, slot, tooltip) -> {
    MeltingFuel fuel = MeltingFuelLookup.findFuel(fluid.getFluid());
    if (fuel != null) {
      tooltip.add(Component.translatable(KEY_TEMPERATURE, fuel.getTemperature()).withStyle(ChatFormatting.GRAY));
    }
  };

  private final IDrawable background;
  private final IDrawable arrow;
  private final IDrawable tank;

  public AlloyRecipeCategory(IGuiHelper helper) {
    super(TConstructJEIConstants.ALLOY, TITLE, helper.createDrawableItemLike(TinkerSmeltery.smelteryController), 172, 62);
    this.background = helper.createDrawable(BACKGROUND_LOC, 0, 0, 172, 62);
    this.arrow = helper.drawableBuilder(BACKGROUND_LOC, 172, 0, 24, 17).buildAnimated(200, StartDirection.LEFT, false);
    this.tank = helper.createDrawable(BACKGROUND_LOC, 172, 17, 16, 16);
  }

  @Override
  public void createRecipeExtras(IRecipeExtrasBuilder builder, NamedRecipe<AlloyRecipe> recipe, IFocusGroup focuses) {
    builder.addDrawableWidget(arrow).setPosition(90, 21);
    builder.addText(Component.translatable(KEY_TEMPERATURE, recipe.recipe().getTemperature()), 139, 9)
      .setPosition(33, 5)
      .setColor(Color.GRAY.getRGB())
      .setTextAlignment(HorizontalAlignment.CENTER);
  }

  @Override
  public void draw(NamedRecipe<AlloyRecipe> recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
    background.draw(graphics);
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, NamedRecipe<AlloyRecipe> holder, IFocusGroup focuses) {
    AlloyRecipe recipe = holder.recipe();
    // inputs
    int maxAmount = CategoryUtil.drawMultipleFluids(builder, ingredient -> ingredient.catalyst() ? RecipeIngredientRole.CATALYST : RecipeIngredientRole.INPUT,
                                       19, 11, 48, 32, recipe.getInputs(), recipe.getOutput().getAmount(),
                                       ingredient -> Arrays.asList(ingredient.fluid().getFluids()),
                                       ingredient -> ingredient.catalyst() ? CATALYST_TOOLTIP : FluidTooltipCallback.UNITS);

    // output
    builder.addOutputSlot(137, 11)
           .addRichTooltipCallback(FluidTooltipCallback.UNITS)
           .setFluidRenderer(maxAmount, false, 16, 32)
           .addIngredient(NeoForgeTypes.FLUID_STACK, recipe.getOutput());

    // fuel
    builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 94, 43)
           .addRichTooltipCallback(FUEL_TOOLTIP)
           .setFluidRenderer(1, false, 16, 16)
           .setOverlay(tank, 0, 0)
           .addIngredients(NeoForgeTypes.FLUID_STACK, MeltingFuelHandler.getUsableFuels(recipe.getTemperature()));
  }
}
