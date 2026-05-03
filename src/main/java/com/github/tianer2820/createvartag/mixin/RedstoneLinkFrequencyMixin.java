package com.github.tianer2820.createvartag.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(targets = "com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler$Frequency")
public abstract class RedstoneLinkFrequencyMixin {

    @Shadow
    @Final
    private ItemStack stack;

    /**
     * Redirects or overrides the equality logic.
     * If both items are Paper, compare their names instead of their item types.
     */
    @Inject(method = "equals", at = @At("HEAD"), cancellable = true)
    private void onEquals(Object obj, CallbackInfoReturnable<Boolean> cir) {
        // Access the 'stack' field of the other Frequency object
        if (obj instanceof RedstoneLinkNetworkHandler.Frequency other) {
            ItemStack otherStack = other.getStack();
            if (this.stack.getItem() instanceof com.github.tianer2820.createvartag.VarTagItem
                    && otherStack.getItem() instanceof com.github.tianer2820.createvartag.VarTagItem) {
                String thisName = this.stack
                        .getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                                net.minecraft.world.item.component.CustomData.EMPTY)
                        .copyTag().getString("var_tag_name");
                String otherName = otherStack
                        .getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                                net.minecraft.world.item.component.CustomData.EMPTY)
                        .copyTag().getString("var_tag_name");
                cir.setReturnValue(thisName.equals(otherName));
            }
        }
    }

    /**
     * Ensures that two pieces of Paper with the same name result in the same Hash.
     */
    @Inject(method = "hashCode", at = @At("HEAD"), cancellable = true)
    private void onHashCode(CallbackInfoReturnable<Integer> cir) {
        if (this.stack.getItem() instanceof com.github.tianer2820.createvartag.VarTagItem) {
            // Hash the custom string instead of the Item object
            String name = this.stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getString("var_tag_name");
            cir.setReturnValue(name.hashCode());
        }
    }
}
