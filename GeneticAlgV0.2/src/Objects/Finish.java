package objects;

import engine.rendering.models.ModelEntity;
import engine.rendering.models.TexturedModel;

public class Finish extends Obstacle {

	private final String JSONFILE = "finish.json";

	public Finish(ModelEntity[] obstacles, float chunkLength) {
		super(obstacles, chunkLength);
	}

	public Finish(float chunkLength) {
		super(chunkLength);
	}

	public void finishCheck(Car car) {
		if (super.detectCollision(car) == true) {
			System.out.println("GJ!");
		}
	}

	@Override
	public void saveToJSON() {
		super.saveToJSON(JSONFILE);
	}

	@Override
	public void loadFromJSON(TexturedModel model) {
		super.loadFromJSON(JSONFILE, model);
	}
}
