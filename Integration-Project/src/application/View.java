package application;
	
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;


public class View extends Application {
	
	//Initializing all controls on of the program
	TextArea chatText = new TextArea();
	Button sendButton = new Button();
	TextField inputField = new TextField();
	Button nameButton = new Button();
	TextField nameField = new TextField();
	ComboBox<String> recipient = new ComboBox<String>();
	View view = this;
	
	Controller controller;
	
	String path = "Notification.mp3";
	Media media = new Media(new File(path).toURI().toString());
	MediaPlayer mediaPlayer = new MediaPlayer(media);
	
		
	public void resetMedia() {
		Media media = new Media(new File(path).toURI().toString());
		mediaPlayer = new MediaPlayer(media);
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			GridPane root = new GridPane();
			
			ColumnConstraints coll1 = new ColumnConstraints();
			coll1.setPercentWidth(80);
			ColumnConstraints coll3 = new ColumnConstraints();
			coll3.setPercentWidth(20);
			root.getColumnConstraints().addAll(coll1, coll3);
			
			
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
			recipient.setValue("All");
			recipient.setPrefSize( Double.MAX_VALUE, Double.MAX_VALUE );
			recipient.setDisable(true);
			
			//Finalize chatText
			chatText.setEditable(false);
			chatText.setPrefSize( Double.MAX_VALUE, Double.MAX_VALUE );
			chatText.textProperty().addListener(new ChangeListener<Object>() {
			    @Override
			    public void changed(ObservableValue<?> observable, Object oldValue,
			            Object newValue) {
			    	chatText.setScrollTop(Double.MIN_VALUE);
			    }
			});
			
			//Finalize nameField
			nameField.setId("textfield");
			nameField.setPrefSize( Double.MAX_VALUE, Double.MAX_VALUE );
			nameField.setDisable(true);
			nameField.setOnKeyPressed(new EventHandler<KeyEvent>()
		    {
		        @Override
		        public void handle(KeyEvent ke)
		        {
		            if (ke.getCode().equals(KeyCode.ENTER))
		            {
		            	setName();
		            }
		        }
		    });
			
			//Finalize nameButton
			nameButton.setId("sendButton");
			nameButton.setText("Set Name");
			nameButton.setPrefSize( Double.MAX_VALUE, Double.MAX_VALUE );
			nameButton.setDisable(true);
			nameButton.setOnAction(new EventHandler<ActionEvent>() {
			    @Override 
			    public void handle(ActionEvent e) {
			    	setName();
			    }
			});
			
			//Finalize inputField
			nameField.setId("textfield");
			inputField.setPrefSize( Double.MAX_VALUE, Double.MAX_VALUE );
			inputField.setDisable(true);
			inputField.setOnKeyPressed(new EventHandler<KeyEvent>()
		    {
		        @Override
		        public void handle(KeyEvent ke)
		        {
		            if (ke.getCode().equals(KeyCode.ENTER))
		            {
		                send();
		            }
		        }
		    });
			
			//Finalize sendButton
			sendButton.setId("sendButton");
			sendButton.setText("Send");
			sendButton.setPrefSize( Double.MAX_VALUE, Double.MAX_VALUE );
			sendButton.setDisable(true);
			sendButton.setOnAction(new EventHandler<ActionEvent>() {
			    @Override 
			    public void handle(ActionEvent e) {
		    		send();
			    }
			});
			
		    
			//Put all the controls in the pane.
			root.add(nameField, 0, 0, 2, 1);
			root.add(nameButton, 1, 0, 1, 1);
			root.add(chatText, 0, 2, 3, 1);
			root.add(recipient, 0, 4, 1, 1);
			root.add(inputField, 0, 6, 2, 1);
			root.add(sendButton, 1, 6, 1, 1);
			
			primaryStage.setTitle("Chat Client (dev-Version 1.0)");
			Scene scene = new Scene(root,600,800);
			
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			
			addMessage("SYS", "Setting up Connection...");
			primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWN, new  EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent window)
				{
					controller = new Controller(view);
					controller.setDaemon(true);
					controller.start();
				}
			});
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	byte[] unpack(int bytes) {
		return new byte[] {
			(byte)((bytes >>> 24) & 0xff),
			(byte)((bytes >>> 16) & 0xff),
			(byte)((bytes >>>  8) & 0xff),
			(byte)((bytes       ) & 0xff)
		};
	}
	
	public void start(int address) {
		nameField.setDisable(false);
		inputField.setDisable(false);
		recipient.setDisable(false);
		sendButton.setDisable(false);
		nameButton.setDisable(false);
		addMessage("SYS", "Connection Ready!");
		try {
			addMessage("SYS", "Your IP is: " + InetAddress.getByAddress(unpack(address)).getHostAddress().toString());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void addRecipient(String recipient) {
		try {
			if (!this.recipient.getItems().contains(recipient) && !recipient.equals("Anonymous")) {
				this.recipient.getItems().add(recipient);
			}
		} catch (IllegalStateException e) {
			//TODO actualy do nothing...
		}
	}
	
	public void removeRecipient(String recipient) {
		try {
			if (this.recipient.getItems().contains(recipient)) {
				this.recipient.getItems().remove(recipient);
				this.recipient.setValue(null);
			}
		} catch (IllegalStateException e) {
			//TODO actualy do nothing...
		}
	}
	
	public void removeAllRecipient() {
		try {
			this.recipient.getItems().clear();
			this.recipient.setValue(null);
			this.recipient.getItems().add("All");
		} catch (IllegalStateException e) {
			//TODO actualy do nothing...
		}
	}
	
	private void setName() {
		controller.setClientName(nameField.getText());
		showDialog("Your name is set to: " + nameField.getText());
	}
	
	private void showDialog(String message) {
		Stage dialog = new Stage();
		dialog.initStyle(StageStyle.UTILITY);
		Scene sc = new Scene(new Group(new Text(25, 25, message)), 260, 80);
		dialog.setScene(sc);
		dialog.show();
	}
	
	public void error(String message) {
		showDialog("ERROR: " + message);
	}
	
	private void send() {
		if (recipient.getValue() == null) {
			showDialog("You did not select a recipient.");
		} else {
			if (!inputField.getText().isEmpty()) {
				String dest;
				if (recipient.getValue().equals("All")) {
					dest = "Anonymous";
				} else {
					dest = recipient.getValue();
				}
    			chatText.appendText("\n" + "You" + ": " + inputField.getText());
    			chatText.setScrollTop(Double.MIN_VALUE);
    			controller.receiveFromView(dest, inputField.getText());
    			inputField.requestFocus();
    			inputField.clear();
			}
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public void addMessage(String client, String message) {
		chatText.setText(chatText.getText() + "\n" + client + ": " + message);
		mediaPlayer.play();
		resetMedia();
	}
}
