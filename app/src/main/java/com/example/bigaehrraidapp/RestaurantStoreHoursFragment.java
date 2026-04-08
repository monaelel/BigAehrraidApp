package com.example.bigaehrraidapp;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class RestaurantStoreHoursFragment extends Fragment {

    // Switches
    Switch switchMonday, switchTuesday, switchWednesday, switchThursday,
            switchFriday, switchSaturday, switchSunday;

    // Time row containers
    View timeRowMonday, timeRowTuesday, timeRowWednesday, timeRowThursday,
            timeRowFriday, timeRowSaturday, timeRowSunday;

    // Open time labels
    TextView tvOpenMonday, tvOpenTuesday, tvOpenWednesday, tvOpenThursday,
            tvOpenFriday, tvOpenSaturday, tvOpenSunday;

    // Close time labels
    TextView tvCloseMonday, tvCloseTuesday, tvCloseWednesday, tvCloseThursday,
            tvCloseFriday, tvCloseSaturday, tvCloseSunday;

    Button btnSaveHours;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_store_hours, container, false);

        bindViews(view);
        setupSwitches();
        setupTimePickers();

        btnSaveHours.setOnClickListener(v ->
                Toast.makeText(getContext(), "Store hours saved!", Toast.LENGTH_SHORT).show()
        );

        return view;
    }

    private void bindViews(View view) {
        switchMonday    = view.findViewById(R.id.switchMonday);
        switchTuesday   = view.findViewById(R.id.switchTuesday);
        switchWednesday = view.findViewById(R.id.switchWednesday);
        switchThursday  = view.findViewById(R.id.switchThursday);
        switchFriday    = view.findViewById(R.id.switchFriday);
        switchSaturday  = view.findViewById(R.id.switchSaturday);
        switchSunday    = view.findViewById(R.id.switchSunday);

        timeRowMonday    = view.findViewById(R.id.timeRowMonday);
        timeRowTuesday   = view.findViewById(R.id.timeRowTuesday);
        timeRowWednesday = view.findViewById(R.id.timeRowWednesday);
        timeRowThursday  = view.findViewById(R.id.timeRowThursday);
        timeRowFriday    = view.findViewById(R.id.timeRowFriday);
        timeRowSaturday  = view.findViewById(R.id.timeRowSaturday);
        timeRowSunday    = view.findViewById(R.id.timeRowSunday);

        tvOpenMonday    = view.findViewById(R.id.tvOpenMonday);
        tvOpenTuesday   = view.findViewById(R.id.tvOpenTuesday);
        tvOpenWednesday = view.findViewById(R.id.tvOpenWednesday);
        tvOpenThursday  = view.findViewById(R.id.tvOpenThursday);
        tvOpenFriday    = view.findViewById(R.id.tvOpenFriday);
        tvOpenSaturday  = view.findViewById(R.id.tvOpenSaturday);
        tvOpenSunday    = view.findViewById(R.id.tvOpenSunday);

        tvCloseMonday    = view.findViewById(R.id.tvCloseMonday);
        tvCloseTuesday   = view.findViewById(R.id.tvCloseTuesday);
        tvCloseWednesday = view.findViewById(R.id.tvCloseWednesday);
        tvCloseThursday  = view.findViewById(R.id.tvCloseThursday);
        tvCloseFriday    = view.findViewById(R.id.tvCloseFriday);
        tvCloseSaturday  = view.findViewById(R.id.tvCloseSaturday);
        tvCloseSunday    = view.findViewById(R.id.tvCloseSunday);

        btnSaveHours = view.findViewById(R.id.btnSaveHours);
    }

    private void setupSwitches() {
        Switch[] switches = {switchMonday, switchTuesday, switchWednesday,
                switchThursday, switchFriday, switchSaturday, switchSunday};
        View[] timeRows = {timeRowMonday, timeRowTuesday, timeRowWednesday,
                timeRowThursday, timeRowFriday, timeRowSaturday, timeRowSunday};

        for (int i = 0; i < switches.length; i++) {
            final View timeRow = timeRows[i];
            // Set initial visibility based on default checked state
            timeRow.setVisibility(switches[i].isChecked() ? View.VISIBLE : View.GONE);

            switches[i].setOnCheckedChangeListener((btn, isChecked) ->
                    timeRow.setVisibility(isChecked ? View.VISIBLE : View.GONE)
            );
        }
    }

    private void setupTimePickers() {
        // Open times
        setTimePicker(tvOpenMonday);
        setTimePicker(tvOpenTuesday);
        setTimePicker(tvOpenWednesday);
        setTimePicker(tvOpenThursday);
        setTimePicker(tvOpenFriday);
        setTimePicker(tvOpenSaturday);
        setTimePicker(tvOpenSunday);

        // Close times
        setTimePicker(tvCloseMonday);
        setTimePicker(tvCloseTuesday);
        setTimePicker(tvCloseWednesday);
        setTimePicker(tvCloseThursday);
        setTimePicker(tvCloseFriday);
        setTimePicker(tvCloseSaturday);
        setTimePicker(tvCloseSunday);
    }

    private void setTimePicker(TextView tv) {
        tv.setOnClickListener(v -> {
            // Parse current hour/minute from the label
            String current = tv.getText().toString().trim(); // e.g. "09:00 AM"
            int hour = 9, minute = 0;
            try {
                String[] parts = current.split(":");
                hour = Integer.parseInt(parts[0].trim());
                String[] minPeriod = parts[1].trim().split(" ");
                minute = Integer.parseInt(minPeriod[0]);
                if (minPeriod[1].equals("PM") && hour != 12) hour += 12;
                if (minPeriod[1].equals("AM") && hour == 12) hour = 0;
            } catch (Exception ignored) {}

            new TimePickerDialog(getContext(), (picker, selectedHour, selectedMinute) -> {
                String period = selectedHour >= 12 ? "PM" : "AM";
                int displayHour = selectedHour % 12;
                if (displayHour == 0) displayHour = 12;
                tv.setText(String.format(Locale.getDefault(), "%02d:%02d %s",
                        displayHour, selectedMinute, period));
            }, hour, minute, false).show();
        });
    }
}
