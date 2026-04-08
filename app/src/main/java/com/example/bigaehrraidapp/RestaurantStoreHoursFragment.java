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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RestaurantStoreHoursFragment extends Fragment {

    Switch switchMonday, switchTuesday, switchWednesday, switchThursday,
           switchFriday, switchSaturday, switchSunday;

    View timeRowMonday, timeRowTuesday, timeRowWednesday, timeRowThursday,
         timeRowFriday, timeRowSaturday, timeRowSunday;

    TextView tvOpenMonday,  tvOpenTuesday,  tvOpenWednesday,  tvOpenThursday,
             tvOpenFriday,  tvOpenSaturday, tvOpenSunday;

    TextView tvCloseMonday,  tvCloseTuesday,  tvCloseWednesday,  tvCloseThursday,
             tvCloseFriday,  tvCloseSaturday, tvCloseSunday;

    Button btnSaveHours;

    RestaurantRepository restaurantRepo;

    private final String[] DAY_KEYS = {
        "monday","tuesday","wednesday","thursday","friday","saturday","sunday"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_store_hours, container, false);

        bindViews(view);
        setupSwitches();
        setupTimePickers();

        AuthRepository authRepo = AuthRepository.getInstance(requireContext());
        restaurantRepo = RestaurantRepository.getInstance(authRepo);

        loadStoreHours();

        btnSaveHours.setOnClickListener(v -> saveStoreHours());

        return view;
    }

    // ── Firebase ──────────────────────────────────────────────────────────────

    private void loadStoreHours() {
        restaurantRepo.loadStoreHours(new RestaurantRepository.Callback<Map<String, Object>>() {
            @Override
            @SuppressWarnings("unchecked")
            public void onSuccess(Map<String, Object> hoursMap) {
                if (hoursMap == null || hoursMap.isEmpty()) return;

                Switch[]   switches  = getSwitches();
                View[]     timeRows  = getTimeRows();
                TextView[] openTvs  = getOpenTextViews();
                TextView[] closeTvs = getCloseTextViews();

                for (int i = 0; i < DAY_KEYS.length; i++) {
                    Object dayObj = hoursMap.get(DAY_KEYS[i]);
                    if (!(dayObj instanceof Map)) continue;
                    Map<String, Object> day = (Map<String, Object>) dayObj;

                    Boolean isOpen    = (Boolean) day.get("isOpen");
                    String  openTime  = day.get("openTime")  != null ? day.get("openTime").toString()  : "09:00 AM";
                    String  closeTime = day.get("closeTime") != null ? day.get("closeTime").toString() : "10:00 PM";

                    switches[i].setChecked(Boolean.TRUE.equals(isOpen));
                    timeRows[i].setVisibility(Boolean.TRUE.equals(isOpen) ? View.VISIBLE : View.GONE);
                    openTvs[i].setText(openTime);
                    closeTvs[i].setText(closeTime);
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Could not load store hours", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveStoreHours() {
        Map<String, Object> hoursMap = new HashMap<>();
        Switch[]   switches  = getSwitches();
        TextView[] openTvs  = getOpenTextViews();
        TextView[] closeTvs = getCloseTextViews();

        for (int i = 0; i < DAY_KEYS.length; i++) {
            Map<String, Object> day = new HashMap<>();
            day.put("isOpen",    switches[i].isChecked());
            day.put("openTime",  openTvs[i].getText().toString());
            day.put("closeTime", closeTvs[i].getText().toString());
            hoursMap.put(DAY_KEYS[i], day);
        }

        restaurantRepo.saveStoreHours(hoursMap, new RestaurantRepository.Callback<Void>() {
            @Override public void onSuccess(Void data) {
                Toast.makeText(getContext(), "Store hours saved!", Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(String error) {
                Toast.makeText(getContext(), "Failed to save: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Switch[]   getSwitches()       { return new Switch[]{switchMonday,switchTuesday,switchWednesday,switchThursday,switchFriday,switchSaturday,switchSunday}; }
    private View[]     getTimeRows()       { return new View[]{timeRowMonday,timeRowTuesday,timeRowWednesday,timeRowThursday,timeRowFriday,timeRowSaturday,timeRowSunday}; }
    private TextView[] getOpenTextViews()  { return new TextView[]{tvOpenMonday,tvOpenTuesday,tvOpenWednesday,tvOpenThursday,tvOpenFriday,tvOpenSaturday,tvOpenSunday}; }
    private TextView[] getCloseTextViews() { return new TextView[]{tvCloseMonday,tvCloseTuesday,tvCloseWednesday,tvCloseThursday,tvCloseFriday,tvCloseSaturday,tvCloseSunday}; }

    // ── Existing bind/switch/timepicker methods (unchanged) ───────────────────

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
        Switch[] switches  = getSwitches();
        View[]   timeRows  = getTimeRows();
        for (int i = 0; i < switches.length; i++) {
            final View timeRow = timeRows[i];
            timeRow.setVisibility(switches[i].isChecked() ? View.VISIBLE : View.GONE);
            switches[i].setOnCheckedChangeListener((btn, isChecked) ->
                    timeRow.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        }
    }

    private void setupTimePickers() {
        for (TextView tv : getOpenTextViews())  setTimePicker(tv);
        for (TextView tv : getCloseTextViews()) setTimePicker(tv);
    }

    private void setTimePicker(TextView tv) {
        tv.setOnClickListener(v -> {
            String current = tv.getText().toString().trim();
            int hour = 9, minute = 0;
            try {
                String[] parts    = current.split(":");
                hour = Integer.parseInt(parts[0].trim());
                String[] minPeriod = parts[1].trim().split(" ");
                minute = Integer.parseInt(minPeriod[0]);
                if ("PM".equals(minPeriod[1]) && hour != 12) hour += 12;
                if ("AM".equals(minPeriod[1]) && hour == 12) hour  = 0;
            } catch (Exception ignored) {}

            new TimePickerDialog(getContext(), (picker, h, m) -> {
                String period = h >= 12 ? "PM" : "AM";
                int dh = h % 12; if (dh == 0) dh = 12;
                tv.setText(String.format(Locale.getDefault(), "%02d:%02d %s", dh, m, period));
            }, hour, minute, false).show();
        });
    }
}
