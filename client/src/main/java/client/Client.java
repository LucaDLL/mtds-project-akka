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
		final ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", 9090).usePlaintext().build();
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
			} else throw new Exception("Wrong input");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

/*
class Client {

	public static void main(String[] args) {
		final Scanner scanner = new Scanner(System.in);
		final ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", 9090).usePlaintext().build();
		final StoreServiceBlockingStub blockingStub = StoreServiceGrpc.newBlockingStub(channel);

		while (true) {
			final String line = scanner.nextLine();
			final String[] words = line.split(" ");

			if (words[0].equalsIgnoreCase("put")) {
				final String key = words[1];
				final String val = words[2];

				try {
					PutRequest msg = PutRequest.newBuilder().setKey(key).setValue(val).build();
					PutReply reply = blockingStub.put(msg);
					System.out.println(reply.toString());
				} catch (StatusRuntimeException e) {
					System.out.println(e.toString());
				}

			} else if (words[0].equalsIgnoreCase("get")) {
				final String key = words[1];

				try {
					GetRequest msg = GetRequest.newBuilder().setKey(key).build();
					GetReply reply = blockingStub.get(msg);
					System.out.println(reply.toString());
				} catch (StatusRuntimeException e) {
					System.out.println(e.toString());
				}

			} else if (words[0].equalsIgnoreCase("quit")) {
				break;
			} else {
				System.out.println("Unknown command");
			}
		}

		try {
			channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
			scanner.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}
*/