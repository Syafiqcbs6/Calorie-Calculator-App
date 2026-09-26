package com.example.caloriecalculator;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * ExerciseEntry
 * -------------
 * One exercise the user logged, e.g. "Walking, 30 min, Moderate, 120 kcal".
 */
public class ExerciseEntry {

    public final long id;
    public final String name;
    public final int minutes;
    public final int intensity;   // ExerciseDatabase.LIGHT / MODERATE / VIGOROUS
    public final double met;
    public final int calories;

    public ExerciseEntry(long id, String name, int minutes, int intensity, double met, int calories) {
        this.id = id;
        this.name = name;
        this.minutes = minutes;
        this.intensity = intensity;
        this.met = met;
        this.calories = calories;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("name", name);
        o.put("minutes", minutes);
        o.put("intensity", intensity);
        o.put("met", met);
        o.put("calories", calories);
        return o;
    }

    public static ExerciseEntry fromJson(JSONObject o) throws JSONException {
        return new ExerciseEntry(
                o.getLong("id"),
                o.getString("name"),
                o.getInt("minutes"),
                o.getInt("intensity"),
                o.getDouble("met"),
                o.getInt("calories"));
    }
}
