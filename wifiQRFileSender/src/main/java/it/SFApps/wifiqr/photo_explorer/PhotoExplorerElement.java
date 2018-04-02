package it.SFApps.wifiqr.photo_explorer;

import java.io.File;

public class PhotoExplorerElement {
public String name;
public File file;
public boolean isPhoto=true;
public Integer date;
public Long id;
PhotoExplorerElement(String name,File file,Integer date, boolean isPhoto, Long id)
{
	this.file = file;
	this.name = name;
	this.isPhoto = isPhoto;
	this.date = date;
	this.id = id;
}

PhotoExplorerElement(String name,File file, boolean isPhoto)
{
	this.file = file;
	this.name = name;
	this.isPhoto = isPhoto;
}

PhotoExplorerElement(String name,File file)
{
	this.file = file;
	this.name = name;

}


}
