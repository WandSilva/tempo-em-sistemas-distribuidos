package view;

import comunicacao.Cliente;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import javax.swing.*;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class ControllerView implements Initializable {

    @FXML
    Label lblContador;

    @FXML
    Label lblID;

    private int contadorTempo = 0;
    private int contadorAuxiliar = 0;
    private int tempoAtraso = 0;
    private float drifft = 0;

    private Cliente cliente;
    private String id;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String x = JOptionPane.showInputDialog("contadorTempo");
        String y = JOptionPane.showInputDialog("drifft");
        this.id = JOptionPane.showInputDialog("informe o ID");
        this.cliente = new Cliente(id);
        cliente.iniciarGrupo();
        contadorTempo = Integer.parseInt(x);
        drifft = Float.parseFloat(y);
        lblID.setText(id);
        this.sincronizar();
    }

    private void sincronizar() {
        Task t = new Task() {
            @Override
            protected Object call() throws Exception {
                while (true) {
                    Platform.runLater(() -> {
                        atualizarTela();

                        if (cliente.getIdCoordenador().equals(id)) {
                            cliente.enviarContadorHorario(contadorTempo+ tempoAtraso, id);

                            if(cliente.isControleSolicitacao()){
                                calcularTempoAtraso();
                            }
                        }
                        if (contadorAuxiliar > 5 && !cliente.getIdCoordenador().equals(id)) {
                            int tempoRecebido = cliente.receberContadorHorario();

                            if (tempoRecebido >= contadorTempo) {
                                contadorTempo = tempoRecebido;
                                System.out.println("atualizei");
                            }
                            else {
                                cliente.setControleSolicitacao(true);
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
    private void calcularTempoAtraso(){
        cliente.setControleSolicitacao(false);
        long aux1 = new Date().getTime();
        cliente.solicitarTempoResposta(id);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long aux2 = cliente.getTempoResp();
        cliente.setTempoResp(0);
        tempoAtraso = (int)(aux2-aux1)/1000;
        if (tempoAtraso<0) tempoAtraso=0;
    }

    public void atualizarTela(){
        contadorTempo++;
        contadorAuxiliar++;
        int seg = contadorTempo % 60;
        int min = contadorTempo / 60;
        int hora = min / 60;
        if (hora == 24) hora = 0;
        min %= 60;
        lblContador.setText(String.format("%02d:%02d:%02d", hora, min, seg));
    }
}