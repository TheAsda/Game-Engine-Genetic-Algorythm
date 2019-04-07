package NeuralNetwork;

import java.util.Random;
import java.util.function.Function;

public class Matrix {
	private int rows, cols;
	public float data[][];

	public Matrix(int rows, int cols) {
		if (rows < 1 || cols < 1)
			throw new Error("Wrong matrix size");
		this.rows = rows;
		this.cols = cols;
		this.data = new float[rows][];
		for (int i = 0; i < rows; i++) {
			this.data[i] = new float[cols];
			for (int j = 0; j < cols; j++) {
				this.data[i][j] = 0;
			}
		}
	}

	public void multiply(float n) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				this.data[i][j] *= n;
			}
		}
	}

	public static Matrix multiply(Matrix m, Matrix n) {
		if (m.cols != n.rows) {
			throw new Error("Wrong matrixes");
		}
		Matrix result = new Matrix(m.rows, n.cols);
		for (int i = 0; i < result.rows; i++) {
			for (int j = 0; j < result.cols; j++) {
				float sum = 0;
				for (int k = 0; k < m.cols; k++) {
					sum += m.data[i][k] * n.data[k][j];
				}
				result.data[i][j] = sum;
			}
		}
		return result;
	}

	public void multiply(Matrix n) {
		Matrix result = new Matrix(this.rows, n.cols);
		for (int i = 0; i < result.rows; i++) {
			for (int j = 0; j < result.cols; j++) {
				float sum = 0;
				for (int k = 0; k < this.cols; k++) {
					sum += this.data[i][k] * n.data[k][j];
				}
				result.data[i][j] = sum;
			}
		}
		this.data = result.data;
	}

	public void add(int n) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				this.data[i][j] += n;
			}
		}
	}

	public void add(Matrix n) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				this.data[i][j] += n.data[i][j];
			}
		}
	}

	public void randomize() {
		Random rand = new Random();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				this.data[i][j] = rand.nextFloat() * (rand.nextBoolean() ? 1 : -1);
			}
		}
	}

	public static Matrix transpose(Matrix a) {
		Matrix result = new Matrix(a.cols, a.rows);
		for (int i = 0; i < a.rows; i++) {
			for (int j = 0; j < a.cols; j++) {
				result.data[j][i] = a.data[i][j];
			}
		}
		return result;
	}

	public void map(Function<Float, Float> func) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				this.data[i][j] = (float) func.apply(this.data[i][j]);
			}
		}
	}

	public static Matrix map(Matrix a, Function<Float, Float> func) {
		Matrix result = new Matrix(a.rows, a.cols);
		for (int i = 0; i < a.rows; i++) {
			for (int j = 0; j < a.cols; j++) {
				result.data[i][j] = (float) func.apply(a.data[i][j]);
			}
		}
		return result;
	}

	// map with func that takes data,i,j
	public static Matrix fromArray(float arr[]) {
		Matrix m = new Matrix(arr.length, 1);
		for (int i = 0; i < arr.length; i++) {
			m.data[i][0] = arr[i];
		}
		return m;
	}

	public float[] toArray() {
		float arr[] = new float[rows * cols];
		int index = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				arr[index] = this.data[i][j];
				index++;
			}
		}
		return arr;
	}

	public static Matrix subtract(Matrix a, Matrix b) {
		Matrix result = new Matrix(a.rows, a.cols);
		for (int i = 0; i < a.rows; i++) {
			for (int j = 0; j < a.cols; j++) {
				result.data[i][j] = a.data[i][j] - b.data[i][j];
			}
		}
		return result;
	}

	public void print() {
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				System.out.print(this.data[i][j] + " ");
			}
			System.out.print("\n");
		}
	}
}
