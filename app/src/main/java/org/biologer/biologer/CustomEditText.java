package org.biologer.biologer;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by brjovanovic on 12/26/2017.
 */

public class CustomEditText extends android.support.v7.widget.AppCompatEditText {
    public CustomEditText(Context context) {
        super(context);
        setFont();
    }
    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }
    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFont();
    }

    private void setFont() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/ABeeZee-Regular.ttf");
        setTypeface(font, Typeface.NORMAL);
    }
}
