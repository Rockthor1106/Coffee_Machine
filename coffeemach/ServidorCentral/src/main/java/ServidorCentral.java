
import java.util.ArrayList;
import java.util.List;
import com.zeroc.Ice.*;
import comunicacion.*;
import gatewayCommunication.ObservableImp;
import gatewayCommunication.RMImp;
import interfaz.ControladorRecetas;
import receta.ProductoReceta;
import servicios.*;
import ventas.VentasManager;
import ServerControl.*;
import alarma.Alarma;
import alarma.AlarmasManager;

public class ServidorCentral {

    public static void main(String[] args) {
        List<String> params = new ArrayList<>();
        try (Communicator communicator = Util.initialize(args, "server.cfg", params)) {

            ObjectAdapter adapter = communicator.createObjectAdapter("Server");

            ServerControl control = new ServerControl(communicator);

            ServicioComLogistica log = new ControlComLogistica(control);

            Alarma alarma = new Alarma(new AlarmasManager(communicator));

            ProductoReceta recetas = new ProductoReceta();
            recetas.setCommunicator(communicator);

            VentasManager ventas = new VentasManager();
            ventas.setCommunicator(communicator);

            ObservableImp gateway = new ObservableImp();
            gateway.setCommunicator(communicator);

            RMImp reliableMessage = new RMImp();
            reliableMessage.setCommunicator(communicator);

            adapter.add(alarma, Util.stringToIdentity("Alarmas"));
            adapter.add(ventas, Util.stringToIdentity("Ventas"));
            adapter.add(log, Util.stringToIdentity("logistica"));
            adapter.add(recetas, Util.stringToIdentity("Recetas"));
            adapter.add(gateway, Util.stringToIdentity("Gateway"));
            adapter.add(reliableMessage, Util.stringToIdentity("ReliableMessage"));
            
            ControladorRecetas controladorRecetas = new ControladorRecetas(gateway);
            controladorRecetas.setRecetaService(recetas);
            gateway.setRecetaService(recetas);
            controladorRecetas.run();

            adapter.activate();
            communicator.waitForShutdown();

        }
    }
}
