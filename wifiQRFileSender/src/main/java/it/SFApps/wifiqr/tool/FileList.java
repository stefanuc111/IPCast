package it.SFApps.wifiqr.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileList extends ArrayList<File>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -578131046899215584L;

	/**
	 * 
	 */
	
private List<OnFileListChangeListener> listeners = new ArrayList<OnFileListChangeListener>();
public interface OnFileListChangeListener
{
	void onFileListChange();
}


public void registerOnFileListChangeListener(OnFileListChangeListener obj)
{
	listeners.add(obj);
}

public void unregisterOnFileListChangeListener(OnFileListChangeListener obj)
{
	listeners.remove(obj);
}

public void unregisterAll()
{
	listeners.clear();
}



@Override
public boolean add(File object)
{
	if(object!=null)
	{
	super.add(object);
	}
	onChange();
	return object!=null?true:false;
}

@Override
public boolean remove(Object object)
{
	boolean b =  super.remove(object);
	onChange();
	return b;
	
}

@Override
public void clear()
{
	super.clear();
	onChange();
}

private void onChange()
{
	for(OnFileListChangeListener l:listeners)
	{
		l.onFileListChange();
	}
}
	
	
}
