package navigation;

public class NavigateObstacles extends Thread implements UltrasonicController{

	private int distance;
	
	@Override
	public void processUSData(int distance) {
		// TODO Auto-generated method stub
		this.distance = distance;
	}

	@Override
	public int readUSDistance() {
		// TODO Auto-generated method stub
		return distance;
	}

}
