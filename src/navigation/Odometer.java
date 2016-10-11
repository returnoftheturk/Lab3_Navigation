/*
 * Odometer.java
 */

package navigation;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends Thread {
	// robot position
	private double x, y, theta;
	private int leftMotorTachoCount, rightMotorTachoCount;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;

	// declare variables
	public static double wheelRadius;
	public static double wheelTrack;
	public static int leftTachoNow;// Current tacho L
	public static int rightTachoNow;// Current tacho R

	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;

	// lock object for mutual exclusion
	private Object lock;

	// default constructor
	public Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double wheelRadius,
			double wheelTrack) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.wheelRadius = wheelRadius;
		this.wheelTrack = wheelTrack;
		this.x = 0.0;
		this.y = 0.0;
		this.theta = 0.0;
		this.leftMotorTachoCount = 0;
		this.rightMotorTachoCount = 0;
		lock = new Object();
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;

		//reset values to start
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		
		// get first tacho value.
		leftMotorTachoCount = leftMotor.getTachoCount();
		rightMotorTachoCount = rightMotor.getTachoCount();

		// the declare variables for the tachometers
		int deltaLeftTacho;
		int deltaRightTacho;
		double deltatLeftRad;
		double deltatRightRad;

		while (true) {
			updateStart = System.currentTimeMillis();
			// get the current tacho count.
			leftTachoNow = leftMotor.getTachoCount();
			rightTachoNow = rightMotor.getTachoCount();

			// find the change in tacho count for each motor 
			deltaLeftTacho = leftTachoNow - leftMotorTachoCount;
			deltatLeftRad = 2.0 * Math.PI * deltaLeftTacho / 360.0;
			deltaRightTacho = rightTachoNow - rightMotorTachoCount;
			deltatRightRad = 2.0 * Math.PI * deltaRightTacho / 360.0;

			// set previous tacho value to current value
			leftMotorTachoCount = leftTachoNow;
			rightMotorTachoCount = rightTachoNow;

			// compute both arclengths
			double arcLengthL = wheelRadius * deltatLeftRad;
			double arcLengthR = wheelRadius * deltatRightRad;

			// find the change in theta
			double deltaTheta = (arcLengthL - arcLengthR) / wheelTrack;

			// calculate the center arclength.
			double deltaCenterArclength = (arcLengthR + arcLengthL) / 2.0;

			synchronized (lock) {
				/**
				 * Don't use the variables x, y, or theta anywhere but here!
				 * Only update the values of x, y, and theta in this block. Do
				 * not perform complex math
				 * 
				 */
				// update theta, x, y. Theta will be in radians here, but
				// displayed in degrees.
				theta += deltaTheta; // TODO replace example value
				
				//wrap theta from 0 to 2pi
				if (theta>6.28){
					theta = theta%6.28;
				}
				if (theta<0){
					theta = 6.28-Math.abs(theta)%6.28;
				}

				double deltaX = deltaCenterArclength * Math.sin(theta);
				double deltaY = deltaCenterArclength * Math.cos(theta);

				x += deltaX;
				y += deltaY;

			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// accessors
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
	}

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}

	/**
	 * @return the leftMotorTachoCount
	 */
	public int getLeftMotorTachoCount() {
		return leftMotorTachoCount;
	}

	/**
	 * @param leftMotorTachoCount
	 *            the leftMotorTachoCount to set
	 */
	public void setLeftMotorTachoCount(int leftMotorTachoCount) {
		synchronized (lock) {
			this.leftMotorTachoCount = leftMotorTachoCount;
		}
	}

	/**
	 * @return the rightMotorTachoCount
	 */
	public int getRightMotorTachoCount() {
		return rightMotorTachoCount;
	}

	/**
	 * @param rightMotorTachoCount
	 *            the rightMotorTachoCount to set
	 */
	public void setRightMotorTachoCount(int rightMotorTachoCount) {
		synchronized (lock) {
			this.rightMotorTachoCount = rightMotorTachoCount;
		}
	}
}