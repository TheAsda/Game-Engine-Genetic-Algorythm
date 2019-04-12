package NeuralNetwork;

import java.util.Random;

import engine.maths.Vector3f;
import engine.rendering.models.TexturedModel;
import objects.Car;

public class Population {

	private final static float mutatationRate = 0.1f;

	public static Car[] nextGeneration(Car[] carEntities, TexturedModel car, int population) {

		System.out.println("Next generation");

		float fitnesses[] = calculateFintess(carEntities);

		for (int i = 0; i < population; i++) {
			carEntities[i] = pickOne(carEntities, fitnesses, car);
			carEntities[i].setRender(true);
		}
		return carEntities;
	}

	private static Car pickOne(Car[] carEntities, float[] fitnesses, TexturedModel car) {
		int index = 0;
		float r = new Random().nextFloat();

		while (r > 0) {
			r -= fitnesses[index++];
		}
		index--;

		Car carEntity = carEntities[index];
		carEntity.mutate(mutatationRate);

		return new Car(car, new Vector3f(0f, 0f, 0f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f), 0.001f, carEntity.getBrain());
	}

	private static float[] calculateFintess(Car[] carEntities) {
		int sum = 0;
		for (int i = 0; i < carEntities.length; i++) {
			sum += carEntities[i].getScore();
		}

		float fitnesses[] = new float[carEntities.length];

		for (int i = 0; i < carEntities.length; i++) {
			fitnesses[i] = (float)carEntities[i].getScore() / sum;
		}
		return fitnesses;
	}

}
