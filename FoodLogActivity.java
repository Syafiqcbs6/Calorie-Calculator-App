package com.example.caloriecalculator;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * FoodLogActivity (Daily Log)
 * ---------------------------
 * Shows today's meals, exercise and water, and compares them with the daily target:
 *
 *     Net calories = Eaten − Burned
 *     Remaining    = Target − Net calories
 */
public class FoodLogActivity extends AppCompatActivity {

    private static final String[] MEALS = {
            FoodEntry.MEAL_BREAKFAST, FoodEntry.MEAL_LUNCH, FoodEntry.MEAL_DINNER, FoodEntry.MEAL_SNACKS
    };
    private static final int[] MEAL_CARD_IDS = {
            R.id.mealBreakfast, R.id.mealLunch, R.id.mealDinner, R.id.mealSnacks
    };

    private FoodLogStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_food_log);
        applyWindowInsets();

        storage = new FoodLogStorage(this);

        ((TextView) findViewById(R.id.tvLogDate)).setText(
                new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(new Date()));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnLogCalc).setOnClickListener(v -> showCalculationSheet());

        setupTabs();

        // Meal cards
        for (int i = 0; i < MEALS.length; i++) {
            String meal = MEALS[i];
            View card = findViewById(MEAL_CARD_IDS[i]);
            ((TextView) card.findViewById(R.id.tvMealTitle)).setText(FoodEntry.getMealTitle(meal));
            card.findViewById(R.id.btnSearch).setOnClickListener(v -> openAddFood(meal, AddFoodActivity.MODE_SEARCH));
            card.findViewById(R.id.btnCamera).setOnClickListener(v -> openAddFood(meal, AddFoodActivity.MODE_CAMERA));
        }

        // Exercise
        findViewById(R.id.btnAddExercise).setOnClickListener(v -> showAddExerciseSheet());

        // Water
        findViewById(R.id.btnWater250).setOnClickListener(v -> addWater(250));
        findViewById(R.id.btnWater500).setOnClickListener(v -> addWater(500));
        findViewById(R.id.btnWaterCustom).setOnClickListener(v -> showCustomWaterDialog());
        findViewById(R.id.btnWaterReset).setOnClickListener(v -> {
            storage.resetWater();
            refresh();
        });

        findViewById(R.id.btnClearLog).setOnClickListener(v -> confirmClearLog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh(); // reload after coming back from AddFoodActivity
    }

    private void applyWindowInsets() {
        View root = findViewById(R.id.main);
        LinearLayout content = findViewById(R.id.content);
        int baseBottom = content.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            content.setPadding(content.getPaddingLeft(), content.getPaddingTop(),
                    content.getPaddingRight(), baseBottom + bars.bottom);
            return insets;
        });
    }

    /** Meals / Exercise / Water tabs: only one section is shown at a time. */
    private void setupTabs() {
        MaterialButtonToggleGroup toggle = findViewById(R.id.toggleSections);
        View meals = findViewById(R.id.sectionMeals);
        View exercise = findViewById(R.id.sectionExercise);
        View water = findViewById(R.id.sectionWater);

        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            meals.setVisibility(checkedId == R.id.tabMeals ? View.VISIBLE : View.GONE);
            exercise.setVisibility(checkedId == R.id.tabExercise ? View.VISIBLE : View.GONE);
            water.setVisibility(checkedId == R.id.tabWater ? View.VISIBLE : View.GONE);
        });
    }

    private void openAddFood(String meal, String mode) {
        Intent intent = new Intent(this, AddFoodActivity.class);
        intent.putExtra(AddFoodActivity.EXTRA_MEAL, meal);
        intent.putExtra(AddFoodActivity.EXTRA_MODE, mode);
        startActivity(intent);
    }

    /** Recalculates the totals and redraws the whole screen. */
    private void refresh() {
        showSummary();
        showExercise();
        showWater();
        for (int i = 0; i < MEALS.length; i++) {
            showMeal(MEALS[i], findViewById(MEAL_CARD_IDS[i]));
        }
    }

    // =====================================================================
    //  SUMMARY
    // =====================================================================

    private void showSummary() {
        int target = storage.getTargetCalories();
        int eaten = storage.getTotalCalories();
        int burned = storage.getTotalBurned();
        int net = eaten - burned;          // Net calories = Eaten − Burned
        int remaining = target - net;      // Remaining = Target − Net

        TextView tvRemainingLabel = findViewById(R.id.tvRemainingLabel);
        TextView tvRemaining = findViewById(R.id.tvRemaining);
        CircularProgressIndicator ring = findViewById(R.id.ringEaten);

        if (remaining >= 0) {
            tvRemainingLabel.setText(R.string.remaining);
            tvRemaining.setText(String.format(Locale.getDefault(), "%,d kcal", remaining));
            tvRemaining.setTextColor(ContextCompat.getColor(this, R.color.primary));
            ring.setIndicatorColor(ContextCompat.getColor(this, R.color.accent));
        } else {
            tvRemainingLabel.setText("Over target by");
            tvRemaining.setText(String.format(Locale.getDefault(), "%,d kcal", -remaining));
            tvRemaining.setTextColor(ContextCompat.getColor(this, R.color.danger));
            ring.setIndicatorColor(ContextCompat.getColor(this, R.color.danger));
        }

        ((TextView) findViewById(R.id.tvLogTarget)).setText(
                String.format(Locale.getDefault(), "Daily target: %,d kcal", target));

        int percent = target > 0 ? Math.max(0, Math.round(net * 100f / target)) : 0;
        ((TextView) findViewById(R.id.tvRingPercent)).setText(percent + "%");
        ring.setProgressCompat(Math.min(100, percent), true);

        ((TextView) findViewById(R.id.tvStatEaten)).setText(String.format(Locale.getDefault(), "%,d", eaten));
        ((TextView) findViewById(R.id.tvStatBurned)).setText(String.format(Locale.getDefault(), "%,d", burned));
        ((TextView) findViewById(R.id.tvStatWater)).setText(
                String.format(Locale.getDefault(), "%.1f L", storage.getWaterMl() / 1000.0));
    }

    /** "See Calculation" pop-up for the daily totals. */
    private void showCalculationSheet() {
        int target = storage.getTargetCalories();
        int eaten = storage.getTotalCalories();
        int burned = storage.getTotalBurned();
        int net = eaten - burned;
        int remaining = target - net;
        int water = storage.getWaterMl();
        int waterGoal = storage.getWaterGoal();

        StringBuilder eatenFormula = new StringBuilder("Eaten = sum of all foods\n");
        String[] mealNames = {"Breakfast", "Lunch", "Dinner", "Snacks"};
        for (int i = 0; i < MEALS.length; i++) {
            eatenFormula.append(String.format(Locale.getDefault(), "  %-9s %,6d kcal\n",
                    mealNames[i], storage.getMealCalories(MEALS[i])));
        }
        eatenFormula.append(String.format(Locale.getDefault(), "= %,d kcal", eaten));

        String[] titles = {
                "Calories eaten",
                "Net calories",
                "Calories remaining",
                "Water progress"
        };
        String[] formulas = {
                eatenFormula.toString(),
                String.format(Locale.getDefault(),
                        "Net = Eaten − Burned\n= %,d − %,d\n= %,d kcal", eaten, burned, net),
                String.format(Locale.getDefault(),
                        "Remaining = Target − Net\n= %,d − %,d\n= %,d kcal", target, net, remaining),
                String.format(Locale.getDefault(),
                        "Progress = Drank ÷ Goal × 100\n= %,d ÷ %,d × 100\n= %d%%",
                        water, waterGoal, waterGoal > 0 ? Math.round(water * 100f / waterGoal) : 0)
        };
        CalculationSheet.show(this, "Today's Calculation",
                "How your remaining calories are worked out", titles, formulas);
    }

    // =====================================================================
    //  MEALS
    // =====================================================================

    private void showMeal(String meal, View card) {
        List<FoodEntry> entries = storage.getEntries(meal);
        LinearLayout container = card.findViewById(R.id.mealEntries);
        TextView tvTotal = card.findViewById(R.id.tvMealTotal);
        TextView tvEmpty = card.findViewById(R.id.tvMealEmpty);

        container.removeAllViews();
        tvEmpty.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
        tvTotal.setText(String.format(Locale.getDefault(), "%,d kcal", storage.getMealCalories(meal)));

        LayoutInflater inflater = getLayoutInflater();
        for (FoodEntry entry : entries) {
            View row = inflater.inflate(R.layout.item_food_entry, container, false);
            ((TextView) row.findViewById(R.id.tvEntryName)).setText(entry.name);
            ((TextView) row.findViewById(R.id.tvEntryDetail)).setText(String.format(Locale.getDefault(),
                    "%s %s · %d kcal per 100 %s",
                    formatAmount(entry.amount), entry.unit, Math.round(entry.kcalPer100), entry.unit));
            ((TextView) row.findViewById(R.id.tvEntryKcal)).setText(
                    String.format(Locale.getDefault(), "%,d kcal", entry.calories));
            row.findViewById(R.id.btnDeleteEntry).setOnClickListener(v -> confirmDeleteFood(entry));
            container.addView(row);
        }
    }

    private void confirmDeleteFood(FoodEntry entry) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remove food?")
                .setMessage("Remove " + entry.name + " (" + entry.calories + " kcal) from "
                        + FoodEntry.getMealName(entry.meal) + "?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (d, w) -> {
                    storage.removeEntry(entry.id);
                    refresh();
                })
                .show();
    }

    // =====================================================================
    //  EXERCISE
    // =====================================================================

    private void showExercise() {
        List<ExerciseEntry> list = storage.getExercises();
        LinearLayout container = findViewById(R.id.exerciseEntries);
        container.removeAllViews();

        findViewById(R.id.tvExerciseEmpty).setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        ((TextView) findViewById(R.id.tvExerciseTotal)).setText(
                String.format(Locale.getDefault(), "−%,d kcal", storage.getTotalBurned()));

        LayoutInflater inflater = getLayoutInflater();
        int green = ContextCompat.getColor(this, R.color.fat);
        for (ExerciseEntry entry : list) {
            View row = inflater.inflate(R.layout.item_food_entry, container, false);
            ((TextView) row.findViewById(R.id.tvEntryName)).setText(entry.name);
            ((TextView) row.findViewById(R.id.tvEntryDetail)).setText(String.format(Locale.getDefault(),
                    "%d min · %s · MET %s",
                    entry.minutes, ExerciseDatabase.INTENSITY_NAMES[entry.intensity], String.valueOf(entry.met)));
            TextView tvKcal = row.findViewById(R.id.tvEntryKcal);
            tvKcal.setText(String.format(Locale.getDefault(), "−%,d kcal", entry.calories));
            tvKcal.setTextColor(green);
            row.findViewById(R.id.btnDeleteEntry).setOnClickListener(v -> confirmDeleteExercise(entry));
            container.addView(row);
        }
    }

    /** Pop-up panel where the user picks an exercise, minutes and intensity. */
    private void showAddExerciseSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.sheet_add_exercise, null);

        TextInputLayout tilMinutes = view.findViewById(R.id.tilMinutes);
        TextInputEditText etMinutes = view.findViewById(R.id.etMinutes);
        MaterialAutoCompleteTextView actvExercise = view.findViewById(R.id.actvExercise);
        ChipGroup chipIntensity = view.findViewById(R.id.chipIntensity);
        TextView tvHint = view.findViewById(R.id.tvIntensityHint);
        TextView tvResult = view.findViewById(R.id.tvBurnResult);
        TextView tvCalc = view.findViewById(R.id.tvBurnCalc);
        MaterialButton btnToggleCalc = view.findViewById(R.id.btnToggleBurnCalc);

        List<ExerciseDatabase.ExerciseType> types = ExerciseDatabase.getAll();
        String[] names = ExerciseDatabase.getNames();
        actvExercise.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names));
        actvExercise.setText(names[0], false);
        final int[] selected = {0};
        etMinutes.setText("30");

        double weight = storage.getWeight();

        // Recalculates the calories burned whenever anything changes
        Runnable update = () -> {
            ExerciseDatabase.ExerciseType type = types.get(selected[0]);
            int intensity = getIntensity(chipIntensity);
            double met = type.met[intensity];
            int minutes = parseInt(etMinutes);

            tvHint.setText(String.format(Locale.getDefault(), "%s · MET %s",
                    type.examples[intensity], String.valueOf(met)));

            if (minutes <= 0) {
                tvResult.setText("— kcal");
                tvCalc.setText("Enter the duration in minutes.");
                return;
            }
            int kcal = ExerciseDatabase.caloriesBurned(met, weight, minutes);
            tvResult.setText(String.format(Locale.getDefault(), "%,d kcal", kcal));
            tvCalc.setText(String.format(Locale.getDefault(),
                    "Calories = MET × 3.5 × weight\n           ÷ 200 × minutes\n"
                            + "= %s × 3.5 × %.1f ÷ 200 × %d\n= %,d kcal",
                    String.valueOf(met), weight, minutes, kcal));
        };

        actvExercise.setOnItemClickListener((parent, v, position, id) -> {
            selected[0] = position;
            update.run();
        });
        chipIntensity.setOnCheckedStateChangeListener((group, ids) -> update.run());
        etMinutes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                tilMinutes.setError(null);
                update.run();
            }
        });
        update.run();

        // "See Calculation" shows / hides the formula
        btnToggleCalc.setOnClickListener(v -> {
            boolean show = tvCalc.getVisibility() != View.VISIBLE;
            tvCalc.setVisibility(show ? View.VISIBLE : View.GONE);
            btnToggleCalc.setText(show ? "Hide Calculation" : getString(R.string.btn_see_calculation));
        });

        view.findViewById(R.id.btnExerciseCancel).setOnClickListener(v -> sheet.dismiss());
        view.findViewById(R.id.btnExerciseAdd).setOnClickListener(v -> {
            int minutes = parseInt(etMinutes);
            if (minutes <= 0 || minutes > 600) {
                tilMinutes.setError("Enter 1 – 600 minutes");
                return;
            }
            ExerciseDatabase.ExerciseType type = types.get(selected[0]);
            int intensity = getIntensity(chipIntensity);
            double met = type.met[intensity];
            int kcal = ExerciseDatabase.caloriesBurned(met, weight, minutes);

            storage.addExercise(new ExerciseEntry(System.currentTimeMillis(), type.name,
                    minutes, intensity, met, kcal));
            sheet.dismiss();
            refresh();
            Toast.makeText(this, String.format(Locale.getDefault(),
                    "%s added: −%,d kcal", type.name, kcal), Toast.LENGTH_SHORT).show();
        });

        sheet.setContentView(view);
        sheet.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        sheet.getBehavior().setSkipCollapsed(true);
        sheet.show();
    }

    private int getIntensity(ChipGroup group) {
        int id = group.getCheckedChipId();
        if (id == R.id.chipLight) {
            return ExerciseDatabase.LIGHT;
        } else if (id == R.id.chipVigorous) {
            return ExerciseDatabase.VIGOROUS;
        }
        return ExerciseDatabase.MODERATE;
    }

    private void confirmDeleteExercise(ExerciseEntry entry) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remove exercise?")
                .setMessage("Remove " + entry.name + " (" + entry.minutes + " min, "
                        + entry.calories + " kcal)?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (d, w) -> {
                    storage.removeExercise(entry.id);
                    refresh();
                })
                .show();
    }

    // =====================================================================
    //  WATER
    // =====================================================================

    private void showWater() {
        int goal = storage.getWaterGoal();
        int drank = storage.getWaterMl();

        ((TextView) findViewById(R.id.tvWaterLog)).setText(
                String.format(Locale.getDefault(), "%,d ml of %,d ml", drank, goal));
        LinearProgressIndicator bar = findViewById(R.id.barWater);
        int percent = goal > 0 ? Math.min(100, Math.round(drank * 100f / goal)) : 0;
        bar.setProgressCompat(percent, true);
    }

    private void addWater(int ml) {
        storage.addWater(ml);
        refresh();
        Toast.makeText(this, "+" + ml + " ml of water", Toast.LENGTH_SHORT).show();
    }

    private void showCustomWaterDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_input, null);
        TextInputLayout til = view.findViewById(R.id.tilInput);
        TextInputEditText et = view.findViewById(R.id.etInput);
        til.setSuffixText("ml");

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Add water")
                .setView(view)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int ml = parseInt(et);
            if (ml <= 0 || ml > 3000) {
                til.setError("Enter 1 – 3000 ml");
                return;
            }
            addWater(ml);
            dialog.dismiss();
        }));
        dialog.show();
    }

    // =====================================================================
    //  HELPERS
    // =====================================================================

    private void confirmClearLog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Clear today's log?")
                .setMessage("This removes all food, exercise and water you logged today.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear", (d, w) -> {
                    storage.clearToday();
                    refresh();
                })
                .show();
    }

    private int parseInt(TextInputEditText editText) {
        String text = editText.getText() == null ? "" : editText.getText().toString().trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static String formatAmount(double amount) {
        if (amount == Math.floor(amount)) {
            return String.valueOf((long) amount);
        }
        return String.format(Locale.getDefault(), "%.1f", amount);
    }
}
