package hr.fer.zemris.pus.middleware;

import java.io.Serializable;
import java.security.Key;

public class Certificate implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int ID;
	private Key publicKey;
	private byte[] signature;
	
	public Certificate(int ID, Key publicKey, byte[] signature) {
		this.ID = ID;
		this.publicKey = publicKey;
		this.signature = signature;
	}

	public int getID() {
		return ID;
	}

	public Key getPublicKey() {
		return publicKey;
	}
	
	public byte[] getSignature() {
		return signature;
	}
}
