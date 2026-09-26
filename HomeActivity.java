package com.example.caloriecalculator;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * HomeActivity
 * ------------
 * Main menu of the app (launcher screen). Shows a summary of today
 * and lets the user choose: Calorie Calculator, Daily Log or My Results.
 */
public class HomeActivity extends AppCompatActivity {

    private FoodLogStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_home);
        applyWindowInsets();

        storage = new FoodLogStorage(this);

        ((TextView) findViewById(R.id.tvHomeDate)).setText(
                new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(new Date()));

        findViewById(R.id.tileCalculator).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.tileDailyLog).setOnClickListener(v -> openDailyLog());
        findViewById(R.id.cardToday).setOnClickListener(v -> openDailyLog());
        findViewById(R.id.tileMyPlan).setOnClickListener(v -> openMyResults());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh(); // update the numbers every time the user comes back
    }

    private void applyWindowInsets() {
        View root = findViewById(R.id.main);
        LinearLayout content = findViewById(R.id.content);
        int baseBottom = content.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            content.setPadding(
                    content.getPaddingLeft(),
                    content.getPaddingTop(),
                    content.getPaddingRight(),
                    baseBottom + bars.bottom
            );
            return insets;
        });
    }

    private void refresh() {
        String name = storage.getName();

        ((TextView) findViewById(R.id.tvHomeGreeting)).setText(
                TextUtils.isEmpty(name) ? "Welcome! 👋" : "Hi, " + name + " 👋"
        );

        int target = storage.getTargetCalories();
        boolean hasTarget = target > 0;

        findViewById(R.id.layoutTodayEmpty).setVisibility(
                hasTarget ? View.GONE : View.VISIBLE
        );

        findViewById(R.id.layoutTodayStats).setVisibility(
                hasTarget ? View.VISIBLE : View.GONE
        );

        findViewById(R.id.tileMyPlan).setVisibility(
                storage.hasProfile() ? View.VISIBLE : View.GONE
        );

        if (!hasTarget) {
            return;
        }

        int eaten = storage.getTotalCalories();
        int burned = storage.getTotalBurned();
        int net = eaten - burned;
        int remaining = target - net;

        TextView tvRemaining = findViewById(R.id.tvHomeRemaining);

        if (remaining >= 0) {
            tvRemaining.setText(
                    String.format(Locale.getDefault(), "%,d kcal left", remaining)
            );
            tvRemaining.setTextColor(getColor(R.color.primary));
        } else {
            tvRemaining.setText(
                    String.format(Locale.getDefault(), "%,d kcal over", -remaining)
            );
            tvRemaining.setTextColor(getColor(R.color.danger));
        }

        ((TextView) findViewById(R.id.tvHomeTarget)).setText(
                String.format(Locale.getDefault(), "Daily target: %,d kcal", target)
        );

        ((TextView) findViewById(R.id.tvHomeEaten)).setText(
                String.format(Locale.getDefault(), "%,d", eaten)
        );

        ((TextView) findViewById(R.id.tvHomeBurned)).setText(
                String.format(Locale.getDefault(), "%,d", burned)
        );

        ((TextView) findViewById(R.id.tvHomeWater)).setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f L",
                        storage.getWaterMl() / 1000.0
                )
        );

        LinearProgressIndicator bar = findViewById(R.id.barHomeCalories);

        int percent = Math.max(
                0,
                Math.min(100, Math.round(net * 100f / target))
        );

        bar.setProgressCompat(percent, true);
    }

    private void openDailyLog() {
        if (storage.getTargetCalories() > 0) {
            startActivity(new Intent(this, FoodLogActivity.class));
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Calculate your target first")
                    .setMessage(
                            "The Daily Log compares what you eat and burn with your daily calorie target. Please calculate it first."
                    )
                    .setNegativeButton("Later", null)
                    .setPositiveButton("Calculate now", (d, w) ->
                            startActivity(
                                    new Intent(this, MainActivity.class)
                            )
                    )
                    .show();
        }
    }

    /** Opens the result screen again using the details saved from the last calculation. */
    private void openMyResults() {
        Intent intent = new Intent(this, ResultActivity.class);

        intent.putExtra(MainActivity.EXTRA_NAME, storage.getName());
        intent.putExtra(MainActivity.EXTRA_AGE, storage.getAge());
        intent.putExtra(MainActivity.EXTRA_WEIGHT, storage.getWeight());
        intent.putExtra(MainActivity.EXTRA_HEIGHT, storage.getHeight());
        intent.putExtra(MainActivity.EXTRA_IS_MALE, storage.isMale());
        intent.putExtra(MainActivity.EXTRA_ACTIVITY, storage.getActivity());
        intent.putExtra(MainActivity.EXTRA_GOAL, storage.getGoal());

        startActivity(intent);
    }
}
