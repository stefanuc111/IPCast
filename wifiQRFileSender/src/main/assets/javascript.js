$(document).ready(init);
var i =0;
var downloaded = new Array();
var time;
var sendingFiles = false;
var display_data;
var display_data_type;
var auto_download;

var fading;

function sendFiles()
{
	fading=true;
	$('#progressBar').fadeIn(400,function(){fading=false});
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
			$('#progressBar').fadeOut(400,function(){
											$('#progressBar').css('width','0%');
											$("#fileButton").fadeIn();}
											);
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
	
	
	    $("#fileInput").change(function() {
				if($('#fileInput')[0].files.length>0)
				{
					$("#fileButton").fadeOut(400,sendFiles);
				}
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
		sound_beam = data.SoundBeam;
		updateFileList(data.files);

	if(data.downloaded && sendingFiles)progressHandlingFunction(data.downloaded);
	
	if(data.display_data!=undefined)
	{
		if(sound_beam && (data.display_data_type=="audio" || data.display_data_type=="video"))
		{
			showSupportedB();
		}else
		{
			hideSupportedB();
			setCurrentData(data.display_data,data.display_data_type);
		}
		
		
		if(data.isPlaying)
		{
			if(RecivedTimes.length != 0 && Math.abs(getTime()-(data.CurrentTime/1000))>0.5)clearTime();
			var nowtime = getTimer();
			setTime(data.CurrentTime/1000+(nowtime-starttime)/2);
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
		hideSupportedB();
		if(display_data!=null)
		{
			closeAllPlayers();
		}
	}

		if((display_data_type == "audio"|| display_data_type == "video") && RecivedTimes.length<50)
		setTimeout("loadData();",50);
		else{
			setTimeout("loadData();",500);
		}
});
}

var show_supported=false;

function showSupportedB()
{
	if(!show_supported)
	{
		$("#icons").show();
		$("#overlay").show();		
	}
	show_supported = true;
}

function hideSupportedB()
{
	if(show_supported)
	{
		$("#icons").hide();
		$("#overlay").hide();
	}
	show_supported = false;

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
			$('#overlay').fadeOut();
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
	if(next_chunkSource!=null)
	{
		next_chunkSource.disconnect();
		next_chunkSource.stop(0);
		next_chunkSource = null;
	}
}



function closeAudioPlayer()
{
	clearTime();
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
if(!debugM)$('#realBody').addClass("blur");
}

function unblur_background()
{
$('#realBody').removeClass("blur");
}
var sound_beam=false;
function updateVideoPlayer()
{	

	var html5Player;
	if( myPlayer !=null) html5Player = myPlayer.M;
	var errore = myPlayer.currentTime() - getTime();

	if(html5Player!=null && html5Player.readyState == 4 )
	{
		if(html5Player.paused)myPlayer.play();
		
		if(Math.abs(errore)>(sound_beam?0.5:3)||syncronizing)
		{
			syncronizing=true;
			if(Math.abs(errore)<= (sound_beam?0.15:1))
			{
				syncronizing=false;
			}else
			if(!myPlayer.M.seeking)
			{
				var seek_to_time = getTime()+seekingtime/1000;
				if(isBuffered(seek_to_time))myPlayer.currentTime(seek_to_time);
			}
		}else if(sound_beam){
			
		

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

var chunkbuffers = new Array();
function buffer()
{
	this.buffer = null;
	this.chunk_number = null;
}

function getBuffer(chunk, block_other)
{
	if(1000*chunk*frame_duration<duration)
	{
		for(var i = 0;i<chunkbuffers.length;i++)
		{
			if(chunkbuffers[i].chunk_number == chunk)
			{
				return chunkbuffers[i].buffer;
			}
		}


		var loadbuffer = new buffer();
		loadbuffer.chunk_number = chunk;
		chunkbuffers.push(loadbuffer);



		loadBuffer('/'+display_data+"_"+chunk,
			function(buffer){
				if(chunkbuffers.length>3)
				{
					chunkbuffers.splice(0,chunkbuffers.length-3);
				}
				loadbuffer.buffer = buffer;

			},
			function(){
				removeBuffer(loadbuffer);
			},
			block_other);
	}
	return false;
}

function removeBuffer(buffer)
{
	for(var i = 0;i<chunkbuffers.length;i++)
	{
		if(chunkbuffers[i] == buffer)
		{
		chunkbuffers.splice(i,1);
		}
	}	
}


var request;
var response;
function loadBuffer(url,callback,callback_fail,block_other)
{
		if(request )
		{
			if(block_other)
			request.abort();
			else
			{
			callback_fail();
			return;
			}
		}
		
		request = new XMLHttpRequest();
		  request.open('GET', url, true);
		  request.responseType = 'arraybuffer';
		  request.onload = function(e) {
			  if(request.status==200)
			  {
			  response = request.response;
			  context.decodeAudioData(request.response, callback, function(){alert("decoding error!");});
			  }else callback_fail();
			  
			  request=null;
		  };
		  request.onabort = callback_fail;
		  request.onerror = callback_fail;
		   request.send();

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

function getChunkIndex(time)
{
	return (time/(frame_duration*1000))|0;
}

var next_chunkSource=null;
var frame_duration=1152/44100;
var duration;
function updateAudioPlayer()
{
	var newsource = null;
	var tmp = getTime();
	var buffer = getBuffer(getChunkIndex(tmp),true);
	var time;
	var context_time;

	
	if(buffer)
	{
		newsource = context.createBufferSource();
		newsource.connect(context.destination);
		newsource.buffer = buffer;
		newsource.start(0,time = tmp-(getChunkIndex(tmp)*1000*frame_duration));
		context_time = getTimer();
		
		if(next_chunkSource!=null)
		{
			if(next_chunkSource.playbackState!=0)next_chunkSource.stop(0);
			next_chunkSource.disconnect();
			next_chunkSource = null;
		}

			var b = getBuffer(getChunkIndex(tmp)+1,false);
			if(b)
			{
				next_chunkSource = context.createBufferSource();
				next_chunkSource.buffer = b;
				next_chunkSource.connect(context.destination);
				next_chunkSource.start(context_time+(buffer.duration-time),frame_duration*2);
			}
		
		showPlay();
	}else
	{
		unblur_background();
		$("#audio_buttonPlay").fadeOut();
		$("#circularG").show();
	}
	
	
	if(source!=null)
	{
	if(source.playbackState!=0)source.stop(0);
	source.disconnect();
	}
	
	source = newsource;	
}

var bufferedChunk;

var RecivedTimes = new Array();
var NextValuePosition=0;
function getTime()
{
	var sum = 0;
	for(var i=0;i<RecivedTimes.length;i++)
	{
		sum+=RecivedTimes[i];
	}
	if(RecivedTimes.length == 0)return 0;
	return (sum/RecivedTimes.length)+getTimer();
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
	RecivedTimes[NextValuePosition]=time-getTimer();
	if(NextValuePosition==49)
	{
		NextValuePosition=0;
	}else
	NextValuePosition++;

}

function clearTime()
{
	RecivedTimes = new Array();
	NextValuePosition=0;

}




function progressHandlingFunction(percent){
		var x = Math.round((percent/100)*1000);
		if(!fading)$('#progressBar').stop(true);
        $('#progressBar').animate({width: percent+'%','background-position-x': x+"px"});
	
}

var next_audio_file=null;

var file_name,extension;


var source=null;
var sourcebuffer=null;
var next_sourcebuffer=null;
var next_chunk_is_loading=false;

function showPlay()
{
		$("#circularG").hide();
		$("#audio_buttonPlay").fadeIn('slow',function(){blur_background();});
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
//				if(LocalTimer.length != 0 && Math.abs(getTime()-(msg.value/1000))>0.5)clearTime();
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
				hideSupportedB();
				if(msg.data!=undefined)
				{
				setCurrentData(msg.data,msg.data_type);
				if(msg.duration != undefined) duration = msg.duration;
				if(msg.sample_rate != undefined) frame_duration = 1152/msg.sample_rate;
				}
				else closeAllPlayers();
				if(isPlaying && msg.data_type=="audio")playAudio();
				
				
			break;
			case "NextData":
				if(msg.data_type=="audio" && next_audio_file!=msg.data)
				{
					/*next_audio_file = msg.data;
					next_sourcebuffer=null;
					if((sourcebuffer!=null && display_data_type =="audio") || display_data_type=="video")loadNextSound();
					*/
				}
			break;
			case "FileListChange":
				updateFileList(msg.files);
			break;
			case "Seeked":
				clearTime();
				starttime = null;
			break;
			case "SettingsChanges":
			auto_download = msg.AutoDownload;
			sound_beam = msg.SoundBeam;
			if(msg.Debug)
			{
				debugMode();
			}else removeDebugMode();
			
			break;
			case "UploadedStatusChange":
				if(msg.value)progressHandlingFunction(msg.value);
			break;
			case "PlayStatusChange":
			isPlaying = msg.isPlaying;
			if(display_data!=null)
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
				chunkbuffers = new Array();
				closeVideoPlayer();
				closeImageViewer();
				closeAudioPlayer();
			}
			$('#overlay').fadeIn();
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
	
	if(context!=undefined)
	{
		if(audio_update_interval==null)
		{
			startTimeSync();
			updateAudioPlayer();
			audio_update_interval = setInterval("updateAudioPlayer();",500);

		}
	}else
	{
		showSupportedB();
	}
}


function updateFileList(files)
{
	$("#main").html("");
	for(var i =0; i<files.length;i++)
	{
		$("#main").append("<div class='filerow' href='/"+files[i].hash+"'><div class='fileicon'></div><div class='link'>"+files[i].name+" - "+lang_string.download_now+"</div></div>");

		if(downloaded[files[i].hash+""]!=true && auto_download)
		{
			setTimeout('downloadURL("/'+files[i].hash+'");',500+i*500);
			downloaded[files[i].hash+""]=true;
		}
	}

	if(files.length==0)
	{
		$("#main").html("<p id='noFile'>"+lang_string.no_files+"</p>");
	}
	
	$(".filerow").click(function(){
		var href = $(this).attr('href');
		downloadURL(href);
	});
	
}

function downloadURL(url) {
    var hiddenIFrameID = 'hiddenDownloader',
        iframe = document.getElementById(hiddenIFrameID);
    if (iframe === null) {
        iframe = document.createElement('iframe');
        iframe.id = hiddenIFrameID;
        iframe.style.display = 'none';
        document.body.appendChild(iframe);
    }
    iframe.src = url;
};







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