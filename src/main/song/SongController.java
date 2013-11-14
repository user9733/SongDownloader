package main.song;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import main.SongDownloader;
import main.WebPageReader;

import common.StringOps;

public class SongController implements ISongUpdateListener{

	private Song song;
	private SongPanel songPanel;
	String input;
	String log="";
	private Thread convertThread;
	private Thread downloadThread;
	private Process youtubeProc;
	private Process ffmpegProc;
	boolean isCancelled = false;
	ISongDownloadedListener downloadedListener;
	String finalFilName;
	boolean keepVideo = false;
	
	public SongController(String input){
		this.input = input;
		if (StringOps.isEmpty(input)){
			return;
		}
		if (input.contains("http://www.youtube.com/watch?v=")){
			//Get title from youtube
			String link = input;
			String title = getTitleFromYoutubeLink(link);
			title = title.replace("\t"," - ");
			int dashIndex = title.indexOf("-");
			String artist = "";
			String songName = "";
			if (dashIndex>0){
				artist = title.substring(0,dashIndex-1).trim();
				songName = title.substring(dashIndex+1).trim();
				log("About to download: "+artist+" - "+songName);
			}else{
				songName = title;
			}
			song = new Song(songName,artist,"","","","");
			song.addSongUpdateListener(this);
			song.url = link;
			songPanel = new SongPanel(this,false);
			songPanel.label.setText(song.toString());
			log("DLing video");
			downloadVideo(link);
		}else{
			String string = input;
			string = string.trim();
			if (StringOps.isEmpty(string)){
				return;
			}
			string = string.replace("\t", " - ");
			String url = findFirstLinkFromSong(string);
			if (url==null){
				return;
			}
			int dashIndex = string.indexOf("-");
			String artist = "";
			String songName = "";
			if (dashIndex>0){
				artist = string.substring(0,dashIndex-1).trim();
				songName = string.substring(dashIndex+1).trim();
				log("About to download: "+artist+" - "+songName);
			}else{
				String title = getTitleFromYoutubeLink(url);
				title = title.replace("\t"," - ");
				dashIndex = title.indexOf("-");
				if (dashIndex>0){
					artist = title.substring(0,dashIndex-1).trim();
					songName = title.substring(dashIndex+1).trim();
					log("About to download: "+artist+" - "+songName);
				}else{
					songName = title;
				}
			}
			song = new Song(songName,artist,"","","","");
			song.url = url;
			song.addSongUpdateListener(this);
			songPanel = new SongPanel(this,false);
			songPanel.label.setText(song.toString());
			log("DLing video");
			downloadVideo(url);
		}
	}
	
	public SongController(){
		songPanel = new SongPanel(this,true);
	}
	
	public SongController(Song song){
		this.song = song;
		songPanel = new SongPanel(this,true);
	}
	
	public String getTitleFromYoutubeLink(String link){
		String results = WebPageReader.readWebPage(link);
		String title = StringOps.getStringBetween(results, "<title>", "</title>");
		return title.substring(0,title.indexOf(" - YouTube"));
	}
	
	public void log(String status){
		log+=status;
		if (isCancelled)
			return;
		if (songPanel!=null)
			songPanel.log(status);
	}
	
	/**
	 * Downloads the .FLV or .MP4 at the specified youtube link
	 * @param ytLink
	 */
	public void downloadVideo(String ytLink){
		final String[] args = new String[]{"youtube-dl.exe","-c", ytLink};
		final String vidString = ytLink.substring(ytLink.indexOf("watch?v=")+8);
		
		try {
			youtubeProc = Runtime.getRuntime().exec(args);
			downloadThread = new Thread(){
				public void run(){
					BufferedReader input = new BufferedReader(new InputStreamReader(youtubeProc.getInputStream()));
					String line="";
					try {
						while ((line=input.readLine())!=null){
							log(line);
							if ((line.contains ("[youtube:channel]")) || (line.contains("Downloading video #"))){
								log("Couldn't find a good video. Manually put in youtube link.");
								log("Args: "+args.toString());
								log("vidString: "+vidString);
								break;
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (!isCancelled){
						log("Converting");
						setOpenVideoButtonEnabled(true);
						convertVideoToMp3(vidString);
					}
				}
			};
			downloadThread.start();
		}
		catch (IOException e1) {
			e1.printStackTrace();
			SongDownloader.messageBox("You need youtube-dl.exe in the same folder as the program to run this.");
		}
	}
	
	/**
	 * Converts a .FLV or .MP4 file to an .MP3 file in the "Download Folder"
	 * @param vidString
	 */
	public void convertVideoToMp3(String vidString){
		File root = new File(".");
		String []files = root.list();
		String vidFileName = null;
		for(int i=0;i<files.length;i++){
			if (files[i].contains(vidString)){
				vidFileName = files[i]; 
				break;
			}
		}
		if (vidFileName==null){
			log("Couldn't find downloading video. Download and convert manually");
			log("vidString: "+vidString);
			return;
		}
		
		if (SongDownloader.downloadDir != null && !SongDownloader.downloadDir.equals("")){
			if (SongDownloader.downloadDir.charAt(SongDownloader.downloadDir.length()-1)!='\\'){
				SongDownloader.downloadDir = SongDownloader.downloadDir+= "\\";
			}
		}else{
			SongDownloader.downloadDir = ".\\";
		}
		final String songFileName = SongDownloader.downloadDir+vidFileName.replace(".mp4", ".mp3").replace(".flv",".mp3").replace("-"+vidString, "").replace(".part", "");
		String[] args = new String[]{"ffmpeg.exe","-y", "-i","\""+vidFileName+"\"","\""+songFileName+"\""};
		
		try {
			ffmpegProc = Runtime.getRuntime().exec(args);
			finalFilName = vidFileName;
			convertThread = new Thread(){
				public void run(){
					BufferedReader input = new BufferedReader(new InputStreamReader(ffmpegProc.getInputStream()));
					String line="";
					try {
						while ((line=input.readLine())!=null){
							log(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (!isCancelled){
						log("Done");
						song.setFile(songFileName);
						if (!keepVideo){
							new File(finalFilName).delete();
							setOpenVideoButtonEnabled(false);
						}
						if (downloadedListener!=null)
							downloadedListener.songDownloaded();
					}
				}
			};
			convertThread.start();
		}
		catch (IOException e1) {
			e1.printStackTrace();
			SongDownloader.messageBox("You need ffmpeg.exe in the same folder as the program to run this.");
		}
	}
	
	/**
	 * Sends a search query to YouTube with the specific song and returns the first video found.
	 * @param song
	 * @return
	 */
	public String findFirstLinkFromSong(String song){
		song = song.replaceAll(" ", "%20").replace("&", "%26");
		String results="";
		try{
			results = WebPageReader.readWebPage("http://www.youtube.com/results?search_query="+song);
		}catch(Exception e){
		}
		int index = results.indexOf("<ol id=\"search-results\"");
		if (index==-1){
			return null;
		}
		String trimmed = results.substring(index);
		
		String prefix = "<a href=\"";
		String suffix = "\"";
		int start = trimmed.indexOf(prefix)+prefix.length();
		int end = trimmed.indexOf(suffix, start);
		return "http://www.youtube.com"+trimmed.substring(start,end);
	}
	
	public SongPanel getSongPanel(){
		return songPanel;
	}
	
	public Song getSong(){
		return song;
	}
	
	public void openSong(){
		try {
			Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler "+song.getFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void youtube(){
		try {
			Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler "+song.url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void edit(){
		if (songPanel.dialog==null){
			if (song==null){
				setSong(new Song("New Song","New Artist", "New Album", "New Year", "New Comment", "New Genre"));
			}
			songPanel.dialog = new SongDialog(this.song);
		}
		songPanel.dialog.setVisible(true);
	}
	
	public void setSong(Song song){
		this.song = song;
		this.song.addSongUpdateListener(this);
		songPanel.label.setText(this.song.toString());
	}

	@Override
	public void songUpdated(Song s) {
		songPanel.label.setText(song.toString());
		if (songPanel.dialog==null)
			songPanel.dialog = new SongDialog(this.song);
		songPanel.dialog.updateFields(s);
	}
	
	public void cancel(){
		synchronized (this) {
			log("Cancelled");
			isCancelled = true;
			if (youtubeProc!=null)
				youtubeProc.destroy();
			if (convertThread!=null)
				convertThread.interrupt();
			if (downloadThread!=null)
				downloadThread.interrupt();
			if (ffmpegProc!=null)
				ffmpegProc.destroy();
			songPanel.cancelButton.setEnabled(false);
			songPanel.editButton.setEnabled(false);
			if (downloadedListener!=null)
				downloadedListener.songDownloaded();
		}
	}
	
	public void setSongDownloadedListener(ISongDownloadedListener listener){
		this.downloadedListener = listener;
	}
	
	public void setOpenVideoButtonEnabled(boolean b){
		songPanel.openVideoButton.setEnabled(b);
	}
	
	public void setKeepVideo(boolean b){
		keepVideo = b;
	}
	
	public void openVideo(){
		try {
			Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler "+finalFilName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
