package led_strip_control.batalov.ivan.ledstripcontrol;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Interpolator;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.HashMap;

import led_strip_control.batalov.ivan.ledstripcontrol.modules.LEDAnimationModule;
import led_strip_control.batalov.ivan.ledstripcontrol.modules.ModuleActionListener;
import led_strip_control.batalov.ivan.ledstripcontrol.modules.LedModule;
import led_strip_control.batalov.ivan.ledstripcontrol.modules.MusicVisualizationModule;
import led_strip_control.batalov.ivan.ledstripcontrol.modules.PaletteModule;
import led_strip_control.batalov.ivan.ledstripcontrol.modules.RGBModule;


public class LedSettingsFragment extends Fragment
        implements ModuleActionListener {
    private OnFragmentInteractionListener mListener;

    public static final int MODE_OFF = 0;
    public static final int MODE_RGB = 1;
    public static final int MODE_HSV = 2;
    public static final int MODE_NOISE = 3;
    public static final int MODE_AUDIO = 4;
    //public static final int MODE_SET_SINGLE_RGB = 6;
    public static final int MODE_LED_COUNT = 6;
    public static final int MODE_LED_ANIMATION = 7;

    public static final int SEND_DATA = 100;

    public static final int OPEN_BLUETOOTH_CONNECTIONS = 101;

    View topView;

    EditText ledCount;
    Button sendLedCount;

    HashMap<Integer, LedModule> modules = new HashMap<>();

    Button turnLedsOff;
    Button bluetoothConnectionsButton;
    Button forgetSavedDeviceButton;

    private int lastSentMode;

    private int buttonColor;

    public static LedSettingsFragment newInstance() {
        LedSettingsFragment fragment = new LedSettingsFragment();
        return fragment;
    }

    public LedSettingsFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buttonColor = getResources().getColor(R.color.palette_color_4_neutral);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(topView == null) {
            topView = inflater.inflate(R.layout.fragment_led_settings, container, false);

            LinearLayout moduleLayout = (LinearLayout) topView.findViewById(R.id.led_settings_layout_for_modules);
            LayoutTransition transition = moduleLayout.getLayoutTransition();

            if (Build.VERSION.SDK_INT >= 16) {
                transition.enableTransitionType(LayoutTransition.CHANGING);
                transition.enableTransitionType(LayoutTransition.DISAPPEARING);
                transition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
            }
            transition.setDuration(300L);


            ledCount = (EditText) topView.findViewById(R.id.led_settings_led_number);
            sendLedCount = (Button) topView.findViewById(R.id.led_settings_led_count_button);
            sendLedCount.getBackground().setColorFilter(buttonColor, PorterDuff.Mode.MULTIPLY);
            sendLedCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        String message = MODE_LED_COUNT + " ";
                        message += ledCount.getText().toString();
                        Bundle data = new Bundle();
                        data.putInt("eventID", SEND_DATA);
                        data.putInt("mode", LedSettingsFragment.MODE_LED_COUNT);
                        data.putString("message", message);
                        data.putBoolean("erasable", false);
                        data.putBoolean("verify_successful_transfer", true);
                        data.putBoolean("wait_until_receiver_ready", true);
                        mListener.onFragmentInteraction(data);
                    }
                }
            });

            turnLedsOff = (Button) topView.findViewById(R.id.led_settings_turn_off);
            //turnLedsOff.getBackground().setColorFilter(buttonColor, PorterDuff.Mode.MULTIPLY);
            turnLedsOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        String message = String.valueOf(MODE_OFF);
                        Bundle data = new Bundle();
                        data.putInt("eventID", SEND_DATA);
                        data.putInt("mode", LedSettingsFragment.MODE_OFF);
                        data.putString("message", message);
                        data.putBoolean("erasable", false);
                        data.putBoolean("verify_successful_transfer", true);
                        data.putBoolean("wait_until_receiver_ready", true);
                        mListener.onFragmentInteraction(data);
                    }
                }
            });

            bluetoothConnectionsButton = (Button) topView.findViewById(R.id.led_settings_bt_connections_button);
            bluetoothConnectionsButton.getBackground().setColorFilter(buttonColor, PorterDuff.Mode.MULTIPLY);
            bluetoothConnectionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        Bundle data = new Bundle();
                        data.putInt("eventID", OPEN_BLUETOOTH_CONNECTIONS);
                        mListener.onFragmentInteraction(data);
                    }
                }
            });

            forgetSavedDeviceButton = (Button) topView.findViewById(R.id.led_settings_forget_saved_bt_device);
            forgetSavedDeviceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                    builder.setTitle("Are you sure?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.remove("default_device_mac_address");
                            editor.commit();
                        }
                    });
                    builder.setNegativeButton("No", null);
                    builder.create().show();
                }
            });
            forgetSavedDeviceButton.getBackground().setColorFilter(buttonColor, PorterDuff.Mode.MULTIPLY);

            RGBModule rgbModule = new RGBModule();
            rgbModule.setModuleActionListener(this);

            PaletteModule paletteModule = new PaletteModule();
            paletteModule.setModuleActionListener(this);

            MusicVisualizationModule musicVisualizationModule = new MusicVisualizationModule();
            musicVisualizationModule.setModuleActionListener(this);

            LEDAnimationModule ledAnimationModule = new LEDAnimationModule();
            ledAnimationModule.setModuleActionListener(this);


            if(Build.VERSION.SDK_INT >= 17) {
                getChildFragmentManager().beginTransaction().add(R.id.rgb_module_placeholder, rgbModule).commit();
                getChildFragmentManager().beginTransaction().add(R.id.palette_module_placeholder, paletteModule).commit();
                getChildFragmentManager().beginTransaction().add(R.id.music_visualisation_module_placeholder, musicVisualizationModule).commit();
                getChildFragmentManager().beginTransaction().add(R.id.led_animation_module_placeholder, ledAnimationModule).commit();

            } else{
                getFragmentManager().beginTransaction().add(R.id.rgb_module_placeholder, rgbModule).commit();
                getFragmentManager().beginTransaction().add(R.id.palette_module_placeholder, paletteModule).commit();
                getFragmentManager().beginTransaction().add(R.id.music_visualisation_module_placeholder, musicVisualizationModule).commit();
                getFragmentManager().beginTransaction().add(R.id.led_animation_module_placeholder, ledAnimationModule).commit();
            }

            modules.put(rgbModule.getId(), rgbModule);
            modules.put(paletteModule.getId(), paletteModule);
            modules.put(musicVisualizationModule.getId(), musicVisualizationModule);
            modules.put(ledAnimationModule.getId(), ledAnimationModule);
        }
        return topView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
    }


    public static String trimOrPadWithZeros(String s, int finalLength, boolean padLeft){
        if(s.length() >= finalLength){
            return s.substring(0, finalLength);
        }

        String result = new String(s);
        for(int i = s.length(); i < finalLength; i++){
            if(padLeft){
                result = "0" + result;
            } else{
                result = result + "0";
            }
        }
        return result;
    }

    float[] tempHsv = new float[3];
    int mode;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Bundle data);
    }

    /**
     *
     * @param message message to send
     * @param options possible keys:
     *                boolean "erasable" - if true the could be overwritten by a newer erasable message
     *                boolean "verify_successful_transfer" - wait for response from the receiver confirming the amount of data transferred
     *                boolean "wait_until_receiver_ready" - wait until receiver sends a confirmation that it's ready to receive data
     */
    @Override
    public void sendMessage(String message, Bundle options) {
        if(mListener != null) {
            lastSentMode = options.getInt("mode", MODE_OFF);
            options.putInt("eventID", LedSettingsFragment.SEND_DATA);
            options.putString("message", message);
            mListener.onFragmentInteraction(options);
        }
    }

    @Override
    public void onModuleSelected(LedModule module){
        for(LedModule m : modules.values()){
            if(m != module){
                m.toggleModule(false);
            }
        }
    }

    public int getLastSentMode(){
        return lastSentMode;
    }

    public Button getForgetSavedDeviceButton(){
        return forgetSavedDeviceButton;
    }
}
