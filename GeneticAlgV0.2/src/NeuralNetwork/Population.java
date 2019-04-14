package NeuralNetwork;

import java.util.Random;

import engine.maths.Vector3f;
import engine.rendering.models.TexturedModel;
import objects.Car;

public class Population {
	
	private final static float mutatationRate = 0.1f;
	
	public static Car[] nextGeneration(Car[] carEntities) {
		
		System.out.println("Next generation");
		
		float fitnesses[] = calculateFintess(carEntities);
		
		for (int i = 0; i < carEntities.length; i++) {
			carEntities[i] = pickOne(carEntities, fitnesses);
			carEntities[i].setRender(true);
		}
		
		return carEntities;
	}
	
	private static Car pickOne(Car[] carEntities, float[] fitnesses) {
		
		int index = 0;
		float r = new Random().nextFloat();
		
		while (r > 0) {
			r -= fitnesses[index++];
		}
		index--;
		
		Car carEntity = carEntities[index];
		
		return new Car(carEntity, carEntity.mutate(mutatationRate));
	}
	
	private static float[] calculateFintess(Car[] carEntities) {
		
		float sum = 0;
		for (int i = 0; i < carEntities.length; i++) {
			sum += carEntities[i].getDistance();
		}
		
		float fitnesses[] = new float[carEntities.length];
		
		for (int i = 0; i < carEntities.length; i++) {
			fitnesses[i] = carEntities[i].getDistance() / sum;
		}
		return fitnesses;
	}
	
}
