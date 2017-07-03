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

    @FXML
    Label lblID;

    private int contadorTempo = 0;
    private int contadorAuxiliar = 0;
    private float drifft = 0;

    private Cliente cliente;
    private String id;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.cliente = new Cliente();
        cliente.iniciarGrupo();
        String x = JOptionPane.showInputDialog("contadorTempo");
        String y = JOptionPane.showInputDialog("drifft");
        this.id = JOptionPane.showInputDialog("informe o ID");
        contadorTempo = Integer.parseInt(x);
        drifft = Float.parseFloat(y);
        lblID.setText(id);
        this.contar();
    }

    private void contar() {
        Task t = new Task() {
            @Override
            protected Object call() throws Exception {
                while (true) {
                    Platform.runLater(() -> {
                        contadorTempo++;
                        contadorAuxiliar++;
                        int seg = contadorTempo % 60;
                        int min = contadorTempo / 60;
                        int hora = min / 60;
                        if (hora == 24) hora = 0;
                        min %= 60;
                        lblContador.setText(String.format("%02d:%02d:%02d", hora, min, seg));

                        if (cliente.getIdCoordenador().equals(id)) {
                            cliente.enviarContadorHorario(contadorTempo, id);
                        }
                        if (contadorAuxiliar > 5 && !cliente.getIdCoordenador().equals(id)) {
                            int tempoRecebido = cliente.receberContadorHorario();

                            if (tempoRecebido >= contadorTempo) {
                                contadorTempo = tempoRecebido;
                                System.out.println("atualizei");
                            }
                            else {
                                cliente.enviarContadorHorario(contadorTempo, id);
                                System.out.println("enviei meu tempo");
                            }
                            contadorAuxiliar = 0;
                        }


                    });
                    Thread.sleep((long) (1000 * drifft));
                }
            }
        };
        new Thread(t).start();
    }
}