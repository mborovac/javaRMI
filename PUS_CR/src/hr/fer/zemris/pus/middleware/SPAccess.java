package hr.fer.zemris.pus.middleware;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface SPAccess extends Remote {
	
	List<String> getAvailableFiles() throws RemoteException;
	
	String Download(String fileName) throws RemoteException, Exception;
	
	Certificate checkCertificate(Certificate certificate) throws RemoteException, Exception;
	
	byte[] getFile(String fileName, int ID) throws RemoteException;
}
