package tpd.dmx_control;

import tpd.dmx_control.utils.CheckPorts;
import tpd.dmx_control.controllers.DmxController;

import com.fazecast.jSerialComm.SerialPort;

public class App 
{
    public static void main( String[] args ) throws InterruptedException {
        DmxController dmx = new DmxController();

        // 1. Lister les ports pour trouver le bon
        SerialPort[] ports = CheckPorts.listAvailablePorts();

        // 2. Sélectionner le port (Modifier l'index selon votre système)
        // Sur Mac, cherchez quelque chose comme "/dev/cu.usbserial..." ou "FT232R USB UART"
        if (ports.length == 0) {
            System.out.println("Aucun port trouvé !");
            return;
        }
        
        // Par défaut on prend le premier port trouvé pour le test. 
        // À modifier manuellement si ce n'est pas le bon.
        String portName = ports[0].getSystemPortName(); 
        
        if (dmx.connect(portName)) {
            System.out.println("Test DMX démarré. Regardez votre Stairville !");

            // TEST 1 : ROUGE
            System.out.println("Couleur : ROUGE");
            dmx.setChannel(1, 255); // Ch 1 = Rouge
            dmx.setChannel(2, 0);
            dmx.setChannel(3, 0);
            Thread.sleep(2000);

            // TEST 2 : VERT
            System.out.println("Couleur : VERT");
            dmx.setChannel(1, 0);
            dmx.setChannel(2, 255); // Ch 2 = Vert
            dmx.setChannel(3, 0);
            Thread.sleep(2000);

            // TEST 3 : BLEU
            System.out.println("Couleur : BLEU");
            dmx.setChannel(1, 0);
            dmx.setChannel(2, 0);
            dmx.setChannel(3, 255); // Ch 3 = Bleu
            Thread.sleep(2000);
            
            // TEST 4 : FADE (Fondu)
            System.out.println("Fondu...");
            for(int i=0; i<255; i++) {
                dmx.setChannel(1, i); // Monte le rouge
                dmx.setChannel(3, 255 - i); // Descend le bleu
                Thread.sleep(20); // Vitesse du fondu
            }

            System.out.println("Fin du test. Blackout.");
            dmx.blackOut();
            Thread.sleep(1000);
            dmx.stop();
        }
    }
}
