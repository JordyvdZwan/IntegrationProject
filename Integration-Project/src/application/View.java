package application;
	
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;


public class View extends Application {
	
	//Initializing all controls on of the program
	TextArea chatText = new TextArea();
	Button sendButton = new Button();
	TextField inputField = new TextField();
	ComboBox<String> recipient = new ComboBox<String>();
	
	
	Controller controller = new Controller(this);
	
	@Override
	public void start(Stage primaryStage) {
		try {
			GridPane root = new GridPane();
			
			//Finalize Combobox
			recipient.getItems().add("All");
//			recipient.setValue("All");
			recipient.setMinWidth(200);
			
			//Finalize chatText
			chatText.setMinHeight(350);
			
			//Finalize inputField
			inputField.setMinWidth(400);
			inputField.setMinHeight(80);
			
			//Finalize sendButton
			sendButton.setId("sendButton");
			sendButton.setMinWidth(80);
			sendButton.setText("Send");
			sendButton.setOnAction(new EventHandler<ActionEvent>() {
				
			    @Override 
			    public void handle(ActionEvent e) {
		    		if (recipient.getValue() == null) {
		    			Stage dialog = new Stage();
		    			dialog.initStyle(StageStyle.UTILITY);
		    			Scene sc = new Scene(new Group(new Text(25, 25, "You did not select a recipient.")), 260, 80);
		    			dialog.setScene(sc);
		    			dialog.show();
		    		} else {
		    			if (!inputField.getText().isEmpty()) {
			    			chatText.setText(chatText.getText() + "\n" + "You" + ": " + inputField.getText());
			    			//recipient.getValue()
			    			//inputField.getText()
			    			controller.sendMessage(recipient.getValue(), inputField.getText());
			    			inputField.clear();
		    			}
		    		}
			    }
			    
			});
			
			//Put all the controls in the pane.
			root.add(chatText, 0, 1, 2, 1);
			root.add(recipient, 0, 2, 2, 1);
			root.add(inputField, 0, 3, 1, 1);
			root.add(sendButton, 1, 3, 1, 1);
			
			primaryStage.setTitle("Chat Client (dev-Version 1.0)");
			Scene scene = new Scene(root,600,800);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			controller.start();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public void addClient(String client) {
		if (!recipient.getItems().contains(client)) {
			recipient.getItems().add(client);
		}
	}
	
	public void removeClient(String client) {
		if (recipient.getItems().contains(client)) {
			recipient.getItems().remove(client);
			recipient.setValue(null);
		}
	}
	
	public void addMessage(String client, String message) {
		chatText.setText(chatText.getText() + "\n" + client + ": " + message);
	}
}
