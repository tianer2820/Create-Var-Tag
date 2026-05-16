package com.github.tianer2820.createvartag.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.content.redstone.link.LinkRenderer;
import com.github.tianer2820.createvartag.VarTagItem;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;

@Mixin(LinkRenderer.class)
public abstract class LinkRendererMixin {

    @Inject(
        method = "tick",
        at = @At(value = "INVOKE", target = "showHoverTip"),
        remap = false
    )
    private static void onShowHoverTip(CallbackInfo ci, 
            @Local LinkBehaviour behaviour, 
            @Local(name = "first") boolean first, 
            @Local(name = "empty") boolean empty, 
            @Local List<MutableComponent> tip) {
        
        if (!empty) {
            ItemStack stack = behaviour.getNetworkKey().get(first).getStack();
            String varTagName = VarTagItem.getVarTagName(stack);
            if (!varTagName.isEmpty()) {
                // Add the var tag name to the tooltip at index 1 (between label and instructions)
                tip.add(1, Component.literal(varTagName).withStyle(ChatFormatting.GOLD));
            }
        }
    }
}
