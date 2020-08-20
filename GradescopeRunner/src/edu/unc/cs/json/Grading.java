package edu.unc.cs.json;

import java.util.List;

public class Grading {
	private final String userId;
	private final List<GradingFeature> features;
	private final List<GradingFeature> restrictions;
	private final double latePenalty;
	private final double sourcePoints;
	private final double score;
	private final List<String> comments;
	private final List<String> sourceCodeTAComments;
	
	public Grading(String userId, List<GradingFeature> features, List<GradingFeature> restrictions, double latePenalty,
			double sourcePoints, double score, List<String> comments, List<String> sourceCodeTAComments) {
		this.userId = userId;
		this.features = features;
		this.restrictions = restrictions;
		this.latePenalty = latePenalty;
		this.sourcePoints = sourcePoints;
		this.score = score;
		this.comments = comments;
		this.sourceCodeTAComments = sourceCodeTAComments;
	}

	public String getUserId() {
		return userId;
	}

	public List<GradingFeature> getFeatures() {
		return features;
	}

	public List<GradingFeature> getRestrictions() {
		return restrictions;
	}

	public double getLatePenalty() {
		return latePenalty;
	}

	public double getSourcePoints() {
		return sourcePoints;
	}

	public double getScore() {
		return score;
	}

	public List<String> getComments() {
		return comments;
	}

	public List<String> getSourceCodeTAComments() {
		return sourceCodeTAComments;
	}
	
	
}
