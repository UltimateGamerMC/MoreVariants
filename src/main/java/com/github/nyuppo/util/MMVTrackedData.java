package com.github.nyuppo.util;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.animal.chicken.Chicken;

public class MMVTrackedData {
	public static final EntityDataAccessor<String> CHICKEN_VARIANT_ID = SynchedEntityData.defineId(Chicken.class, EntityDataSerializers.STRING);
}
