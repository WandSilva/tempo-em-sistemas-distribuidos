package view;

import comunicacao.Cliente;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import javax.swing.*;
import java.net.URL;
import java.util.ResourceBundle;

public class ControllerView implements Initializable {

    @FXML
    Label lblContador;

    private int contador = 0;
    private float drifft=0;

    private Cliente cliente;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.cliente = new Cliente();
        cliente.iniciarGrupo();
        String x = JOptionPane.showInputDialog("contador");
        String y = JOptionPane.showInputDialog("drifft");
        contador = Integer.parseInt(x);
        drifft = Float.parseFloat(y);
        this.contar();
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
                       System.out.println("loop");
                       min%=60;
                       lblContador.setText(String.format("%02d:%02d:%02d",hora,min,seg));
                       cliente.enviarContadorHorario(contador);
                      // if(cliente.isControleContador()) {
                          // cliente.setControleContador(false);
                           contador = cliente.receberContadorHorario();
                      // }
                   });
                   Thread.sleep((long) (1000*drifft));
                   System.out.println((long) (1000*drifft));
               }
            }
        };new Thread(t).start();
    }
}