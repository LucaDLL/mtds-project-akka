package projectMTDS;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import projectMTDS.messages.DecMessage;
import projectMTDS.messages.IncMessage;
import scala.util.Random;

public class Counter {
	private static final int numThreads = 16;
	private static final int numMessages = 1000;

	public static void main(String[] args) throws InterruptedException, IOException {
		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef counter = sys.actorOf(CounterActor.props(), "counter");

		// Send messages from multiple threads in parallel
		final Random r = new Random();
		final ExecutorService exec = Executors.newFixedThreadPool(numThreads);
		for (int i = 0; i < numMessages; i++) {
			exec.submit(() -> counter.tell(new IncMessage(r.nextInt(10)), ActorRef.noSender()));
			exec.submit(() -> counter.tell(new DecMessage(r.nextInt(10)), ActorRef.noSender()));
		}
		// Wait for all messages to be sent and received
		System.in.read();
		exec.shutdown();
		while (!exec.awaitTermination(10, TimeUnit.SECONDS)) {

		}
		sys.terminate();
	}

}
