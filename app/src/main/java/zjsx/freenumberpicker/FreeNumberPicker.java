package zjsx.freenumberpicker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by di.zhang on 2016/6/16.
 */
public class FreeNumberPicker extends LinearLayout {
    int mViewWidth, mViewHeight;
    int buttonColor;
    int buttonDisableColor;
    int buttonPressColor;
    int buttonTextColor;
    int buttonTextPressColor;
    int buttonTextDisableColor;
    int numberColor = Color.BLACK;
    int maxValue;
    int minValue;
    float buttonTextSize;
    float numberSize;
    int borderColor;
    float buttonRadius;
    float borderWidth;
    float numberWidth;
    boolean supportMove;
    boolean supportEdit;
    Paint paint = new Paint();

    public static interface OnNumberChangeListener {
        void onNumberChanged(int value);
    }

    OnNumberChangeListener onNumberChangeListener;

    public FreeNumberPicker(Context context) {
        super(context);
        init(null);
    }

    public FreeNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public FreeNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    TextView tvAdd, tvSub;
    EditText etNumber;
    int number;

    void init(AttributeSet attrs) {

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.FreeNumberPicker);
        buttonColor = ta.getColor(R.styleable.FreeNumberPicker_buttonColor, Color.parseColor("#00000000"));
        buttonDisableColor = ta.getColor(R.styleable.FreeNumberPicker_buttonDisableColor, Color.parseColor("#AAAAAA"));
        buttonPressColor = ta.getColor(R.styleable.FreeNumberPicker_buttonPressColor, Color.parseColor("#888888"));
        buttonTextColor = ta.getColor(R.styleable.FreeNumberPicker_buttonTextColor, Color.parseColor("#333333"));
        buttonTextPressColor = ta.getColor(R.styleable.FreeNumberPicker_buttonTextPressColor, Color.parseColor("#AAAAAA"));
        buttonTextDisableColor = ta.getColor(R.styleable.FreeNumberPicker_buttonTextDisableColor, Color.parseColor("#CCCCCC"));

        numberColor = ta.getColor(R.styleable.FreeNumberPicker_numberColor, Color.parseColor("#333333"));
        maxValue = ta.getInteger(R.styleable.FreeNumberPicker_maxValue, Integer.MAX_VALUE - 1);
        minValue = ta.getColor(R.styleable.FreeNumberPicker_minValue, Integer.MIN_VALUE + 1);
        buttonTextSize = ta.getDimension(R.styleable.FreeNumberPicker_buttonTextSize, dip2px(getContext(), 5));
        numberSize = ta.getDimension(R.styleable.FreeNumberPicker_numberSize, dip2px(getContext(), 5));
        borderColor = ta.getColor(R.styleable.FreeNumberPicker_borderColor, Color.parseColor("#666666"));
        borderWidth = ta.getDimension(R.styleable.FreeNumberPicker_borderWidth, dip2px(getContext(), 1));
        numberWidth = ta.getDimension(R.styleable.FreeNumberPicker_numberWidth, 0);
        supportMove = ta.getBoolean(R.styleable.FreeNumberPicker_supportMove, true);
        supportEdit = ta.getBoolean(R.styleable.FreeNumberPicker_supportMove, true);
        buttonRadius = ta.getDimension(R.styleable.FreeNumberPicker_buttonRadius, 0);
        ta.recycle();

        paint = new Paint();
        paint.setAntiAlias(true);
        removeAllViews();
        setGravity(Gravity.CENTER);
        setOrientation(HORIZONTAL);

        setPaint();
        setButton();
        setEdit();

        validateNumber();
    }

    void setPaint() {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        paint.setColor(borderColor);
    }

    void setButton() {
        tvAdd = new TextView(getContext());
        tvSub = new TextView(getContext());
        tvAdd.setGravity(Gravity.CENTER);
        tvSub.setGravity(Gravity.CENTER);
        tvAdd.setTextSize(buttonTextSize);
        tvSub.setTextSize(buttonTextSize);
        tvAdd.setPadding(0, 0, 0, 0);
        tvSub.setPadding(0, 0, 0, 0);

        setTextColor(tvAdd);
        setTextColor(tvSub);

        setAddBg();
        setSubBg();

        tvSub.setText("-");
        tvAdd.setText("+");

        LayoutParams tvSubParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        LayoutParams tvAddParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        tvSubParams.weight = 1;
        tvAddParams.weight = 1;


        addView(tvSub, tvSubParams);
        addView(tvAdd, tvAddParams);

        tvSub.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                subNumber();
            }
        });

        tvAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addNumber();
            }
        });

        tvSub.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startSub();
                return false;
            }
        });

        tvAdd.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startAdd();
                return false;
            }
        });

        tvSub.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
                    stopAutoChange();
                }
                return false;
            }
        });

        tvAdd.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
                    stopAutoChange();
                }
                return false;
            }
        });
    }

    static final int ADD = 1;
    static final int SUB = -1;
    int autoChangeGap = 50;
    Handler autoChangeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ADD:
                    addNumber();
                    sendEmptyMessageDelayed(ADD, autoChangeGap);
                    break;
                case SUB:
                    subNumber();
                    sendEmptyMessageDelayed(SUB, autoChangeGap);
                    break;
            }
        }
    };

    void startSub() {
        autoChangeHandler.sendEmptyMessageDelayed(SUB, autoChangeGap);
    }

    void startAdd() {
        autoChangeHandler.sendEmptyMessageDelayed(ADD, autoChangeGap);
    }

    void stopAutoChange() {
        autoChangeHandler.removeMessages(ADD);
        autoChangeHandler.removeMessages(SUB);
    }

    void setEdit() {
        etNumber = new EditText(getContext());
        etNumber.setBackgroundResource(0);
        etNumber.setGravity(Gravity.CENTER);
        etNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        etNumber.setTextColor(numberColor);
        etNumber.setTextSize(numberSize);
        etNumber.setEnabled(supportEdit);
        etNumber.setPadding(0, 0, 0, 0);
        setNumberBg();

        LayoutParams etNumberParams = new LayoutParams(Math.round(numberWidth), ViewGroup.LayoutParams.MATCH_PARENT);
        if (numberWidth <= 0) {
            etNumberParams.weight = 1;
        } else {
            etNumberParams.weight = 0;
        }

        etNumberParams.setMargins(Math.round(-borderWidth), 0, Math.round(-borderWidth), 0);
        addView(etNumber, 1, etNumberParams);
    }

    void subNumber() {
        int expectNumber = number - 1;
        if (expectNumber < minValue) {
            expectNumber = minValue;
        }
        if (expectNumber != number) {
            if (onNumberChangeListener != null) {
                onNumberChangeListener.onNumberChanged(expectNumber);
            }
            number = expectNumber;
            validateNumber();
        }
    }

    void addNumber() {
        int expectNumber = number + 1;
        if (expectNumber > maxValue) {
            expectNumber = maxValue;
        }
        if (expectNumber != number) {
            if (onNumberChangeListener != null) {
                onNumberChangeListener.onNumberChanged(expectNumber);
            }
            number = expectNumber;
            validateNumber();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    void validateNumber() {
        if (number >= maxValue) {
            tvAdd.setEnabled(false);
        } else {
            tvAdd.setEnabled(true);
        }

        if (number <= minValue) {
            tvSub.setEnabled(false);
        } else {
            tvSub.setEnabled(true);
        }
        etNumber.setText(String.valueOf(number));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mViewWidth = w;
        mViewHeight = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public OnNumberChangeListener getOnNumberChangeListener() {
        return onNumberChangeListener;
    }

    public void setOnNumberChangeListener(OnNumberChangeListener onNumberChangeListener) {
        this.onNumberChangeListener = onNumberChangeListener;
    }

    // 根据手机的分辨率从 dp 的单位 转成为 px(像素)
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    float downX;
    float downY;
    float lastX;
    float lastY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                lastX = downX;
                lastY = downY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - downX) > 10 && Math.abs(event.getY() - downY) < 50) {
                    if (supportMove) {
                        if (event.getX() > lastX) {
                            addNumber();
                        } else {
                            subNumber();
                        }
                        lastX = event.getX();
                        lastY = event.getY();
                    }
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }


    void setSubBg() {
        StateListDrawable bg = new StateListDrawable();
        GradientDrawable gd_background = new GradientDrawable();
        GradientDrawable gd_background_press = new GradientDrawable();
        GradientDrawable gd_background_disable = new GradientDrawable();
        gd_background.setColor(buttonColor);
        gd_background_press.setColor(buttonPressColor);
        gd_background_disable.setColor(buttonDisableColor);
        float[] radiusArr = new float[8];
        radiusArr[0] = buttonRadius;
        radiusArr[1] = buttonRadius;
        radiusArr[6] = buttonRadius;
        radiusArr[7] = buttonRadius;
        gd_background.setCornerRadii(radiusArr);
        gd_background_press.setCornerRadii(radiusArr);
        gd_background_disable.setCornerRadii(radiusArr);

        gd_background.setStroke(Math.round(borderWidth), borderColor);
        gd_background_press.setStroke(Math.round(borderWidth), borderColor);
        gd_background_disable.setStroke(Math.round(borderWidth), borderColor);

        bg.addState(new int[]{android.R.attr.state_pressed}, gd_background_press);
        bg.addState(new int[]{android.R.attr.state_enabled}, gd_background);
        bg.addState(new int[]{}, gd_background_disable);
        tvSub.setBackgroundDrawable(bg);
    }

    void setAddBg() {
        StateListDrawable bg = new StateListDrawable();
        GradientDrawable gd_background = new GradientDrawable();
        GradientDrawable gd_background_press = new GradientDrawable();
        GradientDrawable gd_background_disable = new GradientDrawable();
        gd_background.setColor(buttonColor);
        gd_background_press.setColor(buttonPressColor);
        gd_background_disable.setColor(buttonDisableColor);
        float[] radiusArr = new float[8];
        radiusArr[2] = buttonRadius;
        radiusArr[3] = buttonRadius;
        radiusArr[4] = buttonRadius;
        radiusArr[5] = buttonRadius;
        gd_background.setCornerRadii(radiusArr);
        gd_background_press.setCornerRadii(radiusArr);
        gd_background_disable.setCornerRadii(radiusArr);

        gd_background.setStroke(Math.round(borderWidth), borderColor);
        gd_background_press.setStroke(Math.round(borderWidth), borderColor);
        gd_background_disable.setStroke(Math.round(borderWidth), borderColor);

        bg.addState(new int[]{android.R.attr.state_pressed}, gd_background_press);
        bg.addState(new int[]{android.R.attr.state_enabled}, gd_background);
        bg.addState(new int[]{}, gd_background_disable);
        tvAdd.setBackgroundDrawable(bg);
    }

    void setNumberBg() {
        StateListDrawable bg = new StateListDrawable();
        GradientDrawable gd_background = new GradientDrawable();
        GradientDrawable gd_background_press = new GradientDrawable();
        gd_background.setStroke(Math.round(borderWidth), borderColor);
        gd_background_press.setStroke(Math.round(borderWidth), borderColor);
        bg.addState(new int[]{android.R.attr.state_pressed}, gd_background_press);
        bg.addState(new int[]{}, gd_background);
        etNumber.setBackgroundDrawable(bg);
    }

    void setTextColor(TextView tv) {
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_pressed},
                        new int[]{android.R.attr.state_enabled},
                        new int[]{}},
                new int[]{
                        buttonTextPressColor,
                        buttonTextColor,
                        buttonTextDisableColor
                });
        tv.setTextColor(colorStateList);
    }
}
