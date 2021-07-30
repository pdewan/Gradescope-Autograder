package edu.unc.cs.json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonGradingManager {
	private static GradescopeVisibility DEFAULT_TEST_VISIBILITY = GradescopeVisibility.VISIBLE;

	public static void write(Grading grading, int runtime, GradescopeVisibility visibility, Path output)
			throws IOException {
		Files.deleteIfExists(output);
		Files.createFile(output);

//		JSONObject result = new JSONObject();
//		result.put("score", grading.getScore());
//		result.put("exection_time", runtime);
//		result.put("visibility", visibility);
//		result.put("tests", buildTests(grading));
		JSONObject result = new JSONObject();
//		"stdout_visibility": "visible"
		result.put("stdout_visibility", "visible");
		result.put("score", grading.getScore() * grading.getLatePenalty());
		result.put("exection_time", runtime);
		result.put("visibility", visibility);
		result.put("output", "score multiplier: " + grading.getLatePenalty());
		result.put("tests", buildTests(grading));

		Files.write(output, result.toString().getBytes());
		System.out.println (output.getFileName()+ " contents: " + result.toString());
	}

	private static JSONArray buildTests(Grading grading) {
		JSONArray tests = new JSONArray();

		int featureNum = 0;
		for (; featureNum < grading.getFeatures().size(); featureNum++) {
			GradingFeature feature = grading.getFeatures().get(featureNum);
			double maxScore = feature.getPoints() / feature.getTests().size();
			for (int testNum = 0; testNum < feature.getTests().size(); testNum++) {
				GradingTest test = feature.getTests().get(testNum);
				tests.put(testToJSON(feature, test, "" + (featureNum + 1) + "." + (testNum + 1), maxScore, false));
			}
		}
		for (int restrictionNum = 0; restrictionNum < grading.getRestrictions().size(); restrictionNum++) {
			GradingFeature restriction = grading.getRestrictions().get(featureNum);
			double maxScore = restriction.getPoints() / restriction.getTests().size();
			for (int testNum = 0; testNum < restriction.getTests().size(); testNum++) {
				GradingTest test = restriction.getTests().get(testNum);
				tests.put(testToJSON(restriction, test, "" + (featureNum + restrictionNum + 1) + "." + (testNum + 1),
						maxScore, false));
			}
		}

		return tests;
	}

//	private static JSONObject testToJSON(GradingFeature feature, GradingTest test, String number, double maxScore, boolean isRestriction) {
//		JSONObject testResult = new JSONObject();
//		testResult.put("score", maxScore*test.getPercentage());
//		testResult.put("max_score", feature.getPoints()/feature.getTests().size());
//
//		testResult.put("name", feature.getName() + " - " + test.getName());
//		testResult.put("number", number);
//		
//		StringBuilder sb = new StringBuilder();
//		test.getNotes().forEach(note -> sb.append(note).append("\n"));
//		testResult.put("output", sb.toString());
//		
//		JSONArray tags = new JSONArray();
//		tags.put(test.isAutoGraded() ? "autograded" : "manual");
//		if (feature.isExtraCredit()) {
//			tags.put("extra credit");
//		}
//		if (isRestriction) {
//			tags.put("restriction");
//		}
//		
//		testResult.put("tags", tags);
//		testResult.put("visibility", DEFAULT_TEST_VISIBILITY);
//		
//		return testResult;
//	}
	private static JSONObject testToJSON(GradingFeature feature, GradingTest test, String number, double maxScore,
			boolean isRestriction) {
		JSONObject testResult = new JSONObject();
		String name = test.getName();
//		System.out.println ("Tests to JSON Start");
		
//		System.out.println("testTJSON  name " + name );

		if (test.knowsRealScore()) {
			testResult.put("score", test.getScore());
			testResult.put("max_score", test.getMaxScore());
		} else {
			testResult.put("score", maxScore * test.getPercentage());
			testResult.put("max_score", feature.getPoints() / feature.getTests().size());
		}

		testResult.put("name", feature.getName() + " - " + test.getName());
		testResult.put("number", number);

		StringBuilder sb = new StringBuilder();
		test.getNotes().forEach(note -> sb.append(note).append("\n"));
		testResult.put("output", sb.toString());

		JSONArray tags = new JSONArray();
		tags.put(test.isAutoGraded() ? "autograded" : "manual");
		if (feature.isExtraCredit()) {
			tags.put("extra credit");
		}
		if (isRestriction) {
			tags.put("restriction");
		}

		testResult.put("tags", tags);
		testResult.put("visibility", DEFAULT_TEST_VISIBILITY);
		
//		System.out.println ("Tests to JSON End");

		return testResult;
	}

	public static Optional<Grading> parse(Path gradingJson) throws IOException {
		if (Files.exists(gradingJson)) {
			StringBuilder sb = new StringBuilder();
			Files.readAllLines(gradingJson).forEach(line -> sb.append(line).append("\n"));
			JSONObject root = new JSONObject(sb.toString());

			List<GradingFeature> features = parseFeatures(root.getJSONArray("featureResults"));
			List<GradingFeature> restrictions = parseFeatures(root.getJSONArray("restrictionResults"));
			String userId = root.getString("userId");
			String commentStr = root.getString("comments");
			double latePenalty = root.getDouble("latePenalty");
			double sourcePoints = root.getDouble("sourcePoints");
			double score = root.getDouble("score");
			String sourceCodeTACommentStr = root.getString("sourceCodeTAComments");

			List<String> comments = Arrays.asList(commentStr.split("\n"));
			List<String> sourceCodeTAComments = Arrays.asList(sourceCodeTACommentStr.split("\n"));
			return Optional.of(new Grading(userId, features, restrictions, latePenalty, sourcePoints, score, comments,
					sourceCodeTAComments));
		} else {
			return Optional.empty();
		}
	}

	private static List<GradingFeature> parseFeatures(JSONArray json) {
		List<GradingFeature> features = new ArrayList<>(json.length());

		for (int i = 0; i < json.length(); i++) {
			JSONObject feature = json.getJSONObject(i);
			List<GradingTest> tests = parseTests(feature.getJSONArray("results"));

			double score = feature.getDouble("score");
			String noteStr = feature.getString("notes");
			String autoNoteStr = feature.getString("autoNotes");
			String message = feature.getString("message");

			List<String> notes = Arrays.asList(noteStr.split("\n"));
			List<String> autoNotes = Arrays.asList(autoNoteStr.split("\n"));

			JSONObject moreData = feature.getJSONObject("target");
			boolean manual = moreData.getBoolean("manual");
			boolean extraCredit = moreData.getBoolean("extraCredit");
			String name = moreData.getString("name");
			String summary = moreData.getString("summary");
			double points = moreData.getDouble("points");

			features.add(new GradingFeature(name, score, points, tests, notes, autoNotes, message, summary, manual,
					extraCredit));
		}

		return features;
	}

//	private static List<GradingTest> parseTests(JSONArray json) {
//		List<GradingTest> tests = new ArrayList<>(json.length());
//
//		for (int i = 0; i < json.length(); i++) {
//			JSONObject test = json.getJSONObject(i);
//			String noteStr = test.getString("notes");
//			double percentage = test.getDouble("percentage");
//			String name = test.getString("name");
//			boolean autoGraded = test.getBoolean("autoGraded");
//			boolean pass = test.getBoolean("pass");
//			boolean partialPass = test.getBoolean("partialPass");
//			boolean fail = test.getBoolean("fail");
//			List<String> notes = Arrays.asList(noteStr.split("\n"));
//			tests.add(new GradingTest(name, percentage, notes, autoGraded, pass, partialPass, fail));
//		}
//
//		return tests;
//	}
	private static List<GradingTest> parseTests(JSONArray json) {
		List<GradingTest> tests = new ArrayList<>(json.length());

		for(int i = 0; i < json.length(); i ++) {
		JSONObject test = json.getJSONObject(i);
		String noteStr = test.getString("notes");
		double percentage = test.getDouble("percentage");
		String name = test.getString("name");
		boolean autoGraded = test.getBoolean("autoGraded");
		boolean pass = test.getBoolean("pass");
		boolean partialPass = test.getBoolean("partialPass");
		boolean fail = test.getBoolean("fail");
		List<String> notes = Arrays.asList(noteStr.split("\n"));
		try {
		double score = test.getDouble("score");
		double maxScore = test.getDouble("maxScore");
//		System.out.println("maxscore " + maxScore + " name " + name );
		tests.add(new GradingTest(name, score, maxScore, notes, autoGraded, pass, partialPass, fail));
		} catch (JSONException e) {
		e.printStackTrace(System.out);
		tests.add(new GradingTest(name, percentage, notes, autoGraded, pass, partialPass, fail));
		}
		
		}
		return tests;
	}

	@SuppressWarnings("unused")
	private static void setDefaultTestVisiblity(GradescopeVisibility visibility) {
		DEFAULT_TEST_VISIBILITY = visibility;
	}
}
