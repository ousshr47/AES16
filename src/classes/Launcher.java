package classes;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;

public class Launcher extends Application {
	@Override
	public void start(Stage stage) throws Exception {

		AnchorPane root = (AnchorPane) FXMLLoader.load(getClass().getResource("/fxml/App.fxml"));

		// Parent root =
		// FXMLLoader.load(getClass().getResource("/presentation/well_Management/WM_FuelOrder_ListUser.fxml"));

		Scene scene = new Scene(root);
		stage.setTitle("AES Encryption / Decryption");
		stage.setScene(scene);
		stage.getIcons().add(new Image("/resources/logo.png"));
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
