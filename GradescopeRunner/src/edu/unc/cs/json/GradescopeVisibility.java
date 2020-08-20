package edu.unc.cs.json;

public enum GradescopeVisibility {
	HIDDEN, AFTER_DUE_DATE, AFTER_PUBLISHED, VISIBLE;
	
	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
