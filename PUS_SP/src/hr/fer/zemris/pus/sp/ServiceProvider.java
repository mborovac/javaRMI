package hr.fer.zemris.pus.sp;

import hr.fer.zemris.pus.middleware.AvailableFile;
import hr.fer.zemris.pus.middleware.CRAccess;
import hr.fer.zemris.pus.middleware.Certificate;
import hr.fer.zemris.pus.middleware.SP;
import hr.fer.zemris.pus.middleware.SPAccess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;

public class ServiceProvider implements SPAccess{
	
	private int ID;
	private String name;
	private SPAccess address;
	private CRAccess CR;
	private Certificate CRCertificate;
	private List<AvailableFile> localFiles;
	private List<AvailableFile> CRFiles;
	private Certificate myCertificate;
	private KeyPair myKeys;
	private List<Integer> checkedSPs;
	
	public ServiceProvider(int ID, CRAccess CR, String name, List<AvailableFile> files) {
		if(ID <= 0) {
			throw new IllegalArgumentException("Illegal SP ID!");
		}
		this.ID = ID;
		if(CR == null) {
			throw new IllegalArgumentException("CR is null!");
		}
		this.CR = CR;
		this.name = name;
		try {
			this.CRCertificate = CR.getCertificate();
		} catch (RemoteException e1) {
			System.out.println("Can not get CR certificate, exiting...");
			System.exit(-1);
		}
		this.localFiles = new ArrayList<>(files);
		for(AvailableFile file: this.localFiles) {
			try {
				CR.RegisterFile(file.getID(), file.getName(), file.getAuthor(), 
						file.getShortDesc(), file.getServiceProviderID());
			} catch (RemoteException e) {
				System.out.println("Can not register file " + file.getName());
			}
		}
		try {
			this.myKeys = generateKeyPair();
		} catch (Exception e) {
			System.out.println("Can't generate keys.");
			System.exit(-1);
		}
		try {
			this.myCertificate = CR.createCertificate(this.ID, this.myKeys.getPublic());
		} catch (RemoteException e) {
			System.out.println("Can not get a certificate from CR!");
			System.exit(-1);
		}
		this.checkedSPs = new ArrayList<>();
	}
	
	public static void main(String args[]) throws RemoteException {
		try {
			String CRName = "CR";
			Registry registry = LocateRegistry.getRegistry(args[0]);
			CRAccess CR = (CRAccess) registry.lookup(CRName);
			int SPID = Integer.parseInt(args[1]);
			String SPName = args[2];
			List<AvailableFile> listOfFiles = new ArrayList<>();
			if(SPID == 1) {
				listOfFiles.add(new AvailableFile(1214556, "file1.txt", "MB", "", SPID));
			}
			if(SPID == 2) {
				listOfFiles.add(new AvailableFile(7854244, "file2.txt", "MB", "", SPID));
			}
			ServiceProvider SP = new ServiceProvider(SPID, CR, SPName, listOfFiles);
			SPAccess stub =
		              (SPAccess) UnicastRemoteObject.exportObject(SP, 0);
	        registry.rebind(SPName, stub);
	        SP.address = (SPAccess) registry.lookup(SPName);
	        try {
				if(!CR.RegisterSP(SP.ID, SP.name, SP.address)) {
					System.out.println(SP.name + " was not registered with the CR, exiting...");
					System.exit(-1);
				}
			} catch (RemoteException e) {
				System.out.println("Can not register with CR!");
				System.exit(-1);
			}
	        System.out.println("SP bound");
		} catch (Exception e) {
			System.err.println("SP exception:");
			e.printStackTrace();
		}
	}
	
	public List<String> getAvailableFiles() throws RemoteException {
		List<String> fileNames = new ArrayList<>();
		List<AvailableFile> listOfFiles;
		listOfFiles = CR.getFiles();
		for(AvailableFile file: listOfFiles) {
			fileNames.add(file.getName());
		}
		return fileNames;
	}
	
	public String Download(String fileName) throws Exception {
		if(fileName == null || fileName.isEmpty()) {
			System.out.println("Can not retrieve that file.");
		}
		for(AvailableFile file: localFiles) {
			if(file.getName().equalsIgnoreCase(fileName)) {
				byte[] fileBytes = this.getFile(fileName, this.ID);
				try {
					return new String(decrypt(fileBytes, this.myKeys.getPublic()), "UTF-8");
				} catch (Exception e) {
					System.out.println("Can't decrypt the file!");
					return null;
				}
			}
		}
		try {
			this.CRFiles = this.CR.getFiles();
		} catch (RemoteException e) {
			System.out.println("Can not retrieve files.");
			return null;
		}
		int contactID = 0;
		for(AvailableFile file: this.CRFiles) {
			if(file.getName().equalsIgnoreCase(fileName)) {
				contactID = file.getServiceProviderID();
				break;
			}
		}
		if(contactID == 0) {
			System.out.println("No such file.");
			return null;
		}
		List<SP> SPList;
		try {
			SPList = this.CR.getListOfProviders();
		} catch (RemoteException e) {
			System.out.println("Can not retrieve list of providers.");
			return null;
		}
		SP contactSP = null;
		for(SP provider: SPList) {
			if(provider.getID() == contactID) {
				contactSP = provider;
			}
		}
		if(contactSP == null) {
			System.out.println("Can not find the SP holding the file.");
			return null;
		}
		SPAccess provider = (SPAccess) contactSP.getAddress();
		Certificate otherCertificate;
		try {
			otherCertificate = provider.checkCertificate(myCertificate);
		} catch (RemoteException e) {
			System.out.println("Didn't get the other guy's certificate.");
			return null;
		}
		if(otherCertificate == null) {
			System.out.println("Returned certificate is null.");
			return null;
		}
		Certificate temp = this.checkCertificate(otherCertificate);
		if(temp == null) {
			System.out.println("Other guy's certificate isn't good.");
			return null;
		}
		try {
			byte[] file = provider.getFile(fileName, this.ID);
			return new String(decrypt(file, otherCertificate.getPublicKey()), "UTF-8");
		} catch (Exception e) {
			System.out.println("Couldn't get the requested file.");
			return null;
		}
	}
	
	private static KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		keyGen.initialize(1024, random);
		return keyGen.genKeyPair();
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
	
	private static byte[] decrypt(byte[] source, Key key) throws Exception {
		final Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(source);
	}

	@Override
	public Certificate checkCertificate(Certificate certificate) {
		byte[] signature;
		try {
			signature = decrypt(certificate.getSignature(), this.CRCertificate.getPublicKey());
		} catch (Exception e1) {
			System.out.println("Can not decrypt the digest.");
			return null;
		}
		String digestString = Integer.toString(certificate.getID()) + certificate.getPublicKey();
		byte[] calculatedSignature;
		try {
			calculatedSignature = digest(digestString);
		} catch (Exception e) {
			System.out.println("Can not calculate new digest.");
			return null;
		}
		if(compareByteArrays(signature, calculatedSignature)) {
			this.checkedSPs.add(certificate.getID());
			return this.myCertificate;
		}
		return null;
	}

	@Override
	public byte[] getFile(String fileName, int ID) {
		if(ID != this.ID) {
			boolean checked = false;
			for(int id: checkedSPs) {
				if(id == ID) {
					checked = true;
					break;
				}
			}
			if(!checked) {
				System.out.println("SP requesting the file is not a trusted SP, check it's certificate again!");
				return null;
			}
		}
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(this.name+"Files/"+fileName));
			StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	            sb.append(line);
	            sb.append(System.lineSeparator());
	            line = br.readLine();
	        }
	        br.close();
	        String everything = sb.toString();
			byte[] temp = everything.getBytes();
			return encrypt(temp, this.myKeys.getPrivate());
		} catch (Exception e) {
			System.out.println("Can not get the desired file.");
			return null;
		}
	}
	
	private static boolean compareByteArrays(byte[] array1, byte[] array2) {
		if(array1.length != array2.length) {
			return false;
		} else {
			for(int i = 0; i < array1.length; i++) {
				if(array1[i] != array2[i]) {
					return false;
				}
			}
			return true;
		}
	}
}
