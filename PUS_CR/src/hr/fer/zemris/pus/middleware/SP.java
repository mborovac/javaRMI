package hr.fer.zemris.pus.middleware;

import java.io.Serializable;

public class SP implements Serializable  {
	
	private static final long serialVersionUID = 1L;
	private int ID;
	private String name;
	private SPAccess address;
	
	public SP(int ID, String name, SPAccess address) {
		this.ID = ID;
		this.name = name;
		this.address = address;
	}

	public int getID() {
		return ID;
	}

	public String getName() {
		return name;
	}

	public SPAccess getAddress() {
		return address;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ID;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SP)) {
			return false;
		}
		SP other = (SP) obj;
		if (ID != other.ID) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
}
