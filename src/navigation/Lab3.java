package navigation;

import navigation.Odometer;
import navigation.OdometryDisplay;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Lab3 {
	
	private static final Port usPort = LocalEV3.get().getPort("S1");
	
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
//	private static final EV3UltrasonicSensor usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
	
	// Constants
	public static final double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 14.6;

	public static void main(String[] args) {
		int buttonChoice;
		
		@SuppressWarnings("resource")
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);		// usSensor is the instance
		SampleProvider usDistance = usSensor.getMode("Distance");	// usDistance provides samples from this instance
		float[] usData = new float[usDistance.sampleSize()];		// usData is the buffer in which data are returned
		UltrasonicPoller usPoller = null;	

		// some objects that need to be instantiated
		
		final TextLCD t = LocalEV3.get().getTextLCD();
		Odometer odometer = new Odometer(leftMotor, rightMotor, WHEEL_RADIUS, TRACK);
		OdometryDisplay odometryDisplay;
		
		Navigate navigate = new Navigate(leftMotor, rightMotor, odometer, WHEEL_RADIUS, TRACK);
		NavigateObstacles navObs = new NavigateObstacles(leftMotor, rightMotor, odometer, WHEEL_RADIUS, TRACK);
		do {
			// clear the display
			t.clear();

			// ask the user whether the motors should drive in a square or float
			t.drawString("< Left | Right >", 0, 0);
			t.drawString("       |        ", 0, 1);
			t.drawString(" Avoid | Navi   ", 0, 2);
			t.drawString("  Obst | gate   ", 0, 3);
			t.drawString("  acle |        ", 0, 4);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {
			
			odometryDisplay = new OdometryDisplay(odometer, t, navObs);
			usPoller = new UltrasonicPoller(usDistance, usData, navObs);
			odometer.start();
			odometryDisplay.start();
			navObs.start();
			//odometryCorrection.start();
			usPoller.start();
			
			leftMotor.forward();
			leftMotor.flt();
			rightMotor.forward();
			rightMotor.flt();
			
			odometer.start();
			odometryDisplay.start();
			
		} else {
			// start the odometer, the odometry display and the
			// odometry correction
			odometryDisplay = new OdometryDisplay(odometer,t, navigate);
			odometer.start();
			odometryDisplay.start();			
			navigate.start();
		}
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
