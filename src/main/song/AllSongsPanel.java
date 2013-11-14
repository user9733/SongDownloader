package main.song;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import main.SongDownloader;

import common.StringOps;
import common.SwingOps;

public class AllSongsPanel extends JPanel{

	private static final long serialVersionUID = 1L;
	ArrayList<SongController> conts;
	JPanel mainPanel;
	JCheckBox keepArtistCheckBox;
	JCheckBox keepVideoCheckBox;
	JTextField threadField;
	JPanel opPanel;
	SongController cont;
	String threadFieldText = "5";
	
	public AllSongsPanel(){
		super(new BorderLayout());
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane(mainPanel);
		
		conts = new ArrayList<SongController>();
		opPanel = new JPanel(new FlowLayout());
		Song mainSong = new Song(){
			@Override
			public String toString(){
				return "Edit All Songs";
			}
		};
		cont = new SongController(mainSong){
			@Override
			public void songUpdated(Song s) {
				updateAllSongs();
			}
		};
		mainSong.addSongUpdateListener(cont);
		opPanel.add(cont.getSongPanel());
		keepArtistCheckBox = new JCheckBox("Keep Artist in Title");
		keepArtistCheckBox.setSelected(true);
		keepArtistCheckBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateAllSongs();
			}
		});
		
		keepVideoCheckBox = new JCheckBox("Keep Video");
		keepVideoCheckBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				keepVideos();
			}
		});
		JLabel threadLabel = new JLabel("Thread (0=infinite)");
		threadField = new JTextField(threadFieldText);
		threadField.setPreferredSize(new Dimension(30, 20));
		threadField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateNumThreads();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateNumThreads();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateNumThreads();
			}
		});
		opPanel.add(keepArtistCheckBox);
		opPanel.add(keepVideoCheckBox);
		opPanel.add(threadLabel);
		opPanel.add(threadField);
		SwingOps.addHotkey(cont.getSongPanel(), "control E", cont.getSongPanel(),"edit");
		
		this.add(opPanel,BorderLayout.NORTH);
		this.add(scrollPane,BorderLayout.CENTER);
	}
	
	public void updateAllSongs(){
		for(SongController c:conts){
			updateSong(c.getSong());
		}
	} 
	
	public void updateSong(Song song){
		String songPrefix = "";
		Song mainSong = cont.getSong();
		if (!StringOps.isEmpty(mainSong.artist)){
			if (keepArtistCheckBox.isSelected()){
				if (!StringOps.isEmpty(song.origArtist))
					songPrefix = song.origArtist+" - ";
			}
			song.artist = mainSong.artist;
		}else{
			song.artist = song.origArtist;
		}
		if (!StringOps.isEmpty(mainSong.title)){
			song.title = mainSong.title;
		}else{
			song.title = songPrefix+song.origTitle;
		}
		if (!StringOps.isEmpty(mainSong.album)){
			song.album = mainSong.album;
		}else{
			song.album = song.origAlbum;
		}
		if (!StringOps.isEmpty(mainSong.year)){
			song.year = mainSong.year;
		}else{
			song.year = song.origYear;
		}
		if (!StringOps.isEmpty(mainSong.comment)){
			song.comment = mainSong.comment;
		}else{
			song.comment = song.origComment;
		}
		if (!StringOps.isEmpty(mainSong.genre)){
			song.genre = mainSong.genre;
		}else{
			song.genre = song.origGenre;
		}
		song.updateSong();
	}
	
	public void addSongController(SongController cont){
		if ((cont==null)||(cont.getSongPanel()==null))
			return;
		conts.add(cont);
		mainPanel.add(cont.getSongPanel());
		repaint();
		validate();
	}
	
	public void clear(){
		mainPanel.removeAll();
		for(int i=0;i<conts.size();i++){
			conts.remove(i);
		}
	}
	
	public void cancelAll(){
		for(SongController c:conts){
			c.cancel();
		}
	}
	
	public void keepVideos(){
		for(SongController c:conts){
			c.setKeepVideo(keepVideoCheckBox.isSelected());
		}
	}
	
	public int getNumThread(){
		return Integer.parseInt(threadField.getText());
	}
	
	public void updateNumThreads(){
		int numThreads=-1;
		try{
			numThreads=Integer.parseInt(threadField.getText());
		}catch(Exception e){
			return;
		}
		if (numThreads>=0){
			threadFieldText = threadField.getText();
			SongDownloader.thiz.updateNumThreads(numThreads);
		}else{
			
		}
	}
}
