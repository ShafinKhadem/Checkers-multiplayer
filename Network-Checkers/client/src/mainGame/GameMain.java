package mainGame;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * @author Nafiur Rahman Khadem
 */

public class GameMain extends Application {
	//<editor-fold defaultstate="collapsed" desc="public static objects">
	public static Stage game_window, dialog;
	public static Scene game_scene, scene;
	public static Text turn_text;
	public static GridPane checkerboard;
	public static final byte GRID_BASEX = 5, GRID_BASEY = 65, GRID_DIMENSION = 60;
	public static final byte NONE = 0, RED = 1, BLUE = 2, RED_KING = 3, BLUE_KING = 4, MOVE = 5, JUMP = 6;
	public static StackPane[][] grid = new StackPane[10][10];
	public static byte[][] state = new byte[10][10];
	public static byte[][] valid_to = new byte[10][10];//move and jump ke alada korar jonyo
	public static byte selectedRow, selectedCol;
	public static Image[] checker_images = new Image[10];
	public static final Image red_piece = new Image ("images/red_piece.png", GRID_DIMENSION, GRID_DIMENSION, true, true, true);
	public static final Image blue_piece = new Image ("images/blue_piece.png", GRID_DIMENSION, GRID_DIMENSION, true, true, true);
	public static final Image red_king = new Image ("images/red_king.png", GRID_DIMENSION, GRID_DIMENSION, true, true, true);
	public static final Image blue_king = new Image ("images/blue_king.png", GRID_DIMENSION, GRID_DIMENSION, true, true, true);
	public static ObjectInputStream in_server;
	public static ObjectOutputStream out_server;
	public static int itsIndex;
	public static boolean singlePlayer = false;
	//</editor-fold>

	
	//<editor-fold defaultstate="collapsed" desc="Surely done I think">
	private static byte next = RED, this_player = RED;
	private static byte redPieces = 12, bluePieces = 12;
	private static byte dircol[]={1, -1, 1, -1}, dirrow[] = {-1, -1, 1, 1};
	private static boolean selected = false;
	
	public GameMain (byte player) {
		this_player = player;
	}
	
	private static void set_scene (Stage window, String sceneFile) throws IOException {
		Parent parent = FXMLLoader.load (GameMain.class.getResource (sceneFile));
		scene = new Scene (parent);
		window.setScene (scene);
	}
	
	static void showHelp () {
		dialog = new Stage ();
		dialog.initModality (Modality.NONE);
		dialog.initOwner (game_window);
		try {
			set_scene (dialog, "helpscene.fxml");
		} catch (IOException e) {
			e.printStackTrace ();
		}
		dialog.show ();
	}
	
	private static void changeNext(){
		if (next == RED) {
			next = BLUE;
			turn_text.setText ("Blue's turn");
		}
		else if (next==BLUE) {
			next = RED;
			turn_text.setText ("Red's turn");
		}
	}
	
	private static void finish () {
		dialog = new Stage();
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.initOwner(game_window);
		try {
			set_scene (dialog, "finishedscene.fxml");
			Text result = (Text) scene.lookup ("#result");
			if (bluePieces <= 2 && bluePieces==redPieces) {
				result.setText ("Tie");
			}
			else {
				if (next == RED) {
					result.setText ("Blue won");
				}
				else {
					result.setText ("Red won");
				}
			}
		} catch (IOException e) {
			e.printStackTrace ();
		}
		dialog.show();
	}
	
	
	static void surrender () {
		if (!singlePlayer) {
			try {
				out_server.writeObject ("surrender this"+" "+itsIndex);
			} catch (IOException e) {
				e.printStackTrace ();
			}
			next = this_player;
			finish ();
		}
		else finish ();
	}
	
	private static void add_piece (byte row, byte col, byte state) {
		grid[row][col].getChildren ().add (new ImageView (checker_images[state]));
	}
	
	static void reset () {
		redPieces = 12;
		bluePieces = 12;
		if (selected) {
			grid[selectedRow][selectedCol].getChildren ().remove (2);
		}
		for (byte col = 0; col<8; col++) {
			for (byte row = 0; row<8; row++) {
				if (state[row][col] != NONE) {
					grid[row][col].getChildren ().remove (1);
					state[row][col] = NONE;
				}
				if (((row+col)&1) != 0) {
					if (row >= 5) {
						state[row][col] = RED;
					}
					else if (row<=2) {
						state[row][col] = BLUE;
					}
					if (state[row][col] != NONE) {
						add_piece (row, col, state[row][col]);
					}
				}
			}
		}
	}
	
	private void select_cell (byte row, byte col) {
		Rectangle rectangle = new Rectangle (55, 55, Color.TRANSPARENT);
		rectangle.setStroke (Color.GREEN);
		rectangle.setStrokeWidth (5);
		grid[row][col].getChildren ().add (rectangle);
		selectedRow = row;
		selectedCol = col;
		selected = true;
	}
	
	private boolean valid_index (byte row, byte col) {
		return row >= 0 && row<8 && col >= 0 && col<8;
	}
	
	private void reset_validTo () {
		for (int i = 0; i<8; i++) {
			for (int j = 0; j<8; j++) {
				valid_to[i][j] = NONE;
			}
		}
	}
	//</editor-fold>
	
	private void set_valids (byte row, byte col, boolean jumpOnly) {
		byte strt = 0, nd = 2, stt = state[row][col];//strt and nd by default (for red)
		if (stt == RED_KING || stt == BLUE_KING) {
			nd = 4;
		}
		else if (stt == BLUE) {
			strt = 2;
			nd = 4;
		}
		boolean flg_jmp = false;
		for (byte i = strt; i<nd; i++) {
			byte trgtrow = (byte) (row+dirrow[i]), trgtcol = (byte) (col+dircol[i]), trgtstt;
			if (!valid_index (trgtrow, trgtcol)) {
				continue;
			}
			trgtstt = state[trgtrow][trgtcol];
			if (!jumpOnly && trgtstt == NONE) {
				valid_to[trgtrow][trgtcol] = MOVE;
			}
			else {
				byte trgtrow2 = (byte) (trgtrow+dirrow[i]), trgtcol2 = (byte) (trgtcol+dircol[i]);
				boolean vld_jmp = (valid_index (trgtrow2, trgtcol2) && state[trgtrow2][trgtcol2] == NONE);
				if (vld_jmp) {
					if ((stt == BLUE || stt == BLUE_KING) && (trgtstt == RED || trgtstt==RED_KING)) {
						valid_to[trgtrow2][trgtcol2] = JUMP;
						flg_jmp = true;
					} else if ((stt == RED || stt == RED_KING) && (trgtstt == BLUE || trgtstt==BLUE_KING)) {
						valid_to[trgtrow2][trgtcol2] = JUMP;
						flg_jmp = true;
					}
				}
			}
		}
		if (jumpOnly && !flg_jmp) {
			changeNext ();
		}
	}
	
	private void move (byte toRow, byte toCol) {
/*to implement stalemate have to check at every changeNext whether this has any valid move or jump*/
		grid[selectedRow][selectedCol].getChildren ().remove (1, 3);
		if (state[selectedRow][selectedCol] == RED && toRow == 0) {
			state[selectedRow][selectedCol] = RED_KING;
		} else if (state[selectedRow][selectedCol] == BLUE && toRow == 7) {
			state[selectedRow][selectedCol] = BLUE_KING;
		}
		add_piece (toRow, toCol, state[selectedRow][selectedCol]);
		state[toRow][toCol] = state[selectedRow][selectedCol];
		state[selectedRow][selectedCol] = NONE;
		selected = false;
		if (Math.abs (toRow-selectedRow)>1) {
			byte removeRow = (byte) ((selectedRow+toRow)/2), removeCol = (byte) ((selectedCol+toCol)/2);
			grid[removeRow][removeCol].getChildren ().remove (1);
			state[removeRow][removeCol] = NONE;
			if (next == RED) {
				bluePieces--;
				if (bluePieces == 0) {
					changeNext ();
					finish ();
				}
			} else if (next == BLUE) {
				redPieces--;
				if (redPieces == 0) {
					changeNext ();
					finish ();
				}
			}
			reset_validTo ();
			set_valids (toRow, toCol, true);
		}
		else {
			changeNext ();
		}
	}
	
	private void click(byte _row, byte _col){
		if (selected) {
			if (valid_to[_row][_col] != NONE) {
				move (_row, _col);
			}
			else if (state[_row][_col] == state[selectedRow][selectedCol] || state[_row][_col] == state[selectedRow][selectedCol]+2) {
				grid[selectedRow][selectedCol].getChildren ().remove (2);
				select_cell (_row, _col);
				reset_validTo ();
				set_valids (_row, _col, false);
			}
		}
		else {
			if (state[_row][_col] == next || state[_row][_col]==next+2) {
				select_cell (_row, _col);
				reset_validTo ();
				set_valids (_row, _col, false);
			}
		}
	}
	
	@Override
	public void start (Stage primaryStage) throws Exception {
		checker_images[RED] = red_piece;
		checker_images[BLUE] = blue_piece;
		checker_images[RED_KING] = red_king;
		checker_images[BLUE_KING] = blue_king;
		next = RED;
		game_window = primaryStage;
		Parent root = FXMLLoader.load (getClass ().getResource ("mainscene.fxml"));
		game_scene = new Scene (root);
		turn_text = (Text) game_scene.lookup ("#turn");
		if (next==RED) {
			turn_text.setText ("Red's turn");
		}
		else if (next == BLUE) {
			turn_text.setText ("Blue's turn");
		}
		checkerboard = (GridPane) game_scene.lookup ("#checkerBoard");
		for (byte col = 0; col<8; col++) {
			for (byte row = 0; row<8; row++) {
				final byte _col = col, _row = row;
				grid[row][col] = new StackPane (new Rectangle (60, 60, ((row+col)&1) != 0 ? Color.BLACK : Color.WHITE));
				grid[row][col].setOnMouseClicked (event -> {
					if (next == this_player || next == this_player+2 || singlePlayer) {
						System.out.println (_row+" "+_col+" "+valid_to[_row][_col]+" "+next+" "+state[_row][_col]);
						if (!singlePlayer) {
							try {
								out_server.writeObject (Byte.toString (_row)+" "+Byte.toString (_col)+" "+Integer.toString (itsIndex));
							} catch (IOException e) {
								System.out.println ("sending to server error");
							}
						}
						click (_row, _col);
					}
				});
				checkerboard.add (grid[row][col], col, row);
				state[row][col] = NONE;
			}
		}
		reset ();
		game_window.setTitle ("Multiplayer Checkers");
		game_window.setScene (game_scene);
		game_window.show ();
		Thread processingThread = new Thread(() -> {
			try {
				Socket socket = new Socket ("127.0.0.1", 33333);
				in_server = new ObjectInputStream (socket.getInputStream());
				out_server = new ObjectOutputStream (socket.getOutputStream());
				out_server.writeObject ("new client");
				String s;
				StringTokenizer st;
				while (true) {
					s = (String) in_server.readObject ();
					st = new StringTokenizer (s);
					if (s.startsWith ("index")) {
						st.nextToken ();
						itsIndex = Integer.parseInt (st.nextToken ());
						if ((itsIndex&1)!=0) {
							this_player = BLUE;
						}
						System.out.println (itsIndex+" "+(itsIndex^1));
					}
					else if (s.startsWith ("surrender")) {
						if (next == this_player) {
							changeNext ();
						}
						Platform.runLater(() -> {
							finish ();
						});
					}
					else {
						final byte _a = Byte.parseByte (st.nextToken ()), _b = Byte.parseByte (st.nextToken ());
						System.out.println (_a+" "+_b);
						Platform.runLater(() -> {
							click (_a, _b);
						});
					}
					Thread.sleep (300);
				}
			} catch (InterruptedException e) {
				System.out.println ("Game's data processingThread is interrepted");
			} catch (Exception e) {
				System.out.println("Game's data processing error" + e.toString());
				singlePlayer = true;
			}
			try {
				if (!singlePlayer) {
					in_server.close ();
					out_server.close ();
				}
			} catch (IOException e) {
				System.out.println ("socket's input or output stream couldn't be closed");
			}
		});
		processingThread.setDaemon (true);
		processingThread.start ();
		if (singlePlayer) {
			processingThread.interrupt ();
		}
//		primaryStage.setOnCloseRequest(e -> System.exit(1));
	}
}
