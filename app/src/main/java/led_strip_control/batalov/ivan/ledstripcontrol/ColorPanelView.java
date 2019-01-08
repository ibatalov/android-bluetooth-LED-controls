package led_strip_control.batalov.ivan.ledstripcontrol;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.ScrollView;

import java.util.Arrays;

/**
 * Created by ivan on 3/11/16.
 */
public class ColorPanelView  extends View{
    public static final int VAR_HUE = 2;
    public static final int VAR_SAT = 1;
    public static final int VAR_VAL = 0;

    private static final int TOUCH_MODE_OFF = 0;
    private static final int TOUCH_MODE_TOUCH = 1;
    private static final int TOUCH_MODE_MOVE = 2;

    public static final int DEFAULT_MIN_COLOR = 0x000000;
    public static final int DEFAULT_MAX_COLOR = 0xFF0000;

    private int minColor;
    private int maxColor;
    private int xAxis;
    private int yAxis;
    private float aspectRatio;
    ViewParent parent;

    private int pickedColor;

    public ColorPanelView(Context context) {
        super(context);
    }

    public ColorPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        obtainCustomAttributes(context, attrs);
    }

    public ColorPanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainCustomAttributes(context, attrs);
    }


    public void obtainCustomAttributes(Context context, AttributeSet attrs){
        if(attrs != null) {
            System.out.println("begin getting attributes");
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ColorPanelView, 0, 0);
            try {
                minColor = typedArray.getColor(R.styleable.ColorPanelView_lowColor, DEFAULT_MIN_COLOR);
                maxColor = typedArray.getColor(R.styleable.ColorPanelView_highColor, DEFAULT_MAX_COLOR);
                xAxis = typedArray.getInt(R.styleable.ColorPanelView_xAxis, VAR_SAT);
                yAxis = typedArray.getInt(R.styleable.ColorPanelView_yAxis, VAR_VAL);
                aspectRatio = typedArray.getFloat(R.styleable.ColorPanelView_aspectRatio, 1f);
                //System.out.println("aspect ratio: " + typedArray.getFloat(R.styleable.ColorPanelView_aspectRatio, 0));

            } finally {
                typedArray.recycle();
            }
            setUpStuff();
            System.out.println("end getting attributes");
        }
    }

    private void setUpStuff(){
        //thumbRadius = Math.min(thumbRadius, getPaddingLeft());
        //thumbRadius = Math.min(thumbRadius, getPaddingTop());
        //thumbRadius = Math.min(thumbRadius, getPaddingRight());
        //thumbRadius = Math.min(thumbRadius, getPaddingBottom());
        thumbRadius = 5 * getContext().getResources().getDisplayMetrics().density;

        touchPaint.setColor(Color.BLACK);
        touchPaint.setShadowLayer(2 * thumbRadius, 0, 0, Color.BLUE);
        touchPaint.setDither(true);
        touchPaint.setAntiAlias(true);
        touchPaint.setStyle(Paint.Style.STROKE);
        touchPaint.setStrokeWidth(thumbRadius/5);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int)(Resources.getSystem().getDisplayMetrics().density * 100 * aspectRatio);
        int desiredHeight = (int)(Resources.getSystem().getDisplayMetrics().density * 100);

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
                height = Math.min((int)(width/aspectRatio), heightSize);
            } else {
                height = (int)(width/aspectRatio);
            }
        } else if (widthMode == MeasureSpec.AT_MOST) {
            if (heightMode == MeasureSpec.EXACTLY) {
                height = heightSize;
                width = Math.min(widthSize, (int)(aspectRatio*height));
            } else if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(desiredHeight, heightSize);
                width = Math.min(desiredWidth, widthSize);
            } else {
                width = Math.min(widthSize, desiredWidth);
                height = Math.max((int)(width/aspectRatio), desiredHeight);
            }
        } else {
            if (heightMode == MeasureSpec.EXACTLY) {
                height = heightSize;
            } else if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(desiredHeight, heightSize);
            } else {
                height = desiredHeight;
            }
            width = (int)(aspectRatio*height);
        }
        //System.out.println("on measure: " + width + " x " + height);
        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    int bitmapWidth;
    int bitmapHeight;
    Bitmap gradientField;
    Paint bitmapPaint = new Paint();
    Paint gradientFieldPaint = new Paint();
    LinearGradient gradient;
    float[] color0 = new float[3];
    float[] color1 = new float[3];

    float[] minHsv = new float[3];
    float[] maxHsv = new float[3];
    float[] hsvDist = new float[3];
    Canvas drawingCanvas = new Canvas();

    private void createGradientBitmap(){
        //System.out.println("begin generating bitmap");
        if(bitmapWidth != 0 && bitmapHeight != 0){

            if(gradientField != null && !gradientField.isRecycled()){
                if(bitmapWidth != gradientField.getWidth() || bitmapHeight != gradientField.getHeight()) {
                    gradientField.recycle();
                    gradientField = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
                }
            } else {
                gradientField = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            }

            drawingCanvas.setBitmap(gradientField);

            Color.colorToHSV(minColor, minHsv);
            Color.colorToHSV(maxColor, maxHsv);

            for(int i = 0; i < 3; i++){
                hsvDist[i] = maxHsv[i] - minHsv[i];
            }

            // gotta keep one variable constant, smartphones don't come with 3D-displays yet
            if(xAxis != VAR_HUE && yAxis != VAR_HUE){
                minHsv[0] = Math.max(minHsv[0], maxHsv[0]);
                maxHsv[0] = minHsv[0];
                hsvDist[0] = 0;
            } else if(xAxis != VAR_SAT && yAxis != VAR_SAT){
                minHsv[1] = Math.max(minHsv[1], maxHsv[1]);
                maxHsv[1] = minHsv[1];
                hsvDist[1] = 0;
            } else if(xAxis != VAR_VAL && yAxis != VAR_VAL){
                minHsv[2] = Math.max(minHsv[2], maxHsv[2]);
                maxHsv[2] = minHsv[2];
                hsvDist[2] = 0;
            }

            for(int i = 0; i < 3; i++){
                color0[i] = minHsv[i];
                color1[i] = maxHsv[i];
            }
            //System.out.println("hard work begin...");
            for(int x = 0; x < bitmapWidth; x++) {
                color0[2 - xAxis] = minHsv[2 - xAxis] + (float)x/bitmapWidth*hsvDist[2 - xAxis];
                color1[2 - xAxis] = color0[2 - xAxis];

                gradient = new LinearGradient(x, 0, x, bitmapHeight, Color.HSVToColor(color1), Color.HSVToColor(color0), Shader.TileMode.CLAMP);
                gradientFieldPaint.setShader(gradient);
                drawingCanvas.drawLine(x, 0, x, bitmapHeight, gradientFieldPaint);
            }
            //System.out.println("hard work end");

        }
        //System.out.println("end generating bitmap");
    }

    float[][] alphaMask;

    // works only if the third value is saturation
    private void createAlternativeGradientBitmap(){
        if(bitmapWidth != 0 && bitmapHeight != 0){

            if(gradientField != null && !gradientField.isRecycled()){
                if(bitmapWidth != gradientField.getWidth() || bitmapHeight != gradientField.getHeight()) {
                    gradientField.recycle();
                    gradientField = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
                }
            } else {
                gradientField = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            }

            drawingCanvas.setBitmap(gradientField);

            Color.colorToHSV(minColor, minHsv);
            Color.colorToHSV(maxColor, maxHsv);

            for(int i = 0; i < 3; i++){
                hsvDist[i] = maxHsv[i] - minHsv[i];
            }

            // gotta keep one variable constant, smartphones don't come with 3D-displays yet
            if(xAxis != VAR_HUE && yAxis != VAR_HUE){
                minHsv[0] = Math.max(minHsv[0], maxHsv[0]);
                maxHsv[0] = minHsv[0];
                hsvDist[0] = 0;
            } else if(xAxis != VAR_SAT && yAxis != VAR_SAT){
                minHsv[1] = Math.max(minHsv[1], maxHsv[1]);
                maxHsv[1] = minHsv[1];
                hsvDist[1] = 0;
            } else if(xAxis != VAR_VAL && yAxis != VAR_VAL){
                minHsv[2] = Math.max(minHsv[2], maxHsv[2]);
                maxHsv[2] = minHsv[2];
                hsvDist[2] = 0;
            }

            if(xAxis == VAR_SAT) {
                gradient = new LinearGradient(0, 0, bitmapWidth, 0, Color.WHITE, maxColor, Shader.TileMode.CLAMP);
                gradientFieldPaint.setShader(gradient);
                drawingCanvas.drawRect(0, 0, bitmapWidth, bitmapHeight, gradientFieldPaint);

                gradient = new LinearGradient(0, 0, 0, bitmapHeight, 0x00000000, 0xFF000000, Shader.TileMode.CLAMP);
                gradientFieldPaint.setShader(gradient);
                drawingCanvas.drawRect(0, 0, bitmapWidth, bitmapHeight, gradientFieldPaint);
            } else if(yAxis == VAR_SAT){
                gradient = new LinearGradient(0, 0, 0, bitmapHeight, Color.WHITE, maxColor, Shader.TileMode.CLAMP);
                gradientFieldPaint.setShader(gradient);
                drawingCanvas.drawRect(0, 0, bitmapWidth, bitmapHeight, gradientFieldPaint);

                gradient = new LinearGradient(0, 0, bitmapWidth, 0, 0x00000000, 0xFF000000, Shader.TileMode.CLAMP);
                gradientFieldPaint.setShader(gradient);
                drawingCanvas.drawRect(0, 0, bitmapWidth, bitmapHeight, gradientFieldPaint);
            }
        }
    }

    float touchX = - 1;
    float touchY = - 1;
    int touchMode = 0;
    Paint touchPaint = new Paint();
    float thumbRadius = 10;

    @Override
    public void onDraw(Canvas canvas){
        if(canvas.getWidth() > 0 && canvas.getHeight() > 0){
            //System.out.println("begin drawing");
            if(canvas.getWidth() - getPaddingLeft() - getPaddingRight() != bitmapWidth || canvas.getHeight() - getPaddingTop() - getPaddingBottom() != bitmapHeight){
                bitmapHeight = canvas.getHeight() - getPaddingTop() - getPaddingBottom();
                bitmapWidth = canvas.getWidth() - getPaddingLeft() - getPaddingRight();

                if(xAxis != VAR_HUE && yAxis != VAR_HUE){
                    createAlternativeGradientBitmap();
                } else {
                    createGradientBitmap();
                }
            }
            canvas.drawBitmap(gradientField, getPaddingLeft(), getPaddingTop(), bitmapPaint);

            if(touchX > 0 && touchY > 0){ // here I rely on the rest of my code to take care of keeping these coordinates within limits, only checking for initialization
                touchPaint.setColor(Color.WHITE);
                canvas.drawCircle(touchX, touchY, thumbRadius, touchPaint);
                touchPaint.setColor(Color.BLACK);
                canvas.drawCircle(touchX, touchY, thumbRadius * 0.8f, touchPaint);
            }
            //System.out.println("end drawing");
        }
    }

    Rect drawingWindow = new Rect();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //System.out.println("touch received");
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
            if(event.getX() > getPaddingLeft() && event.getX() < getWidth() - getPaddingRight()
                    && event.getY() > getPaddingTop() && event.getY() < getHeight() - getPaddingBottom()) {
                touchX = event.getX();
                touchY = event.getY();
                touchMode = TOUCH_MODE_TOUCH;
                invalidate();
                updatePickedColor();

                drawingWindow.bottom = (int)touchY - 2*(int)thumbRadius - 1;
                drawingWindow.top = (int)touchY + 2*(int)thumbRadius + 1;
                drawingWindow.left = (int)touchX - 2*(int)thumbRadius - 1;
                drawingWindow.right = (int)touchX + 2*(int)thumbRadius + 1;
                invalidate(drawingWindow);

                invalidate();
                // this is sketchy, cause I only seek for the scrollview. There could also be listView or stuff like that
                parent = getParent();
                while(!(parent == null || parent instanceof ScrollView)){
                    parent = parent.getParent();
                }
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                return true;
            }
        } else if(event.getActionMasked() == MotionEvent.ACTION_MOVE && touchMode != TOUCH_MODE_OFF){
            touchMode = TOUCH_MODE_MOVE;
            //System.out.println("touch mode: move");
            touchX = event.getX();
            touchX = Math.max(touchX, getPaddingLeft());
            touchX = Math.min(touchX, getPaddingLeft() + bitmapWidth);

            touchY = event.getY();
            touchY = Math.max(touchY, getPaddingTop());
            touchY = Math.min(touchY, getPaddingTop() + bitmapHeight);

            touchMode = TOUCH_MODE_MOVE;
            drawingWindow.bottom = (int)touchY - 2*(int)thumbRadius - 1;
            drawingWindow.top = (int)touchY + 2*(int)thumbRadius + 1;
            drawingWindow.left = (int)touchX - 2*(int)thumbRadius - 1;
            drawingWindow.right = (int)touchX + 2*(int)thumbRadius + 1;
            invalidate(drawingWindow);

            invalidate();
            updatePickedColor();

            if(parent != null){
                parent.requestDisallowInterceptTouchEvent(true);
            }
            return true;

        } else if(event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL){
            touchMode = TOUCH_MODE_OFF;

            if(onColorPickedListener != null) {
                onColorPickedListener.onStopChangingColor();
            }

            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(false);
            }
            return true;
        }

        return super.onTouchEvent(event);
    }

    float colX;
    float colY;
    float[] pickedHsv = new float[3];

    private void updatePickedColor(){
        colX = touchX - getPaddingLeft();
        colY = touchY - getPaddingTop();

        if(xAxis != VAR_HUE && yAxis != VAR_HUE){
            pickedHsv[0] = maxHsv[0];
        } else if(xAxis != VAR_SAT && yAxis != VAR_SAT){
            pickedHsv[1] = maxHsv[1];
        } else if(xAxis != VAR_VAL && yAxis != VAR_VAL){
            pickedHsv[2] = maxHsv[2];
        }

        pickedHsv[2 - xAxis] = (minHsv[2 - xAxis] + colX/bitmapWidth*hsvDist[2 - xAxis]);
        pickedHsv[2 - yAxis] = (maxHsv[2 - yAxis] - colY/bitmapHeight*hsvDist[2 - yAxis]);

        pickedColor = Color.HSVToColor(pickedHsv);
        if(onColorPickedListener != null){
            onColorPickedListener.onColorPicked(pickedColor);
        }
    }

    public void setPickedColorThirdValue(float var){
        if(xAxis != VAR_HUE && yAxis != VAR_HUE){
            pickedHsv[0] = var;
        } else if(xAxis != VAR_SAT && yAxis != VAR_SAT){
            pickedHsv[1] = var;
        } else if(xAxis != VAR_VAL && yAxis != VAR_VAL){
            pickedHsv[2] = var;
        }
        pickedColor = Color.HSVToColor(pickedHsv);
    }

    private OnColorPickedListener onColorPickedListener;

    public interface OnColorPickedListener{
        void onColorPicked(int color);
        void onStopChangingColor();
    }

    public OnColorPickedListener getOnColorPickedListener() {
        return onColorPickedListener;
    }

    public void setOnColorPickedListener(OnColorPickedListener onColorPickedListener) {
        this.onColorPickedListener = onColorPickedListener;
    }

    public int getPickedColor() {
        return pickedColor;
    }

    public float[] getPickedHSV(){
        return pickedHsv;
    }

    public void setPickedColor(float valueX, float valueY) {

        // this piece checks is the chosen parameters are within the range
        // 2 - xAxis = index in HSV[3] that the x axis represents
        if(valueX < Math.min(minHsv[2 - xAxis], maxHsv[2 - xAxis]) || valueX > Math.max(minHsv[2 - xAxis], maxHsv[2 - xAxis])){
            return;
        }
        if(valueY < Math.min(minHsv[2 - yAxis], maxHsv[2 - yAxis]) || valueY > Math.max(minHsv[2 - yAxis], maxHsv[2 - yAxis])){
            return;
        }

        if(xAxis != VAR_HUE && yAxis != VAR_HUE){
            pickedHsv[0] = maxHsv[0];
        } else if(xAxis != VAR_SAT && yAxis != VAR_SAT){
            pickedHsv[1] = maxHsv[1];
        } else if(xAxis != VAR_VAL && yAxis != VAR_VAL){
            pickedHsv[2] = maxHsv[2];
        }

        pickedHsv[2 - xAxis] = valueX;
        colX = bitmapWidth * (valueX - minHsv[2 - xAxis]) / hsvDist[2 - xAxis];
        touchX = colX + getPaddingLeft();

        pickedHsv[2 - yAxis] = valueY;
        colY = bitmapHeight * (maxHsv[2 - yAxis] - valueY) / hsvDist[2 - yAxis];
        touchY = colY + getPaddingTop();

        pickedColor = Color.HSVToColor(pickedHsv);
        invalidate();
    }


    public void setNewPanelParameters(int xAxis, int yAxis, int minColor, int maxColor){

        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.minColor = minColor;
        this.maxColor = maxColor;

        if(xAxis != VAR_HUE && yAxis != VAR_HUE){
            createAlternativeGradientBitmap();
        } else {
            createGradientBitmap();
        }
        setPickedColor(pickedHsv[2 - xAxis], pickedHsv[2 - yAxis]);
        invalidate();
        requestLayout();
    }

    public int getMaxColor() {
        return maxColor;
    }

    public int getMinColor() {
        return minColor;
    }

    public int getxAxis() {
        return xAxis;
    }

    public int getyAxis() {
        return yAxis;
    }
}
