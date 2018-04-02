package it.SFApps.wifiqr.tool;

import it.SFApps.wifiqr.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class HelpDialog extends DialogFragment{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
    	WebView view = new WebView(this.getActivity());
    	view.loadUrl(getString(R.string.guide_link));
    	
    	 view.setWebViewClient(new WebViewClient(){
    		 @Override
    		    public void onReceivedError(WebView view, int errorCod,String description, String failingUrl) {
    	            Toast.makeText(getActivity(), R.string.no_connection , Toast.LENGTH_LONG).show();
    	               dismiss();

    		 	}
    		 });
    	
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.guide_title));
        builder.setView(view);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}