package comunicacao;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Date;


/**
 * Created by wanderson on 27/06/17.
 */

/**
 * classe responsável por fazer a comunicação entre os clientes
 */
public class Comunicacao {

    private static InetAddress enderecoMulticast;
    private static MulticastSocket conexaoGrupo;
    private final static int PORTA_CLIENTE = 44444;
    private static int CONTADOR_HORARIO;
    private static String ID_COORDENADOR;
    private static String MEU_ID;
    private static boolean CONTROLE_SOLICITACAO;
    private static long TEMPO_RESP;


    public Comunicacao(String meuId) {
        ID_COORDENADOR = new String();
        CONTADOR_HORARIO = 0;
        MEU_ID = meuId;
    }

    /**
     * inicia o grupo multicast
     */
    public void iniciarGrupo() {
        try {
            enderecoMulticast = InetAddress.getByName("235.0.0.1");
            conexaoGrupo = new MulticastSocket(PORTA_CLIENTE);
            conexaoGrupo.joinGroup(enderecoMulticast);
            new ThreadCliente(conexaoGrupo).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * o cliente coordenador envia seu tempo para o grupo
     * @param contadorHorario
     * @param id
     */
    public void enviarContadorHorario(int contadorHorario, String id) {
        byte dados[] = ("1000" + ";" + contadorHorario + ";" + id).getBytes();
        DatagramPacket msgPacket = new DatagramPacket(dados, dados.length, enderecoMulticast, PORTA_CLIENTE);
        try {
            conexaoGrupo.send(msgPacket);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * o cliente coordenador solicita uma resposta para saber o atraso da rede.
     * @param id
     */
    public void solicitarTempoResposta(String id) {
        byte dados[] = ("1001" + ";" + id).getBytes();
        DatagramPacket msgPacket = new DatagramPacket(dados, dados.length, enderecoMulticast, PORTA_CLIENTE);
        try {
            conexaoGrupo.send(msgPacket);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public int receberContadorHorario() {
        return this.CONTADOR_HORARIO;
    }

    public String getIdCoordenador() {
        return ID_COORDENADOR;
    }

    public boolean isControleSolicitacao() {
        return CONTROLE_SOLICITACAO;
    }

    public void setControleSolicitacao(boolean controleSolicitacao) {
        CONTROLE_SOLICITACAO = controleSolicitacao;
    }

    public long getTempoResp() {
        return TEMPO_RESP;
    }

    public void setTempoResp(long tempoResp) {
        TEMPO_RESP = tempoResp;
    }


    /**
     * Classe interna responsável por criar a thread e ficar sempre
     * esperando as solicitações dos clientes
     */
    private static class ThreadCliente extends Thread {

        private final MulticastSocket socketMulticast;

        public ThreadCliente(MulticastSocket socketMulticast) {
            this.socketMulticast = socketMulticast;
        }

        /**
         * Método responsável por executar a Thread criada.
         *
         * @author Wanderson
         */
        @Override
        public void run() {
            try {
                while (true) {
                    byte dados[] = new byte[1024];
                    DatagramPacket datagrama = new DatagramPacket(dados, dados.length);
                    socketMulticast.receive(datagrama);
                    String msg = new String(datagrama.getData());

                    if (msg.startsWith("1000")) {
                        String[] dadosRecebidos = msg.split(";");
                        int contadorHorario = Integer.parseInt(dadosRecebidos[1].trim());
                        if (contadorHorario > Comunicacao.CONTADOR_HORARIO) {
                            System.out.println("o coordenador é " + dadosRecebidos[2]);
                            Comunicacao.CONTADOR_HORARIO = contadorHorario;
                            Comunicacao.ID_COORDENADOR = dadosRecebidos[2].trim();
                        } else System.out.println("não sou coordenador");
                    }
                    if (msg.startsWith("1001")) {

                        String[] dadosRecebidos = msg.split(";");
                        if (!
                                MEU_ID.equals(dadosRecebidos[1].trim())) {
                            byte data[] = ("1002").getBytes();
                            DatagramPacket msgPacket = new DatagramPacket(data, data.length, enderecoMulticast, PORTA_CLIENTE);
                            try {
                                conexaoGrupo.send(msgPacket);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }

                    }
                    if (msg.startsWith("1002")) {
                        TEMPO_RESP = new Date().getTime();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

