package it.SFApps.wifiqr.tool;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

public class EditTextPreferencePass extends EditTextPreference {

	public EditTextPreferencePass(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	
	}
	
	
	
	public EditTextPreferencePass(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	
	}
	
	public EditTextPreferencePass(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	
	}
	
	@Override
	public void showDialog(Bundle state)
	{
		super.showDialog(state);
			check();
	}
	
	

	
	private void check()
	{
		final AlertDialog d = (AlertDialog) this.getDialog();
		EditText text = this.getEditText();
		text.addTextChangedListener(new TextWatcher (){
			
			@Override
			public void afterTextChanged(Editable arg0) {
				
				
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				if(arg0.length()>7)
				{
					d.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
				}else if(arg0.length()==0){
					
					d.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
					
				}else
				{
					d.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

				}
			}
			
		});
	}

}
