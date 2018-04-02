package it.SFApps.wifiqr.tool;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;
 
/*
 * This class is useful for using inside of ListView that needs to have checkable items.
 */
public class CheckableRelativeLayout extends RelativeLayout implements Checkable {
    boolean checked=false;
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
	}
    
    @Override 
    public boolean isChecked() { 
        return checked;
    }
    
    @Override 
    public void setChecked(boolean checked) {

    		this.checked=checked;
            refreshDrawableState();

    }
    
    
    @Override 
    public void toggle() { 
    	setChecked(!checked);
    } 
    

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }

        return drawableState;
    }
} 