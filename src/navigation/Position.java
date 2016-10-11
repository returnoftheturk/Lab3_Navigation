package navigation;

public class Position {
	private static double x;
	private static double y;
	private static double theta;
	
	public Position(double x, double y){
		this.x = x;
		this.y = y;
//		this.theta = theta;
		
	}
	
	public void setPosition(double x, double y){
		this.x = x;
		this.y = y;
//		this.theta = theta;
	}
	
	public double getX(){
		return x;
	}
	
	public void setX(double x){
		this.x = x;
	}
	public double getY(){
		return y;
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
