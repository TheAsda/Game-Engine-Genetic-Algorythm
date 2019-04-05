package objects;

import engine.rendering.models.ModelEntity;

public class Finish extends Obstacle {

	public Finish(ModelEntity[] obstacles, float chunkLength) {
		super(obstacles, chunkLength);
	}
	
	public void finishCheck(Car car) {
		if(super.detectCollision(car)==true) {
			System.out.println("GJ!");
		}
	}
	
}
