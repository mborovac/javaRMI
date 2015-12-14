package hr.fer.zemris.pus.middleware;

import java.io.Serializable;

public class AvailableFile implements Serializable  {
	
	private static final long serialVersionUID = 1L;
	private int ID;
	private String name;
	private String author;
	private String shortDesc;
	private int ServiceProviderID;
	
	public AvailableFile(int ID, String name, String author, String shortDesc, 
			int ServiceProviderID) {
		this.ID = ID;
		this.name = name;
		this.author = author;
		this.shortDesc = shortDesc;
		this.ServiceProviderID = ServiceProviderID;
	}

	public int getID() {
		return ID;
	}

	public String getName() {
		return name;
	}

	public String getAuthor() {
		return author;
	}

	public String getShortDesc() {
		return shortDesc;
	}

	public int getServiceProviderID() {
		return ServiceProviderID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		if (!(obj instanceof AvailableFile)) {
			return false;
		}
		AvailableFile other = (AvailableFile) obj;
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
