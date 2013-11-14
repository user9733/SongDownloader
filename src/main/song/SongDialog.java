package main.song;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.SongDownloader;

import common.SwingOps;

public class SongDialog extends JDialog{

	private static final long serialVersionUID = 1L;

	JTextField titleField;
	JTextField artistField;
	JTextField albumField;
	JTextField yearField;
	JTextField commentField;
	JTextField genreField;
	Song song;

	private JButton cancelButton;

	private JButton saveButton;

	private JPanel panel;
	
	public SongDialog(Song song){
		this.song = song;
		panel = new JPanel(new GridLayout(7,2));
		
		panel.add(new JLabel("Title (Ctrl+1): "));
		titleField = new JTextField(song.title);
		panel.add(titleField);
		
		panel.add(new JLabel("Artist (Ctrl+2): "));
		artistField = new JTextField(song.artist);
		panel.add(artistField);
		
		panel.add(new JLabel("Album (Ctrl+3): "));
		albumField = new JTextField(song.album);
		panel.add(albumField);
		
		panel.add(new JLabel("Year (Ctrl+4): "));
		yearField = new JTextField(song.year);
		panel.add(yearField);
		
		panel.add(new JLabel("Comment (Ctrl+5): "));
		commentField = new JTextField(song.comment);
		panel.add(commentField);
		
		panel.add(new JLabel("Genre (Ctrl+6): "));
		genreField = new JTextField(song.genre);
		panel.add(genreField);
		
		saveButton = new JButton("Save (Ctrl+S)");
		saveButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		panel.add(saveButton);
		
		cancelButton = new JButton("Cancel (Esc)");
		cancelButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		panel.add(cancelButton);
		
		SwingOps.addHotkey(panel, "control S",this,"save");
		SwingOps.addHotkey(panel,"ESCAPE",this,"cancel");
		SwingOps.addHotkey(panel, "control 1", this, "selectField",titleField);
		SwingOps.addHotkey(panel, "control 2", this, "selectField",artistField);
		SwingOps.addHotkey(panel, "control 3", this, "selectField",albumField);
		SwingOps.addHotkey(panel, "control 4", this, "selectField",yearField);
		SwingOps.addHotkey(panel, "control 5", this, "selectField",commentField);
		SwingOps.addHotkey(panel, "control 6", this, "selectField",genreField);
		
		this.add(panel);
		this.setSize(new Dimension(500,500));
		this.setLocationRelativeTo(SongDownloader.getFrame());
	}
	
	public void save(){
		song.updateSong(titleField.getText(), artistField.getText(), albumField.getText(), yearField.getText(), commentField.getText(),genreField.getText());
		close();
	}
	
	public void cancel(){
		close();
	}
	
	public void close(){
		this.setVisible(false);
	}
	
	public void updateFields(Song song){
		titleField.setText(song.title);
		artistField.setText(song.artist);
		albumField.setText(song.album);
		yearField.setText(song.year);
		commentField.setText(song.comment);
		genreField.setText(song.genre);
	}
	
	public void selectField(JTextField field){
		field.requestFocus();
		field.requestFocusInWindow();
		field.selectAll();
	}
	
}

