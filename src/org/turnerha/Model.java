package org.turnerha;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.turnerha.environment.impl.ImageBackedPerceivedEnvironment;
import org.turnerha.environment.impl.ImageBackedRealEnvironment;
import org.turnerha.geography.GeoLocation;
import org.turnerha.geography.KmlGeography;
import org.turnerha.geography.KmlReader;
import org.turnerha.geography.Projection;
import org.turnerha.policys.collection.ConstantDataCollection;
import org.turnerha.policys.movement.ProbabilisticMovementPolicy;
import org.turnerha.sensornodes.SensorNode;
import org.turnerha.sensornodes.SmartPhone;
import org.turnerha.server.Server;

/**
 * Holds all data for the running simulation, including all SensorNodes, the
 * Server (internally includes the PerceivedEnvironment), the RealEnvironment,
 * etc.
 * 
 * @author hamiltont
 * 
 */
public class Model {

	// Aggregate phone properties e.g. mobility, total number of sensor inputs,
	// total accuracy of inputs
	// Real Environment Picture
	// Detected Environment Picture
	// Conformance percentage
	// Simulation version number

	private static Model instance_;
	private List<SensorNode> mNodes = new ArrayList<SensorNode>();
	private KmlGeography mKml;
	private Server mServer;
	private ImageBackedRealEnvironment mRealEnv;
	private Random mRandom;

	public static Model getInstance() {
		return instance_;
	}

	public Model(KmlGeography geo) {
		mKml = geo;
		mRandom = new Random();
		instance_ = this;
	}

	public void buildServer() {
		mServer = new Server();
	}

	public Server getServer() {
		return mServer;
	}

	public ImageBackedRealEnvironment getRealEnvironment() {
		return mRealEnv;
	}

	public ImageBackedPerceivedEnvironment getPerceivedEnvironment() {
		return (ImageBackedPerceivedEnvironment) mServer
				.getPerceivedEnvironment();
	}

	public void setNodes(List<SensorNode> nodes) {
		mNodes = nodes;
	}

	/**
	 * @param kmlFile
	 *            A {@link File} that points to some KML data
	 * @return true if the {@link File} was able to be read and loaded as a
	 *         {@link KmlGeography}, false otherwise
	 */
	public void setKml(KmlGeography kmlgeo) {
		mKml = kmlgeo; // TODO If the ModelView is caching a rendering of KmlGeo
		// in a buffer, we should tell it to clear
	}

	public static KmlGeography getGeo(File kmlFile) {
		KmlReader reader = new KmlReader();
		if (false == reader.read(kmlFile))
			throw new IllegalStateException("Couldn't create KML");

		KmlGeography geo = new KmlGeography();
		geo.init(reader.getPoly(), reader.mTopRight, reader.mBottomLeft);

		// TODO If the ModelView is caching a rendering of KmlGeo in
		// a buffer, we should tell it to clear

		return geo;
	}

	public KmlGeography getKml() {
		return mKml;
	}

	public void update() {
		for (SensorNode s : mNodes)
			s.update();
	}

	public List<SensorNode> getNodes() {
		return mNodes;
	}

	public void updateNodeCount(int desiredNodeCount) {
		// TODO Pop up dialog asking if the user desires an immediate
		// transition, or a slow transition
		// TODO Build a progress dialog if they want immediate

		if (mNodes.size() == desiredNodeCount)
			return;
		else if (mNodes.size() < desiredNodeCount) {
			// TODO implement a distribution policy
			Projection p = ModelView.getInstance().getDefaultProjection();
			Rectangle bounds = ModelView.getInstance().getBounds();

			while (mNodes.size() < desiredNodeCount) {
				// TODO ensure this works
				GeoLocation loc;
				do {
					loc = p.getLocationAt(new Point(mRandom
							.nextInt(bounds.width)
							+ bounds.x, mRandom.nextInt(bounds.height)
							+ bounds.y));
				} while (false == getKml().contains(loc));

				SmartPhone s = new SmartPhone(loc,
						new ConstantDataCollection(),
						new ProbabilisticMovementPolicy(1, mRandom), mRandom);
				mNodes.add(s);
			}
		} else if (mNodes.size() > desiredNodeCount) {
			// TODO create some sort of fair removal
			mNodes = mNodes.subList(0, desiredNodeCount - 1);
		}
	}
}