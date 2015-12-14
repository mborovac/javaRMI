package hr.fer.zemris.pus.middleware;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.Key;
import java.util.List;

public interface CRAccess extends Remote {
	
	List<SP> getListOfProviders() throws RemoteException;
	
	List<AvailableFile> getFiles() throws RemoteException;
	
	boolean RegisterSP(int ID, String name, SPAccess address) throws RemoteException;
	
	boolean RegisterFile(int ID, String name, String author, String shortDesc, 
			int ServicePproviderID) throws RemoteException;
	
	Certificate getCertificate() throws RemoteException;
	
	Certificate createCertificate(int ID, Key key) throws RemoteException;
}
