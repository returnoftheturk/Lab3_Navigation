package navigation;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class NavigateObstacles extends Thread implements UltrasonicController{

	private final int bandCenter = 25, bandwidth = 5;
	private final int motorConstant = 150, motorDelta = 150, FILTER_OUT = 32;
	private int distanceUS;
	private int distanceError;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	//private EV3UltrasonicSensor usSensor;
	private Odometer odometer;
	private double wheelRadius;
	private double track;
	private boolean isNavigating = false;
	private int filterControl;
	private boolean seesWall = false;
	private Object lock;
	private int firstTime = 0;
	private double thetaBeforeBlock;
	
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;
	
	public NavigateObstacles(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Odometer odometer, double wheelRadius, double track){
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		//this.usSensor = usSensor;
		this.odometer = odometer;
		this.wheelRadius = wheelRadius;
		this.track = track;		
		lock = new Object();
	}
	
	public void run(){
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
		
		travelTo(0, 60);
		travelTo(60, 0);
	}
	
	public void travelTo(double x, double y){
		synchronized(lock){
			
		isNavigating = true;
		
		double diffTheta = calculateAngle(x, y);
		double distance = calculateDistance(x, y);
		
		turnTo(diffTheta);
		
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		
		leftMotor.rotate(convertDistance(wheelRadius, distance), true);
		rightMotor.rotate(convertDistance(wheelRadius, distance), true);
		
		boolean atDestination = false;
		double currentTheta = odometer.getTheta();
		while (!atDestination){
			if (calculateDistance(x,y)<3){
				atDestination = true;
				try{
					Thread.sleep(1000); 
				}
				catch(Exception e){}
			}
			
			
			
		}
		}
		
//		isNavigating = false;
		
		
	}
	
	public void turnTo(double theta){
		isNavigating = true;
		
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		leftMotor.rotate(convertAngle(wheelRadius, track, theta*180/Math.PI), true);
		rightMotor.rotate(-convertAngle(wheelRadius, track, theta*180/Math.PI), false);
		
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
	
	private double calculateAngle(double x, double y){
		double thetad = 0;
		double diffX = x - odometer.getX();
		double diffY = y - odometer.getY();
		
		if (diffY!=0){
			if (diffY>0){
				thetad = Math.atan(diffX/diffY);
				if (diffX==0)
					thetad = 0;
			}
			
			else if (diffY<0){
				if (diffX<0)
					thetad = Math.atan(diffX/diffY) - Math.PI;
				if (diffX>0)
					thetad = Math.atan(diffX/diffY) + Math.PI;
				if (diffX==0)
					thetad = Math.PI;
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
		
		if (diffTheta<-Math.PI)
			diffTheta += 2* Math.PI;
		if (diffTheta>Math.PI)
			diffTheta -= 2* Math.PI;
		
		return diffTheta;
		
	}
	
	private double calculateDistance(double x, double y){
		double diffX = x - odometer.getX();
		double diffY = y - odometer.getY();
		
		double distance = Math.sqrt(Math.pow(diffX, 2)+Math.pow(diffY, 2));
		
		return distance;
	}
	
	@Override
	public void processUSData(int distance) {
		this.distanceUS = distance;		
		
		// TODO: process a movement based on the us distance passed in (BANG-BANG style)
		distanceError = bandCenter - this.distanceUS;
		// if close to the wall, rotate to the right in place
//		if (this.distanceUS<20){
//			
//			seesWall = true;
//			
//			
//		}
		// if the robot is within the band move towards the wall
		if (firstTime>0){
			if (Math.abs(distanceError)<=bandwidth){
				leftMotor.setSpeed(motorConstant);
				rightMotor.setSpeed(motorConstant + motorDelta - 100);
				leftMotor.forward();
				rightMotor.forward();
				
			}
		}
		// if too close to the wall move away from the wall
//		else if (distanceError>bandwidth){
		if (distanceError>bandwidth){
			seesWall = true;
			if (firstTime == 0){
				thetaBeforeBlock = odometer.getTheta();
				synchronized(lock){
				try {
					lock.wait();
				} catch (InterruptedException e) {}
				}
				
			}
			
			if (firstTime>0){
				leftMotor.setSpeed(motorConstant);
				rightMotor.setSpeed(60);
				leftMotor.forward();
				rightMotor.backward();	
			}
			
			
						
		}
		// if 
		else if (distanceError<bandwidth){
			seesWall = false;
//			leftMotor.backward();
//			rightMotor.backward();
//			leftMotor.setSpeed(motorConstant);
//			rightMotor.setSpeed(motorConstant + motorDelta - 45);
//			leftMotor.forward();
//			rightMotor.forward();
			
			
		}
		if (firstTime>0)
			if (Math.abs(odometer.getTheta()-thetaBeforeBlock)<Math.PI/2)
				lock.notifyAll();
		
	}

	@Override
	public int readUSDistance() {
		// TODO Auto-generated method stub
		return distanceUS;
	}

}
