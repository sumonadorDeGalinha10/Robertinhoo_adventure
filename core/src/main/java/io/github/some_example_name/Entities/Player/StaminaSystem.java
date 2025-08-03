package io.github.some_example_name.Entities.Player;

public class StaminaSystem {
    private float currentStamina;
    private float maxStamina;
    private float normalRegenRate;
    private float exhaustedRegenRate;
    private boolean isExhausted;
    private float exhaustionRecoveryThreshold;
    private float lastConsumptionAmount = 0;

    public StaminaSystem(float maxStamina, float normalRegenRate, float exhaustedRegenRate, 
                        float exhaustionRecoveryThreshold) {
        this.maxStamina = maxStamina;
        this.currentStamina = maxStamina;
        this.normalRegenRate = normalRegenRate;
        this.exhaustedRegenRate = exhaustedRegenRate;
        this.exhaustionRecoveryThreshold = exhaustionRecoveryThreshold;
        this.isExhausted = false;
    }

    public void update(float deltaTime) {
        if (isExhausted) {
            // Regeneração mais lenta durante exaustão
            currentStamina += exhaustedRegenRate * deltaTime;
            
            // Sai da exaustão apenas quando atingir o limiar de recuperação
            if (currentStamina >= maxStamina * exhaustionRecoveryThreshold) {
                isExhausted = false;
            }
        } else {
            // Regeneração normal
            currentStamina += normalRegenRate * deltaTime;
        }
        
        // Garante que a stamina não ultrapasse o máximo
        currentStamina = Math.min(currentStamina, maxStamina);
            
        // // DEBUG: Mostrar estado atual
        // System.err.println("Stamina: " + currentStamina + "/" + maxStamina + 
        //                     " Exhausted: " + isExhausted);
    }

    public boolean consumeStamina(float amount) {
        System.err.println("Consuming stamina: " + amount);
        
        if (isExhausted) {
            return false;
        }
        boolean actionAllowed = true;
        lastConsumptionAmount = amount;
        
        if (currentStamina < amount) {
            currentStamina = 0;
            isExhausted = true;
        } else {
            // Consome normalmente
            currentStamina -= amount;
            
            // Entra em exaustão se ficar com menos de 5% de stamina
            if (currentStamina <= maxStamina * 0.001f) {
                isExhausted = true;
            }
        }
        
        return actionAllowed;
    }

    public float getCurrentStamina() {
        return currentStamina;
    }

    public float getMaxStamina() {
        return maxStamina;
    }

    public boolean isExhausted() {
        return isExhausted;
    }
    
    public boolean canPerformAction() {
        return !isExhausted;
    }

        public boolean hasStamina(float amount) {
        return !isExhausted && currentStamina >= amount;
    }
}