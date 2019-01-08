package led_strip_control.batalov.ivan.ledstripcontrol.modules;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import led_strip_control.batalov.ivan.ledstripcontrol.LedBinPicker;
import led_strip_control.batalov.ivan.ledstripcontrol.LedSettingsFragment;
import led_strip_control.batalov.ivan.ledstripcontrol.R;

/**
 * Created by Ivan on 5/17/2016.
 */
public class MusicVisualizationModule extends Fragment implements LedModule {

    ModuleActionListener mListener;

    private CardView topView;

    TextView moduleTitle;
    LinearLayout moduleContent;

    Button saveButton;
    Button loadButton;

    EditText noiseDuration;
    Button recordNoise;

    TextView fBins;
    SeekBar fBinsSeekBar;
    TextView ledBins;
    SeekBar ledBinsSeekBar;

    RadioGroup binSplitMethod;

    RadioGroup ledBinOrder;
    LinearLayout binOrderChangeSpeedLayout;

    RadioGroup ledBinSize;

    RadioGroup ledBinBrightnessType;
    CheckBox variableTotalLengthBox;
    LinearLayout ledLengthAndBrightnessSettings;

    RadioGroup binLeds;

    SeekBar brightnessOffsetSeekBar;
    TextView brightnessOffsetValue;

    TextView maxIntensityMemoryCoeff;
    SeekBar maxIntensityMemorySeekBar;

    TextView currSignalAveragingCoeff;
    SeekBar currSignalAveragingSeekBar;
    Button sendAudioVisualizationSettings;

    LedBinPicker ledBinPicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        topView = (CardView) inflater.inflate(R.layout.music_visualization_module, null);

        LinearLayout topLayout = (LinearLayout) topView.findViewById(R.id.music_visualisation_module_top_layout);
        LayoutTransition transition = topLayout.getLayoutTransition();
        if (Build.VERSION.SDK_INT > 16) {
            transition.enableTransitionType(LayoutTransition.CHANGING);
        }
        transition.setStartDelay(LayoutTransition.APPEARING, 0);
        transition.setStartDelay(LayoutTransition.DISAPPEARING, 0);
        transition.setStartDelay(LayoutTransition.CHANGING, 0);
        transition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
        transition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        transition.setDuration(300L);

        moduleTitle = (TextView) topView.findViewById(R.id.led_settings_sound_visualizer_module_title);
        moduleTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moduleContent.getVisibility() == View.VISIBLE) {
                    moduleContent.setVisibility(View.GONE);
                    topView.setCardElevation(2 * getResources().getDisplayMetrics().density);
                } else {
                    moduleContent.setVisibility(View.VISIBLE);
                    topView.setCardElevation(8 * getResources().getDisplayMetrics().density);
                    if (mListener != null) {
                        mListener.onModuleSelected(MusicVisualizationModule.this);
                    }
                }
            }
        });

        moduleContent = (LinearLayout) topView.findViewById(R.id.music_visualisation_module_content);
        moduleContent.setVisibility(View.GONE);

        saveButton = (Button) moduleContent.findViewById(R.id.music_visualization_module_save_settings);
        loadButton = (Button) moduleContent.findViewById(R.id.music_visualization_module_load_settings);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentSettings();
            }
        });
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoadSettingsMenu();
            }
        });

        LayoutTransition contentTransition = moduleContent.getLayoutTransition();
        if (Build.VERSION.SDK_INT > 16) {
            contentTransition.enableTransitionType(LayoutTransition.CHANGING);
        }
        contentTransition.setStartDelay(LayoutTransition.APPEARING, 0);
        contentTransition.setStartDelay(LayoutTransition.DISAPPEARING, 0);
        contentTransition.setStartDelay(LayoutTransition.CHANGING, 0);
        contentTransition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
        contentTransition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        contentTransition.setDuration(300L);

        fBins = (TextView) topView.findViewById(R.id.led_settings_freq_bins_value);
        fBinsSeekBar = (SeekBar) topView.findViewById(R.id.led_settings_freq_bins_seek_bar);
        fBinsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress == 0){
                    progress = 1;
                    seekBar.setProgress(progress);
                }
                fBins.setText(String.valueOf(progress));
                if(ledBinsSeekBar.getProgress() > progress || ledBinsSeekBar.getProgress() == ledBinsSeekBar.getMax()){
                    ledBinsSeekBar.setMax(progress);
                    ledBinsSeekBar.setProgress(progress);
                    ledBins.setText(String.valueOf(progress));
                } else {
                    ledBinsSeekBar.setMax(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        ledBins = (TextView) topView.findViewById(R.id.led_settings_led_bins_value);
        ledBinsSeekBar = (SeekBar) topView.findViewById(R.id.led_settings_led_bins_seek_bar);
        ledBinsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress == 0){
                    progress = 1;
                    seekBar.setProgress(progress);
                }
                ledBins.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binSplitMethod = (RadioGroup) topView.findViewById(R.id.led_settings_bin_split_method_radio_group);

        ledBinOrder = (RadioGroup) topView.findViewById(R.id.led_settings_bin_order_radio_group);
        ledBinOrder.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.led_settings_led_bin_order_ascending_freq:
                        binOrderChangeSpeedLayout.setVisibility(View.GONE);
                        break;
                    case R.id.led_settings_led_bin_order_descending_ampl:
                        binOrderChangeSpeedLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.led_settings_led_bin_order_ascending_sorted_freq:
                        binOrderChangeSpeedLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        binOrderChangeSpeedLayout = (LinearLayout) topView.findViewById(R.id.music_visualisation_module_sorting_speed_coeff);

        ledBinSize = (RadioGroup) topView.findViewById(R.id.led_settings_led_bin_size_radio_group);
        ledBinSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.led_settings_led_bin_size_constant){
                    ledLengthAndBrightnessSettings.setVisibility(View.GONE);
                } else if(checkedId == R.id.led_settings_led_bin_size_variable || checkedId == R.id.led_settings_led_bin_size_rainbow){
                    ledLengthAndBrightnessSettings.setVisibility(View.VISIBLE);
                }
                if(checkedId == R.id.led_settings_led_bin_size_rainbow && brightnessOffsetSeekBar.getProgress() == 0){
                    brightnessOffsetSeekBar.setProgress(brightnessOffsetSeekBar.getMax()/10);
                }
            }
        });

        ledLengthAndBrightnessSettings = (LinearLayout) topView.findViewById(R.id.music_visualisation_module_led_length_and_brightness_settings);

        ledBinBrightnessType = (RadioGroup) topView.findViewById(R.id.led_settings_led_bin_brightness_radio_group);
        variableTotalLengthBox = (CheckBox) topView.findViewById(R.id.led_settings_total_lit_led_length);

        brightnessOffsetSeekBar = (SeekBar) topView.findViewById(R.id.led_settings_brightness_offset_seek_bar);
        brightnessOffsetValue = (TextView) topView.findViewById(R.id.led_settings_brightness_offset_value);
        brightnessOffsetSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightnessOffsetValue.setText(LedSettingsFragment.trimOrPadWithZeros(String.valueOf((float) progress / brightnessOffsetSeekBar.getMax()), 4, false));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        maxIntensityMemoryCoeff = (TextView) topView.findViewById(R.id.led_settings_max_averaging_coeff);
        currSignalAveragingCoeff = (TextView) topView.findViewById(R.id.led_settings_current_signal_averaging_coeff);
        currSignalAveragingSeekBar = (SeekBar) topView.findViewById(R.id.led_settings_curr_signal_aver_coeff_seek_bar);
        currSignalAveragingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (currSignalAveragingCoeff != null) {
                    float coeff = (float)Math.pow(0.001, progress/1000d);
                    currSignalAveragingCoeff.setText(LedSettingsFragment.trimOrPadWithZeros(String.valueOf(coeff), 5, false));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        maxIntensityMemorySeekBar = (SeekBar) topView.findViewById(R.id.led_settings_max_averaging_coeff_seek_bar);
        maxIntensityMemorySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(maxIntensityMemoryCoeff != null){
                    float coeff = (float)Math.pow(0.001, progress/1000d);
                    maxIntensityMemoryCoeff.setText(LedSettingsFragment.trimOrPadWithZeros(String.valueOf(coeff), 5, false));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        sendAudioVisualizationSettings = (Button) topView.findViewById(R.id.led_settings_send_sound_visualization);
        sendAudioVisualizationSettings.getBackground().setColorFilter(getResources().getColor(R.color.palette_color_4_neutral), PorterDuff.Mode.MULTIPLY);
        sendAudioVisualizationSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compileMessage();
            }
        });

        noiseDuration = (EditText) topView.findViewById(R.id.led_settings_noise_duration);
        recordNoise = (Button) topView.findViewById(R.id.led_settings_noise_record);
        recordNoise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int duration = Integer.parseInt(noiseDuration.getText().toString());
                if(duration > 0 && duration < 65535){
                    String message = LedSettingsFragment.MODE_NOISE + " " + duration;
                    Bundle options = new Bundle();
                    options.putInt("mode", LedSettingsFragment.MODE_AUDIO);
                    options.putBoolean("erasable", false);
                    options.putBoolean("verify_successful_transfer", true);
                    options.putBoolean("wait_until_receiver_ready", true);
                    mListener.sendMessage(message, options);
                }
            }
        });


        return topView;
    }

    private void compileMessage(){
        String message = LedSettingsFragment.MODE_AUDIO + " ";
        message += fBinsSeekBar.getProgress() + " ";
        message += ledBinsSeekBar.getProgress() + " ";
        switch(ledBinOrder.getCheckedRadioButtonId()){
            case R.id.led_settings_led_bin_order_ascending_freq:
                message += "0 ";
                break;
            case R.id.led_settings_led_bin_order_descending_ampl:
                message += "1 ";
                break;
            case R.id.led_settings_led_bin_order_ascending_sorted_freq:
                message += "2 ";
                break;
        }
        message += maxIntensityMemoryCoeff.getText().toString() + " ";
        message += currSignalAveragingCoeff.getText().toString() + " ";

        if(ledBinSize.getCheckedRadioButtonId() == R.id.led_settings_led_bin_size_constant){
            message += "0 ";
        } else if(ledBinSize.getCheckedRadioButtonId() == R.id.led_settings_led_bin_size_variable){
            message += "1 ";
        } else if(ledBinSize.getCheckedRadioButtonId() == R.id.led_settings_led_bin_size_rainbow){
            message += "2 ";
        }
        message += (variableTotalLengthBox.isChecked() ? "1 " : "0 ");

        switch(ledBinBrightnessType.getCheckedRadioButtonId()){
            case R.id.led_settings_led_bin_individual_brightness:
                message += "0 ";
                break;
            case R.id.led_settings_led_bin_average_brightness:
                message += "1 ";
                break;
            case R.id.led_settings_led_bin_interpolated_brightness:
                message += "2 ";
                break;
        }

        float brightnessOffset = (float)brightnessOffsetSeekBar.getProgress()/brightnessOffsetSeekBar.getMax();
        brightnessOffset = Math.round(brightnessOffset * 100)/100f;
        message += brightnessOffset;

        Bundle options = new Bundle();
        options.putInt("mode", LedSettingsFragment.MODE_AUDIO);
        options.putBoolean("erasable", false);
        options.putBoolean("verify_successful_transfer", true);
        options.putBoolean("wait_until_receiver_ready", true);
        mListener.sendMessage(message, options);

        message = "fsm ";
        if(binSplitMethod.getCheckedRadioButtonId() == R.id.led_settings_bin_split_method_noise){
            message += "0";
        } else if(binSplitMethod.getCheckedRadioButtonId() == R.id.led_settings_bin_split_method_bark){
            message += "1 ";
        }
        options = new Bundle();
        options.putInt("mode", LedSettingsFragment.MODE_AUDIO);
        options.putBoolean("erasable", false);
        options.putBoolean("verify_successful_transfer", true);
        options.putBoolean("wait_until_receiver_ready", true);
        mListener.sendMessage(message, options);
    }

    public void toggleModule(boolean expand){
        if(expand){
            topView.setCardElevation(8 * getResources().getDisplayMetrics().density);
            moduleContent.setVisibility(View.VISIBLE);
        } else{
            topView.setCardElevation(2 * getResources().getDisplayMetrics().density);
            moduleContent.setVisibility(View.GONE);
        }
    }

    public void saveCurrentSettings(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        builder.setTitle("Enter name");
        final EditText editText = new EditText(getActivity());
        //editText.setPadding(10, 5, 10, 5);
        editText.requestFocus();
        editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        builder.setView(editText);
        builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences prefs = getActivity().getSharedPreferences("music_visualization_module_preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                String key;
                if(editText.getText().toString().length() > 0){
                    key = editText.getText().toString();
                } else{
                    key = "No name";
                }
                String value = String.valueOf(System.currentTimeMillis()) + " ";
                value += fBinsSeekBar.getProgress() + " ";
                value += ledBinsSeekBar.getProgress() + " ";
                if(binSplitMethod.getCheckedRadioButtonId() == R.id.led_settings_bin_split_method_noise){
                    value += "noise ";
                } else if(binSplitMethod.getCheckedRadioButtonId() == R.id.led_settings_bin_split_method_bark){
                    value += "bark ";
                }
                switch(ledBinOrder.getCheckedRadioButtonId()){
                    case R.id.led_settings_led_bin_order_ascending_freq:
                        value += "ascending ";
                        break;
                    case R.id.led_settings_led_bin_order_descending_ampl:
                        value += "descending ";
                        break;
                    case R.id.led_settings_led_bin_order_ascending_sorted_freq:
                        value += "ascending_sorted ";
                        break;
                }
                value += maxIntensityMemorySeekBar.getProgress() + " ";

                switch (ledBinSize.getCheckedRadioButtonId()){
                    case R.id.led_settings_led_bin_size_constant:
                        value += "constant ";
                        break;
                    case R.id.led_settings_led_bin_size_variable:
                        value += "variable ";
                        break;
                    case R.id.led_settings_led_bin_size_rainbow:
                        value += "rainbow ";
                        break;
                }
                value += variableTotalLengthBox.isChecked() + " ";

                switch (ledBinBrightnessType.getCheckedRadioButtonId()){
                    case R.id.led_settings_led_bin_individual_brightness:
                        value += "individual ";
                        break;
                    case R.id.led_settings_led_bin_average_brightness:
                        value += "average ";
                        break;
                    case R.id.led_settings_led_bin_interpolated_brightness:
                        value += "smoothed ";
                        break;
                }
                value += brightnessOffsetSeekBar.getProgress() + " ";
                value += currSignalAveragingSeekBar.getProgress() + " ";
                value += noiseDuration.getText().toString();

                editor.putString(key, value);
                editor.commit();
            }
        });
        builder.create().show();
    }

    public void openLoadSettingsMenu(){
        final SharedPreferences prefs = getActivity().getSharedPreferences("music_visualization_module_preferences", Context.MODE_PRIVATE);
        if(prefs.getAll().size() > 0){
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
            //builder.setTitle("Choose setting to load");

            ScrollView scrollView = new ScrollView(getActivity());
            scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            scrollView.setPadding(0,0,0,0);

            final LinearLayout ll = new LinearLayout(getActivity());
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setDividerDrawable(getActivity().getResources().getDrawable(R.drawable.custom_divider));
            ll.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ll.setPadding(5, 5, 5, 5);
            scrollView.addView(ll);
            builder.setView(scrollView);
            final AlertDialog dialog = builder.create();

            Map<String, ?> savedSettings = prefs.getAll();
            TreeMap<Long, LinearLayout> items = new TreeMap<>();

            for(String name : savedSettings.keySet()){
                String value = prefs.getString(name, null);
                final String[] data = value.split(" ");
                long timeStamp = Long.valueOf(data[0]);

                Date date = new Date(timeStamp);
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");


                LinearLayout item = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.saved_settings_item_layout, null);
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fBinsSeekBar.setProgress(Integer.valueOf(data[1]));
                        ledBinsSeekBar.setProgress(Integer.valueOf(data[2]));

                        if(data[3].equals("noise")){
                            binSplitMethod.check(R.id.led_settings_bin_split_method_noise);
                        } else if(data[3].equals("bark")){
                            binSplitMethod.check(R.id.led_settings_bin_split_method_bark);
                        }

                        switch(data[4]){
                            case "ascending":
                                ledBinOrder.check(R.id.led_settings_led_bin_order_ascending_freq);
                                break;
                            case "descending":
                                ledBinOrder.check(R.id.led_settings_led_bin_order_descending_ampl);
                                break;
                            case "ascending_sorted":
                                ledBinOrder.check(R.id.led_settings_led_bin_order_ascending_sorted_freq);
                                break;
                        }
                        maxIntensityMemorySeekBar.setProgress(Integer.valueOf(data[5]));

                        switch (data[6]){
                            case "constant":
                                ledBinSize.check(R.id.led_settings_led_bin_size_constant);
                                break;
                            case "variable":
                                ledBinSize.check(R.id.led_settings_led_bin_size_variable);
                                break;
                            case "rainbow":
                                ledBinSize.check(R.id.led_settings_led_bin_size_rainbow);
                                break;
                        }
                        variableTotalLengthBox.setChecked(data[7].equals("true"));

                        switch (data[8]){
                            case "individual":
                                ledBinBrightnessType.check(R.id.led_settings_led_bin_individual_brightness);
                                break;
                            case "average":
                                ledBinBrightnessType.check(R.id.led_settings_led_bin_average_brightness);
                                break;
                            case "smoothed":
                                ledBinBrightnessType.check(R.id.led_settings_led_bin_interpolated_brightness);
                                break;
                        }
                        brightnessOffsetSeekBar.setProgress(Integer.valueOf(data[9]));
                        currSignalAveragingSeekBar.setProgress(Integer.valueOf(data[10]));
                        noiseDuration.setText(data[11]);
                        dialog.dismiss();
                    }
                });
                item.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View v) {
                        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                        deleteBuilder.setTitle("Remove setting?");
                        deleteBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = prefs.edit();
                                String name = ((TextView) v.findViewById(R.id.saved_settings_item_name)).getText().toString();
                                editor.remove(name);
                                editor.commit();
                                ll.removeView(v);
                            }
                        });
                        deleteBuilder.setNegativeButton("No", null);
                        deleteBuilder.create().show();
                        return true;
                    }
                });

                ((TextView)item.findViewById(R.id.saved_settings_item_date)).setText(format.format(date));
                ((TextView)item.findViewById(R.id.saved_settings_item_name)).setText(name);
                ((TextView)item.findViewById(R.id.saved_settings_item_details)).setText(data[1] + "/"  + data[2] + " " + data[6] + " bins");
                items.put(timeStamp, item);
            }

            for(Long timeStamp : items.descendingKeySet()){
                ll.addView(items.get(timeStamp));
            }
            dialog.show();
        }
    }

    public void setModuleActionListener(ModuleActionListener listener){
        mListener = listener;
    }


    @Override
    public View getView() {
        return super.getView();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
