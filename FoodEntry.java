package com.example.caloriecalculator;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * FoodEntry
 * ---------
 * One item the user logged, e.g. "Nasi Lemak, 250 g, 400 kcal, Breakfast".
 */
public class FoodEntry {

    public static final String MEAL_BREAKFAST = "breakfast";
    public static final String MEAL_LUNCH = "lunch";
    public static final String MEAL_DINNER = "dinner";
    public static final String MEAL_SNACKS = "snacks";

    public final long id;
    public final String meal;
    public final String name;
    public final double amount;
    public final String unit;
    public final double kcalPer100;
    public final int calories;

    public FoodEntry(long id, String meal, String name, double amount, String unit,
                     double kcalPer100, int calories) {
        this.id = id;
        this.meal = meal;
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.kcalPer100 = kcalPer100;
        this.calories = calories;
    }

    public static String getMealName(String meal) {
        switch (meal) {
            case MEAL_LUNCH:
                return "Lunch";
            case MEAL_DINNER:
                return "Dinner";
            case MEAL_SNACKS:
                return "Snacks";
            default:
                return "Breakfast";
        }
    }

    public static String getMealTitle(String meal) {
        switch (meal) {
            case MEAL_LUNCH:
                return "🍛  Lunch";
            case MEAL_DINNER:
                return "🍽  Dinner";
            case MEAL_SNACKS:
                return "🍪  Snacks";
            default:
                return "🍳  Breakfast";
        }
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("meal", meal);
        o.put("name", name);
        o.put("amount", amount);
        o.put("unit", unit);
        o.put("kcalPer100", kcalPer100);
        o.put("calories", calories);
        return o;
    }

    public static FoodEntry fromJson(JSONObject o) throws JSONException {
        return new FoodEntry(
                o.getLong("id"),
                o.getString("meal"),
                o.getString("name"),
                o.getDouble("amount"),
                o.getString("unit"),
                o.getDouble("kcalPer100"),
                o.getInt("calories"));
    }
}
