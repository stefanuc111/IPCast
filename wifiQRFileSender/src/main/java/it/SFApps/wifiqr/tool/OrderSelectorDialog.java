package it.SFApps.wifiqr.tool;

import it.SFApps.wifiqr.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

public class OrderSelectorDialog extends DialogFragment {
	
	public interface OrderSelectorListener{
		public void onOrderClick(DialogInterface dialog,int which);
	}
	public static final class OrderNameFiles{
		public static final int NAME_ASCEND = 0,
							NAME_DECRESENT=1,
							SIZE_ASCEND =2,
							SIZE_DECRESCENT=3;
	}
	public static final class OrderNamePhoto{
		public static final int  RECENT = 0,
							TYPE = 1,
							SIZE_ASCEND =2,
							SIZE_DECRESCENT=3;
	}
	
	private OrderSelectorListener mListener;
	private Integer mOrderNameResource=null;
	
	public void setOrderNames(int Resource)
	{
		mOrderNameResource=Resource;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    int resource;
	    if(mOrderNameResource!=null)
	    {
	    	resource = mOrderNameResource;
	    }else
	    {
	    	resource = R.array.order_names_files;
	    }
	    
	    builder.setTitle(R.string.order_by)
	           .setItems(resource, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	               // The 'which' argument contains the index position
	               // of the selected item
	            	   if(mListener!=null) mListener.onOrderClick(dialog, which);
	           }
	    });
	    return builder.create();
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (OrderSelectorListener) ((FragmentActivity) activity).getSupportFragmentManager().findFragmentById(R.id.content_frame);
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement OrderSelectorListener");
        }
    }

}
