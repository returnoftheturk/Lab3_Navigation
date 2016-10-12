package navigation;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

public class NavigateObstacles extends Thread implements UltrasonicController {
	
	private double currentX;
	private double currentY;
	private double currentTheta;
	private double destinationX;
	private double destinationY;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private EV3UltrasonicSensor usSensor;
	// private EV3UltrasonicSensor usSensor;
	private Odometer odometer;
	private double wheelRadius;
	private double track;
	private boolean isNavigating = false;
	private boolean isDriving = false;
	// Added a filter from P controller so that the robot could avoid gaps.
	private final int FILTER_OUT = 25;
	private int filterControl = 0;
	private int bandCenter, bandwidth, minsensorDistance;
	private int motorLow, motorHigh;
	private int sensorDistance;

	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;

	public NavigateObstacles(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Odometer odometer,
			double wheelRadius, double track) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		// this.usSensor = usSensor;
		this.odometer = odometer;
		this.wheelRadius = wheelRadius;
		this.track = track;
		minsensorDistance = 1;
		bandCenter = 28;
		bandwidth = 3;
		motorLow = 100;
		motorHigh = 300;
	}

	public void run() {
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }) {
			motor.stop();
			motor.setAcceleration(200);
		}

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// there is nothing to be done here because it is not expected that
			// the odometer will be interrupted by another thread
		}

		int[][] position = { { 0, 60 }, { 60, 0 } };
		int positionIndex = 0;
		
		isDriving = true;
		

		for (int i =0; i<position.length; i++){		
			travelTo(position[positionIndex][0], position[positionIndex][1]);
		}

	}

	public void travelTo(double x, double y) {

		isNavigating = true;

		double diffTheta = calculateAngle(x, y);
		double sensorDistance = calculatesensorDistance(x, y);

		turnTo(diffTheta);

		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		leftMotor.rotate(convertsensorDistance(wheelRadius, sensorDistance), true);
		rightMotor.rotate(convertsensorDistance(wheelRadius, sensorDistance), false);

		isNavigating = false;

	}

	public void turnTo(double theta) {
		isNavigating = true;

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		leftMotor.rotate(convertAngle(wheelRadius, track, theta * 180 / Math.PI), true);
		rightMotor.rotate(-convertAngle(wheelRadius, track, theta * 180 / Math.PI), false);

		isNavigating = false;
	}

	public boolean isNavigating() {
		return isNavigating;
	}

	private static int convertsensorDistance(double radius, double sensorDistance) {
		return (int) ((180.0 * sensorDistance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertsensorDistance(radius, Math.PI * width * angle / 360.0);
	}

	private double calculateAngle(double x, double y) {
		double thetad = 0;
		double diffX = x - odometer.getX();
		double diffY = y - odometer.getY();

		if (diffY != 0) {
			if (diffY > 0) {
				thetad = Math.atan(diffX / diffY);
				if (diffX == 0)
					thetad = 0;
			}

			else if (diffY < 0) {
				if (diffX < 0)
					thetad = Math.atan(diffX / diffY) - Math.PI;
				if (diffX > 0)
					thetad = Math.atan(diffX / diffY) + Math.PI;
				if (diffX == 0)
					thetad = Math.PI;
			}
		} else if (diffY == 0) {
			if (diffX > 0)
				thetad = Math.PI / 2;
			if (diffX < 0)
				thetad = -Math.PI / 2;
			if (diffX == 0)
				thetad = 0;
		}

		double thetar = odometer.getTheta();

		double diffTheta = thetad - thetar;

		if (diffTheta < -Math.PI)
			diffTheta += 2 * Math.PI;
		if (diffTheta > Math.PI)
			diffTheta -= 2 * Math.PI;

		return diffTheta;

	}

	private double calculatesensorDistance(double x, double y) {
		double diffX = x - odometer.getX();
		double diffY = y - odometer.getY();

		double sensorDistance = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));

		return sensorDistance;
	}

	@Override
	public void processUSData(int distance) {
		// TODO Auto-generated method stub
		sensorDistance = distance;

	}
	

	@Override
	public int readUSDistance() {
		// TODO Auto-generated method stub
		return this.sensorDistance;
	}

}
