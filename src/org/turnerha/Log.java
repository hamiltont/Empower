package org.turnerha;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.turnerha.environment.MetricCalculator;

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
			mLogFile.append("SystemTime,Simulation Time,Coverage"
					+ ",Accuracy,Usefulness per Reading\n");

		} catch (IOException e) {
			e.printStackTrace();
		}

		instance_ = this;
	}

	public static void log(int simulationTimeInHours) {
		if (instance_ == null)
			throw new IllegalStateException("Has not been initialized");

		try {
			instance_.mLogFile
					.append(Long.toString(System.currentTimeMillis())).append(
							',')
					.append(Integer.toString(simulationTimeInHours))
					.append(',').append(
							Double
									.toString(instance_.mMetricCalc
											.getCoverage())).append(',')
					.append(
							Double
									.toString(instance_.mMetricCalc
											.getAccuracy())).append(',')
					.append(
							Double.toString(instance_.mMetricCalc
									.getUsefulnessPerReading())).append('\n');
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
