import controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class Main extends Application {

    private Pane rootLayout;
    private MainController mainController;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/views/main.fxml"));
        rootLayout = loader.load();
        mainController = loader.getController();
        primaryStage.setTitle("ACO");
        primaryStage.getIcons().add(new Image("file:src/ressources/fourmie.jpg"));
        primaryStage.setScene(new Scene(rootLayout, 1300, 900));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
