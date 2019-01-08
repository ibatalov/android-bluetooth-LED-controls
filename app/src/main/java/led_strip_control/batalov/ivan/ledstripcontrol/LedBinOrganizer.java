package led_strip_control.batalov.ivan.ledstripcontrol;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;
import android.widget.ScrollView;

import java.util.ArrayList;

public class LedBinOrganizer extends View {

    public static final int DOT_SHAPE_CIRCLE = 0;
    public static final int DOT_SHAPE_SQUARE = 1;

    private ArrayList<LedParameters> leds;
    private ArrayList<PointF> ledCoordinates;

    private float radius;
    private float distanceX;
    private float distanceY;
    private int ledsPerRow;

    private int defaultColor;
    private int dotShape;
    private int numDots;

    Paint ledPaint;
    Paint textPaint;

    public LedBinOrganizer(Context context) {
        super(context);
        init(null);
    }

    public LedBinOrganizer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public LedBinOrganizer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        ledPaint = new Paint();
        ledPaint.setStyle(Paint.Style.FILL);
        ledPaint.setDither(true);
        ledPaint.setAntiAlias(true);

        TypedArray array = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.LedBinOrganizer, 0, 0);
        try{
            defaultColor = array.getColor(R.styleable.LedBinOrganizer_defaultColor, 0x000000);
            distanceX = array.getDimension(R.styleable.LedBinOrganizer_dotDistanceX, 0);
            distanceY = array.getDimension(R.styleable.LedBinOrganizer_dotDistanceY, 0);
            radius = array.getDimension(R.styleable.LedBinOrganizer_dotRadius, 0);
            dotShape = array.getInt(R.styleable.LedBinOrganizer_dotShape, DOT_SHAPE_CIRCLE);
            numDots = array.getInt(R.styleable.LedBinOrganizer_numDots, 10);
        } finally {
            array.recycle();
        }

        textPaint = new Paint();
        textPaint.setDither(true);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(radius);
        textPaint.setColor(Color.BLACK);

        leds = new ArrayList<LedParameters>(numDots);
        for(int i = 0; i < numDots; i++){
            LedParameters led = new LedParameters();
            led.color = defaultColor;
            led.ledNumber = i;
            leds.add(led);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int)Math.min(getPaddingLeft() + getPaddingRight() + (numDots - 1) * distanceX + 2*radius, Resources.getSystem().getDisplayMetrics().widthPixels);
        int cols = (int)Math.ceil((float)numDots/((int)((desiredWidth - getPaddingRight() - getPaddingLeft() - 2*radius)/distanceX) + 1));
        int desiredHeight = (int)((cols - 1)*distanceY + 2*radius) + getPaddingBottom() + getPaddingTop();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
            if (heightMode == MeasureSpec.EXACTLY) {
                height = heightSize;
            } else if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(getHeightBasedOnWidth(width), heightSize);
            } else {
                height = getHeightBasedOnWidth(width);
            }
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
            if (heightMode == MeasureSpec.EXACTLY) {
                height = heightSize;
            } else if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(getHeightBasedOnWidth(width), heightSize);
            } else {
                height = getHeightBasedOnWidth(width);
            }
        } else {
            width = desiredWidth;
            if (heightMode == MeasureSpec.EXACTLY) {
                height = heightSize;
            } else if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(desiredHeight, heightSize);
            } else {
                height = desiredHeight;
            }
        }

        ledsPerRow = (int)((width - getPaddingRight() - getPaddingLeft() - 2*radius)/distanceX) + 1;
        redraw = true;
        setMeasuredDimension(width, height);
    }

    private int getHeightBasedOnWidth(int width){
        int cols = (int)Math.ceil((float)numDots/((int)((width - getPaddingRight() - getPaddingLeft() - 2*radius)/distanceX) + 1));
        return getPaddingTop() + getPaddingBottom() + (int)(2*radius + (cols - 1)*distanceY) + (int)(2.5*radius);
    }

    int lastWidth;
    int lastHeight;
    boolean redraw = false;

    @Override
    protected void onDraw(Canvas canvas) {

        if(canvas.getWidth() > 0 && canvas.getHeight() > 0) {
            if(lastWidth != canvas.getWidth() || getHeight() != canvas.getHeight() || redraw){
                lastWidth = canvas.getWidth();
                lastHeight = canvas.getHeight();
                updateLedCoordinates();
                redraw = false;
            }

            for (int i = 0; i < leds.size(); i++) {
                LedParameters led = leds.get(i);
                PointF ledCenter = ledCoordinates.get(i);
                ledPaint.setColor(led.color);

                if (dotShape == DOT_SHAPE_CIRCLE) {
                    canvas.drawCircle(ledCenter.x, ledCenter.y, radius, ledPaint);
                } else {
                    canvas.drawRect(ledCenter.x - radius, ledCenter.y - radius, ledCenter.x + radius, ledCenter.y + radius, ledPaint);
                }
                canvas.drawText(String.valueOf(i), ledCenter.x, ledCenter.y + (int)(radius*1.9f), textPaint);
            }
        }
    }

    void updateLedCoordinates(){
        if(ledCoordinates == null){
            ledCoordinates = new ArrayList<>(numDots);
        } else{
            ledCoordinates.clear();
        }
        for(int i = 0; i < numDots; i++){
            PointF coord = new PointF();
            row = i / ledsPerRow;
            col = i % ledsPerRow;
            coord.x = getPaddingLeft() + radius + col*distanceX;
            coord.y = getPaddingTop() + radius + row*distanceY;
            ledCoordinates.add(coord);
        }
    }

    float touchX;
    float touchY;
    int row;
    int col;
    float rawRow;
    float rawCol;
    int ledIndex;
    private LedParameters chosenLed;
    private LedParameters ledParametersToSet;
    private int touchMode;
    private static final int MODE_OFF = 0;
    private static final int MODE_TOUCH = 1;
    private static final int MODE_MOVE = 2;

    ViewParent parent;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN){

            if(event.getX() < getPaddingLeft() || event.getX() > getWidth() - getPaddingRight()
                    || event.getY() < getPaddingTop() || event.getY() > getHeight() - getPaddingBottom()){
                return false;
            }
            touchX = event.getX();
            touchY = event.getY();

            touchMode = MODE_TOUCH;

            updateLedState();
            if(stateChangedListener != null){
                stateChangedListener.onStateChangeStarted(this, chosenLed);
            }

            // this is sketchy, cause I only seek for the scrollview. There could also be listView or stuff like that
            if(parent == null) {
                parent = getParent();
                while (!(parent == null || parent instanceof ScrollView)) {
                    parent = parent.getParent();
                }
            }

            return true;
        } else if(event.getActionMasked() == MotionEvent.ACTION_MOVE && touchMode == MODE_TOUCH || touchMode == MODE_MOVE){

            touchMode = MODE_MOVE;
            touchX = Math.max(event.getX(), getPaddingLeft());
            touchX = Math.min(touchX, getWidth() - getPaddingRight());

            touchY = Math.max(event.getY(), getPaddingTop());
            touchY = Math.min(touchY, getHeight() - getPaddingBottom());
            updateLedState();

            return true;

        } else if(event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL){

            touchMode = MODE_OFF;

            if(stateChangedListener != null){
                stateChangedListener.onStateChangeFinished(this);
            }

            if(parent != null){
                parent.requestDisallowInterceptTouchEvent(false);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    boolean stateChanged = false;

    private void updateLedState(){
        stateChanged = false;
        rawCol = (touchX - getPaddingLeft() - radius) / distanceX;
        rawRow = (touchY - getPaddingTop() - radius) / distanceY;
        col = Math.round(rawCol);
        row = Math.round(rawRow);
        ledIndex = row * ledsPerRow + col;

        if(ledIndex < leds.size()){

            chosenLed = leds.get(row * ledsPerRow + col);
            PointF coord = ledCoordinates.get(chosenLed.ledNumber);
            if(PointF.length(touchX - coord.x, touchY - coord.y) < Math.min(distanceX, distanceY)) {
                if (ledParametersToSet != null) {

                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }

                    if (ledParametersToSet.color != chosenLed.color) {
                        chosenLed.color = ledParametersToSet.color;
                        stateChanged = true;
                    }
                    if (ledParametersToSet.ledGroup != chosenLed.ledGroup) {
                        chosenLed.ledGroup = ledParametersToSet.ledGroup;
                        stateChanged = true;
                    }
                }
                if (stateChanged) {
                    if (stateChangedListener != null) {
                        stateChangedListener.onStateChanged(this, chosenLed);
                    }
                    invalidate();
                }
            } else{
                chosenLed = null;
            }
        } else{
            chosenLed = null;
        }
    }

    StateChangedListener stateChangedListener;
    public interface StateChangedListener{
        void onStateChangeStarted(LedBinOrganizer ledBinOrganizer, LedParameters led);
        void onStateChanged(LedBinOrganizer ledBinOrganizer, LedParameters led);
        void onStateChangeFinished(LedBinOrganizer ledBinOrganizer);
    }

    public StateChangedListener getStateChangedListener() {
        return stateChangedListener;
    }

    public void setStateChangedListener(StateChangedListener stateChangedListener) {
        this.stateChangedListener = stateChangedListener;
    }

    public LedParameters getChosenLed() {
        return chosenLed;
    }

    public LedParameters getLedParametersToSet() {
        return ledParametersToSet;
    }

    public ArrayList<LedParameters> getLeds() {
        return leds;
    }
    public float getRadius() {
        return radius;
    }

    public void setLedParametersToSet(LedParameters ledParametersToSet) {
        this.ledParametersToSet = ledParametersToSet;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        textPaint.setTextSize(radius * 2);
        requestLayout();
        redraw = true;
        invalidate();

    }

    public void setNumDots(int numDots){
        this.numDots = numDots;
        if(leds == null) {
            leds = new ArrayList<LedParameters>(numDots);
            for(int i = 0; i < numDots; i++){
                LedParameters led = new LedParameters();
                led.color = defaultColor;
                led.ledNumber = i;
                leds.add(led);
            }
        } else if(leds.size() > numDots){
            while(leds.size() != numDots){
                leds.remove(leds.size() - 1);
            }
        } else if(leds.size() < numDots){
            while(leds.size() != numDots){
                LedParameters led = new LedParameters();
                led.color = defaultColor;
                led.ledNumber = leds.size();
                leds.add(led);
            }
        }
        requestLayout();
        invalidate();
    }

    public int getNumDots(){
        return numDots;
    }

    public ArrayList<LedParameters> getLedParameters(){
        return leds;
    }
}
