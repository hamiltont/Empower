package org.turnerha;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.turnerha.metrics.MetricCalculator;

/**
 * This class is a central location for reporting the results of a simulation.
 * Eventually it should log each simulation output into a separate directory,
 * where the directory contains the meta information regarding how that
 * simulation was setup, and information on how to repeat the simulation, and
 * the simulation output
 * 
 * @author hamiltont
 * 
 */
public class Log {

	private MetricCalculator mMetricCalc;
	private FileWriter mLogFile;
	private static Log instance_ = null;

	public Log(MetricCalculator mc) {
		if (instance_ != null)
			return;

		mMetricCalc = mc;

		try {
			mLogFile = new FileWriter(new File("log.csv"));
			mLogFile
					.append("SystemTime,Simulation Time,Coverage"
							+ ",Accuracy,Usefulness per Reading,Wasted Readings Total,"
							+ "Wasted Due To Coverage, Wasted Due To Accuracy, Wasted Due to Both, Total Number of Readings\n");

		} catch (IOException e) {
			e.printStackTrace();
		}

		instance_ = this;
	}

	public static void log(int simulationTimeInHours) {
		if (instance_ == null)
			throw new IllegalStateException("Has not been initialized");

		try {

			MetricCalculator mc = instance_.mMetricCalc;

			int wastedTotal = mc.getPartiallyUselessReadingsTotalCount();
			final StringBuilder lineBuilder = new StringBuilder(Long
					.toString(System.currentTimeMillis()));
			lineBuilder.append(',');
			lineBuilder.append(simulationTimeInHours).append(',');
			lineBuilder.append(mc.getCoverage()).append(',');
			lineBuilder.append(mc.getAccuracy()).append(',');
			lineBuilder.append(mc.getUsefulnessPerReading()).append(',');
			lineBuilder.append(wastedTotal).append(',');
			lineBuilder.append(mc.getUselessReadingsDueToCoverage())
					.append(',');
			lineBuilder.append(mc.getUselessReadingsDueToAccuracy())
					.append(',');
			lineBuilder.append(mc.getUselessReadingsDueToBoth()).append(',');
			lineBuilder.append(mc.getTotalReadings()).append('\n');

			instance_.mLogFile.append(lineBuilder.toString());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void close() {
		if (instance_ == null)
			throw new IllegalStateException("Has not been initialized");

		try {
			instance_.mLogFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
