$(document).ready(init);
var i =0;
var downloaded = new Array();
var time;
var sendingFiles = false;
var display_data;
var display_data_type;
var auto_download;

function sendFiles()
{
	$('#progressBar').fadeIn();
	    var ajaxData = new FormData();
	    ajaxData.append( 'action', 'ajax_handler_import' );

	var files = $('#fileInput')[0].files;
	for (var i=0;i< files.length;i++)
	{
		ajaxData.append('file_'+i,files[i]);
	}

	sendingFiles=true;
	$.ajax({
		  url: 'send_files',
		  data: ajaxData,
		  processData: false,
		  contentType: false,
		  type: 'POST',
		  success: function(data){
			$('#progressBar').fadeOut();
			sendingFiles=false;
		  }
	});

}


var myPlayer;

var startseek;
var seekingtime=0;


var context;
var ws;

function init(jQuery)
{
	var contextClass = (window.AudioContext || 
  window.webkitAudioContext || 
  window.mozAudioContext || 
  window.oAudioContext || 
  window.msAudioContext);
  
	if (contextClass) {
	  // Web Audio API is available.
	  context = new contextClass();
	} else {
	  // Web Audio API is not available. Ask the user to use a supported browser.
	}
	
	if(window.performance)
	{
		performance_now=true;
	}
	
		if(typeof WebSocket =="function")
		{	
			connectWebSocket()
		}else
		{
			loadData();
		}

	$("#audio_buttonPlay").click(enableAudio);
	
	

	$('#sendButton').click(sendFiles);

	if(Modernizr.input["multiple"])
	{
		$('#fileInput').attr('multiple','multiple');
	}else
	{
		alert("Multi upload not supported!");
	}
	

	$('#image_viewer').load(show);
	
	videojs("video_viewer").ready(function(){
		myPlayer = this;
		videojsLoaded=true;
		if(display_data!=null&&display_data_type=="video")
		{
			mPlayer.src("/"+display_data);
			mPlayer.load();
			myPlayer.play();
		}
		myPlayer.M.onseeking=function(){startseek= new Date().getTime();};
		myPlayer.M.onseeked=function(){seekingtime = new Date().getTime()-startseek;};

	});
	
	
}

function connectWebSocket()
{
	var port = window.location.port;
	port++;

	ws = new WebSocket("ws://"+window.location.hostname+":"+port);
	if(ws!=null)
	{
		ws.onopen = WebSocketOpen;
		ws.onmessage = WebSocketMessage;
		ws.onerror = WebSocketError;
		ws.onclose= WebSocketClose;
	}
}

function show()
{
	$('#image_viewer').fadeIn("slow",function(){
			blur_background();
			$("#circularG").hide();

	});
}

var syncronizing=false;
var starttime=null;
function loadData()
{
starttime = getTimer();

$.getJSON( "api.json", function( data ) {
	
		auto_download = data.auto_download;
		updateFileList(data.files);

	if(data.downloaded && sendingFiles)progressHandlingFunction(data.downloaded);
	
	if(data.display_data!=undefined)
	{
		setCurrentData(data.display_data,data.display_data_type);
		if(data.isPlaying)
		{
			if(LocalTimer.length != 0 && Math.abs(getTime()-(data.CurrentTime/1000))>0.5)clearTime();
			var nowtime = getTimer();
			setTime(data.CurrentTime/1000+(nowtime-starttime)/2);
			//console.log("Ping: "+(nowtime-starttime)/2);
			starttime = null;	
			if(display_data_type=="video")updateVideoPlayer();
			if(display_data_type=="audio")updateAudioPlayer();
		}else
		{
			pauseVideoPlayer();
			pauseAudioPlayer();
		}
	}else
	{
		if(display_data!=null)
		{
			closeAllPlayers();
		}
	}
	
	if(data.nex_audio_file!=undefined)
	{
		if(next_audio_file==null || next_audio_file!=data.nex_audio_file)
		{
			if((display_data_type=="audio" && sourcebuffer!=null)||display_data_type!="audio")
			{
			next_audio_file = data.nex_audio_file;
			next_sourcebuffer=null;
			loadNextSound();	
			}
		}
	}else
	{
		next_audio_file=null;
		next_sourcebuffer=null;
	}

		if((display_data_type == "audio"|| display_data_type == "video") && LocalTimer.length<50)
		setTimeout("loadData();",50);
		else{
			setTimeout("loadData();",500);
		}
});
}



function showImage(imageUrl)
{
	$("#image_viewer").attr("src",imageUrl);
	$("#circularG").show();
}

function closeImageViewer()
{
	$("#image_viewer").fadeOut();
	$("#circularG").hide();

}
function closeVideoPlayer()
{
	pauseVideoPlayer();
	$("#video_viewer").fadeOut();

}

function closeAllPlayers()
{
			closeVideoPlayer();			
			closeAudioPlayer();
			closeImageViewer();
			unblur_background();
			$('#realBody').animate({opacity:"1"});
			display_data=null;
			display_data_type = null;
}

function pauseAudioPlayer()
{
	stopTimeSync();
	if(audio_update_interval!=null)clearInterval(audio_update_interval);
	audio_update_interval=null;
	if(source!=null)
	{

			source.disconnect();
			source.stop(0);
			source = null;

	}
	if(source1!=null)
	{

			source1.disconnect();
			source1.stop(0);
			source1 = null;

	}
}



function closeAudioPlayer()
{
	pauseAudioPlayer();
	$("#circularG").hide();
	$("#audio_buttonPlay").fadeOut();
}



function loadVideoSrc(url)
{
	if(myPlayer!=null)
	{
		pauseVideoPlayer();
		myPlayer.src(url);
		myPlayer.load();
	}
	
	$("#video_viewer").fadeIn("slow",function(){blur_background();});

}

function blur_background()
{
	if(!debugM)$('#realBody').css("-webkit-filter",'blur(3px)');
}

function unblur_background()
{
$('#realBody').css("-webkit-filter",'initial');
}
precise_video_sync=false;
function updateVideoPlayer()
{	

	var html5Player;
	if( myPlayer !=null) html5Player = myPlayer.M;
	var errore = myPlayer.currentTime() - getTime();

	if(html5Player!=null && html5Player.readyState == 4 )
	{
		if(html5Player.paused)myPlayer.play();
		
		if(Math.abs(errore)>0.5||syncronizing)
		{
			syncronizing=true;
			if(Math.abs(errore)<=0.05)
			{
				syncronizing=false;
			}else
			if(!myPlayer.M.seeking)
			{
				var seek_to_time = getTime()+seekingtime/1000;
				if(isBuffered(seek_to_time))myPlayer.currentTime(seek_to_time);
			}
		}else if(precise_video_sync){
			
		

				var correzione = 1-errore*0.5;
				if(correzione>1.1)correzione=1.1;
				if(correzione<0.9)correzione=0.9;
				myPlayer.M.playbackRate=correzione;

		}
		
		if(syncronizing)myPlayer.volume(0);
		else
		{
				myPlayer.volume(1);
		}
		
	}else
	{
		myPlayer.pause();
	}
}



function isBuffered(time)
{
	var min = null;
	if(myPlayer!=null)
	{
		for(var i=0;i<myPlayer.M.buffered.length;i++)
		{
			var start = myPlayer.M.buffered.start(i);
			var end = myPlayer.M.buffered.end(i);
			if(time>start && time<end)return true;
			if((min==null|| min>end-time) && end-time>=0)min=end-time;
		}
		
		if( min == null || min>=4 )return true;
		return false;
	}
}



function enableAudio()
{
		if(sourcebuffer!=null)
		{
			source = context.createBufferSource();
			source.buffer = sourcebuffer;
			source.connect(context.destination);
			source.start(0);
		}
}


function updateAudioPlayer()
{

	if(sourcebuffer!=null)
	{
		if(source==null)
		{
			source = context.createBufferSource();
			source.buffer = sourcebuffer;
			source.connect(context.destination);
			source.start(0,getTime());

			if(source1!=null)
			{
			source1.stop(0);
			source1.disconnect();
			source1=null;
			}
		}else
		if(source1==null)
		{
			source1 = context.createBufferSource();
			source1.buffer = sourcebuffer;
			source1.connect(context.destination);
			source1.start(0,getTime());

			
			source.stop(0);
			source.disconnect();
			source=null;
		}
	}
}


var LocalTimer = new Array();
var RecivedTimes = new Array();
var NextValuePosition=0;
function getTime()
{
	var sum = 0;
	for(var i=0;i<RecivedTimes.length;i++)
	{
		sum+=RecivedTimes[i]+((getTimer())-LocalTimer[i]);
	}
	if(RecivedTimes.length == 0)return 0;
	return sum/RecivedTimes.length;
}

var performance_now=false;
function getTimer()
{
	if(display_data_type=="audio")
	{
		return context.currentTime;
	}else
	{
		if(performance_now)
		return window.performance.now()/1000;
		else
		return Date.now()/1000;
	}
	
}

function setTime(time)
{
	LocalTimer[NextValuePosition]=(getTimer());
	RecivedTimes[NextValuePosition]=time;
	if(NextValuePosition==49)
	{
		NextValuePosition=0;
	}else
	NextValuePosition++;

}

function clearTime()
{
	LocalTimer = new Array();
	RecivedTimes = new Array();
	NextValuePosition=0;

}




function progressHandlingFunction(percent){
		var x = Math.round((percent/100)*1000);
		
        $('#progressBar').animate({width: percent+'%','background-position-x': x+"px"});
	
}

var next_audio_file=null;

function loadSoundFile(url) {
	unblur_background();
	$("#circularG").show();
	pauseAudioPlayer();
	sourcebuffer=null;
	if(next_audio_file!=null && "/"+next_audio_file==url && next_sourcebuffer!=null)
	{
			next_audio_file=null;
			initSound(next_sourcebuffer);
			next_sourcebuffer=null;
	}else
	{
		loadBuffer(url,initSound);
	}
}


var source=null;
var source1=null;
var sourcebuffer=null;
var next_sourcebuffer=null;
var loading_next=false;
var inittime=0;

function initSound(buffer)
{
	if(display_data_type=="audio")
	{
		clearTime();
		$("#circularG").hide();
		$("#audio_buttonPlay").fadeIn('slow',function(){blur_background();});
		sourcebuffer = buffer;
		loadNextSound();
	}
}

function loadNextSound()
{
	if(next_audio_file!=null && next_sourcebuffer==null)
	{
	loading_next=true;
	loadBuffer("/"+next_audio_file,function(buffer){
		next_sourcebuffer=buffer;
		loading_next=false;
		});
	}
}

var request;
var response;
function loadBuffer(url,callback)
{
		if(request)request.abort();
		request = new XMLHttpRequest();
		  request.open('GET', url, true);
		  request.responseType = 'arraybuffer';
		  request.onload = function(e) {
			  response = request.response;
				context.decodeAudioData(request.response, callback, function(){alert("error");});
				request=null;
		  };
		   request.send();

}


function WebSocketOpen()
{
	
}



function WebSocketMessage(message)
{
	if(message.data!="w")
	{
		var msg = JSON.parse(message.data);
		//console.log(message.timeStamp);
		if(msg.CurrentTime!=undefined)
		{
			setTime(msg.CurrentTime);
		}
		switch(msg.type)
		{
			case "CurrentTime":
				if(LocalTimer.length != 0 && Math.abs(getTime()-(msg.value/1000))>0.5)clearTime();
				var nowtime;
				
				if(starttime==null)starttime = nowtime = 0;
				else
				nowtime = getTimer();
				
				if(RecivedTimes[RecivedTimes.length-1]!=msg.value)
				setTime(msg.value/1000+(nowtime-starttime)/2);
				//console.log("Ping: "+(nowtime-starttime)/2);
				starttime = null;
			break;
			case "CurrentData":
				if(msg.data!=undefined)
				setCurrentData(msg.data,msg.data_type);
				else closeAllPlayers();
			break;
			case "NextData":
				if(msg.data_type=="audio" && next_audio_file!=msg.data)
				{
					next_audio_file = msg.data;
					next_sourcebuffer=null;
					if((sourcebuffer!=null && display_data_type =="audio") || display_data_type=="video")loadNextSound();
				}
			break;
			case "FileListChange":
				updateFileList(msg.files);
			break;
			case "Seeked":
			break;
			case "SettingsChanges":
			auto_download = msg.AutoDownload;
			precise_video_sync = msg.FineVideoSync;
			if(msg.Debug)
			{
				debugMode();
			}else removeDebugMode();
			
			break;
			case "UploadedStatusChange":
				if(msg.value)progressHandlingFunction(msg.value);
			break;
			case "PlayStatusChange":
				if(display_data_type=="video")
				{
					if(msg.isPlaying)playVideo();
					else pauseVideoPlayer();
				}else
				{
					if(msg.isPlaying)playAudio();
					else pauseAudioPlayer();
				}
			break;
			
		}
	}else
	{
		console.log(message.data);
	}
	
	
}

function WebSocketClose()
{
	closeAllPlayers();
	setTimeout("connectWebSocket();",2000);
}


function WebSocketError()
{
	closeAllPlayers();
	//setTimeout("connectWebSocket();",500);
}

function pauseVideoPlayer()
{
	stopTimeSync();
	if(video_update_interval!=null)
	clearInterval(video_update_interval);
	isPlaying=false;
	video_update_interval=null;
	if(myPlayer!=null) myPlayer.pause();
}


function setCurrentData(data, data_type)
{
		display_data_type = data_type;

		if(display_data!=data)
		{
			display_data = data;
			if(display_data_type=="image")
			{
				closeVideoPlayer();
				closeAudioPlayer();
				showImage('/' + display_data);				
			}else if(display_data_type=="video")
			{
				$("#circularG").hide();
				closeAudioPlayer();
				closeImageViewer();
				loadVideoSrc('/' + display_data);
			}else if(display_data_type=="audio")
			{
				closeVideoPlayer();
				closeImageViewer();
				closeAudioPlayer();
				loadSoundFile('/'+display_data);
			}
			$('#realBody').animate({opacity:"0.5"});
		}
}

var video_update_interval=null;
var isPlaying=false;
function playVideo()
{
	myPlayer.M.oncanplay=null;
	if(myPlayer!=null && myPlayer.M !=null)
	{
		if(myPlayer.M.readyState==4)
		{
			myPlayer.play();
			if(video_update_interval==null)
			video_update_interval = setInterval("updateVideoPlayer();",500);
			startTimeSync();
			isPlaying = true;
		}else
		{
				myPlayer.M.oncanplay=playVideo;

		}
	}
}

var getTime_interval=null;
function startTimeSync()
{
		if(getTime_interval==null)
		getTime_interval = setInterval("requestNewTime();",50);
}

function requestNewTime()
{
	if(starttime==null)
	{
		starttime = getTimer();
		ws.send('getCurrentTime');
	}
}

function stopTimeSync()
{
	if(getTime_interval!=null)
	clearTimeout(getTime_interval);
	starttime=null;
	getTime_interval=null;
}

var audio_update_interval=null;
function playAudio()
{
	if(audio_update_interval==null)
	{
		startTimeSync();
		updateAudioPlayer();
		audio_update_interval = setInterval("updateAudioPlayer();",500);
		
	}
}


function updateFileList(files)
{
		$("#main").html("");
		for(var i =0; i<files.length;i++)
		{
			$("#main").append("<div class='filerow'><div class='fileicon'></div><div class='link'><a href='/"+files[i].hash+"'>"+files[i].name+" - "+lang_string.download_now+"</a></div></div>");

			if(downloaded[files[i].hash+""]!=true && auto_download)
			{
				setTimeout('window.location= "/'+files[i].hash+'";',500+i*500);
				downloaded[files[i].hash+""]=true;
			}
		}

	if(files.length==0)
	{
		$("#main").html("<p id='noFile'>"+lang_string.no_files+"</p>");
	}
}






var debugM=false;
var debug_interval=null;
function debugMode()
{
	debugM=true;
	$("body").append("<div id='time'></div> </ br><div id='ping'></div>");
	debug_interval = setInterval("$('#time').html(Math.floor(getTime()*1000))",30);
	$('#time').css("font-size","100px");
	unblur_background();
}

function removeDebugMode()
{
		debugM=false;
		if(debug_interval!=null)clearInterval(debug_interval);
		$("#ping").remove();
		$("#time").remove();

}
