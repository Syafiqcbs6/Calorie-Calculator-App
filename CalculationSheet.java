package com.example.caloriecalculator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

/**
 * CalculationSheet
 * ----------------
 * A pop-up panel that slides up from the bottom and shows the step-by-step
 * working of a calculation. Used by the "See Calculation" buttons,
 * so the main screens stay clean and simple.
 */
public class CalculationSheet {

    public static void show(Context context, String title, String subtitle,
                            String[] stepTitles, String[] formulas) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.sheet_calculation, null);

        ((TextView) view.findViewById(R.id.tvSheetTitle)).setText(title);
        ((TextView) view.findViewById(R.id.tvSheetSubtitle)).setText(subtitle);

        LinearLayout container = view.findViewById(R.id.stepsContainer);
        for (int i = 0; i < stepTitles.length; i++) {
            View step = inflater.inflate(R.layout.item_calc_step, container, false);
            ((TextView) step.findViewById(R.id.tvStepNumber)).setText(String.valueOf(i + 1));
            ((TextView) step.findViewById(R.id.tvStepTitle)).setText(stepTitles[i]);
            ((TextView) step.findViewById(R.id.tvStepFormula)).setText(formulas[i]);
            container.addView(step);
        }

        view.findViewById(R.id.btnSheetClose).setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.getBehavior().setSkipCollapsed(true);
        dialog.show();
    }
}
