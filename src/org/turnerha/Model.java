package org.turnerha;

import java.util.List;

import org.turnerha.sensornodes.SensorNode;

public class Model {

	// Aggregate phone properties e.g. mobility, total number of sensor inputs,
	// total accuracy of inputs
	// Real Environment Picture
	// Detected Environment Picture
	// Conformance percentage
	// Simulation version number

	private List<SensorNode> mNodes;

	public Model(List<SensorNode> nodes) {
		mNodes = nodes;
	}

	public void update() {
		for (SensorNode s : mNodes)
			s.update();
	}
	
	public List<SensorNode> getNodes() {
		return mNodes;
	}

}