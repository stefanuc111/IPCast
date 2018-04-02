package it.SFApps.wifiqr.tool;

import it.SFApps.wifiqr.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.ScrollView;
import android.widget.TextView;

public class LicenseDialog extends DialogFragment{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
    	TextView text = new TextView(this.getActivity());
    	ScrollView scrollView = new ScrollView(this.getActivity());
    	text.setPadding(15, 15, 15, 15);
    	scrollView.addView(text);
    	text.setSingleLine(false);
    	text.setTextSize(17);
    	text.setText(getString(R.string.nanohttpd_license));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.open_source));
        builder.setView(scrollView);
               
        // Create the AlertDialog object and return it
        return builder.create();
    }
}