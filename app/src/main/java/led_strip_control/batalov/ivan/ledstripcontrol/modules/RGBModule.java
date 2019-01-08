package led_strip_control.batalov.ivan.ledstripcontrol.modules;

import android.animation.LayoutTransition;
import android.app.ActionBar;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import led_strip_control.batalov.ivan.ledstripcontrol.LedSettingsFragment;
import led_strip_control.batalov.ivan.ledstripcontrol.R;

/**
 * Created by Ivan on 5/17/2016.
 */
public class RGBModule extends Fragment implements LedModule{

    ModuleActionListener mListener;

    CardView topView;
    TextView moduleTitle;
    LinearLayout moduleContent;

    Button saveButton;
    Button loadButton;

    TextView r;
    TextView g;
    TextView b;

    SeekBar rSeekBar;
    SeekBar gSeekBar;
    SeekBar bSeekBar;

    CheckBox instantUpdate;
    Button sendRGB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        topView = (CardView) inflater.inflate(R.layout.rgb_module, null);

        LinearLayout topLayout = (LinearLayout) topView.findViewById(R.id.rgb_module_top_layout);
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

        moduleTitle = (TextView) topView.findViewById(R.id.rgb_module_title);
        moduleTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moduleContent.getVisibility() == View.VISIBLE) {
                    moduleContent.setVisibility(View.GONE);
                    topView.setCardElevation(2*getResources().getDisplayMetrics().density);
                } else {
                    moduleContent.setVisibility(View.VISIBLE);
                    if (mListener != null) {
                        mListener.onModuleSelected(RGBModule.this);
                        topView.setCardElevation(8 * getResources().getDisplayMetrics().density);
                    }
                }
            }
        });

        moduleContent = (LinearLayout) topView.findViewById(R.id.rgb_module_content);
        moduleContent.setVisibility(View.GONE);

        saveButton = (Button) moduleContent.findViewById(R.id.rgb_module_save_settings);
        loadButton = (Button) moduleContent.findViewById(R.id.rgb_module_load_settings);

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

        r = (TextView) topView.findViewById(R.id.led_settings_red);
        g = (TextView) topView.findViewById(R.id.led_settings_green);
        b = (TextView) topView.findViewById(R.id.led_settings_blue);

        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float progressFraction = (float)progress/seekBar.getMax();

                if(seekBar.getId() == R.id.led_settings_seek_bar_red){
                    int red = Math.round(255 * progressFraction);
                    r.setText(LedSettingsFragment.trimOrPadWithZeros(String.valueOf(red), 3, true));

                } else if(seekBar.getId() == R.id.led_settings_seek_bar_green){
                    int green = Math.round(255 * progressFraction);
                    g.setText(LedSettingsFragment.trimOrPadWithZeros(String.valueOf(green), 3, true));

                } else if(seekBar.getId() == R.id.led_settings_seek_bar_blue){
                    int blue = Math.round(255 * progressFraction);
                    b.setText(LedSettingsFragment.trimOrPadWithZeros(String.valueOf(blue), 3, true));
                }
                if(instantUpdate.isChecked()){
                    compileMessage(false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(instantUpdate.isChecked()){
                    compileMessage(true); // send the final state with verification
                }
            }
        };

        rSeekBar = (SeekBar) topView.findViewById(R.id.led_settings_seek_bar_red);
        rSeekBar.setMax(255);
        rSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        gSeekBar = (SeekBar) topView.findViewById(R.id.led_settings_seek_bar_green);
        gSeekBar.setMax(255);
        gSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        bSeekBar = (SeekBar) topView.findViewById(R.id.led_settings_seek_bar_blue);
        bSeekBar.setMax(255);
        bSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        instantUpdate = (CheckBox) topView.findViewById(R.id.rgb_module_instant_update);

        sendRGB = (Button) topView.findViewById(R.id.led_settings_send_rgb);
        sendRGB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compileMessage(true);
            }
        });

        return topView;
    }

    int[] lastSentRGB = new int[3];

    private void compileMessage(boolean checkReception){

        String message = LedSettingsFragment.MODE_HSV + " ";
        lastSentRGB[0] = Math.round(((float) rSeekBar.getProgress() / rSeekBar.getMax()) * 255);
        lastSentRGB[1] = Math.round(((float) gSeekBar.getProgress() / gSeekBar.getMax()) * 255);
        lastSentRGB[2] = Math.round(((float) bSeekBar.getProgress() / bSeekBar.getMax()) * 255);

        if(lastSentRGB[0] < 0){
            lastSentRGB[0] = 0;
            r.setText(String.valueOf(0));
        } else if(lastSentRGB[0] > 255){
            lastSentRGB[0] = 255;
            r.setText(String.valueOf(255));
        }
        if(lastSentRGB[1] < 0){
            lastSentRGB[1] = 0;
            g.setText(String.valueOf(0));
        } else if(lastSentRGB[1] > 255){
            lastSentRGB[1] = 255;
            g.setText(String.valueOf(255));
        }
        if(lastSentRGB[2] < 0){
            lastSentRGB[2] = 0;
            b.setText(String.valueOf(0));
        } else if(lastSentRGB[2] > 255){
            lastSentRGB[2] = 255;
            b.setText(String.valueOf(255));
        }

        // I do it like this so the zeros in textViews get removed when converted to int and then back to String
        String r_s = String.valueOf(lastSentRGB[0]);
        String g_s = String.valueOf(lastSentRGB[1]);
        String b_s = String.valueOf(lastSentRGB[2]);

        message = LedSettingsFragment.MODE_RGB + " " + r_s + " " + g_s + " " + b_s;

        Bundle options = new Bundle();
        if(checkReception){
            options.putInt("mode", LedSettingsFragment.MODE_RGB);
            options.putBoolean("erasable", false);
            options.putBoolean("verify_successful_transfer", true);
            options.putBoolean("wait_until_receiver_ready", true);
        } else{
            options.putInt("mode", LedSettingsFragment.MODE_RGB);
            options.putBoolean("erasable", true);
            options.putBoolean("verify_successful_transfer", false);
            options.putBoolean("wait_until_receiver_ready", false);
        }
        mListener.sendMessage(message, options);

    }

    public void toggleModule(boolean expand){
        if(expand){
            moduleContent.setVisibility(View.VISIBLE);
            topView.setCardElevation(8*getResources().getDisplayMetrics().density);
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
                SharedPreferences prefs = getActivity().getSharedPreferences("rgb_module_preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                String key;
                if(editText.getText().toString().length() > 0){
                    key = editText.getText().toString();
                } else{
                    key = "No name";
                }
                String value = String.valueOf(System.currentTimeMillis()) + " ";
                value += rSeekBar.getProgress() + " ";
                value += gSeekBar.getProgress() + " ";
                value += bSeekBar.getProgress() + " ";
                value += instantUpdate.isChecked() ? "true" : "false";
                editor.putString(key, value);
                editor.commit();
            }
        });
        builder.create().show();
    }

    public void openLoadSettingsMenu(){
        final SharedPreferences prefs = getActivity().getSharedPreferences("rgb_module_preferences", Context.MODE_PRIVATE);
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
                String[] data = value.split(" ");
                long timeStamp = Long.valueOf(data[0]);
                final int red = Integer.valueOf(data[1]);
                final int green = Integer.valueOf(data[2]);
                final int blue = Integer.valueOf(data[3]);
                final boolean updateInstantly = "true".equals(data[4]);

                Date date = new Date(timeStamp);
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");


                LinearLayout item = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.saved_settings_item_layout, null);
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rSeekBar.setProgress(red);
                        gSeekBar.setProgress(green);
                        bSeekBar.setProgress(blue);
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
                ((TextView)item.findViewById(R.id.saved_settings_item_details)).setText("(r,g,b) = (" + data[1] + "," + data[2] + "," + data[3] + ") " + (updateInstantly ? "+" : "-"));
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
