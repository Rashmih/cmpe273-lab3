package edu.sjsu.cmpe.cache.client;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class Client {

	private final static SortedMap<Integer, String> circle = new TreeMap<Integer, String>();
	private static HashFunction hashfunction = Hashing.md5();
	
	private static ArrayList<String> servers = new ArrayList<String>();
	static char[] ch = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'};

	public static void main(String[] args) throws Exception {
		System.out.println("Starting Cache Client...");
		servers.add("http://localhost:3000");
		servers.add("http://localhost:3001");
		servers.add("http://localhost:3002");
		for (int i = 0; i < servers.size(); i++) {
			
			add(servers.get(i), i);
		}

		for (int j = 0; j < 10; j++) {
			
		 
			int bucket = Hashing.consistentHash(Hashing.md5().hashInt(j),
					servers.size());
			String server = get(bucket);
			
			System.out.println("Routed to Server: " + server);
			CacheServiceInterface cache = new DistributedCacheService(server);
			cache.put(j + 1, String.valueOf(ch[j]));
			System.out.println("put(" + (j + 1) + ") : " + String.valueOf(ch[j]));
			String value = cache.get(j + 1);
			System.out.println("get(" + (j + 1) + ") : " + value);

		}

	}
	

	public static void add( String server, int i) {
		
				circle.put(hashfunction.hashLong(i).asInt()+i, server);
		
	
	}

	public static void remove(int key) {
		circle.remove(hashfunction.hashLong(key).asInt());
	}

	public static String get(Object key) {
		if (circle.isEmpty()) {
			return null;
		}
		int hashcode = hashfunction.hashLong((Integer) key).asInt();
		if (!circle.containsKey(hashcode)) {
			SortedMap<Integer, String> tailMap = circle.tailMap(hashcode);
			hashcode = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
		}
		return circle.get(hashcode);
	}
}