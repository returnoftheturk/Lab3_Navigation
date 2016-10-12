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
	private boolean detected = false;
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

		while (true) {
			// done traveling all the positions, so break
			if (positionIndex >= position.length) {
				break;
			}
			travelTo(position[positionIndex][0], position[positionIndex][1]);
			
			// if there is a obstacle, do wall follower
			if (detectObstacle() == true) {
				// bb controller
				// set local distance variable
				double currentTheta = odometer.getTheta();
				double firstTheta = currentTheta;
				
				this.sensorDistance = sensorDistance;
				// speed of left motor always remains constant
				// if robot is closer to the wall than the lowest allowed band (bandCenter-bandwidth)
				if (sensorDistance < bandCenter - bandwidth) {
					// rotate to the right if too close to the wall
					if (sensorDistance <= sensorDistance) {
						// set the speed to the lower speed
						leftMotor.setSpeed(motorLow + 100);
						rightMotor.setSpeed(motorLow + 100);

						// rotate
						leftMotor.forward(); // Spin left motor forward
						rightMotor.backward(); // spin right motor backward.

					} else {
						// get back into our allowed band.
						leftMotor.setSpeed(motorLow + 100); // set left high
						rightMotor.setSpeed(motorLow); // set right low
						// move away from wall (to the right)

						// move forward
						leftMotor.forward();
						rightMotor.forward();
					}
					// if robot is farther than the highest allowed band (bandCenter + bandwidth)
				} else if (sensorDistance > bandCenter + bandwidth) {
					leftMotor.setSpeed(motorLow + 100); // set left low
					rightMotor.setSpeed(motorHigh); // set right high
					// this will move robot closer to wall

					// move forward
					leftMotor.forward();
					rightMotor.forward();

					// if our robot is inside the band, we just want to move forward.
				} else {
					// set both motor speeds to high
					leftMotor.setSpeed(motorLow + 100);
					rightMotor.setSpeed(motorLow + 100);

					// move forward
					leftMotor.forward();
					rightMotor.forward();
				}
				// if we reach the initial theta, move a bit and travel to the position again
				if (firstTheta == currentTheta) {
					leftMotor.rotate(6);
					rightMotor.rotate(6);
					travelTo(position[positionIndex][0], position[positionIndex][1]);
				}
			}
			
			positionIndex++;
			
			
		}

	}

	public boolean detectObstacle() {
		detected = false;
		if (sensorDistance < 30) {
			detected = true;
		}
		return detected;

	}

	public void travelTo(double x, double y) {

		isNavigating = true;

		double diffTheta = calculateAngle(x, y);
		double distance = calculatesensorDistance(x, y);

		turnTo(diffTheta);

		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		leftMotor.rotate(convertsensorDistance(wheelRadius, distance), true);
		rightMotor.rotate(convertsensorDistance(wheelRadius, distance), false);

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
