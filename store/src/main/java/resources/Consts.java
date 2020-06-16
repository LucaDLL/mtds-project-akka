package resources;

import resources.Consts;

import com.google.common.primitives.UnsignedLong;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class Consts {

	public static Config CONFIG = ConfigFactory.parseFile(new File("conf/application.conf"));
	public static int REPLICATION_FACTOR = 3;
	public static String RPC_ADDRESS = "192.168.1.174";
	public static int RPC_PORT = 9090;
	public static int SEED_PORT = 25251;

	public static final String SYSTEM_NAME = "StoreSystem";
	public static final String SUPERVISOR_ACTOR_NAME = "SupervisorActor";
	public static final String NODE_ACTOR_NAME = "NodeActor";
	public static final String SUPERVISOR_ACTOR_SUFFIX = "/user/SupervisorActor";
	public static final String NODE_ACTOR_SUFFIX = "/user/NodeActor";

	public static final UnsignedLong RING_SIZE = UnsignedLong.valueOf("4294967296"); //2^32
	
}