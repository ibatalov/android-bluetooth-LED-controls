package led_strip_control.batalov.ivan.ledstripcontrol;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LedBinPicker.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class LedBinPicker extends Fragment implements View.OnClickListener, LedBinOrganizer.StateChangedListener {

    public static final int LED_BINS_CHANGED = 100;

    private View topView;
    LinearLayout binLayout;
    LedBinOrganizer binOrganizer;

    Button uniform;
    Button centered1;
    Button centered2;
    Button mixed;

    private int numBins;
    ArrayList<View> bins;
    ArrayList<Integer> ids;
    private int ledCount;

    private OnFragmentInteractionListener mListener;

    public LedBinPicker() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(topView == null) {
            topView = inflater.inflate(R.layout.fragment_led_bin_pick, null);
            binLayout = (LinearLayout) topView.findViewById(R.id.led_bin_pick_grid_layout);
            binOrganizer = (LedBinOrganizer) topView.findViewById(R.id.led_bin_pick_organizer);
            binOrganizer.setStateChangedListener(this);

            uniform = (Button) topView.findViewById(R.id.led_bin_pick_uniform_button);
            centered1 = (Button) topView.findViewById(R.id.led_bin_pick_centered1_button);
            centered2 = (Button) topView.findViewById(R.id.led_bin_pick_centered2_button);
            mixed = (Button) topView.findViewById(R.id.led_bin_pick_mixed_button);

            uniform.setOnClickListener(preSetButtonsListener);
            centered1.setOnClickListener(preSetButtonsListener);
            centered2.setOnClickListener(preSetButtonsListener);
            mixed.setOnClickListener(preSetButtonsListener);

            if(ledCount > 0){
                binOrganizer.setNumDots(ledCount);
            } else{
                ledCount = binOrganizer.getNumDots();
            }
            if(numBins > 0) {
                setUpBinViews();
            }
        }
        return topView;
    }

    void setUpBinViews(){
        if(bins == null || bins.size() != numBins) {
            if(bins == null){
                bins = new ArrayList<>(numBins);
                ids = new ArrayList<>(numBins);
            } else{
                ids.clear();
                bins.clear();
                binLayout.removeAllViews();
            }

            float[] hsv = new float[3];
            hsv[1] = 1;
            hsv[2] = 1;

            for (int i = 0; i < numBins; i++) {
                hsv[0] = (float) i * 360 / numBins;
                TextView view = new TextView(getActivity());
                view.setText(String.valueOf(i));
                view.setPadding(5, 5, 5, 5);
                view.setGravity(Gravity.CENTER);
                view.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                view.setTextColor(Color.WHITE);
                view.setBackgroundColor(Color.HSVToColor(hsv));
                int id = generateViewId1();
                view.setId(id);
                view.setOnClickListener(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.weight = 1;
                params.setMargins(5,5,5,5);
                view.setLayoutParams(params);

                ids.add(id);
                bins.add(view);
                if(binLayout != null){
                    binLayout.addView(view);
                }
            }
        }
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
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mListener = null;
    }

    float[] hsvColor = new float[]{0,1,1};

    @Override
    public void onClick(View v) {
        if(ids.contains(v.getId())){
            int bin = bins.indexOf(v);
            hsvColor[0] = 360*(float)bin/bins.size();
            int color = Color.HSVToColor(hsvColor);

            LedParameters params = new LedParameters();
            params.color = color;
            params.ledGroup = bin;

            if(binOrganizer != null){
                binOrganizer.setLedParametersToSet(params);
            }
        }
    }

    @Override
    public void onStateChangeStarted(LedBinOrganizer ledBinOrganizer, LedParameters led) {

    }

    private String message;

    @Override
    public void onStateChanged(LedBinOrganizer ledBinOrganizer, LedParameters led) {

    }

    @Override
    public void onStateChangeFinished(LedBinOrganizer ledBinOrganizer) {

    }

    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Bundle data);
    }

    public int getNumBins() {
        return numBins;
    }

    public void setNumBins(int numBins) {
        this.numBins = numBins;
        if(binLayout != null){
            setUpBinViews();
            topView.requestLayout();
            topView.invalidate();
        }
    }

    public void setLedCount(int ledCount){
        this.ledCount = ledCount;
        if(binOrganizer != null){
            binOrganizer.setNumDots(ledCount);
        }
    }

    public int getLedCount(){
        return ledCount;
    }

    private View.OnClickListener preSetButtonsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(numBins > 0) {
                float[] hsv = new float[]{0,1,1};
                ledCount = binOrganizer.getNumDots();

                if (v.getId() == R.id.led_bin_pick_uniform_button) {
                    for(int bin = 0; bin < numBins; bin++){
                        hsv[0] = 360*(float)bin/numBins;
                        for(int led = bin*ledCount/numBins; led < (bin+1)*ledCount/numBins; led++){
                            LedParameters params = binOrganizer.getLedParameters().get(led);
                            params.ledGroup = bin;
                            params.color = Color.HSVToColor(hsv);
                        }
                    }
                    binOrganizer.invalidate();
                } else if (v.getId() == R.id.led_bin_pick_centered1_button) {
                    for(int bin = 0; bin < numBins; bin++){
                        hsv[0] = 360*(float)bin/numBins;
                        for(int dist = (ledCount/2)*bin/numBins; dist <= (ledCount/2)*(bin + 1)/numBins; dist++){
                            LedParameters params1 = binOrganizer.getLedParameters().get(Math.max(0, (ledCount - 1)/2 - dist));
                            params1.ledGroup = bin;
                            params1.color = Color.HSVToColor(hsv);

                            LedParameters params2 = binOrganizer.getLedParameters().get(Math.min(ledCount - 1, (int)Math.ceil((float)(ledCount - 1)/2) + dist));
                            params2.ledGroup = bin;
                            params2.color = Color.HSVToColor(hsv);
                        }
                    }
                    binOrganizer.invalidate();
                } else if (v.getId() == R.id.led_bin_pick_centered2_button) {
                    int bigBinsEnd = 4*(numBins/3);
                    int mediumBinsEnd = numBins*2 - bigBinsEnd/2;

                    for(int bin = 0; bin < 2*numBins; bin++){
                        int finalBin;
                        if(bin < bigBinsEnd){
                            finalBin = bin/4;
                        } else if(bin < mediumBinsEnd){
                            finalBin = bigBinsEnd/4 + (bin - bigBinsEnd)/2;
                        } else{
                            finalBin = bigBinsEnd/4 + (mediumBinsEnd - bigBinsEnd)/2 + (bin - mediumBinsEnd);
                        }
                        hsv[0] = 360*(float)(finalBin)/(numBins);
                        for(int dist = (ledCount/2)*bin/(numBins*2); dist <= (ledCount/2)*(bin + 1)/(numBins*2); dist++){
                            LedParameters params1 = binOrganizer.getLedParameters().get(Math.max(0, (ledCount - 1)/2 - dist));
                            params1.ledGroup = finalBin;
                            params1.color = Color.HSVToColor(hsv);

                            LedParameters params2 = binOrganizer.getLedParameters().get(Math.min(ledCount - 1, (int)Math.ceil((float)(ledCount - 1)/2) + dist));
                            params2.ledGroup = finalBin;
                            params2.color = Color.HSVToColor(hsv);
                        }
                    }
                    binOrganizer.invalidate();
                } else if (v.getId() == R.id.led_bin_pick_mixed_button) {
                    for(int led = 0; led < ledCount; led++){
                        hsv[0] = 360*(float)(led % numBins)/numBins;
                        LedParameters params = binOrganizer.getLedParameters().get(led);
                        params.ledGroup = (led % numBins);
                        params.color = Color.HSVToColor(hsv);
                    }
                    binOrganizer.invalidate();
                }
            }
        }
    };

    public void sendBinLedData(){
        if(mListener != null){
            SparseArray<ArrayList<Integer>> binLeds = new SparseArray<>(numBins);
            for(int led = 0; led < ledCount; led++){
                int bin = binOrganizer.getLedParameters().get(led).ledGroup;
                if(binLeds.get(bin) == null){
                    ArrayList<Integer> leds = new ArrayList<>();
                    leds.add(led);
                    binLeds.put(bin, leds);
                } else{
                    binLeds.get(bin).add(led);
                }
            }
            // message example: bin 123(34)[1,2,4-7,8,13,15,15-23]
            for(int i = 0; i < binLeds.size(); i++){
                int bin = binLeds.keyAt(i);
                ArrayList<Integer> leds = binLeds.valueAt(i);
                String string = "bin " + bin + "(" + leds.size() + ")[";

                while(!binLeds.get(bin).isEmpty()){
                    Integer led = leds.get(0);
                    string += led;
                    leds.remove(0);
                    Integer lastLed = new Integer(led);

                    while (leds.contains(lastLed + 1)){
                        lastLed++;
                        leds.remove(lastLed);
                    }
                    if(!lastLed.equals(led)){
                        string += "-" + lastLed;
                    }
                    if(!leds.isEmpty()) {
                        string += ",";
                    } else{
                        string += "]";
                    }
                }
                Bundle data = new Bundle();
                data.putInt("eventID", LED_BINS_CHANGED);
                data.putString("message", string);
                mListener.onFragmentInteraction(data);
            }
        }
    }


    public String getMessage() {
        return message;
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    /**
     *
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId1() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

}
