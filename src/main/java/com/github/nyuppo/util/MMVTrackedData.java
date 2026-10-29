package com.github.nyuppo.util;

import net.minecraft.network.syncher.EntityDataAccessor;

public class MMVTrackedData {
	/**
	 * Assigned from ChickenVariantsMixin's static initialiser, i.e. while Chicken itself is class-initialising.
	 * Defining it lazily from here let the id be handed out after a chicken's data builder was already sized,
	 * crashing servers on the first chicken spawn ("Data value id is too big").
	 */
	public static EntityDataAccessor<String> CHICKEN_VARIANT_ID;
}
