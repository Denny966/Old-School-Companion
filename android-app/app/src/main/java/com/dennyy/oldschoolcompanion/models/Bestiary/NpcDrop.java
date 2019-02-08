package com.dennyy.oldschoolcompanion.models.Bestiary;

import com.dennyy.oldschoolcompanion.enums.DropRarity;
import com.dennyy.oldschoolcompanion.helpers.Utils;

public final class NpcDrop {
    public final String name;
    public final String nameNotes;
    public final String quantity;
    public final DropRarity rarity;
    public final String rarityNotes;

    public NpcDrop(Builder builder) {
        this.name = builder.name;
        this.nameNotes = builder.nameNotes;
        this.quantity = builder.quantity;
        this.rarity = builder.rarity;
        this.rarityNotes = builder.rarityNotes;
    }

    public static class Builder {
        private String name;
        private String nameNotes;
        private String quantity;
        private DropRarity rarity;
        private String rarityNotes;

        public Builder setName(String name) {
            if (Utils.isNullOrEmpty(this.name)) {
                this.name = name;
            }
            return this;
        }

        public Builder setNameNotes(String nameNotes) {
            this.nameNotes = nameNotes;
            return this;
        }

        public Builder setQuantity(String quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder setRarity(String rarity) {
            this.rarity = DropRarity.fromString(rarity);
            return this;
        }

        public Builder setRarityNotes(String rarityNotes) {
            this.rarityNotes = rarityNotes;
            return this;
        }

        Builder() {
            this.rarity = DropRarity.VERY_RARE;
            this.nameNotes = "";
        }

        public NpcDrop build() {
            return new NpcDrop(this);
        }
    }
}
