package navigation;
import lejos.robotics.SampleProvider;

public class UltrasonicPoller extends Thread{
	private SampleProvider us;
	private UltrasonicController cont;
	private float[] usData;
	
	public UltrasonicPoller(SampleProvider us, float[] usData, UltrasonicController cont){
		this.us = us;
		this.usData = usData;
		this.cont = cont;
	}
	
	public void run(){
		int distance;
		while (true){
			us.fetchSample(usData, 0);
			distance = (int)(usData[0]*100.0);
			cont.processUSData(distance);
			try{
				Thread.sleep(50); //Steves code has sleep for 50
			}
			catch(Exception e){}
			
		}
		
	}
}
