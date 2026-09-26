package com.example.caloriecalculator;

/**
 * FoodItem
 * --------
 * One food in the app's built-in food list.
 * Calories are stored per 100 g (or per 100 ml for drinks) so any amount can be calculated:
 *
 *     calories = amount ÷ 100 × kcal per 100
 */
public class FoodItem {

    public final String name;
    public final String category;
    public final String unit;            // "g" or "ml"
    public final double kcalPer100;
    public final int servingAmount;      // typical serving size, used as the default amount
    public final String servingLabel;    // e.g. "1 plate", "1 piece"
    public final String[] modelLabels;   // names the AI food model uses for this food

    private FoodItem(String name, String category, String unit, double kcalPer100,
                     int servingAmount, String servingLabel, String[] modelLabels) {
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.kcalPer100 = kcalPer100;
        this.servingAmount = servingAmount;
        this.servingLabel = servingLabel;
        this.modelLabels = modelLabels;
    }

    /**
     * For foods where the reference gives calories per typical serving.
     * kcal per 100 = serving calories ÷ serving size × 100
     */
    public static FoodItem fromServing(String name, String category, String unit,
                                       int servingAmount, String servingLabel, int servingKcal,
                                       String... modelLabels) {
        double per100 = servingKcal * 100.0 / servingAmount;
        return new FoodItem(name, category, unit, per100, servingAmount, servingLabel, modelLabels);
    }

    /** For foods where the reference already gives calories per 100 g / 100 ml. */
    public static FoodItem fromPer100(String name, String category, String unit,
                                      double kcalPer100, int servingAmount, String servingLabel,
                                      String... modelLabels) {
        return new FoodItem(name, category, unit, kcalPer100, servingAmount, servingLabel, modelLabels);
    }

    /** Calories for the amount the user ate. */
    public int caloriesFor(double amount) {
        return (int) Math.round(amount / 100.0 * kcalPer100);
    }

    public int getRoundedPer100() {
        return (int) Math.round(kcalPer100);
    }

    /** True if the AI model's label refers to this food. */
    public boolean matchesModelLabel(String label) {
        for (String l : modelLabels) {
            if (l.equalsIgnoreCase(label)) {
                return true;
            }
        }
        return false;
    }
}
