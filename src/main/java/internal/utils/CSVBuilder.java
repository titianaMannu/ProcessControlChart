package main.java.internal.utils;

import scala.Tuple2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Logger;



public class CSVBuilder {

	/**
	 * private constructor
	 */
	private CSVBuilder() {
		super();
	}

	/*
	 * This method generates a csv file containing for each month of project
	 * development the corresponding commits of the searched type
	 */
	public static void generateCommitCsv(Map<YearMonth, Tuple2<Integer, Integer>> mapToSaveOnCsv) {
		String header = "Date,Nr. fixed commit,Nr. commit";
		File out = new File("data-" + JSONConfig.getProjectName() + ".csv");
		DateTimeFormatter f = DateTimeFormatter.ofPattern("y-MM");
		try (FileOutputStream fos = new FileOutputStream(out);
			 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))) {
			bw.write(header);
			bw.newLine();
			for (Map.Entry<YearMonth, Tuple2<Integer, Integer>> entry : mapToSaveOnCsv.entrySet()) {
				String key = entry.getKey().format(f);
				Tuple2<Integer, Integer> value = entry.getValue();
				bw.write(key + "," + value._1.toString() + "," + value._2.toString());
				bw.newLine();

			}

		} catch (Exception e) {
			Logger.getGlobal().info(e.toString());
		}
	}
}