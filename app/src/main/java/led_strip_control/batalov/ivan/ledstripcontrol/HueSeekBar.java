package led_strip_control.batalov.ivan.ledstripcontrol;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.ScrollView;
import android.widget.SeekBar;

/**
 * Created by ivan on 3/10/16.
 */
public class HueSeekBar extends SeekBar {
    public static final int DIRECTION_DIRECT = 0;
    public static final int DIRECTION_REVERSE = 1;

    public static final int GRADIENT_TYPE_HSV = 0;
    public static final int GRADIENT_TYPE_RGB = 1;

    public static final int DEFAULT_MIN_COLOR = 0xFF0000;
    public static final int DEFAULT_MAX_COLOR = 0xFF0004;

    public static final float DEFAULT_BAR_THICKNESS_FRACTION = 0.3f;

    public static final int ORIENTATION_HORIZONTAL = 0;
    public static final int ORIENTATION_VERTICAL = 1;

    public static final int TOUCH_MODE_OFF = 0;
    public static final int TOUCH_MODE_TOUCH = 1;
    public static final int TOUCH_MODE_MOVE = 2;


    private int minColor;
    private int maxColor;
    private int gradientDirection;
    private int gradientType;
    private float barThicknessFraction;
    private int orientation;

    private int thumbLineColor = Color.BLACK;

    private int touchMode;

    public HueSeekBar(Context context) {
        super(context);
    }

    public HueSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        obtainCustomAttributes(context, attrs);
    }

    public HueSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainCustomAttributes(context, attrs);
    }

    private void obtainCustomAttributes(Context context, AttributeSet attrs){
        if(attrs != null) {
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HueSeekBar, 0, 0);
            try {
                minColor = typedArray.getColor(R.styleable.HueSeekBar_minColor, DEFAULT_MIN_COLOR);
                maxColor = typedArray.getColor(R.styleable.HueSeekBar_maxColor, DEFAULT_MAX_COLOR);
                gradientDirection = typedArray.getInt(R.styleable.HueSeekBar_colorShiftDirection, DIRECTION_DIRECT);
                gradientType = typedArray.getInt(R.styleable.HueSeekBar_gradientType, GRADIENT_TYPE_HSV);
                barThicknessFraction = typedArray.getFloat(R.styleable.HueSeekBar_barThickness, DEFAULT_BAR_THICKNESS_FRACTION);
                orientation = typedArray.getInt(R.styleable.HueSeekBar_orientation, ORIENTATION_HORIZONTAL);
                //System.out.println("minColor: " + Integer.toHexString(minColor) + "; maxColor: " + Integer.toHexString(maxColor) + "; grad dir: " + gradientDirection + "; grad type: " + gradientType + "; bar thickness: " + barThicknessFraction);
            } finally {
                typedArray.recycle();
            }
            updateDrawingParameters();
        }
    }

    Paint paint = new Paint();
    Paint thumbPaint = new Paint();
    Path thumbPath;

    float[] hsv = {0f, 1f, 1f};
    float[] minHsv = {0f,0f,0f};
    float[] maxHsv = {0f,0f,0f};
    float[] hsvDist = new float[3];

    int[] rgb = {0, 0, 0};
    int[] minRgb = {0,0,0};
    int[] maxRgb = {0,0,0};
    int[] rgbDist = new int[3];

    int barStart;
    int barEnd;
    int thumbPositionX;
    int thumbHalfThickness = dpToPx(0.5);
    int thumbLineThickness = dpToPx(1.3);
    float thumbHeight;
    int hueLineLength;

    private void updateDrawingParameters(){
        if(gradientType == GRADIENT_TYPE_HSV){
            Color.colorToHSV(minColor, minHsv);
            Color.colorToHSV(maxColor, maxHsv);

            for(int i = 0; i < 3; i++){
                hsvDist[i] = maxHsv[i] - minHsv[i];
            }

            if(gradientDirection == DIRECTION_DIRECT){
                while(hsvDist[0] < 0){
                    hsvDist[0] += 360;
                }
            } else if(gradientDirection == DIRECTION_REVERSE){
                while(hsvDist[0] > 0){
                    hsvDist[0] -= 360;
                }
            }
        } else if(gradientType == GRADIENT_TYPE_RGB){
            minRgb[0] = (minColor >> 16) & 0xFF;
            minRgb[1] = (minColor >> 8) & 0xFF;
            minRgb[2] = minColor & 0xFF;

            maxRgb[0] = (maxColor >> 16) & 0xFF;
            maxRgb[1] = (maxColor >> 8) & 0xFF;
            maxRgb[2] = maxColor & 0xFF;

            for(int i = 0; i < 3; i++){
                rgbDist[i]  = maxRgb[i] - minRgb[i];
            }
        }
    }

    Bitmap barBitmap;
    Canvas bitmapCanvas = new Canvas();
    int bitmapWidth;
    int bitmapHeight;

    private void drawBitmap(){
        if(bitmapWidth > 0 && bitmapHeight > 0){

            if(barBitmap != null && !barBitmap.isRecycled()){
                if(bitmapWidth != barBitmap.getWidth() || bitmapHeight != barBitmap.getHeight()) {
                    barBitmap.recycle();
                    barBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
                }
            } else {
                barBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            }

            bitmapCanvas.setBitmap(barBitmap);

            if(orientation == ORIENTATION_HORIZONTAL) {
                barStart = (int) ((1 - barThicknessFraction) * bitmapHeight / 2);
                barEnd = bitmapHeight - barStart;
                hueLineLength = bitmapWidth;
            } else{
                barStart = (int) ((1 - barThicknessFraction) * bitmapWidth / 2);
                barEnd = bitmapCanvas.getWidth() - barStart;
                hueLineLength = bitmapHeight;
            }

            if(gradientType == GRADIENT_TYPE_HSV){
                if(gradientDirection == DIRECTION_DIRECT){
                    for (int i = 0; i < hueLineLength; i++) {
                        hsv[0] = (minHsv[0] + ((float)i/hueLineLength)*hsvDist[0]) % 360;
                        hsv[1] = (minHsv[1] + ((float)i/hueLineLength)*hsvDist[1]);
                        hsv[2] = (minHsv[2] + ((float)i/hueLineLength)*hsvDist[2]);

                        paint.setColor(Color.HSVToColor(hsv));

                        if(orientation == ORIENTATION_HORIZONTAL) {
                            bitmapCanvas.drawLine(i, barStart, i, barEnd, paint);
                        } else{
                            bitmapCanvas.drawLine(barStart, i, barEnd, i, paint);
                        }
                    }
                } else if(gradientDirection == DIRECTION_REVERSE){
                    for (int i = 0; i < hueLineLength; i++) {
                        hsv[0] = (minHsv[0] + ((float)i/hueLineLength)*hsvDist[0]);
                        while(hsv[0] < 0){
                            hsv[0] += 360;
                        }

                        hsv[1] = (minHsv[1] + ((float)i/hueLineLength)*hsvDist[1]);
                        hsv[2] = (minHsv[2] + ((float)i/hueLineLength)*hsvDist[2]);

                        paint.setColor(Color.HSVToColor(hsv));
                        if(orientation == ORIENTATION_HORIZONTAL) {
                            bitmapCanvas.drawLine(i, barStart, i, barEnd, paint);
                        } else{
                            bitmapCanvas.drawLine(barStart, i, barEnd, i, paint);
                        }
                    }
                }
            } else if(gradientType == GRADIENT_TYPE_RGB){
                for (int i = 0; i < hueLineLength; i++) {
                    rgb[0] = (int)(minRgb[0] + ((float)i/hueLineLength)*rgbDist[0]);
                    rgb[1] = (int)(minRgb[1] + ((float)i/hueLineLength)*rgbDist[1]);
                    rgb[2] = (int)(minRgb[2] + ((float)i/hueLineLength)*rgbDist[2]);

                    paint.setColor(Color.rgb(rgb[0], rgb[1], rgb[2]));
                    if(orientation == ORIENTATION_HORIZONTAL) {
                        bitmapCanvas.drawLine(i, barStart, i, barEnd, paint);
                    } else{
                        bitmapCanvas.drawLine(barStart, i, barEnd, i, paint);
                    }
                }
            }
        }
    }

    Paint bitmapPaint = new Paint();
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        if(canvas.getWidth() > 0 && canvas.getHeight() > 0) {

            if(canvas.getWidth() - getPaddingLeft() - getPaddingRight() != bitmapWidth || canvas.getHeight() - getPaddingTop() - getPaddingBottom() != bitmapHeight){
                bitmapHeight = canvas.getHeight() - getPaddingTop() - getPaddingBottom();
                bitmapWidth = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
                drawBitmap();
            }
            canvas.drawBitmap(barBitmap, getPaddingLeft(), getPaddingTop(), bitmapPaint);
            // drawing the thumb
            if(thumbHeight == 0){
                thumbPaint.setColor(thumbLineColor);
                thumbPaint.setStrokeWidth(thumbLineThickness);
                thumbPaint.setStrokeCap(Paint.Cap.ROUND);
                thumbPaint.setDither(true);
                thumbPaint.setFilterBitmap(true);
                thumbPaint.setAntiAlias(true);

                float thumbKnobWidth = (float)(getWidth() - getPaddingLeft() - getPaddingRight())*(1 - barThicknessFraction)/2;
                thumbHeight = 1.5f*(thumbKnobWidth + thumbHalfThickness);
                float thumbWidth = (getWidth() - getPaddingLeft() - getPaddingRight());

                thumbPath = new Path();
                thumbPath.moveTo(thumbLineThickness, thumbLineThickness);
                thumbPath.lineTo(thumbLineThickness, thumbHeight - thumbLineThickness);
                thumbPath.lineTo(thumbKnobWidth, thumbHeight / 2 + thumbHalfThickness);
                thumbPath.lineTo(thumbWidth - thumbKnobWidth, thumbHeight / 2 + thumbHalfThickness);
                thumbPath.lineTo(thumbWidth - thumbLineThickness, thumbHeight - thumbLineThickness);
                thumbPath.lineTo(thumbWidth - thumbLineThickness, thumbLineThickness);
                thumbPath.lineTo(thumbWidth - thumbKnobWidth, thumbHeight/2 - thumbHalfThickness);
                thumbPath.lineTo(thumbKnobWidth, thumbHeight / 2 - thumbHalfThickness);
                thumbPath.close();
            }
            if(orientation == ORIENTATION_HORIZONTAL) {
                thumbPositionX = (int)((float)getProgress()/getMax()*hueLineLength) + getPaddingLeft();
                canvas.drawRect(thumbPositionX - thumbHalfThickness, getPaddingTop(), thumbPositionX + thumbHalfThickness, canvas.getHeight() - getPaddingBottom(), thumbPaint);
            } else{
                thumbPositionX = (int)((float)getProgress()/getMax()*hueLineLength) + getPaddingTop();
                //canvas.drawRect(getPaddingLeft(), thumbPositionX - thumbHalfThickness, getWidth() - getPaddingRight(), thumbPositionX + thumbHalfThickness, thumbPaint);

                hsv[0] = minHsv[0] + (maxHsv[0] - minHsv[0])*((float)getProgress()/getMax());
                hsv[1] = 1;
                hsv[2] = 1;

                canvas.save();
                thumbPaint.setStyle(Paint.Style.FILL);
                thumbPaint.setColor(Color.HSVToColor(hsv));
                canvas.translate(getPaddingLeft(), thumbPositionX - thumbHeight / 2);
                canvas.drawPath(thumbPath, thumbPaint);

                thumbPaint.setStyle(Paint.Style.STROKE);
                thumbPaint.setColor(thumbLineColor);
                canvas.drawPath(thumbPath, thumbPaint);


                canvas.restore();

            }

        }

    }

    public static int dpToPx(double dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    float touchX;
    float touchY;
    float progressFraction = 0;
    ViewParent parent;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(orientation == ORIENTATION_HORIZONTAL) {
            return super.onTouchEvent(event);
        } else{
            if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
                if(event.getX() < getPaddingLeft() || event.getX() > getWidth() - getPaddingRight()
                        || event.getY() < getPaddingTop() || event.getY() > getHeight() - getPaddingBottom()){
                    return false;
                }

                touchMode = TOUCH_MODE_TOUCH;

                touchX = event.getX();
                touchY = event.getY();
                progressFraction = (touchY - getPaddingTop())/hueLineLength;
                setProgress((int)(progressFraction*getMax()));

                parent = getParent();
                while(!(parent == null || parent instanceof ScrollView)){
                    parent = parent.getParent();
                }
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }

                if(onSeekBarChangeListener != null){
                    onSeekBarChangeListener.onStartTrackingTouch(this);
                }

                return true;

            } else if(event.getActionMasked() == MotionEvent.ACTION_MOVE && touchMode != TOUCH_MODE_OFF){
                touchMode = TOUCH_MODE_MOVE;
                touchX = event.getX();
                touchY = event.getY();

                touchX = Math.max(touchX, getPaddingLeft());
                touchX = Math.min(touchX, getWidth() - getPaddingRight());

                touchY = Math.max(touchY, getPaddingTop());
                touchY = Math.min(touchY, getHeight() - getPaddingBottom());

                progressFraction = (touchY - getPaddingTop())/hueLineLength;
                setProgress((int)(progressFraction*getMax()));

                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }

                if(onSeekBarChangeListener != null){
                    onSeekBarChangeListener.onProgressChanged(this, getProgress(), true);
                }

                return true;

            } else if(event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL){
                touchMode = TOUCH_MODE_OFF;
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
                if(onSeekBarChangeListener != null){
                    onSeekBarChangeListener.onStopTrackingTouch(this);
                }

                return true;
            }
        }
        return false;
    }

    public float getBarThicknessFraction() {
        return barThicknessFraction;
    }

    public void setBarThicknessFraction(float barThicknessFraction) {
        this.barThicknessFraction = barThicknessFraction;
        invalidate();
        requestLayout();
    }

    public int getGradientDirection() {
        return gradientDirection;
    }

    public void setGradientDirection(int gradientDirection) {
        this.gradientDirection = gradientDirection;
        updateDrawingParameters();
        invalidate();
    }

    public int getGradientType() {
        return gradientType;
    }

    public void setGradientType(int gradientType) {
        this.gradientType = gradientType;
        updateDrawingParameters();
        invalidate();
    }

    public int getMaxColor() {
        return maxColor;
    }

    public void setMaxColor(int maxColor) {
        this.maxColor = maxColor;
        updateDrawingParameters();
        invalidate();
    }

    public int getMinColor() {
        return minColor;
    }

    public void setMinColor(int minColor) {
        this.minColor = minColor;
        updateDrawingParameters();
        invalidate();
    }

    public int getThumbLineColor() {
        return thumbLineColor;
    }

    public void setThumbLineColor(int thumbLineColor) {
        this.thumbLineColor = thumbLineColor;
        thumbPaint.setColor(thumbLineColor);
        invalidate();
    }

    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener;

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener){
        this.onSeekBarChangeListener = listener;
        super.setOnSeekBarChangeListener(listener);
    }

}
