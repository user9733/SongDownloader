package main.song;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SongPanel extends JPanel{

	private static final long serialVersionUID = 1L;
	
	JLabel label;
	JButton editButton;
	JButton openSongButton;
	JButton openVideoButton;
	JButton cancelButton;
	JButton youtubeButton;
	public SongDialog dialog;
	boolean mainSongPanel;
	String logText="";
	SongController cont;
	JPanel buttonPanel;
	JTextField logField;

	public SongPanel(boolean mainSongPanel){
		super(new GridLayout(0,1));
		this.mainSongPanel = mainSongPanel;
		buttonPanel = new JPanel(new GridLayout(1,0));
		if (mainSongPanel){
			editButton = new JButton("Edit All Song Details (Ctrl+E)");
		}else{
			label = new JLabel("New Song");
			logField = new JTextField("Waiting...");
			logField.setEditable(false);
			editButton = new JButton("Edit Song");
		}
		editButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				edit();
			}
		});

		if (!mainSongPanel){
			openSongButton = new JButton("Open Song");
			openSongButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					openSong();
				}
			});
			openSongButton.setEnabled(false);
			
			openVideoButton = new JButton("Open Video");
			openVideoButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					openVideo();
				}
			});
			openVideoButton.setEnabled(false);
			
			youtubeButton = new JButton("YouTube");
			youtubeButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					youtube();
				}
			});
			
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					cancel();
				}
			});
		}
		
		
		if (!mainSongPanel){
			this.add(label);
		}
		buttonPanel.add(editButton);
		this.add(buttonPanel);
		if (!mainSongPanel){
			buttonPanel.add(openSongButton);
			buttonPanel.add(openVideoButton);
			buttonPanel.add(youtubeButton);
			buttonPanel.add(cancelButton);
			this.add(logField);
		}
	}
	
	public SongPanel(SongController cont, boolean mainSongPanel){
		this(mainSongPanel);
		this.cont = cont;
	}
	
	public void openSong(){
		cont.openSong();
	}
	
	public void openVideo(){
		cont.openVideo();
	}
	
	public void youtube(){
		cont.youtube();
	}
	
	public void edit(){
		cont.edit();
	}
	
	public void setSong(Song song){
		cont.setSong(song);
	}
	
	public void log(String text){
		logText += text;
		logField.setText(text);
		if (text.equalsIgnoreCase("done")){
			openSongButton.setEnabled(true);
			cancelButton.setEnabled(false);
		}
	}
	
	public void cancel(){
		cancelButton.setEnabled(false);
		editButton.setEnabled(false);
		cont.cancel();
	}
}
