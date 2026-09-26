package com.example.caloriecalculator;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Locale;

/**
 * ResultActivity
 * --------------
 * Receives the user's inputs, performs the calculations using CalorieCalculator
 * and displays the results. The step-by-step working is shown in a pop-up
 * panel when the user taps "See Calculation".
 */
public class ResultActivity extends AppCompatActivity {

    // Inputs
    private String name;
    private int age;
    private double weight, height;
    private boolean isMale;
    private int activityIndex, goal;

    // Results
    private double bmr, tdee, rawTarget, bmi;
    private int adjustment, target, minimum;
    private boolean usedMinimum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_result);
        applyWindowInsets();

        // ---------- 1. Read the inputs sent from MainActivity ----------
        Intent intent = getIntent();
        name = intent.getStringExtra(MainActivity.EXTRA_NAME);
        age = intent.getIntExtra(MainActivity.EXTRA_AGE, 0);
        weight = intent.getDoubleExtra(MainActivity.EXTRA_WEIGHT, 0);
        height = intent.getDoubleExtra(MainActivity.EXTRA_HEIGHT, 0);
        isMale = intent.getBooleanExtra(MainActivity.EXTRA_IS_MALE, true);
        activityIndex = intent.getIntExtra(MainActivity.EXTRA_ACTIVITY, 0);
        goal = intent.getIntExtra(MainActivity.EXTRA_GOAL, CalorieCalculator.GOAL_MAINTAIN);

        // ---------- 2. Perform the calculations ----------
        bmr = CalorieCalculator.calculateBMR(isMale, weight, height, age);
        tdee = CalorieCalculator.calculateTDEE(bmr, activityIndex);
        adjustment = CalorieCalculator.getGoalAdjustment(goal);

        rawTarget = tdee + adjustment;
        minimum = CalorieCalculator.getMinimumCalories(isMale);
        usedMinimum = rawTarget < minimum;
        target = (int) Math.round(Math.max(rawTarget, minimum));

        bmi = CalorieCalculator.calculateBMI(weight, height);
        String bmiCategory = CalorieCalculator.getBMICategory(bmi);

        int protein = CalorieCalculator.proteinGrams(target);
        int carbs = CalorieCalculator.carbsGrams(target);
        int fat = CalorieCalculator.fatGrams(target);
        int waterMl = CalorieCalculator.waterIntakeMl(weight);

        // ---------- 3. Display the results ----------
        showHeader();
        showTarget();
        showTiles(waterMl);
        showBmi(bmiCategory);
        showMacros(protein, carbs, fat);
        playEntranceAnimation();

        // Save today's targets so the Daily Log can compare against them
        new FoodLogStorage(this).saveTargets(target, waterMl);

        // ---------- 4. Buttons ----------
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSeeCalc).setOnClickListener(v -> showCalculationSheet());
        findViewById(R.id.btnFoodLog).setOnClickListener(v ->
                startActivity(new Intent(this, FoodLogActivity.class)));

        findViewById(R.id.btnRecalculate).setOnClickListener(v -> {
            Intent form = new Intent(this, MainActivity.class);
            form.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(form);
            finish();
        });

        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent home = new Intent(this, HomeActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(home);
            finish();
        });
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

    private void showHeader() {
        TextView tvGreeting = findViewById(R.id.tvGreeting);
        tvGreeting.setText(String.format(Locale.getDefault(),
                "Hi %s! Here is your personalised plan", name));
    }

    /** Big number in the ring. The ring shows the target as a % of maintenance calories. */
    private void showTarget() {
        TextView tvTarget = findViewById(R.id.tvTargetCalories);
        TextView tvGoal = findViewById(R.id.tvGoalLabel);
        TextView tvCaption = findViewById(R.id.tvRingCaption);
        TextView tvNote = findViewById(R.id.tvTargetNote);
        CircularProgressIndicator ring = findViewById(R.id.ringCalories);

        tvTarget.setText(String.format(Locale.getDefault(), "%,d", target));
        tvGoal.setText(CalorieCalculator.getGoalName(goal));

        int percent = (int) Math.round(target / tdee * 100);
        tvCaption.setText(String.format(Locale.getDefault(),
                "%d%% of your maintenance calories", percent));

        ring.setProgress(0);
        ring.postDelayed(() -> ring.setProgressCompat(Math.min(percent, 100), true), 400);

        if (usedMinimum) {
            tvNote.setVisibility(View.VISIBLE);
            tvNote.setText(String.format(Locale.getDefault(),
                    "Raised to the safe minimum of %,d kcal/day", minimum));
        } else {
            tvNote.setVisibility(View.GONE);
        }
    }

    private void showTiles(int waterMl) {
        ((TextView) findViewById(R.id.tvTileBmr)).setText(
                String.format(Locale.getDefault(), "%,d", Math.round(bmr)));
        ((TextView) findViewById(R.id.tvTileTdee)).setText(
                String.format(Locale.getDefault(), "%,d", Math.round(tdee)));
        ((TextView) findViewById(R.id.tvTileWater)).setText(
                String.format(Locale.getDefault(), "%.1f L", waterMl / 1000.0));
    }

    private void showBmi(String category) {
        TextView tvBmiValue = findViewById(R.id.tvBmiValue);
        TextView tvBmiCategory = findViewById(R.id.tvBmiCategory);

        tvBmiValue.setText(String.format(Locale.getDefault(), "%.1f", bmi));
        tvBmiCategory.setText(category);

        int colorRes;
        if (bmi < 18.5) {
            colorRes = R.color.bmi_under;
        } else if (bmi < 23.0) {
            colorRes = R.color.bmi_normal;
        } else if (bmi < 27.5) {
            colorRes = R.color.bmi_over;
        } else {
            colorRes = R.color.bmi_obese;
        }
        int color = ContextCompat.getColor(this, colorRes);
        tvBmiValue.setTextColor(color);
        tvBmiCategory.setTextColor(color);
        tvBmiCategory.setBackgroundTintList(
                ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 40)));

        // Slide the marker along the colour scale (scale covers BMI 15 to 35)
        FrameLayout scale = findViewById(R.id.bmiScale);
        View marker = findViewById(R.id.bmiMarker);
        scale.post(() -> {
            double clamped = Math.max(15, Math.min(35, bmi));
            float fraction = (float) ((clamped - 15) / 20.0);
            float x = fraction * (scale.getWidth() - marker.getWidth());
            marker.animate().translationX(x).setStartDelay(500).setDuration(900)
                    .setInterpolator(new DecelerateInterpolator()).start();
        });
    }

    private void showMacros(int protein, int carbs, int fat) {
        ((TextView) findViewById(R.id.tvProtein)).setText(protein + " g");
        ((TextView) findViewById(R.id.tvCarbs)).setText(carbs + " g");
        ((TextView) findViewById(R.id.tvFat)).setText(fat + " g");

        ((TextView) findViewById(R.id.tvProteinDetail)).setText(String.format(Locale.getDefault(),
                "30%% of calories · %d kcal", Math.round(target * CalorieCalculator.PROTEIN_RATIO)));
        ((TextView) findViewById(R.id.tvCarbsDetail)).setText(String.format(Locale.getDefault(),
                "40%% of calories · %d kcal", Math.round(target * CalorieCalculator.CARBS_RATIO)));
        ((TextView) findViewById(R.id.tvFatDetail)).setText(String.format(Locale.getDefault(),
                "30%% of calories · %d kcal", Math.round(target * CalorieCalculator.FAT_RATIO)));

        animateBar(R.id.barProtein, 30, 600);
        animateBar(R.id.barCarbs, 40, 750);
        animateBar(R.id.barFat, 30, 900);
    }

    private void animateBar(int barId, int value, long delay) {
        LinearProgressIndicator bar = findViewById(barId);
        bar.setProgress(0);
        bar.postDelayed(() -> bar.setProgressCompat(value, true), delay);
    }

    /** Opens the pop-up panel with every formula and the user's actual numbers. */
    private void showCalculationSheet() {
        String genderConstant = isMale ? "+ 5" : "− 161";
        double factor = CalorieCalculator.ACTIVITY_FACTORS[activityIndex];
        String activityName = CalorieCalculator.ACTIVITY_NAMES[activityIndex];
        String sign = adjustment < 0 ? "−" : "+";
        double heightM = height / 100.0;

        String[] titles = {
                "Basal Metabolic Rate (BMR)",
                "Total Daily Energy Expenditure (TDEE)",
                "Adjust for your goal",
                "Body Mass Index (BMI)",
                "Water intake"
        };

        String step3 = String.format(Locale.getDefault(),
                "Target = TDEE %s %d\n= %.0f %s %d\n= %.0f kcal",
                sign, Math.abs(adjustment), tdee, sign, Math.abs(adjustment), rawTarget);
        if (usedMinimum) {
            step3 += String.format(Locale.getDefault(),
                    "\n\nBelow the safe minimum,\nso target = %,d kcal", minimum);
        }

        String[] formulas = {
                String.format(Locale.getDefault(),
                        "BMR = (10 × weight) + (6.25 × height)\n      − (5 × age) %s\n"
                                + "= (10 × %.1f) + (6.25 × %.1f)\n  − (5 × %d) %s\n= %.0f kcal",
                        genderConstant, weight, height, age, genderConstant, bmr),
                String.format(Locale.getDefault(),
                        "TDEE = BMR × activity factor\n= %.0f × %s (%s)\n= %.0f kcal",
                        bmr, String.valueOf(factor), activityName, tdee),
                step3,
                String.format(Locale.getDefault(),
                        "BMI = weight ÷ height²\n= %.1f ÷ (%.2f × %.2f)\n= %.1f",
                        weight, heightM, heightM, bmi),
                String.format(Locale.getDefault(),
                        "Water = weight × 35 ml\n= %.1f × 35\n= %,d ml",
                        weight, CalorieCalculator.waterIntakeMl(weight))
        };

        CalculationSheet.show(this, "How We Calculated It",
                "Mifflin-St Jeor equation, using your details", titles, formulas);
    }

    /** Cards fade in and slide up one after another. */
    private void playEntranceAnimation() {
        LinearLayout content = findViewById(R.id.content);
        for (int i = 0; i < content.getChildCount(); i++) {
            View child = content.getChildAt(i);
            child.setAlpha(0f);
            child.setTranslationY(60f);
            child.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(90L * i)
                    .setDuration(450)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }
}
