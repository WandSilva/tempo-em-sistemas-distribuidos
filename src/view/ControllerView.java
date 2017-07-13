package view;

import comunicacao.Comunicacao;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import javax.swing.*;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Classe responsável por gerir os eventos do relógio e
 * sincronizar o tempo mostrado com o tempo recebido
 */
public class ControllerView implements Initializable {

    @FXML
    Label lblContador;

    @FXML
    Label lblID;

    private int contadorTempo = 0;
    private int contadorAuxiliar = 0;
    private int tempoAtraso = 0;
    private float drifft = 0;

    private Comunicacao comunicacao;
    private String id;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String x = JOptionPane.showInputDialog("contadorTempo");
        String y = JOptionPane.showInputDialog("drifft");
        this.id = JOptionPane.showInputDialog("informe o ID");
        this.comunicacao = new Comunicacao(id);
        comunicacao.iniciarGrupo();
        contadorTempo = Integer.parseInt(x);
        drifft = Float.parseFloat(y);
        lblID.setText(id);
        this.sincronizar();
    }

    /**
     * cria a thread e chama os métodos reponsáveis pela sincronização do tempo
     */
    private void sincronizar() {
        Task t = new Task() {
            @Override
            protected Object call() throws Exception {
                while (true) {
                    Platform.runLater(() -> {
                        atualizarTela();
                        enviarTempo();
                        receberTempo();
                    });
                    Thread.sleep((long) (1000 * drifft));
                }
            }
        };
        new Thread(t).start();
    }

    /**
     * Caso o comunicacao seja o coordenador, seu tempo será enviado a cada segundo.
     * Para cada coordenador, será feito o calculo do atraso quando este método for
     * utilizado pela primeira vez
     */
    public void enviarTempo(){
        if (comunicacao.getIdCoordenador().equals(id)) {
            comunicacao.enviarContadorHorario(contadorTempo+ tempoAtraso, id);

            if(comunicacao.isControleSolicitacao()){
                calcularTempoAtraso();
            }
        }
    }

    /**
     * recebe o horário do coordenador após contar 5 segundos(levando o drifft em consideração).
     * Caso haja algum problema com o tempo recebido, o comunicacao enviará seu tempo e
     * poderá assumir o papel de coordenador.
     */
    public void receberTempo(){
        if (contadorAuxiliar > 5 && !comunicacao.getIdCoordenador().equals(id)) {
            int tempoRecebido = comunicacao.receberContadorHorario();

            if (tempoRecebido >= contadorTempo) {
                contadorTempo = tempoRecebido;
                System.out.println("atualizei");
            }
            else {
                comunicacao.setControleSolicitacao(true);
                comunicacao.enviarContadorHorario(contadorTempo, id);
                System.out.println("enviei meu tempo");
            }
            contadorAuxiliar = 0;
        }
    }

    /**
     * Envia uma mensagem para o grupo e aguarda uma resposta, calculando a diferença entre
     * o tempo de envio e o tempo de resposta.
     */
    private void calcularTempoAtraso(){
        comunicacao.setControleSolicitacao(false);
        long aux1 = new Date().getTime();
        comunicacao.solicitarTempoResposta(id);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long aux2 = comunicacao.getTempoResp();
        comunicacao.setTempoResp(0);
        tempoAtraso = (int)(aux2-aux1)/1000;
        if (tempoAtraso<0) tempoAtraso=0;
    }

    /**
     * Faz a contagem do tempo na tela
     */
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