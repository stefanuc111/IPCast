package it.SFApps.wifiqr.app_explorer;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

public class AppElement {
public Drawable icon;
public String name;
public ApplicationInfo info;
AppElement(ApplicationInfo info,Drawable icon,String name)
{
	this.icon = icon;
	this.name = name;
	this.info = info;
}

AppElement()
{

}

}
