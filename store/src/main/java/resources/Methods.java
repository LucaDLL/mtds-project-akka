package resources;

import actors.*;

import akka.actor.ActorSystem;
import akka.cluster.Member;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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
	
	public static int Hash(String value) {
		return MurmurHash3.hash32x86(value.getBytes());
	}

	public static boolean idBelongsToInterval(Integer id, Integer first, Integer second) {
		/*
			if first < second
		*/
		if(Integer.compareUnsigned(first, second) == -1)
			return Integer.compareUnsigned(id, first) == 1 && Integer.compareUnsigned(id, second) != -1;
		/*
			if first == second
		*/
		else if(Integer.compareUnsigned(first, second) == 0)
			return Integer.compareUnsigned(id, first) == 0;
		/*
			if first > second
		*/
		else{
			Long newId = new Long(id);
			Long newFirst = Long.sum(first, Integer.MAX_VALUE);
			if (Integer.compareUnsigned(id, second) == -1)
				newId = (long) (id + Integer.MAX_VALUE);
			return Long.compareUnsigned(newId, second) == 1 && Long.compareUnsigned(newId, newFirst) != -1;
		}
	}

	public static Map<Integer, String> MapScan(Map<Integer, String> map, Integer first, Integer second) {
		final Map<Integer, String> newMap = new HashMap<>();

		for(Map.Entry<Integer,String> entry : map.entrySet()){
			if(idBelongsToInterval(entry.getKey(), first, second))
				newMap.put(entry.getKey(), entry.getValue());
		}
		
		return newMap;
	}	

}