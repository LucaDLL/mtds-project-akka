package resources;

import actors.*;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.AbstractActor.ActorContext;
import akka.cluster.Member;

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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

import org.apache.commons.codec.digest.MurmurHash3;

public class Methods {

    public static InetAddress getLocalHostLANAddress() throws UnknownHostException {
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

    public static String GetMemberAddress(Member member, String suffix) {
        return new String(member.address().toString() + suffix);
    }

    public static String GetMemberUniqueAddress(Member member) {
        /*
            For hashing
        */
        return new String(member.uniqueAddress().toString());
	}
	
	public static UnsignedInteger Hash(String value) {
		return UnsignedInteger.valueOf(UnsignedInts.toString(MurmurHash3.hash32x86(value.getBytes())));
	}

	public static ActorSelection SelectActor(ActorContext context, String path) {
		return context.getSystem().actorSelection(path);
	}
	
	public static NodePointer TargetSelection(TreeSet<NodePointer> nodes, UnsignedInteger value) {
		NodePointer np = new NodePointer("", value);
		return (nodes.higher(np) != null) ? nodes.higher(np) : nodes.first();
	}

	public static boolean IdBelongsToInterval(UnsignedInteger id, UnsignedInteger first, UnsignedInteger second) {
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
			/*
				return true if id == second
			*/
			return id.compareTo(second) == 0;
		}
		/*
			if first > second
		*/
		else {
			/*
				newSecond = second + 2^32
			*/
			UnsignedLong newId = (id.compareTo(second) == -1) ? 
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

	public static Map<UnsignedInteger, String> MapSelector(Map<UnsignedInteger, String> map, UnsignedInteger first, UnsignedInteger second) {
		final Map<UnsignedInteger, String> newMap = new HashMap<>();

		for(Map.Entry<UnsignedInteger,String> entry : map.entrySet()){
			if(IdBelongsToInterval(entry.getKey(), first, second))
				newMap.put(entry.getKey(), entry.getValue());
		}
		
		return newMap;
	}

	public static UnsignedInteger GetCleaningId(TreeSet<NodePointer> nodes, NodePointer np) {
		Object arr[] = (nodes.headSet(np).isEmpty()) ? nodes.toArray() : nodes.headSet(np).toArray();

		int index = arr.length - Consts.REPLICATION_FACTOR;
		
		if(index < 0) {
			arr = nodes.toArray();
			return ((NodePointer) arr[arr.length + index - 1]).getId();
		}
		else
			return ((NodePointer) arr[index]).getId();
	}

	public static UnsignedInteger GetPredId(TreeSet<NodePointer> nodes, NodePointer np) {
		return (nodes.lower(np) == null) ? nodes.last().getId() : nodes.lower(np).getId();
	}

	public static List<String> GetSuccAddresses(TreeSet<NodePointer> nodes, NodePointer np) {
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
