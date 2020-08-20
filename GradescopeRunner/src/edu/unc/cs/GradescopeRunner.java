package edu.unc.cs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import edu.unc.cs.json.GradescopeVisibility;
import edu.unc.cs.json.Grading;
import edu.unc.cs.json.JsonGradingManager;


public class GradescopeRunner {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Please provide source and destination json files");
			System.exit(-1);
		}
		Path source = Paths.get(args[0]);
		Path dest = Paths.get(args[1]);
		
		try {
			Files.createDirectories(dest.subpath(0, dest.getNameCount() - 1));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			Optional<Grading> grading = JsonGradingManager.parse(source);
			if (grading.isPresent()) {
				JsonGradingManager.write(grading.get(), 0, GradescopeVisibility.VISIBLE, dest);
			} else {
				System.err.println("source grading json file not found");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
