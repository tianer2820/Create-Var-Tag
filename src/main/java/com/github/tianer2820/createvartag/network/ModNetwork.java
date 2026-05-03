package com.github.tianer2820.createvartag.network;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.github.tianer2820.createvartag.VarTagItem;
import com.github.tianer2820.createvartag.VarTagMod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = VarTagMod.MODID)
public class ModNetwork {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0");
        registrar.playToServer(
                SetVarTagPacket.TYPE,
                SetVarTagPacket.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        Player player = context.player();
                        if (player != null) {
                            ItemStack stack = player.getMainHandItem();
                            if (stack.getItem() instanceof VarTagItem) {
                                CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA,
                                        CustomData.EMPTY);
                                CompoundTag tag = customData.copyTag();
                                tag.putString("var_tag_name", payload.tagName());
                                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                            }
                        }
                    });
                });

        registrar.playToClient(
                OpenVarTagScreenPacket.TYPE,
                OpenVarTagScreenPacket.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        com.github.tianer2820.createvartag.VarTagItem.openScreenClientSide(payload.initialName(),
                                payload.nearbyVars());
                    });
                });
    }
}
