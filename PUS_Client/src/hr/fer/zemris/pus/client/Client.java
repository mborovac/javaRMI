package hr.fer.zemris.pus.client;

import hr.fer.zemris.pus.middleware.SPAccess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class Client {
	
	public static void main(String[] args) {
		String name = args[1];
		String SPName = args[2];
		Registry registry;
		SPAccess SP = null;
		try {
			registry = LocateRegistry.getRegistry(args[0]);
			SP = (SPAccess) registry.lookup(SPName);
		} catch (Exception e) {
			System.out.println("Can't find registry, exiting...");
			System.exit(-1);
		}
		BufferedReader br = new BufferedReader( new InputStreamReader(System.in));
		System.out.println("Welcome " + name);
		while(true) {
			System.out.println("Type \"getFiles\" for a list of available files, \"download FILE_NAME\"" +
					"to download a file:");
			String string;
			try {
				string = br.readLine();
			} catch (IOException e) {
				System.out.println("An error occured...");
				continue;
			}
			if(string.matches("getFiles")) {
				List<String> availableFiles;
				try {
					availableFiles = SP.getAvailableFiles();
				} catch (RemoteException e) {
					System.out.println("Can not access available files at this moment.");
					continue;
				}
				System.out.println("Available files:");
				for(String file: availableFiles) {
					System.out.println(file);
				}
			} else if(string.matches("download .+")) {
				String fileContent;
				try {
					String substring = string.substring(9);
					fileContent = SP.Download(substring);
				} catch (RemoteException e) {
					System.out.println("Can not download the file.");
					continue;
				}
				if(fileContent == null) {
					System.out.println("File is currenty unavailable.");
				} else {
					System.out.println("File contents:\n" + fileContent);
				}
			} else if(string.matches("exit")) {
				System.out.println("Exiting...");
				break;
			} else {
				System.out.println("Unknown command.");
				continue;
			}
		}
	}
}
