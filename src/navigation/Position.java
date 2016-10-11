package navigation;

public class Position {
	private static double x;
	private static double y;
	private static double theta;
	private Object lock;
	
	public Position(double x, double y){
		this.x = x;
		this.y = y;
		lock = new Object();
//		this.theta = theta;
		
	}
	
	public void setPosition(double x, double y){
		this.x = x;
		this.y = y;
//		this.theta = theta;
	}
	
	public double getX(){
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}
	
	public void setX(double x){
		this.x = x;
	}
	public double getY(){
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}
	
	public void setY(double y){
		this.y = y;
	}
	
//	public double getTheta(){
//		return theta;
//	}
//	
//	public void setTheta(double theta){
//		this.theta = theta;
//	}

}
