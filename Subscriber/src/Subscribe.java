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
		subscription.setProperty(new Triplet( "BusId", 100, "<" ));
		subscriber.subscribe(subscription);
	}

}
