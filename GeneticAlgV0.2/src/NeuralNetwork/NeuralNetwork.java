package NeuralNetwork;

import java.util.Random;

public class NeuralNetwork {

	@SuppressWarnings ("unused")
	private int numberOfInput, numberOfHidden, numberOfOutput;
	private Matrix weightsIH, weightsHO, biasH, biasO;
	private float learningRate = 0.1f;

	public NeuralNetwork(int numberOfInput, int numberOfHidden, int numberOfOutput) {
		this.numberOfInput  = numberOfInput;
		this.numberOfHidden = numberOfHidden;
		this.numberOfOutput = numberOfOutput;
		this.weightsIH      = new Matrix(numberOfHidden, numberOfInput);
		this.weightsHO      = new Matrix(numberOfOutput, numberOfHidden);
		this.weightsIH.randomize();
		this.weightsHO.randomize();
		this.biasH = new Matrix(numberOfHidden, 1);
		this.biasO = new Matrix(numberOfOutput, 1);
		this.biasH.randomize();
		this.biasO.randomize();
	}

	public float[] feedforward(float inputArray[]) {
		Matrix inputs = Matrix.fromArray(inputArray);

		Matrix hidden = Matrix.multiply(this.weightsIH, inputs);
		hidden.add(biasH);
		hidden.map(Activation::sigmoid);

		Matrix output = Matrix.multiply(weightsHO, hidden);
		output.add(biasO);
		output.map(Activation::sigmoid);

		return output.toArray();
	}

	public Matrix[] feedforward(float inputArray[], boolean a) {
		Matrix inputs = Matrix.fromArray(inputArray);

		Matrix hidden = Matrix.multiply(this.weightsIH, inputs);
		hidden.add(biasH);
		hidden.map(Activation::sigmoid);

		Matrix output = Matrix.multiply(weightsHO, hidden);
		output.add(biasO);
		output.map(Activation::sigmoid);

		return new Matrix[]{output, hidden};
	}

	public void mutate(float mutationRate) {
		float rand = new Random().nextFloat();
		if (rand <= mutationRate) {
			System.out.println("Mutate");
			this.weightsIH.randomize();
			this.weightsHO.randomize();
			this.biasH.randomize();
			this.biasO.randomize();
		}
	}

	public void train(float[] inputs, float[] targets) {
		Matrix[] result = this.feedforward(inputs, true);
		Matrix outputs = result[0];
		Matrix hidden = result[1];

		Matrix outputErrors = Matrix.subtract(Matrix.fromArray(targets), outputs);

		Matrix gradients = Matrix.map(outputs, Activation::dsigmoid);

		gradients.multiply(outputErrors);
		gradients.multiply(learningRate);

		Matrix hiddenT = Matrix.transpose(hidden);
		Matrix weightsHOD = Matrix.multiply(gradients, hiddenT);

		weightsHO.add(weightsHOD);
		biasO.add(gradients);

		Matrix weightsHOT = Matrix.transpose(weightsHO);
		Matrix hiddenErrors = Matrix.multiply(weightsHOT, outputErrors);

		Matrix hiddenGradients = Matrix.map(hidden, Activation::dsigmoid);

		hiddenGradients.multiply(hiddenErrors);
		hiddenGradients.multiply(learningRate);

		Matrix inputsT = Matrix.transpose(Matrix.fromArray(inputs));// ???
		Matrix weightsIHD = Matrix.multiply(hiddenGradients, inputsT);

		weightsIH.add(weightsIHD);
		biasH.add(hiddenGradients);
	}
}
