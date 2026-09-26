package com.example.caloriecalculator;

/**
 * CalorieCalculator
 * -----------------
 * Contains all the calculation logic for the app, kept separate from the
 * user interface so it is easy to read, test and explain.
 *
 * Formulas used:
 *  - BMR  : Mifflin-St Jeor equation
 *           Male   = (10 x weight) + (6.25 x height) - (5 x age) + 5
 *           Female = (10 x weight) + (6.25 x height) - (5 x age) - 161
 *  - TDEE : BMR x activity factor
 *  - Target calories : TDEE - 500 (lose), TDEE (maintain), TDEE + 500 (gain)
 *  - BMI  : weight (kg) / height (m)^2
 *  - Macros : 30% protein, 40% carbohydrate, 30% fat
 *  - Water  : 35 ml per kg of body weight
 */
public class CalorieCalculator {

    // Activity multipliers (same order as the spinner items in strings.xml)
    public static final double[] ACTIVITY_FACTORS = {1.2, 1.375, 1.55, 1.725, 1.9};

    public static final String[] ACTIVITY_NAMES = {
            "Sedentary", "Lightly active", "Moderately active", "Very active", "Extra active"
    };

    // Goal options
    public static final int GOAL_LOSE = 0;
    public static final int GOAL_MAINTAIN = 1;
    public static final int GOAL_GAIN = 2;

    // 500 kcal/day difference is roughly 0.5 kg change per week
    public static final int GOAL_ADJUSTMENT = 500;

    // Safe minimum daily intake so the app never recommends too little food
    public static final int MIN_CALORIES_MALE = 1500;
    public static final int MIN_CALORIES_FEMALE = 1200;

    // Energy per gram of each macronutrient
    private static final int KCAL_PER_G_PROTEIN = 4;
    private static final int KCAL_PER_G_CARBS = 4;
    private static final int KCAL_PER_G_FAT = 9;

    // Macronutrient split
    public static final double PROTEIN_RATIO = 0.30;
    public static final double CARBS_RATIO = 0.40;
    public static final double FAT_RATIO = 0.30;

    /** Step 1: Basal Metabolic Rate (calories burned at complete rest). */
    public static double calculateBMR(boolean isMale, double weightKg, double heightCm, int age) {
        double bmr = (10 * weightKg) + (6.25 * heightCm) - (5 * age);
        if (isMale) {
            bmr = bmr + 5;
        } else {
            bmr = bmr - 161;
        }
        return bmr;
    }

    /** Step 2: Total Daily Energy Expenditure (BMR adjusted for activity). */
    public static double calculateTDEE(double bmr, int activityIndex) {
        return bmr * ACTIVITY_FACTORS[activityIndex];
    }

    /** Step 3: calories to add or remove depending on the user's goal. */
    public static int getGoalAdjustment(int goal) {
        if (goal == GOAL_LOSE) {
            return -GOAL_ADJUSTMENT;
        } else if (goal == GOAL_GAIN) {
            return GOAL_ADJUSTMENT;
        }
        return 0;
    }

    public static String getGoalName(int goal) {
        if (goal == GOAL_LOSE) {
            return "Lose weight";
        } else if (goal == GOAL_GAIN) {
            return "Gain weight";
        }
        return "Maintain weight";
    }

    public static int getMinimumCalories(boolean isMale) {
        return isMale ? MIN_CALORIES_MALE : MIN_CALORIES_FEMALE;
    }

    /** Body Mass Index = weight (kg) / height (m)^2 */
    public static double calculateBMI(double weightKg, double heightCm) {
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    /**
     * BMI category based on the Malaysian Clinical Practice Guidelines
     * (Asian cut-off points).
     */
    public static String getBMICategory(double bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi < 23.0) {
            return "Normal weight";
        } else if (bmi < 27.5) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }

    public static int proteinGrams(int calories) {
        return (int) Math.round(calories * PROTEIN_RATIO / KCAL_PER_G_PROTEIN);
    }

    public static int carbsGrams(int calories) {
        return (int) Math.round(calories * CARBS_RATIO / KCAL_PER_G_CARBS);
    }

    public static int fatGrams(int calories) {
        return (int) Math.round(calories * FAT_RATIO / KCAL_PER_G_FAT);
    }

    /** Recommended water intake in millilitres (35 ml per kg). */
    public static int waterIntakeMl(double weightKg) {
        return (int) Math.round(weightKg * 35);
    }
}
