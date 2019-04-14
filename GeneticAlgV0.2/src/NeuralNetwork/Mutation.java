package NeuralNetwork;

import java.util.Random;

public class Mutation {
	
	private static Random randomizer = new Random();
	
	public static Float gauss(float x) {
		
		return (float)(x + randomizer.nextGaussian() % 0.1 * (randomizer.nextBoolean() ? 1 : -1));
	}
	
}
