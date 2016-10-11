package navigation;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

public class Navigate extends Thread {
	
	private double currentX;
	private double currentY;
	private double currentTheta;
	private double destinationX;
	private double destinationY;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	//private EV3UltrasonicSensor usSensor;
	private Odometer odometer;
	private double wheelRadius;
	private double track;
	private Position[] destination;
	private boolean isNavigating = false;
	
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;
	
	public Navigate(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Odometer odometer, double wheelRadius, double track, Position[] destination){
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		//this.usSensor = usSensor;
		this.odometer = odometer;
		this.wheelRadius = wheelRadius;
		this.track = track;
		this.destination = destination;
		
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }) {
			motor.stop();
			motor.setAcceleration(200);
		}
		
	}
	
	public void run(){
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// there is nothing to be done here because it is not expected that
			// the odometer will be interrupted by another thread
		}
		for (int i = 0; i<destination.length; i++){
			travelTo(destination[i]);
		}
	}
	
	public void travelTo(Position position){
		isNavigating = true;
		
		double diffTheta = calculateAngle(position);
		double distance = calculateDistance(position);
		
		double errorTolerance = 3;
		
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		
		leftMotor.rotate(convertDistance(wheelRadius, distance), true);
		rightMotor.rotate(convertDistance(wheelRadius, distance), true);
		
		
		while (Math.abs(diffTheta)>0||distance<errorTolerance){
			
			turnTo(diffTheta);
			distance = calculateDistance(position);
			diffTheta = calculateAngle(position);

			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			
			leftMotor.rotate(convertDistance(wheelRadius, distance), true);
			rightMotor.rotate(convertDistance(wheelRadius, distance), true);
			
			
		}
		
		
		isNavigating = false;
		
		
	}
	
	public void turnTo(double theta){
		isNavigating = true;
		
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		leftMotor.rotate(convertAngle(wheelRadius, track, theta), true);
		rightMotor.rotate(convertAngle(wheelRadius, track, theta), false);
		
		isNavigating = false;
	}
	
	public boolean isNavigating(){
		return isNavigating;
	}
	
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	private double calculateAngle(Position position){
		double thetad = 0;
		double diffX = position.getX() - odometer.getX();
		double diffY = position.getY() - odometer.getY();
		
		if (diffY!=0){
			if (diffY>0){
				thetad = Math.atan(diffX/diffY);
			}
			else if (diffY<0){
				if (diffX<0)
					thetad = Math.atan(diffX/diffY) - Math.PI;
				if (diffX>0)
					thetad = Math.atan(diffX/diffY) + Math.PI;
			}
		} else if (diffY==0){
			if (diffX>0)
				thetad = Math.PI/2;
			if (diffX<0)
				thetad = -Math.PI/2;
			if (diffX==0)
				thetad = 0;
		}
		
		double thetar = odometer.getTheta();
		
		double diffTheta = thetad - thetar;
		
		if (diffTheta<-Math.PI/2)
			diffTheta += 2* Math.PI;
		if (diffTheta>Math.PI/2)
			diffTheta -= 2* Math.PI;
		
		return diffTheta;
		
	}
	
	private double calculateDistance(Position position){
		double diffX = position.getX() - odometer.getX();
		double diffY = position.getY() - odometer.getY();
		
		double distance = Math.sqrt(Math.pow(diffX, 2)+Math.pow(diffY, 2));
		
		return distance;
	}

}
