package comunicacao;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


/**
 * Created by wanderson on 27/06/17.
 */


public class Cliente {

    private InetAddress enderecoMulticast;
    private MulticastSocket conexaoGrupo;
    private final static int PORTA_CLIENTE = 44444;
    private static int CONTADOR_HORARIO;
    private static String ID_COORDENADOR;


    public Cliente() {
        ID_COORDENADOR = new String();
        CONTADOR_HORARIO = 0;
    }

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

    public synchronized void enviarContadorHorario(int contadorHorario, String id) {
        byte dados[] = ("1000" + ";" + contadorHorario + ";" +id).getBytes();
        DatagramPacket msgPacket = new DatagramPacket(dados, dados.length, enderecoMulticast, PORTA_CLIENTE);
        try {
            conexaoGrupo.send(msgPacket);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized int receberContadorHorario() {
        return this.CONTADOR_HORARIO;
    }

    public String getIdCoordenador() {
        return ID_COORDENADOR;
    }

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
                        if (contadorHorario > Cliente.CONTADOR_HORARIO) {
                            System.out.println("o coordenador é "+dadosRecebidos[2]);
                            Cliente.CONTADOR_HORARIO = contadorHorario;
                            Cliente.ID_COORDENADOR = dadosRecebidos[2].trim();
                        }
                        else System.out.println("não sou coordenador");
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }
}
