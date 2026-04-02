package com.github.nyuppo.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerRespondVariantPayload(int entityId, String variant, boolean sittingOrMuddy, int muddyTimeout, String sheepHornColour) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ServerRespondVariantPayload> TYPE = new CustomPacketPayload.Type<>(MMVNetworkingConstants.SERVER_RESPOND_VARIANT_ID);
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerRespondVariantPayload> CODEC = CustomPacketPayload.codec(ServerRespondVariantPayload::write, ServerRespondVariantPayload::new);

	private ServerRespondVariantPayload(RegistryFriendlyByteBuf buf) {
		this(buf.readInt(), buf.readUtf(), buf.readBoolean(), buf.readInt(), buf.readUtf());
	}

	private void write(RegistryFriendlyByteBuf buf) {
		buf.writeInt(entityId);
		buf.writeUtf(variant);
		buf.writeBoolean(sittingOrMuddy);
		buf.writeInt(muddyTimeout);
		buf.writeUtf(sheepHornColour);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
