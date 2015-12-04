import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import hr.fer.tel.pubsub.artefact.HashtablePublication;
import hr.fer.tel.pubsub.entity.Publisher;
import com.google.transit.realtime.GtfsRealtime;

public class Publish {

	public static void main(String[] args) {
		
		//Connect to publisher of specified IP adress and port
		Publisher publisher = new Publisher( "Publisher", args[0], Integer.parseInt(args[1]));
		publisher.setLogWriting( false );
		publisher.setTesting( false );
		publisher.connect();

		while(true)
		{
		URL url;
		float longitude;
		float latitude;
		HashtablePublication publication = new HashtablePublication();
		int i = 1;
		try {
		url = new URL( "http://developer.mbta.com/lib/gtrtfs/Vehicles.pb" );
		GtfsRealtime.FeedMessage theFeed =
		GtfsRealtime.FeedMessage.parseFrom((InputStream)url.openStream());
		
			//Loop through every vehicle and publish its position
			for (GtfsRealtime.FeedEntity entity : theFeed.getEntityList()) {
				if (!entity.hasVehicle()) {
					continue;
				}
				GtfsRealtime.VehiclePosition vehicle = entity.getVehicle();
				if (!vehicle.hasPosition()) {
					continue;
				}
				GtfsRealtime.Position position = vehicle.getPosition();
				latitude = position.getLatitude();
				longitude = position.getLongitude();
	
				publication.setStartTime(System.currentTimeMillis());
				publication.setValidity(System.currentTimeMillis() + 60000);
				publication.setProperty("BusId", i);
				publication.setProperty("Latitude", latitude);
				publication.setProperty("Longitude", longitude);
				publisher.publish(publication);
				System.out.println("ID:" + i + " lat:" + latitude + " long:" + longitude);
				++i;
			}
		} catch ( MalformedURLException ex ) {
		} catch ( IOException ex ) {
		}
		
		// Only do this once a minute
		while(publication.getValidity() > System.currentTimeMillis())
		{
		}
		
		}
		
		//publisher.disconnectFromBroker();
	}
	
	

}
