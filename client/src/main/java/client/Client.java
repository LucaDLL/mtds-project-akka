package client;

import grpc.*;
import grpc.StoreServiceGrpc.*;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;
import java.io.File;
import java.util.Scanner;

class Client {
	
	public static void main(String[] args) {
		final ManagedChannel channel = ManagedChannelBuilder.forAddress("192.168.1.174", 9090).usePlaintext().build();
		final StoreServiceBlockingStub blockingStub = StoreServiceGrpc.newBlockingStub(channel);

		try {
			if (args[0].equals("init") && args.length == 1) {
				Scanner keys = new Scanner(new File("src/main/resources/keys.txt"));
				Scanner values = new Scanner(new File("src/main/resources/values.txt"));
				
				while (keys.hasNext() && values.hasNext()){
					final String key = keys.next();
					final String val = values.next();
					PutRequest msg = PutRequest.newBuilder().setKey(key).setValue(val).build();
					blockingStub.put(msg);
				}
				keys.close();
				values.close();

				channel.shutdownNow().awaitTermination(2, TimeUnit.SECONDS);
			}

			else if (args[0].equals("put") && args.length == 3) {
				final String key = args[1];
				final String val = args[2];
				PutRequest msg = PutRequest.newBuilder().setKey(key).setValue(val).build();

				PutReply reply = blockingStub.put(msg);
				System.out.println(reply.toString());
				
				channel.shutdownNow().awaitTermination(2, TimeUnit.SECONDS);
			} else if (args[0].equals("get") && args.length == 2) {
				final String key = args[1];
				GetRequest msg = GetRequest.newBuilder().setKey(key).build();
				
				GetReply reply = blockingStub.get(msg);
				System.out.println(reply.toString());
				
				channel.shutdownNow().awaitTermination(2, TimeUnit.SECONDS);
			} else if (args[0].equals("debug") && args.length == 1) {
				DebugRequest msg = DebugRequest.newBuilder().setMessage("Debug").build();
	
				DebugReply reply = blockingStub.debug(msg);
				System.out.println(reply.toString());
				
				channel.shutdownNow().awaitTermination(2, TimeUnit.SECONDS);
			} else throw new Exception("Wrong input");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}