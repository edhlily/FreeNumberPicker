package zjsx.freenumberpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * Created by di.zhang on 2016/6/16.
 */
public class FreeNumberPicker extends LinearLayout {
    int mViewWidth, mViewHeight;
    int btnColor;
    int btnDisableColor;
    int btnPressColor;
    int btnTextColor;
    int btnTextPressColor;
    int btnTextDisableColor;
    int numberColor;
    int numberTextColor;
    int maxValue;
    int minValue;
    float btnTextSize;
    float numberSize;
    int storkColor;
    float btnRadius;
    float storkWidth;
    float numberWidth;
    boolean supportMove;
    boolean supportEdit;
    boolean longPressScroll;
    Paint paint = new Paint();

    public static interface OnNumberChangeListener {
        void onNumberClick();

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

    BtnView tvAdd, tvSub;
    EditText etNumber;
    int number;

    void init(AttributeSet attrs) {

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.FreeNumberPicker);
        btnColor = ta.getColor(R.styleable.FreeNumberPicker_btnColor, Color.TRANSPARENT);
        btnDisableColor = ta.getColor(R.styleable.FreeNumberPicker_btnDisableColor, Color.parseColor("#AAAAAA"));
        btnPressColor = ta.getColor(R.styleable.FreeNumberPicker_btnPressColor, Color.parseColor("#888888"));
        btnTextColor = ta.getColor(R.styleable.FreeNumberPicker_btnTextColor, Color.parseColor("#333333"));
        btnTextPressColor = ta.getColor(R.styleable.FreeNumberPicker_btnTextPressColor, Color.parseColor("#AAAAAA"));
        btnTextDisableColor = ta.getColor(R.styleable.FreeNumberPicker_btnTextDisableColor, Color.parseColor("#CCCCCC"));

        numberColor = ta.getColor(R.styleable.FreeNumberPicker_numberColor, Color.TRANSPARENT);
        numberTextColor = ta.getColor(R.styleable.FreeNumberPicker_numberTextColor, Color.BLACK);
        maxValue = ta.getInteger(R.styleable.FreeNumberPicker_maxValue, Integer.MAX_VALUE - 1);
        minValue = ta.getInteger(R.styleable.FreeNumberPicker_minValue, Integer.MIN_VALUE + 1);
        btnTextSize = ta.getDimension(R.styleable.FreeNumberPicker_btnTextSize, dip2px(getContext(), 2));
        numberSize = ta.getDimension(R.styleable.FreeNumberPicker_numberSize, dip2px(getContext(), 10));
        storkColor = ta.getColor(R.styleable.FreeNumberPicker_storkColor, Color.parseColor("#666666"));
        storkWidth = ta.getDimension(R.styleable.FreeNumberPicker_storkWidth, dip2px(getContext(), 1));
        numberWidth = ta.getDimension(R.styleable.FreeNumberPicker_numberWidth, 0);
        supportMove = ta.getBoolean(R.styleable.FreeNumberPicker_supportMove, true);
        supportEdit = ta.getBoolean(R.styleable.FreeNumberPicker_supportEdit, true);
        btnRadius = ta.getDimension(R.styleable.FreeNumberPicker_btnRadius, 0);
        longPressScroll = ta.getBoolean(R.styleable.FreeNumberPicker_longPressScroll, true);
        ta.recycle();

        paint = new Paint();
        paint.setAntiAlias(true);

        setGravity(Gravity.CENTER);
        setOrientation(HORIZONTAL);

        reset();
    }

    public void reset() {
        removeAllViews();
        setPaint();
        setBg();
        setButton();
        setEdit();

        validateNumber();
    }

    void setPaint() {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(storkWidth);
        paint.setColor(storkColor);
    }

    void setButton() {
        tvAdd = new BtnView(getContext(), BtnView.TYPE_ADD);
        tvSub = new BtnView(getContext(), BtnView.TYPE_SUB);
        tvAdd.setTextSize(btnTextSize);
        tvSub.setTextSize(btnTextSize);
        tvAdd.setPadding(0, 0, 0, 0);
        tvSub.setPadding(0, 0, 0, 0);
        setAddBg();
        setSubBg();

        LayoutParams tvSubParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        LayoutParams tvAddParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        tvSubParams.weight = 1;
        tvAddParams.weight = 1;


        addView(tvSub, tvSubParams);
        addDivider();
        addDivider();
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

    void addDivider() {
        View view = new View(getContext());
        view.setBackgroundColor(storkColor);
        LinearLayout.LayoutParams dividerLayoutParams = new LinearLayout.LayoutParams(Math.round(storkWidth), ViewGroup.LayoutParams.MATCH_PARENT);
        addView(view, dividerLayoutParams);
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

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        if (this.number != number && number <= maxValue && number >= minValue) {
            this.number = number;
            if (onNumberChangeListener != null) {
                onNumberChangeListener.onNumberChanged(number);
            }
        }
        validateNumber();
    }

    void startSub() {
        if (longPressScroll)
            autoChangeHandler.sendEmptyMessageDelayed(SUB, autoChangeGap);
    }

    void startAdd() {
        if (longPressScroll)
            autoChangeHandler.sendEmptyMessageDelayed(ADD, autoChangeGap);
    }

    void stopAutoChange() {
        autoChangeHandler.removeMessages(ADD);
        autoChangeHandler.removeMessages(SUB);
    }

    void setEdit() {
        etNumber = new EditText(getContext());
        etNumber.setBackgroundColor(numberColor);
        etNumber.setGravity(Gravity.CENTER);
        etNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        etNumber.setTextColor(numberTextColor);
        etNumber.setTextSize(numberSize);
        etNumber.setPadding(0, 0, 0, 0);
        etNumber.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onNumberChangeListener != null) {
                    onNumberChangeListener.onNumberClick();
                }
            }
        });
        etNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String expr = "^[0-9]+$";
                if (s.toString().matches(expr)) {
                    number = Integer.valueOf(s.toString());
                    if (number > maxValue) {
                        setNumber(maxValue);
                    } else if (number < minValue) {
                        setNumber(minValue);
                    }
                }
            }
        });
        etNumber.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && "".equals(etNumber.getText().toString())) {
                    setNumber(0);
                }
            }
        });
        etNumber.setFocusable(supportEdit);
        setNumberBg();

        LayoutParams etNumberParams = new LayoutParams(Math.round(numberWidth), ViewGroup.LayoutParams.MATCH_PARENT);
        if (numberWidth <= 0) {
            etNumberParams.weight = 1;
        } else {
            etNumberParams.weight = 0;
        }

        addView(etNumber, 2, etNumberParams);
    }

    void subNumber() {
        int expectNumber = number - 1;
        if (expectNumber < minValue) {
            expectNumber = minValue;
        }
        if (expectNumber != number) {
            number = expectNumber;
            if (onNumberChangeListener != null) {
                onNumberChangeListener.onNumberChanged(expectNumber);
            }
            validateNumber();
        }
    }

    void addNumber() {
        int expectNumber = number + 1;
        if (expectNumber > maxValue) {
            expectNumber = maxValue;
        }
        if (expectNumber != number) {
            number = expectNumber;
            if (onNumberChangeListener != null) {
                onNumberChangeListener.onNumberChanged(expectNumber);
            }
            validateNumber();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    void validateNumber() {
        if (number >= maxValue) {
            number = maxValue;
            tvAdd.setEnabled(false);
        } else {
            tvAdd.setEnabled(true);
        }

        if (number <= minValue) {
            tvSub.setEnabled(false);
            number = minValue;
        } else {
            tvSub.setEnabled(true);
        }
        etNumber.setText(String.valueOf(number));
        etNumber.setSelection(etNumber.length());
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
        gd_background.setColor(btnColor);
        gd_background_press.setColor(btnPressColor);
        gd_background_disable.setColor(btnDisableColor);
        float[] radiusArr = new float[8];
        radiusArr[0] = btnRadius;
        radiusArr[1] = btnRadius;
        radiusArr[6] = btnRadius;
        radiusArr[7] = btnRadius;
        gd_background.setCornerRadii(radiusArr);
        gd_background_press.setCornerRadii(radiusArr);
        gd_background_disable.setCornerRadii(radiusArr);
        bg.addState(new int[]{android.R.attr.state_pressed}, gd_background_press);
        bg.addState(new int[]{android.R.attr.state_enabled}, gd_background);
        bg.addState(new int[]{}, gd_background_disable);
        tvSub.setBackgroundDrawable(bg);
    }

    void setBg() {
        setPadding(Math.round(storkWidth), Math.round(storkWidth), Math.round(storkWidth), Math.round(storkWidth));
        StateListDrawable bg = new StateListDrawable();
        GradientDrawable gd_background = new GradientDrawable();
        gd_background.setColor(Color.WHITE);
        float[] radiusArr = new float[8];
        radiusArr[0] = btnRadius;
        radiusArr[1] = btnRadius;
        radiusArr[2] = btnRadius;
        radiusArr[3] = btnRadius;
        radiusArr[4] = btnRadius;
        radiusArr[5] = btnRadius;
        radiusArr[6] = btnRadius;
        radiusArr[7] = btnRadius;
        gd_background.setCornerRadii(radiusArr);
        gd_background.setStroke(Math.round(storkWidth), storkColor);
        bg.addState(new int[]{}, gd_background);
        setBackgroundDrawable(bg);
    }

    void setAddBg() {
        StateListDrawable bg = new StateListDrawable();
        GradientDrawable gd_background = new GradientDrawable();
        GradientDrawable gd_background_press = new GradientDrawable();
        GradientDrawable gd_background_disable = new GradientDrawable();
        gd_background.setColor(btnColor);
        gd_background_press.setColor(btnPressColor);
        gd_background_disable.setColor(btnDisableColor);
        float[] radiusArr = new float[8];
        radiusArr[2] = btnRadius;
        radiusArr[3] = btnRadius;
        radiusArr[4] = btnRadius;
        radiusArr[5] = btnRadius;
        gd_background.setCornerRadii(radiusArr);
        gd_background_press.setCornerRadii(radiusArr);
        gd_background_disable.setCornerRadii(radiusArr);
        bg.addState(new int[]{android.R.attr.state_pressed}, gd_background_press);
        bg.addState(new int[]{android.R.attr.state_enabled}, gd_background);
        bg.addState(new int[]{}, gd_background_disable);
        tvAdd.setBackgroundDrawable(bg);
    }

    void setNumberBg() {
        StateListDrawable bg = new StateListDrawable();
        GradientDrawable gd_background = new GradientDrawable();
        gd_background.setColor(numberColor);
        GradientDrawable gd_background_press = new GradientDrawable();
        gd_background_press.setColor(numberColor);
        bg.addState(new int[]{android.R.attr.state_pressed}, gd_background_press);
        bg.addState(new int[]{}, gd_background);
        etNumber.setBackgroundDrawable(bg);
    }

    public int getBtnColor() {
        return btnColor;
    }

    public FreeNumberPicker setBtnColor(int btnColor) {
        this.btnColor = btnColor;
        return this;
    }

    public int getBtnDisableColor() {
        return btnDisableColor;
    }

    public FreeNumberPicker setBtnDisableColor(int btnDisableColor) {
        this.btnDisableColor = btnDisableColor;
        return this;
    }

    public int getBtnPressColor() {
        return btnPressColor;
    }

    public FreeNumberPicker setBtnPressColor(int btnPressColor) {
        this.btnPressColor = btnPressColor;
        return this;
    }

    public int getBtnTextColor() {
        return btnTextColor;
    }

    public FreeNumberPicker setBtnTextColor(int btnTextColor) {
        this.btnTextColor = btnTextColor;
        return this;
    }

    public int getBtnTextPressColor() {
        return btnTextPressColor;
    }

    public FreeNumberPicker setBtnTextPressColor(int btnTextPressColor) {
        this.btnTextPressColor = btnTextPressColor;
        return this;
    }

    public int getBtnTextDisableColor() {
        return btnTextDisableColor;
    }

    public FreeNumberPicker setBtnTextDisableColor(int btnTextDisableColor) {
        this.btnTextDisableColor = btnTextDisableColor;
        return this;
    }

    public int getNumberColor() {
        return numberColor;
    }

    public FreeNumberPicker setNumberColor(int numberColor) {
        this.numberColor = numberColor;
        return this;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public FreeNumberPicker setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public int getMinValue() {
        return minValue;
    }

    public FreeNumberPicker setMinValue(int minValue) {
        this.minValue = minValue;
        return this;
    }

    public float getBtnTextSize() {
        return btnTextSize;
    }

    public FreeNumberPicker setBtnTextSize(float btnTextSize) {
        this.btnTextSize = btnTextSize;
        return this;
    }

    public float getNumberSize() {
        return numberSize;
    }

    public FreeNumberPicker setNumberSize(float numberSize) {
        this.numberSize = numberSize;
        return this;
    }

    public int getStorkColor() {
        return storkColor;
    }

    public FreeNumberPicker setStorkColor(int storkColor) {
        this.storkColor = storkColor;
        return this;
    }

    public float getBtnRadius() {
        return btnRadius;
    }

    public FreeNumberPicker setBtnRadius(float btnRadius) {
        this.btnRadius = btnRadius;
        return this;
    }

    public float getStorkWidth() {
        return storkWidth;
    }

    public FreeNumberPicker setStorkWidth(float storkWidth) {
        this.storkWidth = storkWidth;
        return this;
    }

    public float getNumberWidth() {
        return numberWidth;
    }

    public FreeNumberPicker setNumberWidth(float numberWidth) {
        this.numberWidth = numberWidth;
        return this;
    }

    public boolean isSupportMove() {
        return supportMove;
    }

    public FreeNumberPicker setSupportMove(boolean supportMove) {
        this.supportMove = supportMove;
        return this;
    }

    public boolean isSupportEdit() {
        return supportEdit;
    }

    public FreeNumberPicker setSupportEdit(boolean supportEdit) {
        this.supportEdit = supportEdit;
        return this;
    }

    public boolean isLongPressScroll() {
        return longPressScroll;
    }

    public void setLongPressScroll(boolean longPressScroll) {
        this.longPressScroll = longPressScroll;
    }

    public class BtnView extends View {
        public static final int TYPE_SUB = -1;
        public static final int TYPE_ADD = 1;
        public int type;
        public float textSize;

        Paint paint = new Paint();

        public BtnView(Context context, int type) {
            super(context);
            this.type = type;
            paint.setAntiAlias(true);
        }

        int mViewWidth, mViewHeight;

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mViewWidth = w;
            mViewHeight = h;
            Log.d(BtnView.class.getName(), "mViewWidth：" + w + "-" + "mViewHeight:" + h);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            switch (type) {
                case TYPE_SUB:
                    drawHorizontal(canvas);
                    break;
                case TYPE_ADD:
                    drawHorizontal(canvas);
                    drawVertical(canvas);
                    break;
            }
        }

        void drawHorizontal(Canvas canvas) {
            canvas.drawRect(mViewWidth * 0.2f, (mViewHeight - textSize) / 2, mViewWidth * 0.8f, (mViewHeight + textSize) / 2, paint);
        }

        void drawVertical(Canvas canvas) {
            canvas.drawRect((mViewWidth - textSize) / 2, mViewHeight * 0.2f, (mViewWidth + textSize) / 2, mViewHeight * 0.8f, paint);
        }

        public void setTextSize(float textSize) {
            this.textSize = textSize;
            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (isEnabled()) {
                        paint.setColor(btnTextPressColor);
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (isEnabled()) {
                        paint.setColor(btnTextColor);
                        invalidate();
                    }
                    break;
            }
            return super.onTouchEvent(event);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if (enabled) {
                paint.setColor(btnTextColor);
            } else {
                paint.setColor(btnTextDisableColor);
            }
            invalidate();
        }
    }
}
