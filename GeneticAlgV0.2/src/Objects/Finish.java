package objects;

import engine.rendering.models.ModelEntity;
import engine.rendering.models.TexturedModel;

public class Finish extends Obstacle {

	private final String JSONFILE = "finish.json";

	public Finish(ModelEntity[] obstacles, float chunkLength) {
		super(obstacles, chunkLength);
		super.calculateDimentions(obstacles[0].getModel());
	}

	public Finish(float chunkLength) {
		super(chunkLength);
	}

	public boolean finishCheck(Car car) {
		return super.detectCollision(car);
	}

	@Override
	public void saveToJSON() {
		super.saveToJSON(JSONFILE);
	}

	@Override
	public void loadFromJSON(TexturedModel model) {
		super.calculateDimentions(model);
		super.loadFromJSON(JSONFILE, model);
	}
}
