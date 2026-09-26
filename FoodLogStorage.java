package com.example.caloriecalculator;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * FoodLogStorage
 * --------------
 * Saves the user's profile, today's food log, exercise log and water intake
 * on the phone using SharedPreferences, so the data is kept when the app is closed.
 * Each day uses its own keys, so the log starts fresh every morning.
 */
public class FoodLogStorage {

    private static final String PREFS_NAME = "food_log";
    private static final String KEY_TARGET = "target_calories";
    private static final String KEY_WATER_GOAL = "water_goal_ml";

    // Profile keys
    private static final String KEY_HAS_PROFILE = "has_profile";
    private static final String KEY_NAME = "profile_name";
    private static final String KEY_AGE = "profile_age";
    private static final String KEY_WEIGHT = "profile_weight";
    private static final String KEY_HEIGHT = "profile_height";
    private static final String KEY_IS_MALE = "profile_is_male";
    private static final String KEY_ACTIVITY = "profile_activity";
    private static final String KEY_GOAL = "profile_goal";

    private final SharedPreferences prefs;

    public FoodLogStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private static String entriesKey() {
        return "entries_" + today();
    }

    private static String exerciseKey() {
        return "exercise_" + today();
    }

    private static String waterKey() {
        return "water_" + today();
    }

    // ================= PROFILE =================

    public void saveProfile(String name, int age, double weight, double height,
                            boolean isMale, int activity, int goal) {
        prefs.edit()
                .putBoolean(KEY_HAS_PROFILE, true)
                .putString(KEY_NAME, name)
                .putInt(KEY_AGE, age)
                .putFloat(KEY_WEIGHT, (float) weight)
                .putFloat(KEY_HEIGHT, (float) height)
                .putBoolean(KEY_IS_MALE, isMale)
                .putInt(KEY_ACTIVITY, activity)
                .putInt(KEY_GOAL, goal)
                .apply();
    }

    public boolean hasProfile() {
        return prefs.getBoolean(KEY_HAS_PROFILE, false);
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "");
    }

    public int getAge() {
        return prefs.getInt(KEY_AGE, 0);
    }

    /** Body weight in kg (60 kg is used if the user has not calculated yet). */
    public double getWeight() {
        return prefs.getFloat(KEY_WEIGHT, 60f);
    }

    public double getHeight() {
        return prefs.getFloat(KEY_HEIGHT, 0f);
    }

    public boolean isMale() {
        return prefs.getBoolean(KEY_IS_MALE, true);
    }

    public int getActivity() {
        return prefs.getInt(KEY_ACTIVITY, 0);
    }

    public int getGoal() {
        return prefs.getInt(KEY_GOAL, CalorieCalculator.GOAL_MAINTAIN);
    }

    // ================= TARGETS =================

    public void saveTargets(int targetCalories, int waterGoalMl) {
        prefs.edit()
                .putInt(KEY_TARGET, targetCalories)
                .putInt(KEY_WATER_GOAL, waterGoalMl)
                .apply();
    }

    public int getTargetCalories() {
        return prefs.getInt(KEY_TARGET, 0);
    }

    public int getWaterGoal() {
        return prefs.getInt(KEY_WATER_GOAL, 2000);
    }

    // ================= FOOD =================

    public List<FoodEntry> getEntries() {
        List<FoodEntry> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString(entriesKey(), "[]"));
            for (int i = 0; i < array.length(); i++) {
                list.add(FoodEntry.fromJson(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            // Corrupted data: start with an empty list
        }
        return list;
    }

    public List<FoodEntry> getEntries(String meal) {
        List<FoodEntry> result = new ArrayList<>();
        for (FoodEntry e : getEntries()) {
            if (e.meal.equals(meal)) {
                result.add(e);
            }
        }
        return result;
    }

    private void saveEntries(List<FoodEntry> entries) {
        JSONArray array = new JSONArray();
        try {
            for (FoodEntry e : entries) {
                array.put(e.toJson());
            }
        } catch (JSONException ignored) {
        }
        prefs.edit().putString(entriesKey(), array.toString()).apply();
    }

    public void addEntry(FoodEntry entry) {
        List<FoodEntry> entries = getEntries();
        entries.add(entry);
        saveEntries(entries);
    }

    public void removeEntry(long id) {
        List<FoodEntry> entries = getEntries();
        for (int i = entries.size() - 1; i >= 0; i--) {
            if (entries.get(i).id == id) {
                entries.remove(i);
            }
        }
        saveEntries(entries);
    }

    /** Total calories eaten today. */
    public int getTotalCalories() {
        int total = 0;
        for (FoodEntry e : getEntries()) {
            total += e.calories;
        }
        return total;
    }

    public int getMealCalories(String meal) {
        int total = 0;
        for (FoodEntry e : getEntries(meal)) {
            total += e.calories;
        }
        return total;
    }

    // ================= EXERCISE =================

    public List<ExerciseEntry> getExercises() {
        List<ExerciseEntry> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString(exerciseKey(), "[]"));
            for (int i = 0; i < array.length(); i++) {
                list.add(ExerciseEntry.fromJson(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            // Corrupted data: start with an empty list
        }
        return list;
    }

    private void saveExercises(List<ExerciseEntry> exercises) {
        JSONArray array = new JSONArray();
        try {
            for (ExerciseEntry e : exercises) {
                array.put(e.toJson());
            }
        } catch (JSONException ignored) {
        }
        prefs.edit().putString(exerciseKey(), array.toString()).apply();
    }

    public void addExercise(ExerciseEntry entry) {
        List<ExerciseEntry> list = getExercises();
        list.add(entry);
        saveExercises(list);
    }

    public void removeExercise(long id) {
        List<ExerciseEntry> list = getExercises();
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).id == id) {
                list.remove(i);
            }
        }
        saveExercises(list);
    }

    /** Total calories burned by exercise today. */
    public int getTotalBurned() {
        int total = 0;
        for (ExerciseEntry e : getExercises()) {
            total += e.calories;
        }
        return total;
    }

    // ================= WATER =================

    public int getWaterMl() {
        return prefs.getInt(waterKey(), 0);
    }

    public void addWater(int ml) {
        prefs.edit().putInt(waterKey(), getWaterMl() + ml).apply();
    }

    public void resetWater() {
        prefs.edit().remove(waterKey()).apply();
    }

    /** Removes today's food, exercise and water. */
    public void clearToday() {
        prefs.edit().remove(entriesKey()).remove(exerciseKey()).remove(waterKey()).apply();
    }
}
