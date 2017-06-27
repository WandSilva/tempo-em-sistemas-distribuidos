package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerView implements Initializable {

    @FXML
    Label lblContador;

    private int contador = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.contar();
        lblContador.setText("teste");
    }

    private void contar() {
        Task t = new Task() {
            @Override
            protected Object call() throws Exception {
               while (true){
                   Platform.runLater(() ->{
                       contador++;
                       int seg = contador % 60;
                       int min = contador / 60;
                       int hora = min / 60;
                       if(hora == 24) hora = 0;
                       min%=60;
                       lblContador.setText(String.format("%02d:%02d:%02d",hora,min,seg));
                   });
                   Thread.sleep(0,1);
               }
            }
        };new Thread(t).start(); System.out.println("criei outra thread");
    }
}

    /*contador++;
    int seg = contador % 60;
    int min = contador / 60;
    int hora = min / 60;
    min%=60;
    lblContador.setText(String.format("%02d:%02d:%02d:",hora,min,seg));
    System.out.println("entrei aqui 2");
        */
