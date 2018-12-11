package org.biologer.biologer;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by brjovanovic on 1/5/2018.
 */

public class CustomSpinner extends android.support.v7.widget.AppCompatSpinner {
    public CustomSpinner(Context context) {
        super(context);
        setFont();
    }
    public CustomSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }
    public CustomSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFont();
    }

    private void setFont() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/ABeeZee-Regular.ttf");
        //setTypeface(font, Typeface.NORMAL);
    }
}
