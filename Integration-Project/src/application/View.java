package application;
	
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;


public class View extends Application {
	
	public static final int MAXINPUTLENGTH = 200;
	
	//======================================================================================================================
	//||                                             Displayable controls                                                 ||  
	//======================================================================================================================

	/**
	 * Area that displays all the text corresponding to the selected recipient.
	 */
	TextArea chatText = new TextArea();
	
	/**
	 * field in which you can write your message.
	 */
	TextField inputField = new TextField();
	
	/**
	 * field used for entering in your name.
	 */
	TextField nameField = new TextField();
	
	/**
	 * button used for sending a message typed in the inputField
	 */
	Button sendButton = new Button();
	
	/**
	 * Button which opens an explorer window and sends the file you have selected.
	 */
	Button sendFileButton = new Button();
	
	/**
	 * Button which will enable if a displayable file has been received.
	 * It will then open a new window with the image if clicked on.
	 */
	Button showFileButton = new Button();
	
	/**
	 * Button which will set the text written in the nameField as your name and send it to the controller.
	 */
	Button nameButton = new Button();
	
	/**
	 * Combobox in which you can select to who you want to send your messages
	 * It will also display the amount of new messages if you open the combobox
	 * This is only updated at opening the menu.
	 */
	ComboBox<String> recipient = new ComboBox<String>();
	
	/**
	 * The parent of all controls.
	 */
	GridPane root = new GridPane();
	
	/**
	 * Variable used for accessing the view in a event handler where this did not work.
	 */
	View view = this;
	
	//======================================================================================================================
	//||                                                  Variables                                                       ||  
	//======================================================================================================================
	
	/**
	 * Controller which controls all of the logic of the gui.
	 */
	Controller controller;
	
	//======================================================================================================================
	//||                                             Initializing methods                                                 ||  
	//======================================================================================================================
	
	String path = "Notification.mp3";
	Media media = new Media(new File(path).toURI().toString());
	MediaPlayer mediaPlayer = new MediaPlayer(media);
	
	String path2 = "back_to_the_future.mp3";
	Media media2 = new Media(new File(path2).toURI().toString());
	MediaPlayer startMedia = new MediaPlayer(media2);
		
	public static void main(String[] args) {
		launch(args);
	}
	
	//======================================================================================================================
	//||                                             Initializing methods                                                 ||  
	//======================================================================================================================
	
	@Override
	public void start(Stage primaryStage) {
		startMedia.play();
		primaryStage.getIcons().add(new Image("file:icon.png"));
		
		ColumnConstraints coll1 = new ColumnConstraints();
		coll1.setPercentWidth(50);
		ColumnConstraints coll2 = new ColumnConstraints();
		coll2.setPercentWidth(40);
		ColumnConstraints coll3 = new ColumnConstraints();
		coll3.setPercentWidth(40);
		root.getColumnConstraints().addAll(coll1, coll2, coll3);
			
			
		RowConstraints row6 = new RowConstraints();
		row6.setPercentHeight(12);
		RowConstraints row7 = new RowConstraints();
		row7.setPercentHeight(1);
		RowConstraints row1 = new RowConstraints();
		row1.setPercentHeight(80);
		RowConstraints row4 = new RowConstraints();
		row4.setPercentHeight(1);
		RowConstraints row2 = new RowConstraints();
		row2.setPercentHeight(6);
		RowConstraints row5 = new RowConstraints();
		row5.setPercentHeight(1);
		RowConstraints row3 = new RowConstraints();
		row3.setPercentHeight(12);
		root.getRowConstraints().addAll(row6, row7, row1, row4, row2, row5, row3);
		
		root.setId("root");
		
		//Finalize Combobox
		recipient.getItems().add("All");
		recipient.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		recipient.setValue("All");
		recipient.setDisable(true);
		recipient.setOnAction(new EventHandler<ActionEvent>() {
			    @Override 
			    public void handle(ActionEvent e) {
			    	updateText(getRecipientValue());
			    }
			});
			
		recipient.setOnShowing(new EventHandler<Event>() {
			    @Override 
			    public void handle(Event e) {
			    	updateRecipient();
			    }
			});
			
			//Finalize chatText
		chatText.setEditable(false);
		chatText.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		chatText.textProperty().addListener(new ChangeListener<Object>() {
		    @Override
			    public void changed(ObservableValue<?> observable, Object oldValue,
			            Object newValue) {
			    	chatText.setScrollTop(Double.MIN_VALUE);
			    }
			});
			
			//Finalize nameField
		nameField.setId("textfield");
		nameField.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		nameField.setDisable(true);
		nameField.setOnKeyPressed(new EventHandler<KeyEvent>()
		    {
		        @Override
		        public void handle(KeyEvent ke) {
		            if (ke.getCode().equals(KeyCode.ENTER)) {
		            	setName();
		            }
		        }
		    });
			
			//Finalize nameButton
		nameButton.setId("sendButton");
		nameButton.setText("Set Name");
		nameButton.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		nameButton.setDisable(true);
		nameButton.setOnAction(new EventHandler<ActionEvent>() {
			    @Override 
			    public void handle(ActionEvent e) {
			    	setName();
			    }
			});
			
			//Finalize inputField
		nameField.setId("textfield");
		inputField.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		inputField.setDisable(true);
		inputField.setOnKeyPressed(new EventHandler<KeyEvent>()
		    {
		        @Override
		        public void handle(KeyEvent ke) {
		            if (ke.getCode().equals(KeyCode.ENTER)) {
		                send();
		            }
		        }
		    });
			
			//Finalize sendButton
		sendButton.setId("sendButton");
		sendButton.setText("Send");
		sendButton.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		sendButton.setDisable(true);
		sendButton.setOnAction(new EventHandler<ActionEvent>() {
			    @Override 
			    public void handle(ActionEvent e) {
		    		send();
			    }
			});
			
		sendFileButton.setId("sendButton");
		sendFileButton.setText("Send File");
		sendFileButton.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		sendFileButton.setDisable(true);
		sendFileButton.setOnAction(new EventHandler<ActionEvent>() {
			    @Override 
			    public void handle(ActionEvent e) {
		    		File file = (new FileChooser()).showOpenDialog(primaryStage);
			    	if (file != null) {
			    		sendFile(file);
			    	}
			    }
			});
			
		showFileButton.setId("sendButton");
		showFileButton.setText("Show File");
		showFileButton.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		showFileButton.setDisable(true);
		showFileButton.setOnAction(new EventHandler<ActionEvent>() {
			    @Override 
			    public void handle(ActionEvent e) {
			    	showImage();
			    }
			});
			
		    
			//Put all the controls in the pane.
		root.add(nameField, 0, 0, 3, 1);
		root.add(nameButton, 2, 0, 1, 1);
		root.add(chatText, 0, 2, 4, 1);
		root.add(recipient, 0, 4, 1, 1);
		root.add(showFileButton, 1, 4, 1, 1);
		root.add(sendFileButton, 2, 4, 1, 1);
		root.add(inputField, 0, 6, 3, 1);
		root.add(sendButton, 2, 6, 1, 1);
		
		primaryStage.setTitle("Chat Client (dev-Version 1.0)");
		Scene scene = new Scene(root, 800, 900);
		
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setScene(scene);
		
		newMessagesAmount.put("All", 0);
		conversations.put("All", "");
		chatText.appendText("SYS Setting up Connection...");
		startMedia.play();
		primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent window) {
					controller = new Controller(view);
					controller.setDaemon(true);
					controller.start();
				}
			});
		primaryStage.show();
	}
	
	/**
	 * Resets the media to be able to play again.
	 */
	private void resetMedia() {
		Media medium = new Media(new File(path).toURI().toString());
		mediaPlayer = new MediaPlayer(medium);
	}
	
	/**
	 * returns a bytearray given an integer.
	 * @param bytes integer to be converted to bytes.
	 * @return bytearray corresponing to the integer given.
	 */
	byte[] unpack(int bytes) {
		return new byte[] {
			(byte) ((bytes >>> 24) & 0xff),
			(byte) ((bytes >>> 16) & 0xff),
			(byte) ((bytes >>>  8) & 0xff),
			(byte) (bytes          & 0xff)
		};
	}
	
	/**
	 * Enables controlls after the controller has initialized.
	 * @param address ip address of local computer to be displayed.
	 */
	public void start(int address) {
		nameField.setDisable(false);
		
		nameButton.setDisable(false);
		
		chatText.appendText("\nSYS Connection Ready!");
		try {
			chatText.appendText("\nSYS Your IP is: " 
						 + InetAddress.getByAddress(unpack(address)).getHostAddress().toString());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	//======================================================================================================================
	//||                                            GUI data displaying methods:                                          ||  
	//======================================================================================================================
	
	/**
	 * Map which maps the source Name to a string which is the conversation.
	 */
	private Map<String, String> conversations = new HashMap<String, String>();
	
	/**
	 * recipient seleced at this time.
	 */
	private String selectedRecipient = "All";
	
	/**
	 * recipient last selected.
	 */
	private String lastSelectedRecipient = "All";
	
	/**
	 * list of recipients reachable used for displaying the items in the combobox.
	 */
	private List<String> recipients = new ArrayList<String>();
	
	/**
	 * Maps names of sources to the amount of new messages.
	 */
	private Map<String, Integer> newMessagesAmount = new HashMap<String, Integer>();
	
	/**
	 * Maps names of sources to Images they are able to view using the show file button.
	 */
	private Map<String, List<Image>> images = new HashMap<String, List<Image>>();
	
	/**
	 * Updates the recipient combobox used when opening.
	 */
	private void updateRecipient() {
		recipient.getItems().clear();
		recipient.getItems().add("All" + " (" + newMessagesAmount.get("All") + ")");
		for (String name : recipients) {
			if (newMessagesAmount.containsKey(name) && newMessagesAmount.get(name) != 0) {
				recipient.getItems().add(name + " (" + newMessagesAmount.get(name) + ")");
			} else {
				recipient.getItems().add(name + " (" + newMessagesAmount.get(name) + ")");
			}
		}
		for (String item : recipient.getItems()) {
			if (selectedRecipient != null && item.contains(selectedRecipient)) {
				recipient.setValue(item);
			}
		}
	}
	
	/**
	 * Adds a recipient to the list of available recipients.
	 * @param receiver recipient to be added.
	 */
	public void addRecipient(String receiver) {
		try {
			if (!recipients.contains(receiver) && !receiver.equals("Anonymous")) {
				recipients.add(receiver);
				newMessagesAmount.put(receiver, 0);
				if (!conversations.containsKey(receiver)) {
					conversations.put(receiver, "");
				}
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Removes a recipient from the list of available recipients.
	 * @param receiver recipient to be removed.
	 */
	public void removeRecipient(String receiver) {
		if (receiver != null) {
			try {
				if (recipients.contains(receiver)) {
					recipients.remove(receiver);
					if (selectedRecipient != null && selectedRecipient.contains(receiver)) {
						selectedRecipient = null;
					}
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Adds an image to the to be displayed images map.
	 * @param image Image to be added to the list.
	 * @param source Source of the person who send it.
	 */
	public void addImage(Image image, String source) {
		if (!images.containsKey(source)) {
			images.put(source, new ArrayList<Image>());
		}
		images.get(source).add(image);
		if (images.containsKey(selectedRecipient) && !images.get(selectedRecipient).isEmpty()) {
			showFileButton.setDisable(false);
		} else {
			showFileButton.setDisable(true);
		}
	}
	
	/**
	 * Shows an image and enables/disables the show file button as neccesary.
	 */
	private void showImage() {
		if (images.containsKey(selectedRecipient) && !images.get(selectedRecipient).isEmpty()) {
			showImage(images.get(selectedRecipient).get(0));
			images.get(selectedRecipient).remove(0);
			if (images.containsKey(selectedRecipient) && !images.get(selectedRecipient).isEmpty()) {
				showFileButton.setDisable(false);
			} else {
				showFileButton.setDisable(true);
			}
		}
	}
	
	/**
	 * This method determines the current recipient that is selected without the new message counter.
	 * @return currently selected recipient.
	 */
	private String getRecipientValue() {
		if (recipient.getValue() != null) {
			return recipient.getValue().split(Pattern.quote(" ("))[0];
		} else {
			return null;
		}
	}
	
	/**
	 * Updates the chat text and displays the chat conversationt coresponding to the receiver.
	 * @param receiver client you will be sending to and which chat conversation you will see.
	 */
	private void updateText(String receiver) {
		if (receiver != null) {
			conversations.put(selectedRecipient, chatText.getText());
			chatText.setText(conversations.get(receiver));
			selectedRecipient = receiver;
			lastSelectedRecipient = receiver;
			newMessagesAmount.put(receiver, 0);
			if (images.containsKey(selectedRecipient) && !images.get(selectedRecipient).isEmpty()) {
				showFileButton.setDisable(false);
			} else {
				showFileButton.setDisable(true);
			}
		}
	}
	
	/**
	 * Adds a message to the chat and plays a sound and updates the new message counter if source not selected recipient.
	 * @param client client who send the message.
	 * @param message Message that is received.
	 * @param broadcasted boolean telling if the message was broadcasted or a private message.
	 */
	public void addMessage(String client, String message, boolean broadcasted) {
		if (broadcasted) {
			if (lastSelectedRecipient.equals("All")) {
				chatText.appendText("\n" + client + ": " + message);
			} else {
				conversations.put("All", conversations.get("All").concat("\n" 
																+ client + ": " + message));
				newMessagesAmount.put("All", newMessagesAmount.get("All") + 1);
			}
		} else {
			if (lastSelectedRecipient.equals(client)) {
				chatText.appendText("\n" + client + ": " + message);
			} else {
				conversations.put(client, conversations.get(client).concat("\n" 
																+ client + ": " + message));
				newMessagesAmount.put(client, newMessagesAmount.get(client) + 1);
			}
		}
		mediaPlayer.play();
		resetMedia();
	}
	
	//======================================================================================================================
	//||                                            GUI popup/new window methods:                                         ||  
	//======================================================================================================================
	
	/**
	 * Shows a new window with the image that has been received first by the person.
	 * @param image image that has been received
	 */
	private void showImage(Image image) {
		ImageView iv1 = new ImageView();
		iv1.setImage(image);
		
		GridPane rooot = new GridPane();
		
		Stage stage = new Stage();
		iv1.fitWidthProperty().bind(stage.widthProperty());
		Scene scene = new Scene(rooot, 600, 600);
		
		rooot.add(iv1, 1, 1);
		
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
	}
	
	/**
	 * Displays a diaglog with a certain message.
	 * @param message message that it send.
	 */
	private void showDialog(String message) {
		Stage dialog = new Stage();
		dialog.initStyle(StageStyle.UTILITY);
		Scene sc = new Scene(new Group(new Text(25, 25, message)), 260, 80);
		dialog.setScene(sc);
		dialog.show();
	}
	
	/**
	 * Displays a new dialog with the string error: and a message.
	 * @param message message to be displayed.
	 */
	public void error(String message) {
		showDialog("ERROR: " + message);
	}
	
	//======================================================================================================================
	//||                                       GUI data relay to controller methods:                                      ||  
	//======================================================================================================================
	
	/**
	 * This method takes the string in the inputField and sends it to the controller to be send to the selected recipient.
	 */
	private void send() {
		if (selectedRecipient == null) {
			showDialog("You did not select a recipient.\nIt might have changed its name");
		} else if (inputField.getText().length() > MAXINPUTLENGTH) {
			showDialog("Too many characters");
		} else {
			if (!inputField.getText().isEmpty()) {
				String dest;
				
				if (selectedRecipient.equals("All")) {
					dest = "Anonymous";
				} else {
					dest = selectedRecipient;
				}
				
				chatText.appendText("\n" + "You: " + inputField.getText());
    			controller.receiveFromView(dest, inputField.getText());
    			inputField.requestFocus();
    			inputField.clear();
			}
		}
	}
	
	/**
	 * This method takes the string in the nameField and passes it to the controller to set is as the clientname.
	 */
	private void setName() {
		if (nameField.getText().contains("(")) {
			error("Cant use the sign: '(' ");
		} else {
			controller.setClientName(nameField.getText());
			showDialog("Your name is set to: " + nameField.getText());
			root.getChildren().remove(nameButton);
			root.getChildren().remove(nameField);
			root.getChildren().remove(chatText);
			root.add(chatText, 0, 0, 3, 3);
			inputField.setDisable(false);
			recipient.setDisable(false);
			sendButton.setDisable(false);
			sendFileButton.setDisable(false);
		}
	}
	
	/**
	 * Passes a file on to the controller to tbe send to the selected recipient.
	 * @param file
	 */
	private void sendFile(File file) {
		if (selectedRecipient == null) {
			showDialog("You did not select a recipient.\nYou might have lost connection");
		} else {
			String dest;
			
			if (selectedRecipient.equals("All")) {
				showDialog("You cannot send a file in a group chat!");
			} else {
				dest = selectedRecipient;
				controller.sendFile(file, dest);
			}
		}
	}
}
