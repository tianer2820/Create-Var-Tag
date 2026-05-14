package com.github.tianer2820.createvartag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;

import net.createmod.catnip.data.Couple;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class ConversionWandItem extends Item {

    public ConversionWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        Map<Couple<Frequency>, Set<IRedstoneLinkable>> networks = Create.REDSTONE_LINK_NETWORK_HANDLER.networksIn((LevelAccessor) level);
        
        Set<Item> usedVanillaItems = new HashSet<>();
        Set<String> uniqueVarTagNames = new HashSet<>();
        List<LinkBehaviour> linkBehaviours = new ArrayList<>();

        // 1. Collect all link behaviours and used items
        for (Set<IRedstoneLinkable> linkables : networks.values()) {
            for (IRedstoneLinkable linkable : linkables) {
                if (linkable instanceof LinkBehaviour behaviour) {
                    linkBehaviours.add(behaviour);
                    
                    Couple<Frequency> networkKey = behaviour.getNetworkKey();
                    processFrequencyForCollection(networkKey.getFirst(), usedVanillaItems, uniqueVarTagNames);
                    processFrequencyForCollection(networkKey.getSecond(), usedVanillaItems, uniqueVarTagNames);
                }
            }
        }

        // 2. Assign unique vanilla items to each var tag string
        List<Item> allItems = new ArrayList<>();
        BuiltInRegistries.ITEM.forEach(allItems::add);
        Collections.shuffle(allItems);
        
        Map<String, Item> varTagToVanillaItem = new HashMap<>();
        int itemIndex = 0;
        
        for (String varTagName : uniqueVarTagNames) {
            Item assignedItem = null;
            while (itemIndex < allItems.size()) {
                Item potentialItem = allItems.get(itemIndex);
                itemIndex++;
                if (!usedVanillaItems.contains(potentialItem) && !(potentialItem instanceof VarTagItem) && !(potentialItem instanceof ConversionWandItem)) {
                    assignedItem = potentialItem;
                    break;
                }
            }
            
            if (assignedItem != null) {
                varTagToVanillaItem.put(varTagName, assignedItem);
                usedVanillaItems.add(assignedItem); // Mark as used
            } else {
                player.sendSystemMessage(Component.literal("Failed to convert all tags: Not enough unique vanilla items available!"));
                return InteractionResultHolder.fail(stack);
            }
        }

        // 3. Update LinkBehaviours
        int convertedCount = 0;
        for (LinkBehaviour behaviour : linkBehaviours) {
            boolean changed = false;
            Couple<Frequency> networkKey = behaviour.getNetworkKey();
            
            ItemStack newFirst = getNewStackForFrequency(networkKey.getFirst(), varTagToVanillaItem);
            if (newFirst != null) {
                behaviour.setFrequency(true, newFirst);
                changed = true;
            }
            
            ItemStack newSecond = getNewStackForFrequency(networkKey.getSecond(), varTagToVanillaItem);
            if (newSecond != null) {
                behaviour.setFrequency(false, newSecond);
                changed = true;
            }
            
            if (changed) {
                convertedCount++;
            }
        }

        player.sendSystemMessage(Component.literal(String.format("Converted %d Var Tags in %d Redstone Links to vanilla items.", uniqueVarTagNames.size(), convertedCount)));
        return InteractionResultHolder.success(stack);
    }

    private void processFrequencyForCollection(Frequency frequency, Set<Item> usedVanillaItems, Set<String> uniqueVarTagNames) {
        ItemStack freqStack = frequency.getStack();
        if (freqStack == null || freqStack.isEmpty()) {
            return;
        }
        
        if (freqStack.getItem() instanceof VarTagItem) {
            String name = freqStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("var_tag_name");
            if (name != null && !name.isEmpty()) {
                uniqueVarTagNames.add(name);
            }
        } else {
            usedVanillaItems.add(freqStack.getItem());
        }
    }

    private ItemStack getNewStackForFrequency(Frequency frequency, Map<String, Item> varTagToVanillaItem) {
        ItemStack freqStack = frequency.getStack();
        if (freqStack == null || freqStack.isEmpty()) {
            return null;
        }
        
        if (freqStack.getItem() instanceof VarTagItem) {
            String name = freqStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("var_tag_name");
            if (name != null && !name.isEmpty() && varTagToVanillaItem.containsKey(name)) {
                return new ItemStack(varTagToVanillaItem.get(name));
            }
        }
        return null;
    }
}
