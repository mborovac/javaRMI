package hr.fer.zemris.pus.cr;

import hr.fer.zemris.pus.middleware.AvailableFile;
import hr.fer.zemris.pus.middleware.CRAccess;
import hr.fer.zemris.pus.middleware.Certificate;
import hr.fer.zemris.pus.middleware.SP;
import hr.fer.zemris.pus.middleware.SPAccess;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;

public class CentralRegistry implements CRAccess {

	private int ID;
	private Set<SP> listOfProviders;
	private Set<AvailableFile> files;
	private Map<Integer, Certificate> certificates;
	private Certificate myCertificate;
	private PrivateKey myPrivateKey;
	
	public CentralRegistry() {
		this.ID = 0;
		this.listOfProviders = new HashSet<>();
		this.files = new HashSet<>();
		this.certificates = new HashMap<>();
		KeyPair keys;
		int counter = 0;
		while(true) {
			try {
				keys = generateKeyPair();
				break;
			} catch (Exception e) {
				counter++;
				if(counter >= 10) {
					System.out.println("Can not generate keys! Exiting...");
					System.exit(-1);
				}
			}
		}
		this.myPrivateKey = keys.getPrivate();
		counter = 0;
		while(true) {
			try {
				this.myCertificate = new Certificate(this.ID, keys.getPublic(), 
						encrypt(digest(Integer.toString(this.ID) + keys.getPublic()), this.myPrivateKey));
				break;
			} catch (Exception e) {
				counter++;
				if(counter >= 10) {
					System.out.println("Can not generate my certificate! Exiting...");
					System.exit(-1);
				}
			}
		}
	}
	
	public static void main(String[] args) {
      try {
          String name = "CR";
          CRAccess CR = new CentralRegistry();
          CRAccess stub =
              (CRAccess) UnicastRemoteObject.exportObject(CR, 0);
          Registry registry = LocateRegistry.getRegistry();
          registry.rebind(name, stub);
          System.out.println("CR bound");
      } catch (Exception e) {
          System.err.println("CR exception:");
          e.printStackTrace();
      }
  }
	
	public List<SP> getListOfProviders() {
		return new ArrayList<>(listOfProviders);
	}
	
	public List<AvailableFile> getFiles() {
		return new ArrayList<>(files);
	}
	
	public boolean RegisterSP(int ID, String name, SPAccess address) {
		if(ID <= 0) {
			return false;
		}
		if(name == null || name.isEmpty()) {
			return false;
		}
		if(address == null) {
			return false;
		}
		return listOfProviders.add(new SP(ID, name, address));
	}
	
	public boolean RegisterFile(int ID, String name, String author,
			String shortDesc, int ServicePproviderID) {
		if(ID <= 0) {
			return false;
		}
		if(name == null || name.isEmpty()) {
			return false;
		}
		if(author == null || author.isEmpty()) {
			return false;
		}
		if(shortDesc == null) {
			shortDesc = "";
		}
		if(ServicePproviderID <= 0) {
			return false;
		}
		return files.add(new AvailableFile(ID, name, author, shortDesc, ServicePproviderID));
	}
	
	public Certificate getCertificate() {
		return this.myCertificate;
	}

	public Certificate createCertificate(int ID, Key key) {
		if(ID < 0) {
			return null;
		}
		Certificate certificate;
		int counter = 0;
		while(true) {
			try {
				certificate = new Certificate(ID, key, 
						encrypt(digest(Integer.toString(ID) + key), this.myPrivateKey));
				break;
			} catch (Exception e) {
				counter++;
				if(counter >= 10) {
					return null;
				}
			}
		}
		certificates.put(ID, certificate);
		return certificate;
	}
	
	private static byte[] digest(String source) throws IOException, NoSuchAlgorithmException {
		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		return sha.digest(source.getBytes());
	}
	
	private static byte[] encrypt(byte[] source, Key key) throws Exception {
		final Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(source);
	}
	
	private KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		keyGen.initialize(1024, random);
		return keyGen.genKeyPair();
	}
}
