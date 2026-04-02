package com.github.nyuppo.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record ClientRequestVariantPayload(UUID uuid) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ClientRequestVariantPayload> TYPE = new CustomPacketPayload.Type<>(MMVNetworkingConstants.CLIENT_REQUEST_VARIANT_ID);
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientRequestVariantPayload> CODEC = CustomPacketPayload.codec(ClientRequestVariantPayload::write, ClientRequestVariantPayload::new);

	private ClientRequestVariantPayload(RegistryFriendlyByteBuf buf) {
		this(buf.readUUID());
	}

	private void write(RegistryFriendlyByteBuf buf) {
		buf.writeUUID(uuid);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
