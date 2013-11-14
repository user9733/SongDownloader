package main.song;

import java.util.ArrayList;

import com.beaglebuddy.mp3.MP3;
import common.StringOps;

public class Song {

	public String title;
	public String artist;
	public String album;
	public String year;
	public String comment;
	public String genre;
	private String songFileName;
	public String origTitle;
	public String origArtist;
	public String origAlbum;
	public String origYear;
	public String origComment;
	public String origGenre;
	public String url;
	
	ArrayList<ISongUpdateListener> listeners;
	
	public Song(){
		this("","","","","","");
	}
	
	public Song(String title, String artist, String album, String year, String comment, String genre){
		listeners = new ArrayList<ISongUpdateListener>();
		updateSong(title,artist,album,year,comment,genre);
	}
	
	@Override
	public String toString(){
		if (StringOps.isEmpty(artist))
			return title;
		return artist + " - " + title;
	}
	
	public void updateSong(){
		for(ISongUpdateListener l:listeners){
			l.songUpdated(this);
		}
		
		if (notEmpty(songFileName))
			updateFile();
	}
	
	public void updateFile(){
		MP3 mp3=null;
		try {
			mp3 = new MP3(songFileName);
			if (notEmpty(title))
				mp3.setTitle(title);
			if (notEmpty(artist))
				mp3.setBand(artist);
			if (notEmpty(album))
				mp3.setAlbum(album);
			if (notEmpty(year))
				mp3.setYear(Integer.parseInt(year));
			if (notEmpty(comment))
				mp3.setComments(comment);
			mp3.save();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public boolean notEmpty(String string){
		return (string!=null)&&(!string.equals(""));
	}
	
	public String getFile(){
		return songFileName;
	}
	
	public void setFile(String fileName){
		try {
			this.songFileName = fileName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		updateFile();
	}
	
	public void updateSong(String title, String artist, String album, String year, String comment,String genre){
		this.title = title;
		origTitle = title;
		this.artist = artist;
		origArtist = artist;
		this.album = album;
		origAlbum = album;
		this.year = year;
		origYear = year;
		this.comment = comment;
		origComment = comment;
		this.genre = genre;
		origGenre = genre;
		updateSong();
	}
	
	public void addSongUpdateListener(ISongUpdateListener l){
		listeners.add(l);
	}
	
	public void removeSongUpdateListener(ISongUpdateListener l){
		listeners.remove(l);
	}
}
