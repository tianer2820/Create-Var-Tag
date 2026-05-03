package com.github.tianer2820.createvartag.network;

import com.github.tianer2820.createvartag.VarTagMod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SetVarTagPacket(String tagName) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetVarTagPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(VarTagMod.MODID, "set_var_tag"));

    public static final StreamCodec<ByteBuf, SetVarTagPacket> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
            .map(SetVarTagPacket::new, SetVarTagPacket::tagName);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
