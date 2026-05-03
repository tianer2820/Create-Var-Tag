package com.github.tianer2820.createvartag.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import com.github.tianer2820.createvartag.VarTagMod;

public record OpenVarTagScreenPacket(String initialName, List<String> nearbyVars) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenVarTagScreenPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(VarTagMod.MODID, "open_var_tag_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenVarTagScreenPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeUtf(packet.initialName());
                buf.writeCollection(packet.nearbyVars(), FriendlyByteBuf::writeUtf);
            },
            buf -> new OpenVarTagScreenPacket(
                    buf.readUtf(),
                    buf.readList(FriendlyByteBuf::readUtf)));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
