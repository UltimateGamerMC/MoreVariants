package com.github.nyuppo.util;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.ChickenEntity;

public class MMVTrackedData {
    public static final TrackedData<String> CHICKEN_VARIANT_ID = DataTracker.registerData(ChickenEntity.class, TrackedDataHandlerRegistry.STRING);
}
