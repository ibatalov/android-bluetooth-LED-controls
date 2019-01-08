package led_strip_control.batalov.ivan.ledstripcontrol.modules;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import led_strip_control.batalov.ivan.ledstripcontrol.ColorPanelView;
import led_strip_control.batalov.ivan.ledstripcontrol.HueSeekBar;
import led_strip_control.batalov.ivan.ledstripcontrol.LedSettingsFragment;
import led_strip_control.batalov.ivan.ledstripcontrol.R;

/**
 * Created by Ivan on 5/17/2016.
 */
public class PaletteModule extends Fragment implements LedModule{

    ModuleActionListener mListener;

    CardView topView;
    TextView moduleTitle;
    LinearLayout moduleContent;

    Button saveButton;
    Button loadButton;

    ColorPanelView colorPickerPanel;

    HueSeekBar hSeekBar;
    CheckBox instantUpdate;
    Button sendHSV;

    int minColor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        minColor = getActivity().getResources().getColor(R.color.val_min);

        topView = (CardView) inflater.inflate(R.layout.palette_module, null);

        LinearLayout topLayout = (LinearLayout) topView.findViewById(R.id.palette_module_top_layout);
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

        moduleTitle = (TextView) topView.findViewById(R.id.led_settings_hsv_module_title);
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
                        mListener.onModuleSelected(PaletteModule.this);
                    }
                }
            }
        });

        moduleContent = (LinearLayout) topView.findViewById(R.id.palette_module_content);
        moduleContent.setVisibility(View.GONE);

        saveButton = (Button) moduleContent.findViewById(R.id.palette_module_save_settings);
        loadButton = (Button) moduleContent.findViewById(R.id.palette_module_load_settings);

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

        colorPickerPanel = (ColorPanelView) topView.findViewById(R.id.led_settings_color_picker_panel);
        colorPickerPanel.setOnColorPickedListener(new ColorPanelView.OnColorPickedListener() {
            @Override
            public void onColorPicked(int color) {
                if(instantUpdate.isChecked()){
                    compileMessage(false);
                }
            }

            @Override
            public void onStopChangingColor(){
                if(instantUpdate.isChecked()){
                    compileMessage(true);
                }
            }
        });

        hSeekBar = (HueSeekBar) topView.findViewById(R.id.led_settings_seek_bar_hue);
        hSeekBar.setMax(359);
        hSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            float[] tempHsv = new float[3];

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float progressFraction = (float) progress / seekBar.getMax();
                int hue = Math.round(359 * progressFraction);
                tempHsv[0] = hue;
                tempHsv[1] = 1f;
                tempHsv[2] = 1f;
                //colorPickerPanel.setPickedColorThirdValue(hue);
                colorPickerPanel.setNewPanelParameters(ColorPanelView.VAR_SAT, ColorPanelView.VAR_VAL, minColor, Color.HSVToColor(tempHsv));

                if (instantUpdate.isChecked()) {
                    compileMessage(false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(seekBar.getId() == R.id.led_settings_seek_bar_hue) {
                    float progressFraction = (float)seekBar.getProgress()/seekBar.getMax();
                    int hue = Math.round(359 * progressFraction);
                    tempHsv[0] = hue;
                    tempHsv[1] = 1f;
                    tempHsv[2] = 1f;
                    colorPickerPanel.setNewPanelParameters(ColorPanelView.VAR_SAT, ColorPanelView.VAR_VAL, minColor, Color.HSVToColor(tempHsv));
                }
                if (instantUpdate.isChecked()) {
                    compileMessage(true);
                }
            }
        });

        instantUpdate = (CheckBox) topView.findViewById(R.id.palette_module_instant_update);

        sendHSV = (Button) topView.findViewById(R.id.led_settings_send_hsv);
        sendHSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compileMessage(true);
            }
        });

        return topView;
    }

    private void compileMessage(boolean checkReception){

        String message = LedSettingsFragment.MODE_HSV + " ";
        float[] lastSentHSV = colorPickerPanel.getPickedHSV();

        if(lastSentHSV[0] < 0){
            lastSentHSV[0] = 0;
        } else if(lastSentHSV[0] > 359){
            lastSentHSV[0] = 359;
        }
        if(lastSentHSV[1] < 0){
            lastSentHSV[1] = 0;
        } else if(lastSentHSV[1] > 1){
            lastSentHSV[1] = 1;
        }
        if(lastSentHSV[2] < 0){
            lastSentHSV[2] = 0;
        } else if(lastSentHSV[2] > 1){
            lastSentHSV[2] = 1;
        }

        String h_s = String.valueOf(Math.round(lastSentHSV[0]*1000)/1000f);
        String s_s = String.valueOf(Math.round(lastSentHSV[1]*1000)/1000f);
        String v_s = String.valueOf(Math.round(lastSentHSV[2]*1000)/1000f);

        message += h_s + " " + s_s + " " + v_s;

        Bundle options = new Bundle();
        if(!checkReception){
            options.putInt("mode", LedSettingsFragment.MODE_HSV);
            options.putBoolean("erasable", true);
            options.putBoolean("verify_successful_transfer", false);
            options.putBoolean("wait_until_receiver_ready", false);
        } else{
            options.putInt("mode", LedSettingsFragment.MODE_HSV);
            options.putBoolean("erasable", false);
            options.putBoolean("verify_successful_transfer", true);
            options.putBoolean("wait_until_receiver_ready", true);
        }
        if(mListener != null) {
            mListener.sendMessage(message, options);
        }
    }

    public void toggleModule(boolean expand){
        if(expand){
            moduleContent.setVisibility(View.VISIBLE);
            topView.setCardElevation(8 * getResources().getDisplayMetrics().density);
        } else{
            moduleContent.setVisibility(View.GONE);
            topView.setCardElevation(2*getResources().getDisplayMetrics().density);
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
                SharedPreferences prefs = getActivity().getSharedPreferences("palette_module_preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                String key;
                if(editText.getText().toString().length() > 0){
                    key = editText.getText().toString();
                } else{
                    key = "No name";
                }
                String value = String.valueOf(System.currentTimeMillis()) + " ";
                value += Math.round(colorPickerPanel.getPickedHSV()[0]*1000)/1000f + " ";
                value += Math.round(colorPickerPanel.getPickedHSV()[1]*1000)/1000f + " ";
                value += Math.round(colorPickerPanel.getPickedHSV()[2]*1000)/1000f + " ";
                value += instantUpdate.isChecked() ? "true" : "false";

                editor.putString(key, value);
                editor.commit();
            }
        });
        builder.create().show();
    }

    public void openLoadSettingsMenu(){
        final SharedPreferences prefs = getActivity().getSharedPreferences("palette_module_preferences", Context.MODE_PRIVATE);
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
                final float hue = Float.valueOf(data[1]);
                final boolean updateInstantly = data[4].equals("true");

                Date date = new Date(timeStamp);
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");


                LinearLayout item = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.saved_settings_item_layout, null);
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hSeekBar.setProgress(Math.round(hue));

                        float[] tempHsv = new float[3];
                        tempHsv[0] = hue;
                        tempHsv[1] = 1f;
                        tempHsv[2] = 1f;
                        colorPickerPanel.setNewPanelParameters(ColorPanelView.VAR_SAT, ColorPanelView.VAR_VAL, getActivity().getResources().getColor(R.color.val_min), Color.HSVToColor(tempHsv));
                        colorPickerPanel.setPickedColor(Float.valueOf(data[2]), Float.valueOf(data[3]));
                        instantUpdate.setChecked(updateInstantly);
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
                ((TextView)item.findViewById(R.id.saved_settings_item_details)).setText("(h,s,v) = (" + Math.round(hue) + "," + data[2] + "," + data[3] + ") " + (updateInstantly ? "+" : "-"));
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
