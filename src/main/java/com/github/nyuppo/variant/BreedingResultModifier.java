package com.github.nyuppo.variant;

import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public record BreedingResultModifier(Identifier parent1, Identifier parent2, double breedingChance) implements VariantModifier {
    public boolean validParents(MobVariant parent1, MobVariant parent2) {
        return (parent1.getIdentifier().equals(this.parent1) && parent2.getIdentifier().equals(this.parent2))
                || (parent1.getIdentifier().equals(this.parent2) && parent2.getIdentifier().equals(this.parent1));
    }

    public boolean shouldBreed(RandomSource random) {
        return random.nextDouble() < this.breedingChance;
    }
}
