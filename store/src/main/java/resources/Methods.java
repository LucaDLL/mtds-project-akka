package resources;

import actors.*;
import application.StoreServiceImpl;
import grpc.*;

import akka.actor.AbstractActor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.cluster.Member;
import akka.http.javadsl.*;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

import com.google.common.primitives.UnsignedInts;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.CompletionStage;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

import org.apache.commons.codec.digest.MurmurHash3;

public class Methods {

    public static InetAddress getLocalHostLANAddress() throws UnknownHostException {
		/*
			Method used to retrieve the local IP address.
		*/
		try {
			InetAddress candidateAddress = null;
			// Iterate all NICs (network interface cards)...
			for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
				NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
				// Iterate all IP addresses assigned to each card...
				for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
					InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
					if (!inetAddr.isLoopbackAddress()) {
	
						if (inetAddr.isSiteLocalAddress()) {
							// Found non-loopback site-local address. Return it immediately...
							return inetAddr;
						}
						else if (candidateAddress == null) {
							// Found non-loopback address, but not necessarily site-local.
							// Store it as a candidate to be returned if site-local address is not subsequently found...
							candidateAddress = inetAddr;
							// Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
							// only the first. For subsequent iterations, candidate will be non-null.
						}
					}
				}
			}
			if (candidateAddress != null) {
				// We did not find a site-local address, but we found some other non-loopback address.
				// Server might have a non-site-local address assigned to its NIC (or it might be running
				// IPv6 which deprecates the "site-local" concept).
				// Return this non-loopback candidate address...
				return candidateAddress;
			}
			// At this point, we did not find a non-loopback address.
			// Fall back to returning whatever InetAddress.getLocalHost() returns...
			InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
			if (jdkSuppliedAddress == null) {
				throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
			}
			return jdkSuppliedAddress;
		}
		catch (Exception e) {
			UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
			unknownHostException.initCause(e);
			throw unknownHostException;
		}
	}

	public static ActorSystem startSystem() throws UnknownHostException {
		/*
			Override the configuration of the port
		*/
		final Config supervisorConfig = Consts.CONFIG
			.withValue("akka.remote.artery.canonical.hostname", ConfigValueFactory.fromAnyRef(getLocalHostLANAddress().getHostAddress()))
			.withValue("akka.remote.artery.canonical.port", ConfigValueFactory.fromAnyRef(Consts.SEED_PORT))
			.withValue("akka.cluster.roles", ConfigValueFactory.fromIterable(Collections.singletonList(Consts.SUPERVISOR_ACTOR_NAME)));
		/*
			Create an Akka system
		*/
		return ActorSystem.create(Consts.SYSTEM_NAME, supervisorConfig);
	}

	public static CompletionStage<ServerBinding> run(ActorSystem sys, ActorRef supervisor, String rpcAddress, int rpcPort) throws Exception {
		Materializer mat = ActorMaterializer.create(sys);
		/*
			Instantiate RPC handler
		*/
		StoreService impl = new StoreServiceImpl(supervisor);
		/*
			Start listening on port
		*/
		return Http.get(sys).bindAndHandleAsync(
			StoreServiceHandlerFactory.create(impl, sys),
			ConnectHttp.toHost(rpcAddress, rpcPort),
			mat
		);
	}

	public static void startNode(int port) throws UnknownHostException {
		/*
			Override the configuration of the port
		*/
		final Config nodeConfig = Consts.CONFIG
			.withValue("akka.remote.artery.canonical.hostname", ConfigValueFactory.fromAnyRef(getLocalHostLANAddress().getHostAddress()))
			.withValue("akka.remote.artery.canonical.port", ConfigValueFactory.fromAnyRef(port))
			.withValue("akka.cluster.roles", ConfigValueFactory.fromIterable(Collections.singletonList(Consts.NODE_ACTOR_NAME)));
		/*
			Create an Akka system
		*/
		final ActorSystem system = ActorSystem.create(Consts.SYSTEM_NAME, nodeConfig);
		/*
			Create an actor
		*/
		system.actorOf(NodeActor.props(), Consts.NODE_ACTOR_NAME);
	}

    public static String getMemberAddress(Member member, String suffix) {
		/*
			Given a Member of the cluster, return the address used to send messages to it.
		*/
        return new String(member.address().toString() + suffix);
    }

    public static String getMemberUniqueAddress(Member member) {
		/*
			Given a Member of the cluster, return its uniqueAddress.
            This address is used for hashing.
        */
        return new String(member.uniqueAddress().toString());
	}
	
	public static UnsignedInteger hash(String value) {
		/*
			The 32-bit version of the MurmurHash3 hash function is used.
        */
		return UnsignedInteger.valueOf(UnsignedInts.toString(MurmurHash3.hash32x86(value.getBytes())));
	}

	public static ActorSelection selectActor(ActorContext context, String path) {
		/*
			Given the address of an actor and the context, return the ActorSelection necessary to send messages to it.
        */
		return context.getSystem().actorSelection(path);
	}
	
	public static NodePointer targetSelection(TreeSet<NodePointer> nodes, UnsignedInteger value) {
		/*
			Given a key, return the NodeActor responsible for it.
        */
		NodePointer np = new NodePointer("", value);
		return (nodes.higher(np) != null) ? nodes.higher(np) : nodes.first();
	}

	public static boolean idBelongsToInterval(UnsignedInteger id, UnsignedInteger first, UnsignedInteger second) {
		/*
			if first < second
		*/
		if(first.compareTo(second) == -1) {
			/*
				return true if first < id <= second
			*/
			return id.compareTo(first) == 1 && id.compareTo(second) != 1;
		}
		/*
			if first == second
		*/
		else if(first.compareTo(second) == 0){
			return false;
		}
		/*
			if first > second
		*/
		else {
			/*
				newSecond = second + 2^32
			*/
			UnsignedLong newId = (id.compareTo(second) != 1) ? 
									Consts.RING_SIZE.plus(UnsignedLong.valueOf(id.longValue())) : 
									UnsignedLong.valueOf(id.longValue());

			UnsignedLong newFirst = UnsignedLong.valueOf(first.longValue());
			UnsignedLong newSecond = Consts.RING_SIZE.plus(UnsignedLong.valueOf(second.longValue()));
			/*
				return true if first < id <= second
			*/
			return newId.compareTo(newFirst) == 1 && newId.compareTo(newSecond) != 1;
		}
	}

	public static Map<UnsignedInteger, String> mapSelector(Map<UnsignedInteger, String> map, UnsignedInteger first, UnsignedInteger second) {
		/*
			Given a map of entries, return all the keys that belong to the required interval
		*/
		final Map<UnsignedInteger, String> newMap = new HashMap<>();

		for(Map.Entry<UnsignedInteger,String> entry : map.entrySet()){
			if(idBelongsToInterval(entry.getKey(), first, second))
				newMap.put(entry.getKey(), entry.getValue());
		}
		
		return newMap;
	}

	public static UnsignedInteger getCleaningId(TreeSet<NodePointer> nodes, NodePointer np) {
		/*
			Given a NodePointer, return the ID that, in addition to the NodePointer's own ID, 
			defines the range of keys that the corresponding NodeActor should store.
		*/

		Object arr[];
		int index;

		if(nodes.headSet(np).size() < Consts.REPLICATION_FACTOR){
			arr = nodes.toArray();
			index = arr.length - (Consts.REPLICATION_FACTOR - nodes.headSet(np).size());
		} else{
			arr = nodes.headSet(np).toArray();
			index = arr.length - Consts.REPLICATION_FACTOR;
		}

		return ((NodePointer) arr[index]).getId();
	}

	public static UnsignedInteger getPredId(TreeSet<NodePointer> nodes, NodePointer np) {
		/*
			Get the ID of the predecessor of NodePointer np.
		*/
		return (nodes.lower(np) == null) ? nodes.last().getId() : nodes.lower(np).getId();
	}

	public static List<String> getSuccAddresses(TreeSet<NodePointer> nodes, NodePointer np) {
		/*
			Get the addresses of the R-1 nodes that follow NodePointer np.
		*/
		List<String> succAddresses = new ArrayList<String>();
		Iterator<NodePointer> it = (nodes.tailSet(np, false).isEmpty()) ? nodes.iterator() : nodes.tailSet(np, false).iterator();

		for(int i = 0; i < Consts.REPLICATION_FACTOR - 1; i++) {
			if(!it.hasNext())
				it = nodes.iterator();
			succAddresses.add(it.next().getAddress());
		}

		return succAddresses;
	}
}
