package com.example.caloriecalculator;

import java.util.Arrays;
import java.util.List;

/**
 * ExerciseDatabase
 * ----------------
 * List of exercises with their MET values (Metabolic Equivalent of Task)
 * for light, moderate and vigorous intensity.
 * MET values are approximate, based on the Compendium of Physical Activities.
 *
 * Calories burned = MET × 3.5 × body weight (kg) ÷ 200 × minutes
 */
public class ExerciseDatabase {

    public static final int LIGHT = 0;
    public static final int MODERATE = 1;
    public static final int VIGOROUS = 2;
    public static final String[] INTENSITY_NAMES = {"Light", "Moderate", "Vigorous"};

    /** One type of exercise, with a MET value and an example for each intensity. */
    public static class ExerciseType {
        public final String name;
        public final double[] met;         // [light, moderate, vigorous]
        public final String[] examples;     // what each intensity looks like

        ExerciseType(String name, double light, double moderate, double vigorous,
                     String lightExample, String moderateExample, String vigorousExample) {
            this.name = name;
            this.met = new double[]{light, moderate, vigorous};
            this.examples = new String[]{lightExample, moderateExample, vigorousExample};
        }
    }

    private static final List<ExerciseType> EXERCISES = Arrays.asList(
            new ExerciseType("Walking", 2.8, 3.5, 5.0,
                    "Slow stroll", "Normal pace", "Brisk walk"),
            new ExerciseType("Jogging / Running", 6.0, 9.8, 11.5,
                    "Light jog", "Steady run (about 10 km/h)", "Fast run (about 12 km/h)"),
            new ExerciseType("Cycling", 4.0, 6.8, 10.0,
                    "Leisure ride", "Moderate effort", "Fast / uphill"),
            new ExerciseType("Swimming", 6.0, 8.3, 9.8,
                    "Leisure swim", "Steady laps", "Fast laps"),
            new ExerciseType("Badminton", 4.5, 5.5, 7.0,
                    "Casual rally", "Social game", "Competitive match"),
            new ExerciseType("Football / Futsal", 7.0, 8.0, 10.0,
                    "Casual kick-about", "Friendly game", "Competitive match"),
            new ExerciseType("Basketball", 4.5, 6.5, 8.0,
                    "Shooting practice", "General play", "Full game"),
            new ExerciseType("Volleyball / Sepak Takraw", 3.0, 4.0, 6.0,
                    "Casual play", "Regular game", "Competitive match"),
            new ExerciseType("Tennis", 5.0, 7.3, 8.0,
                    "Doubles / casual", "Singles", "Competitive singles"),
            new ExerciseType("Gym / Weight Training", 3.5, 5.0, 6.0,
                    "Light weights", "Normal workout", "Heavy lifting"),
            new ExerciseType("HIIT / Circuit Training", 4.3, 6.0, 8.0,
                    "Easy circuit", "Moderate circuit", "Hard intervals"),
            new ExerciseType("Aerobics / Zumba", 5.0, 6.5, 7.3,
                    "Low impact", "Regular class", "High impact"),
            new ExerciseType("Skipping Rope", 8.8, 11.8, 12.3,
                    "Slow pace", "Moderate pace", "Fast pace"),
            new ExerciseType("Hiking", 5.3, 6.0, 7.8,
                    "Flat trail", "Hilly trail", "Steep / with backpack"),
            new ExerciseType("Stair Climbing", 4.0, 6.0, 8.8,
                    "Slow pace", "Steady pace", "Fast pace"),
            new ExerciseType("Yoga / Stretching", 2.5, 3.0, 4.0,
                    "Stretching", "Hatha yoga", "Power yoga"),
            new ExerciseType("Martial Arts / Silat", 5.3, 7.7, 10.3,
                    "Slow practice", "Regular training", "Sparring"),
            new ExerciseType("Housework", 2.5, 3.5, 4.0,
                    "Light tidying", "Sweeping / mopping", "Heavy cleaning")
    );

    public static List<ExerciseType> getAll() {
        return EXERCISES;
    }

    public static String[] getNames() {
        String[] names = new String[EXERCISES.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = EXERCISES.get(i).name;
        }
        return names;
    }

    /** Calories burned = MET × 3.5 × weight (kg) ÷ 200 × minutes */
    public static int caloriesBurned(double met, double weightKg, int minutes) {
        return (int) Math.round(met * 3.5 * weightKg / 200.0 * minutes);
    }
}
