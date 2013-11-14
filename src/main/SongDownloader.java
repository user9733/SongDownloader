package main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import main.song.AllSongsPanel;
import main.song.ISongDownloadedListener;
import main.song.SongController;

import common.GosuFrame;
import common.StringOps;
import common.SwingOps;

public class SongDownloader implements ISongDownloadedListener{

	//VAR START
	static GosuFrame frame;
	private JButton goButton;
	private JButton cancelButton;
	private JButton openFolderButton;
	HashMap<String, String> configs;
	JTextArea songArea;
	public static String downloadDir="";
	private JTextField fileField;
	private JTextArea logArea;
	String []songLines = null;
	private AllSongsPanel allSongsPanel;
	private JFileChooser fileChooser;
	private JPanel mainPanel;
	private JScrollPane logAreaScroll;
	int numSongs=-1;
	int numSongsDone=-1;
	int numSongsThreshold=5;
	int numSongsDownloading=-1;
	int curSong=-1;
	Thread goThread;
	boolean going=false;
	public static SongDownloader thiz = null;
	//VAR END
	
	public static void main(String []args){
		thiz = new SongDownloader();
	}
	
	public static JFrame getFrame(){
		return frame;
	}
	
	/**
	 * Sets up all GUI
	 */
	private SongDownloader(){
		//Change look and feel (from Swing to OS native)
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		//Log Panel
		JPanel logPanel = new JPanel(new BorderLayout());
		logArea = new JTextArea("");
		logArea.setEditable(false);
		logAreaScroll = new JScrollPane(logArea);
		logAreaScroll.setPreferredSize(new Dimension(logArea.getWidth(),200));
		logPanel.add(logAreaScroll,BorderLayout.CENTER);
		
		// FRAME STUFF
		frame = new GosuFrame("Song Downloader",1100,800);
		mainPanel = new JPanel(new BorderLayout());
		frame.add(mainPanel);
		
		//SET CONFIG STUFF
		readConfigFile();
		String dir = configs.get("download_dir");
		if (dir!=null)
			downloadDir = dir;
		
		//FILE DIALOG
		JLabel fileLabel = new JLabel("Download Folder: ");
		fileField = new JTextField(downloadDir);
		fileField.setPreferredSize(new Dimension(400,20));
		fileChooser = new JFileChooser(".");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		JButton fileButton = new JButton("File (Ctrl+F)");
		fileButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				file();
			}
		});
		JPanel filePanel = new JPanel(new FlowLayout());
		openFolderButton = new JButton("Open (Ctrl+D)");
		openFolderButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				open();
			}
		});
		filePanel.add(fileLabel);
		filePanel.add(fileField);
		filePanel.add(fileButton);
		filePanel.add(openFolderButton);
		
		//Song panel
		JPanel songPanel = new JPanel(new BorderLayout());
		JPanel songAreaPanel = new JPanel(new GridLayout(1,2));
		songArea = new JTextArea("http://www.youtube.com/watch?v=boanuwUMNNQ\nMetallica - Bleeding Me\nwill sasso bathtub");
		JScrollPane songAreaScroll = new JScrollPane(songArea);
		allSongsPanel = new AllSongsPanel();
		JPanel songOpPanel = new JPanel(new GridLayout(1,2));
		goButton = new JButton("Go (Ctrl+Enter)");
		goButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				go();
			}
		});
		cancelButton = new JButton("Cancel (Esc)");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		songPanel.add(songAreaPanel,BorderLayout.CENTER);
		songAreaPanel.add(songAreaScroll);
		songAreaPanel.add(allSongsPanel);
		songPanel.add(songOpPanel,BorderLayout.SOUTH);
		songOpPanel.add(goButton);
		songOpPanel.add(cancelButton);
		
		//Main Panel
		mainPanel.add(filePanel,BorderLayout.NORTH);
		mainPanel.add(songPanel,BorderLayout.CENTER);
		mainPanel.add(logPanel,BorderLayout.SOUTH);
		
		//Enable correct buttons
		setGoEnabled(true);
		
		//Add some nifty hotkeys
		addHotkeys();
		
		//Show frame
		frame.vis();
	}
	
	/**
	 * Adds some nifty hotkeys
	 */
	public void addHotkeys(){
		SwingOps.addHotkey(mainPanel, "control ENTER", this,"go");
		SwingOps.addHotkey(mainPanel, "ESCAPE", this,"cancel");
		SwingOps.addHotkey(mainPanel, "shift ENTER", this,"skip");
		SwingOps.addHotkey(mainPanel, "control F", this,"file");
		SwingOps.addHotkey(mainPanel, "control D", this,"open");
	}
	
	/**
	 * Launches an explorer window at the specified "Download FoldeR"
	 */
	public void open(){
		if ((downloadDir==null)||(downloadDir.equals("")))
			return;
		try {
			Runtime.getRuntime().exec(new String[]{"explorer.exe",downloadDir});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Launches the "Download Folder" file chooser dialog.
	 */
	public void file(){
		fileChooser.showOpenDialog(frame);
		if (fileChooser.getSelectedFile()==null){
			return;
		}
		String dir = fileChooser.getSelectedFile().getAbsolutePath();
		fileField.setText(dir);
		configs.put("download_dir", dir);
		downloadDir = dir;
		writeConfigs();
	}
	
	/**
	 * Reads in the config.txt file into the config map
	 */
	public void readConfigFile(){
		configs = new HashMap<String,String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File("config.txt")));
			String line = "";
			while ((line=reader.readLine())!=null){
				String []split = line.split("::");
				if (split.length>1){
					configs.put(split[0], split[1]);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			log("No config file. Will create one.");
		} catch (IOException e) {
			log("Couldn't read config file");
			e.printStackTrace();
		}
	}
	
	/**
	 * Start processing all the songs in the song text area.
	 */
	public void go(){
		if (!goButton.isEnabled())
			return;
		downloadDir = fileField.getText();
		if (StringOps.isEmpty(downloadDir)){
			downloadDir = ".";
			log("Songs will be downloaded to "+new File(".").getAbsolutePath());
		}
		File dir = new File(downloadDir);
		if (!dir.exists()){
			messageBox("Directory \""+downloadDir+"\" does not exist.");
			return;
		}
		if (!dir.isDirectory()){
			messageBox("\""+downloadDir+"\" is not a directory.");
			return;
		}
		
		setGoEnabled(false);
		allSongsPanel.clear();
		songLines= songArea.getText().split("\n");
		if (songLines!=null){
			numSongs = 0;
			numSongsDone = 0;
			numSongsDownloading=0;
			curSong = 0;
			going = true;
			continueGoing();
		}
	}
	
	public void continueGoing(){
		if (!going)
			return;
		if (goThread!=null){
			goThread.interrupt();
		}
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while(curSong<songLines.length){
					if (!canContinue()){
						break;
					}
					String songLine = songLines[curSong];
					if (!StringOps.isEmpty(songLine)){
						SongController cont = new SongController(songLine);
						cont.setSongDownloadedListener(SongDownloader.this);
						allSongsPanel.addSongController(cont);
						numSongs++;
						numSongsDownloading++;
						curSong++;
					}
				}
			}
		};
		goThread = new Thread(runnable);
		goThread.start();
	}
	
	/**
	 * Cancels all processing and re-enables buttons
	 */
	public void cancel(){
		allSongsPanel.cancelAll();
		done();
	}
	
	public void done(){
		numSongs = -1;
		numSongsDone = -1;
		numSongsDownloading = -1;
		going=false;
		setGoEnabled(true);
	}
	
	/**
	 * Enables or disables go/cancel/skip buttons
	 * @param b If true, will enabled go and disabled cancel/skip
	 */
	public void setGoEnabled(boolean b){
		songArea.setEditable(b);
		cancelButton.setEnabled(!b);
		goButton.setEnabled(b);
	}

	/**
	 * Writes all things in the configs map to the "config.txt" text file
	 */
	public void writeConfigs(){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("config.txt",true));
			String line = "";
			for(String key:configs.keySet()){
				line = key+"::"+configs.get(key);
				writer.write(line+"\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a string to the log area.
	 * @param text
	 */
	public void log(String text){
		final JScrollBar vertBar = logAreaScroll.getVerticalScrollBar();
		boolean shouldScroll = (vertBar.getMaximum() - vertBar.getVisibleAmount() == vertBar.getValue());
		logArea.setText(logArea.getText()+text+"\n");
		if (shouldScroll){
			EventQueue.invokeLater(new Runnable(){
				@Override
				public void run() {
					vertBar.setValue(vertBar.getMaximum());
				}
			});
		}
	}
	
	/**
	 * Creates and popups a message box.
	 * @param text
	 */
	public static void messageBox(String text){
		JOptionPane.showMessageDialog(frame, text);
	}

	@Override
	public void songDownloaded() {
		numSongsDone++;
		numSongsDownloading--;
		if (numSongsDone>=numSongs){
			done();
		}else if (canContinue()){
			continueGoing();
		}
	}

	@Override
	public void songCancelled() {
		numSongsDone++;
		numSongsDownloading--;
		if (numSongsDone>=numSongs){
			done();
		}else if (canContinue()){
			continueGoing();
		}
	}
	
	public void updateNumThreads(int numThreads){
		numSongsThreshold = numThreads;
		if (canContinue()){
			continueGoing();
		}
	}
	
	public boolean canContinue(){
		return (((numSongsDownloading<numSongsThreshold)||(numSongsThreshold==0))&&(going));
	}
	
}
