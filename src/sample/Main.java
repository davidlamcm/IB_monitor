package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.ib.client.EClientSocket;

//import com.ib.client.SummaryModel;



public class Main extends Application {
    Controller controllerOb;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));

        Parent root = (Parent) loader.load();
        primaryStage.getIcons().add(new Image("icon.jpeg"));

        primaryStage.setTitle("Pinpoint IB risk monitor");
        primaryStage.setScene(new Scene(root, 650, 550));
        controllerOb = loader.getController();
        IBWrapper ibWrapper = new IBWrapper();
        EClientSocket ibClient = new EClientSocket(ibWrapper);
        controllerOb.setIBWrapper(ibWrapper);
        controllerOb.setEClientSocket(ibClient);
        ibWrapper.setController(controllerOb);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        try {
            controllerOb.riskMonitorThread.interrupt();
        }catch(Exception e){}
        controllerOb.stop();
        super.stop();
        System.out.println("logout");
    }


}
