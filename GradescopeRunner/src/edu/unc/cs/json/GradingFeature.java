package edu.unc.cs.json;

import java.util.List;

public class GradingFeature {
	private final String name;
	private final double score;
	private final double points;
	private final List<GradingTest> tests;
	private final List<String> notes;
	private final List<String> autoNotes;
	private final String message;
	private final String summary;
	private final boolean manual;
	private final boolean extraCredit;
	public GradingFeature(String name, double score, double points, List<GradingTest> tests, List<String> notes, List<String> autoNotes,
			String message, String summary, boolean manual, boolean extraCredit) {
		this.name = name;
		this.score = score;
		this.points = points;
		this.tests = tests;
		this.notes = notes;
		this.autoNotes = autoNotes;
		this.message = message;
		this.summary = summary;
		this.manual = manual;
		this.extraCredit = extraCredit;
	}
	public String getName() {
		return name;
	}
	public double getScore() {
		return score;
	}
	public double getPoints() {
		return points;
	}
	public List<GradingTest> getTests() {
		return tests;
	}
	public List<String> getNotes() {
		return notes;
	}
	public List<String> getAutoNotes() {
		return autoNotes;
	}
	public String getMessage() {
		return message;
	}
	public String getSummary() {
		return summary;
	}
	public boolean isManual() {
		return manual;
	}
	public boolean isExtraCredit() {
		return extraCredit;
	}
}
