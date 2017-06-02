package meltery.compat.jei;


import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.awt.*;
import java.util.List;

import javax.annotation.Nonnull;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.smeltery.MeltingRecipe;

public class SmeltingRecipeWrapper extends BlankRecipeWrapper {

    protected final List<ItemStack> inputs;
    protected final List<FluidStack> outputs;
    protected final int temperature;
    protected final List<FluidStack> fuels;

    public SmeltingRecipeWrapper(MeltingRecipe recipe) {
        this.inputs = recipe.input.getInputs();
        this.outputs = ImmutableList.of(recipe.getResult());
        this.temperature = recipe.getTemperature();

        ImmutableList.Builder<FluidStack> builder = ImmutableList.builder();
        for(FluidStack fs : TinkerRegistry.getSmelteryFuels()) {
            if(fs.getFluid().getTemperature(fs) >= temperature) {
                fs = fs.copy();
                fs.amount = 1000;
                builder.add(fs);
            }
        }
        fuels = builder.build();
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(ItemStack.class, inputs);
        ingredients.setOutputs(FluidStack.class, outputs);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String tmpStr = String.valueOf(temperature) + "\u00B0";
        int x = 80 - minecraft.fontRenderer.getStringWidth(tmpStr) / 2;
        minecraft.fontRenderer.drawString(tmpStr, x, 10, Color.gray.getRGB());
    }
}