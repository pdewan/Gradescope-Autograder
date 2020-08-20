package edu.unc.cs.json;

import java.util.List;

public class GradingTest {
	private final String name;
	private final double percentage;
	private final List<String> notes;
	private final boolean autoGraded;
	private final boolean pass;
	private final boolean paritalPass;
	private final boolean fail;
	
	public GradingTest(String name, double percentage, List<String> notes, boolean autoGraded, boolean pass,
			boolean paritalPass, boolean fail) {
		this.name = name;
		this.percentage = percentage;
		this.notes = notes;
		this.autoGraded = autoGraded;
		this.pass = pass;
		this.paritalPass = paritalPass;
		this.fail = fail;
	}

	public String getName() {
		return name;
	}

	public double getPercentage() {
		return percentage;
	}

	public List<String> getNotes() {
		return notes;
	}

	public boolean isAutoGraded() {
		return autoGraded;
	}

	public boolean isPass() {
		return pass;
	}

	public boolean isParitalPass() {
		return paritalPass;
	}

	public boolean isFail() {
		return fail;
	}
	
	
}
