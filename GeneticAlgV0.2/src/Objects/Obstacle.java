package Objects;

import java.util.ArrayList;

import engine.io.obj.OBJLoader;
import engine.maths.Vector2f;
import engine.maths.Vector3f;
import engine.rendering.Renderer;
import engine.rendering.models.ModelEntity;

class Chunk {

	private float x1, y1, x2, y2;
	public ArrayList<ModelEntity> obstacles;

	Chunk(ArrayList<ModelEntity> obstacles, float x1, float y1, float x2, float y2) {
		this.x1        = x1;
		this.y1        = y1;
		this.x2        = x2;
		this.y2        = y2;
		this.obstacles = obstacles;
	}
}

public class Obstacle {

	private Chunk chunks[];
	private int length, gridSize;
	private float chunkLength;

	public Obstacle(ModelEntity obstacles[], float chunkLength) {
		this.length      = obstacles.length;
		this.chunkLength = chunkLength;
		this.gridSize    = (int)(100f / chunkLength);
		chunks           = new Chunk[gridSize * gridSize];
		int k = 0;
		for (float xStart = -100f / 2; xStart < 100f / 2 - chunkLength; xStart += chunkLength) {
			for (float yStart = -100f / 2; yStart < 100f / 2 - chunkLength; yStart += chunkLength) {
				ArrayList<ModelEntity> tempObstacles = new ArrayList<ModelEntity>();
				for (int i = 0; i < length; i++) {
					Vector3f position = obstacles[i].getPosition();
					if (position.getX() >= xStart && position.getX() < xStart + chunkLength && position.getZ() >= yStart && position.getZ() < yStart
						+ chunkLength) {
						tempObstacles.add(obstacles[i]);
					}
				}
				chunks[k++] = new Chunk(tempObstacles.size() != 0 ? tempObstacles : null, xStart, yStart, xStart + chunkLength, yStart + chunkLength);
			}
		}
	}

	public void render(Renderer renderer) {
		for (int i = 0; i < chunks.length; i++) {
			if (chunks[i] != null && chunks[i].obstacles != null)
				for (int j = 0; j < chunks[i].obstacles.size(); j++) {
					renderer.proseeEntity(chunks[i].obstacles.get(j));
				}
		}
	}

	public boolean detectCollision(Car car) {
		ArrayList<Chunk> BFResults = broadPhase(car);
		if (BFResults.size() != 0) {
			narrowPhase(BFResults, car);
			return false;
		}
		return false;
	}

	private ArrayList<Chunk> broadPhase(Car car) {
		Vector3f carPosition = car.getPosition();
		int i = (int)(carPosition.getX() / this.chunkLength + this.gridSize / 2);
		int j = (int)(carPosition.getZ() / this.chunkLength);

		Chunk chunk;
		ArrayList<Chunk> list = new ArrayList<Chunk>();
		for (int m = -1; m < 2; m++) {
			for (int n = -1; n < 2; n++) {
				int index = (i + m) * gridSize + j + n;
				chunk = chunks[index];
				if (chunk.obstacles != null) {
					list.add(chunk);
				}
			}
		}
		return list;
	}

	private void narrowPhase(ArrayList<Chunk> BFResults, Car car) {
		ArrayList<ModelEntity> obstacles = new ArrayList<ModelEntity>();
		for (int i = 0; i < BFResults.size(); i++) {
			for (int j = 0; j < BFResults.get(i).obstacles.size(); j++) {
				obstacles.add(BFResults.get(i).obstacles.get(j));
			}
		}

		System.out.println("Test");
		for (int i = 0; i < obstacles.size(); i++) {
			ModelEntity model = obstacles.get(i);
			boolean result = sepAxis(model, car);
			System.out.println(result);
		}
		System.out.println("End");
	}

	private static boolean sepAxis(ModelEntity a, Car b) {
		Vector3f aPosition = a.getPosition();
		Vector3f bPosition = b.getPosition();
		float xOffset = (float)Math.sqrt(Math.pow(aPosition.getX() - bPosition.getX(), 2));
		float zOffset = (float)Math.sqrt(Math.pow(aPosition.getZ() - bPosition.getZ(), 2));
		Vector2f offset = new Vector2f(xOffset, zOffset);
		Vector2f xAxis = new Vector2f();
		float vertices[] = a.getModel().getVertices();
		Vector2f aVerticesVectors[] = new Vector2f[vertices.length / 3];
		for (int i = 0; i < vertices.length / 3; i++) {
			float x = vertices[3 * i];
			float z = vertices[3 * i + 2];
			aVerticesVectors[i] = new Vector2f(x, z);
		}
		vertices = b.boxVertices;
		Vector2f bVerticesVectors[] = new Vector2f[vertices.length / 3];
		for (int i = 0; i < vertices.length / 3; i++) {
			float x = vertices[3 * i];
			float z = vertices[3 * i + 2];
			bVerticesVectors[i] = new Vector2f(x, z);
		}
		for (int j = aVerticesVectors.length - 1, i = 0; i < aVerticesVectors.length; j = i, i++) {
			Vector2f E = aVerticesVectors[j].sub(aVerticesVectors[i]);
			xAxis = new Vector2f(-E.getY(), E.getX());
			if (!intervalIntersect(aVerticesVectors, bVerticesVectors, xAxis, offset))
				return false;
		}

		for (int j = bVerticesVectors.length - 1, i = 0; i < bVerticesVectors.length; j = i, i++) {
			Vector2f E = bVerticesVectors[j].sub(bVerticesVectors[i]);
			xAxis = new Vector2f(-E.getY(), E.getX());
			if (!intervalIntersect(aVerticesVectors, bVerticesVectors, xAxis, offset))
				return false;
		}
		return true;
	}

	private static boolean intervalIntersect(Vector2f[] a, Vector2f[] b, Vector2f xAxis, Vector2f offset) {
		float min0 = 0, max0 = 0, min1 = 0, max1 = 0;
		float res[] = GetInterval(a, xAxis, min0, max0);
		min0 = res[0];
		max0 = res[1];
		res  = GetInterval(b, xAxis, min1, max1);
		min1 = res[0];
		max1 = res[1];
		float h = offset.dot(xAxis);

		min0 += h;
		max0 += h;

		float d0 = min0 - max1;
		float d1 = min1 - max0;

		if (d0 > 0f || d1 > 0f)
			return false;
		else
			return true;
	}

	private static float[] GetInterval(Vector2f[] a, Vector2f xAxis, float min0, float max0) {
		min0 = a[0].dot(xAxis);
		max0 = min0;

		for (int i = 1; i < a.length; i++) {
			float dot = a[i].dot(xAxis);
			if (dot < min0)
				min0 = dot;
			else if (dot > max0)
				max0 = dot;
		}
		return new float[]{min0, max0};
	}
}