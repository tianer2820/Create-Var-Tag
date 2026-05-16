package com.github.tianer2820.createvartag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;

import net.createmod.catnip.data.Couple;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class VarTagItem extends Item {

    public VarTagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            String initialName = getVarTagName(stack);

            // get nearby variables and sort by distance
            Map<Couple<Frequency>, Set<IRedstoneLinkable>> networks = Create.REDSTONE_LINK_NETWORK_HANDLER
                    .networksIn(level);

            Map<String, Float> varNameDistance = new HashMap<>();

            for (var network : networks.entrySet()) {
                Couple<Frequency> frequency = network.getKey();
                String firstVarName = getVarTagName(frequency.getFirst().getStack());
                String secondVarName = getVarTagName(frequency.getSecond().getStack());

                double distance = network.getValue().stream()
                        .map(linkable -> linkable.getLocation().distToCenterSqr(player.position())).min(Double::compare)
                        .orElse(Double.MAX_VALUE);
                if (firstVarName != null) {
                    if (varNameDistance.containsKey(firstVarName)) {
                        varNameDistance.put(firstVarName,
                                Math.min(varNameDistance.get(firstVarName), (float) distance));
                    } else {
                        varNameDistance.put(firstVarName, (float) distance);
                    }
                }
                if (secondVarName != null) {
                    if (varNameDistance.containsKey(secondVarName)) {
                        varNameDistance.put(secondVarName,
                                Math.min(varNameDistance.get(secondVarName), (float) distance));
                    } else {
                        varNameDistance.put(secondVarName, (float) distance);
                    }
                }
            }

            List<String> nearbyVars = varNameDistance.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .filter(name -> !name.isEmpty())
                    .limit(128)
                    .toList();

            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                    (net.minecraft.server.level.ServerPlayer) player,
                    new com.github.tianer2820.createvartag.network.OpenVarTagScreenPacket(initialName, nearbyVars));
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag) {

        tooltipComponents.add(Component.translatable("item.create_var_tag.var_tag.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        String customName = getVarTagName(stack);
        if (customName.isEmpty()) {
            customName = "Unnamed";
        }
        return Component.translatable("item.create_var_tag.var_tag", customName);
    }

    public static String getVarTagName(ItemStack stack) {
        if (stack == null) {
            return "";
        }
        if (!stack.is(VarTagMod.VAR_TAG_ITEM.get())) {
            return "";
        }
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getString("var_tag_name");
    }

    public static void openScreenClientSide(String initialName, List<String> nearbyVars) {
        ClientMenuHelper.openScreen(initialName, nearbyVars);
    }

    private static class ClientMenuHelper {
        public static void openScreen(String initialName, List<String> nearbyVars) {
            net.minecraft.client.Minecraft.getInstance().setScreen(new VarTagScreen(initialName, nearbyVars));
        }
    }
}
