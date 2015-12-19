import hr.fer.tel.pubsub.artefact.Publication;
import hr.fer.tel.pubsub.entity.NotificationListener;
import hr.fer.tel.pubsub.artefact.HashtablePublication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import gmapstatic.*;
import src.googlemapsexample.GoogleMapsExampleGUI;

public class Listener extends NotificationListener{

	GoogleMapsExampleGUI gui = new GoogleMapsExampleGUI();
	@Override
	 public void notify( UUID subscriberId, String subscriberName, Publication notification ) {
		
		if(notification instanceof HashtablePublication)
		{
			if(notification.getValidity() > System.currentTimeMillis())
			{		
				HashMap<String, Object> update = ((HashtablePublication) notification).getProperties();
				MapMarker marker = new MapMarker((Float)update.get("Latitude"), (Float)update.get("Longitude"));
				gui.update(marker);
				gui.setVisible(true);
				System.out.println(marker);
			}
		}
		else
		{
			System.out.println("Not the notification you're looking for");
		}
	}

}
