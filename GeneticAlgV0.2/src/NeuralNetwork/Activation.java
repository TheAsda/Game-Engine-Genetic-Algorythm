package NeuralNetwork;

public class Activation {
	
	public static Float sigmoid(Float x) {
		
		return 1 / (1 + (float)Math.exp(-x));
	}
	
	public static Float dsigmoid(Float y) {
		
		return y * (1 - y);
	}
	
	public static Float bigSigmoid(Float x) {
		
		return sigmoid(x) * 2 - 1;
	}
}
