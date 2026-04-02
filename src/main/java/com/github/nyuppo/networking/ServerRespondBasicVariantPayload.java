package com.github.nyuppo.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerRespondBasicVariantPayload(int entityId, String variantId) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ServerRespondBasicVariantPayload> TYPE = new CustomPacketPayload.Type<>(MMVNetworkingConstants.SERVER_RESPOND_BASIC_VARIANT_ID);
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerRespondBasicVariantPayload> CODEC = CustomPacketPayload.codec(ServerRespondBasicVariantPayload::write, ServerRespondBasicVariantPayload::new);

	private ServerRespondBasicVariantPayload(RegistryFriendlyByteBuf buf) {
		this(buf.readInt(), buf.readUtf());
	}

	private void write(RegistryFriendlyByteBuf buf) {
		buf.writeInt(entityId);
		buf.writeUtf(variantId);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
