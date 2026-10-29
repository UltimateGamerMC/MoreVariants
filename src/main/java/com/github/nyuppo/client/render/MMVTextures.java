package com.github.nyuppo.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public final class MMVTextures {
	private static final Map<Identifier, Boolean> EXISTS = new ConcurrentHashMap<>();

	private MMVTextures() {
	}

	/**
	 * Babies have their own models and texture layouts since 26.1, so an adult skin can't be drawn on them.
	 * Returns the skin to use: the adult one, its "_baby" twin, or null when no baby skin ships for it
	 * (e.g. a datapack variant) so vanilla's own baby texture is kept instead of a scrambled one.
	 */
	@Nullable
	public static Identifier forAge(Identifier adult, boolean baby) {
		if (!baby) return adult;
		String path = adult.getPath();
		Identifier babyTex = Identifier.fromNamespaceAndPath(adult.getNamespace(), path.substring(0, path.length() - ".png".length()) + "_baby.png");
		return EXISTS.computeIfAbsent(babyTex, id -> Minecraft.getInstance().getResourceManager().getResource(id).isPresent()) ? babyTex : null;
	}
}
