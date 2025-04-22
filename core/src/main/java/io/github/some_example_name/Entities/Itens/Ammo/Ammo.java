package io.github.some_example_name.Entities.Itens.Ammo;

import com.badlogic.gdx.graphics.g2d.TextureRegion;



public abstract class Ammo {
    private String caliber; // Ex: "9mm", "12g", ".44 Magnum"
    private int quantity;
    private int maxStack;
    private TextureRegion icon;

    public Ammo(String caliber, int maxStack, TextureRegion icon) {
        this.caliber = caliber;
        this.maxStack = maxStack;
        this.icon = icon;
        this.quantity = 1;
    }

    // Métodos comuns
    public boolean canMerge(Ammo other) {
        return this.caliber.equals(other.caliber) && this.quantity < this.maxStack;
    }

    public void merge(Ammo other) {
        if(canMerge(other)) {
            int total = this.quantity + other.quantity;
            this.quantity = Math.min(total, maxStack);
        }
    }

    public void use(int amount) {
        quantity = Math.max(0, quantity - amount);
    }

    // Getters
    public String getCaliber() { return caliber; }
    public int getQuantity() { return quantity; }
    public TextureRegion getIcon() { return icon; }
}