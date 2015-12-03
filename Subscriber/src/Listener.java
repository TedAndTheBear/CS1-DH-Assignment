import hr.fer.tel.pubsub.artefact.Publication;
import hr.fer.tel.pubsub.entity.NotificationListener;
import hr.fer.tel.pubsub.artefact.HashtablePublication;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.UUID;
import src.googlemapsexample.GoogleMapsExampleGUI;

public class Listener extends NotificationListener{

		//GUI setup
		GoogleMapsExampleGUI gui = new GoogleMapsExampleGUI();
	
	@Override
	 public void notify( UUID subscriberId, String subscriberName, Publication notification ) {
		assert notification instanceof HashtablePublication;
		
		HashMap<String, Object> update = ((HashtablePublication) notification).getProperties();
		String cmd = Integer.toString((int)update.get("BusId")) + "|" + Double.toString((double)update.get("Latitude")) + "|" + Double.toString((double)update.get("Longitude"));
		gui.updateMarkers(cmd);
	}

}
