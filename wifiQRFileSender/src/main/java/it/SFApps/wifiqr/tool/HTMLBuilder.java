package it.SFApps.wifiqr.tool;

import it.SFApps.wifiqr.R;

import java.io.File;

import android.content.Context;

public class HTMLBuilder {

	public static String home(File[] files, boolean auto_download, Context c)
	{
		String title;
		
		if(auto_download)
		{
			title="Downloading..";
		}else
		{
			title="Download";
		}
		String html="";

		html+=	"<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no' />"+
				"<html>" +
				"<head>" +
				"<title>IPCast</title>" +
				" <link href='/css/stile.css' rel='stylesheet' type='text/css'>" +
				" <link href='/css/loader.css' rel='stylesheet' type='text/css'>" +
				"<script type='text/javascript'> var lang_string = { no_files : '"+c.getString(R.string.no_file_selected)+"', download_now : '"+c.getString(R.string.donwload_now)+"' };</script>" +
				"<script type='text/javascript' src='/jquery-2.0.3.min.js'></script>" +
				"<script type='text/javascript' src='/modernizr.min.js'></script>" +
				"<link href='/videojs/video-js.css' rel='stylesheet'>"+
				"<script src='/videojs/video.js'></script>" +
				"<script type='text/javascript' src='/javascript-compiled.js'></script>" +
				"</head>" +
					"<body>" +
					"<div id='icons'><p>"+c.getString(R.string.sound_beam_not_supported)+"</p><img src='/images/chrome.png'><img src='/images/firefox.png'><img src='/images/opera.png'></div>" +	
						"<div id='overlay'></div>" +
						"<div id='circularG'>"+
								"<div id='circularG_1' class='circularG'></div>"+
								"<div id='circularG_2' class='circularG'></div>"+
								"<div id='circularG_3' class='circularG'></div>"+
								"<div id='circularG_4' class='circularG'></div>"+
								"<div id='circularG_5' class='circularG'></div>"+
								"<div id='circularG_6' class='circularG'></div>"+
								"<div id='circularG_7' class='circularG'></div>"+
								"<div id='circularG_8' class='circularG'></div>"+
						"</div>" +
						"<img id='image_viewer'></img>" +
						"<img id='audio_buttonPlay' src='/images/play.png'></img>" +
						"<video id='video_viewer' class='video-js vjs-default-skin' controls preload='none' width='854' height='480' data-setup='{}'><source type='video/mp4' src='#'></video>"+

						"<div id='realBody'>" +
						"<div id='testata'>" +
						"<div id='img'></div><div id='title'><b>IPCast Receiver</b> - "+title+"</div>" +
						"</div>" +
						"<div id='main'>";
//qui ci vanno i vari files
					html+="</div>" +
					"<div id='bottom'>" +
					"<div id='fileUploaderForm'>" +
						"<div style='position: absolute; left:10px; right:140px;'>" +
							"<div id='progressBar'></div>" +
							"<div id='fileButton'>" +
								"<input id='fileInput' name='files' type='file'>"+
								"<div id='sendButton' class='button'>"+c.getString(R.string.send_file)+"</div>" +
							"</div>" +
						"</div>" +
						"<a id='playstorelink' href='https://play.google.com/store/apps/details?id=it.SFApps.wifiqr'>"+
							"<img id='play_store' alt='Android app on Google Play' src='/images/play_store.png' />"+
						"</a>"+
					"</div>" +
					"</div>" +
       				"</div>" +
					"</body>" +
				"</html>";
					
		return html;
	}


}
