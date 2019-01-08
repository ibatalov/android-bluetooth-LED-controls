package led_strip_control.batalov.ivan.ledstripcontrol.modules;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import led_strip_control.batalov.ivan.ledstripcontrol.LedSettingsFragment;
import led_strip_control.batalov.ivan.ledstripcontrol.R;

/**
 * Created by Ivan on 5/17/2016.
 */
public class LEDAnimationModule extends Fragment implements View.OnClickListener, LedModule {
    ModuleActionListener mListener;

    private CardView topView;
    private TextView title;
    private LinearLayout moduleContent;

    Button saveButton;
    Button loadButton;

    private RadioGroup colorOrder;
    private RadioGroup transitionType;
    private SeekBar durationBar;
    private TextView durationValue;
    private SeekBar brightnessBar;
    private TextView brightnessValue;
    private Button startAnimation;

    RadioGroup animationDurationScale;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        topView = (CardView) inflater.inflate(R.layout.led_animation_module, null);

        LinearLayout topLayout = (LinearLayout) topView.findViewById(R.id.led_animation_module_top_layout);
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

        title = (TextView) topView.findViewById(R.id.led_settings_animation_module_title);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(moduleContent.getVisibility() == View.VISIBLE){
                    moduleContent.setVisibility(View.GONE);
                    topView.setCardElevation(2*getResources().getDisplayMetrics().density);
                } else{
                    moduleContent.setVisibility(View.VISIBLE);
                    topView.setCardElevation(8 * getResources().getDisplayMetrics().density);
                    if(mListener != null){
                        mListener.onModuleSelected(LEDAnimationModule.this);
                    }
                }
            }
        });

       // LinearLayout topLayout = (LinearLayout) topView.findViewById(R.id.animation_module_top_layout);
       // LayoutTransition layoutTransition = new LayoutTransition();
       // topLayout.setLayoutTransition(layoutTransition);

        moduleContent = (LinearLayout) topView.findViewById(R.id.led_animation_module_content);
        moduleContent.setVisibility(View.GONE);

        saveButton = (Button) moduleContent.findViewById(R.id.led_animation_module_save_settings);
        loadButton = (Button) moduleContent.findViewById(R.id.led_animation_module_load_settings);

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

        colorOrder = (RadioGroup) topView.findViewById(R.id.led_animation_color_order_radio_group);
        transitionType = (RadioGroup) topView.findViewById(R.id.led_animation_radio_group);
        transitionType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(mListener != null && mListener.getLastSentMode() == LedSettingsFragment.MODE_LED_ANIMATION){
                    compileMessage(true);
                }
            }
        });
        durationBar = (SeekBar) topView.findViewById(R.id.led_animation_duration_seek_bar);
        durationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float multiplier = 0;
                switch(animationDurationScale.getCheckedRadioButtonId()){
                    case R.id.led_animation_duration_scale_x0_01:
                        multiplier = 0.01f;
                        break;
                    case R.id.led_animation_duration_scale_x1:
                        multiplier = 1.f;
                        break;
                    case R.id.led_animation_duration_scale_x100:
                        multiplier = 100.f;
                        break;
                    case R.id.led_animation_duration_scale_x10000:
                        multiplier = 10000.f;
                        break;
                }
                if(progress == 0){
                    progress = 1;
                    durationBar.setProgress(progress);
                }
                durationValue.setText(String.valueOf(Math.round(progress * multiplier*100)/100f));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        durationValue = (TextView) topView.findViewById(R.id.led_animation_duration_value);
        animationDurationScale = (RadioGroup) topView.findViewById(R.id.led_animation_duration_scale);
        animationDurationScale.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                float multiplier = 0f;
                switch(checkedId){
                    case R.id.led_animation_duration_scale_x0_01:
                        multiplier = 0.01f;
                        break;
                    case R.id.led_animation_duration_scale_x1:
                        multiplier = 1.f;
                        break;
                    case R.id.led_animation_duration_scale_x100:
                        multiplier = 100.f;
                        break;
                    case R.id.led_animation_duration_scale_x10000:
                        multiplier = 10000.f;
                        break;
                }
                int progress = durationBar.getProgress();
                durationValue.setText(String.valueOf(Math.round(progress * multiplier*100)/100f));
            }
        });

        brightnessBar = (SeekBar) topView.findViewById(R.id.led_animation_brightness_seek_bar);
        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightnessValue.setText(LedSettingsFragment.trimOrPadWithZeros(String.valueOf((float) progress / 100), 4, false));
                if(mListener.getLastSentMode() == LedSettingsFragment.MODE_LED_ANIMATION){
                    compileMessage(false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(mListener.getLastSentMode() == LedSettingsFragment.MODE_LED_ANIMATION){
                    compileMessage(true);
                }
            }
        });
        brightnessValue = (TextView) topView.findViewById(R.id.led_animation_brightness_value);
        startAnimation = (Button) topView.findViewById(R.id.led_animation_start_button);
        startAnimation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compileMessage(true);
            }
        });

        return topView;
    }

    private void compileMessage(boolean checkReception){

        //anim  color_order color_change_method  color_duration/1000000  color_brightness
        String message = "anim ";
        if(colorOrder.getCheckedRadioButtonId() == R.id.led_animation_color_order_determined){
            message += "0 ";
        } else if(colorOrder.getCheckedRadioButtonId() == R.id.led_animation_color_order_random){
            message += "1 ";
        }
        if(transitionType.getCheckedRadioButtonId() == R.id.led_animation_continuous_button){
            message += "0 ";
        } else if(transitionType.getCheckedRadioButtonId() == R.id.led_animation_instant_button){
            message += "1 ";
        } else if(transitionType.getCheckedRadioButtonId() == R.id.led_animation_smoothed_button){
            message += "2 ";
        }
        float multiplier = 0;
        switch(animationDurationScale.getCheckedRadioButtonId()){
            case R.id.led_animation_duration_scale_x0_01:
                multiplier = 0.01f;
                break;
            case R.id.led_animation_duration_scale_x1:
                multiplier = 1.f;
                break;
            case R.id.led_animation_duration_scale_x100:
                multiplier = 100.f;
                break;
            case R.id.led_animation_duration_scale_x10000:
                multiplier = 10000.f;
                break;
        }
        message += String.valueOf((long)(durationBar.getProgress() * multiplier*1000000)) + " ";
        message += String.valueOf((float)brightnessBar.getProgress()/100);
        if(mListener != null){
            if(checkReception) {
                Bundle options = new Bundle();
                options.putInt("mode", LedSettingsFragment.MODE_LED_ANIMATION);
                options.putBoolean("erasable", false);
                options.putBoolean("verify_successful_transfer", true);
                options.putBoolean("wait_until_receiver_ready", true);
                mListener.sendMessage(message, options);
            } else{
                Bundle options = new Bundle();
                options.putInt("mode", LedSettingsFragment.MODE_LED_ANIMATION);
                options.putBoolean("erasable", true);
                options.putBoolean("verify_successful_transfer", false);
                options.putBoolean("wait_until_receiver_ready", false);
                mListener.sendMessage(message, options);
            }
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
                SharedPreferences prefs = getActivity().getSharedPreferences("led_animation_module_preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                String key;
                if(editText.getText().toString().length() > 0){
                    key = editText.getText().toString();
                } else{
                    key = "No name";
                }
                String value = String.valueOf(System.currentTimeMillis()) + " ";

                if(colorOrder.getCheckedRadioButtonId() == R.id.led_animation_color_order_determined){
                    value += "deterministic ";
                } else if(colorOrder.getCheckedRadioButtonId() == R.id.led_animation_color_order_random){
                    value += "random ";
                }
                if(transitionType.getCheckedRadioButtonId() == R.id.led_animation_continuous_button){
                    value += "continuous ";
                } else if(transitionType.getCheckedRadioButtonId() == R.id.led_animation_instant_button){
                    value += "instant ";
                } else if(transitionType.getCheckedRadioButtonId() == R.id.led_animation_smoothed_button){
                    value += "smoothed ";
                }
                switch(animationDurationScale.getCheckedRadioButtonId()){
                    case R.id.led_animation_duration_scale_x0_01:
                        value += "0.01 ";
                        break;
                    case R.id.led_animation_duration_scale_x1:
                        value += "1 ";
                        break;
                    case R.id.led_animation_duration_scale_x100:
                        value += "100 ";
                        break;
                    case R.id.led_animation_duration_scale_x10000:
                        value += "10000 ";
                        break;
                }
                value += durationBar.getProgress() + " ";
                value += brightnessBar.getProgress() + " ";

                editor.putString(key, value);
                editor.commit();
            }
        });
        builder.create().show();
    }

    public void openLoadSettingsMenu(){
        final SharedPreferences prefs = getActivity().getSharedPreferences("led_animation_module_preferences", Context.MODE_PRIVATE);
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

                final int durationBarProgress = Integer.valueOf(data[4]);
                final int brightnessBarProgress = Integer.valueOf(data[5]);

                Date date = new Date(timeStamp);
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");


                LinearLayout item = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.saved_settings_item_layout, null);
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(data[1].equals("deterministic")){
                            colorOrder.check(R.id.led_animation_color_order_determined);
                        }else if(data[1].equals("random")){
                            colorOrder.check(R.id.led_animation_color_order_random);
                        }

                        if(data[2].equals("continuous")){
                            transitionType.check(R.id.led_animation_continuous_button);
                        } else if(data[2].equals("instant")){
                            transitionType.check(R.id.led_animation_instant_button);
                        } else if(data[2].equals("smoothed")){
                            transitionType.check(R.id.led_animation_smoothed_button);
                        }
                        switch(data[3]){
                            case "0.01":
                                animationDurationScale.check(R.id.led_animation_duration_scale_x0_01);
                                break;
                            case "1":
                                animationDurationScale.check(R.id.led_animation_duration_scale_x1);
                                break;
                            case "100":
                                animationDurationScale.check(R.id.led_animation_duration_scale_x100);
                                break;
                            case "10000":
                                animationDurationScale.check(R.id.led_animation_duration_scale_x10000);
                                break;
                        }
                        durationBar.setProgress(durationBarProgress);
                        brightnessBar.setProgress(brightnessBarProgress);
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
                ((TextView)item.findViewById(R.id.saved_settings_item_details)).setText(""); // no details for now, maybe I'll come with what to write here later
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
        return topView;
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

    @Override
    public void onClick(View v) {
        
    }

    @Override
    public void toggleModule(boolean expand) {
        if(expand){
            moduleContent.setVisibility(View.VISIBLE);
            topView.setCardElevation(8 * getResources().getDisplayMetrics().density);
        } else{
            moduleContent.setVisibility(View.GONE);
            topView.setCardElevation(2*getResources().getDisplayMetrics().density);
        }
    }
}
