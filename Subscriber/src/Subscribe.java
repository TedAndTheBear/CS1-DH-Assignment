import hr.fer.tel.pubsub.artefact.TripletSubscription;
import hr.fer.tel.pubsub.common.Triplet;
import hr.fer.tel.pubsub.entity.Subscriber;

public class Subscribe {

	public static void main(String[] args) {
		
		//subscriber setup
		Subscriber subscriber = new Subscriber ("Subscriber", "PNALGORITHM", "193.10.227.205", 6237);
		subscriber.setLogWriting( false );
		subscriber.setTesting( false );
		subscriber.connect();
		
		//listener setup
		Listener listener = new Listener();
		subscriber.setNotificationListener(listener);
		
		TripletSubscription subscription = new TripletSubscription();
		subscription.setStartTime(System.currentTimeMillis());
		subscription.setValidity(System.currentTimeMillis() + 60000);
		int test = 1;
		subscription.setProperty(new Triplet( "BusId", test, "=" ));
		subscription.setProperty(new Triplet( "BusId", 2, "=" ));
		subscription.setProperty(new Triplet( "BusId", 3, "=" ));
		subscription.setProperty(new Triplet( "BusId", 4, "=" ));
		subscription.setProperty(new Triplet( "BusId", 5, "=" ));
		subscription.setProperty(new Triplet( "BusId", 6, "=" ));
		subscription.setProperty(new Triplet( "BusId", 7, "=" ));
		subscription.setProperty(new Triplet( "BusId", 8, "=" ));
		subscription.setProperty(new Triplet( "BusId", 9, "=" ));
		subscription.setProperty(new Triplet( "BusId", 10, "=" ));
		subscriber.subscribe(subscription);
	}

}
