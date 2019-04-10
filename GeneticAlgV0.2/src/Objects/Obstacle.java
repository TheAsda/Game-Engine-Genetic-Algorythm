package objects;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import engine.maths.Vector2f;
import engine.maths.Vector3f;
import engine.rendering.Renderer;
import engine.rendering.models.ModelEntity;
import engine.rendering.models.TexturedModel;

class Chunk {

	@SuppressWarnings ("unused")
	private float x1, y1, x2, y2;
	public ArrayList<ModelEntity> obstacles;
	public ArrayList<ArrayList<Vector3f>> coordinates;

	Chunk(ArrayList<ModelEntity> obstacles, float x1, float y1, float x2, float y2) {
		this.x1        = x1;
		this.y1        = y1;
		this.x2        = x2;
		this.y2        = y2;
		this.obstacles = obstacles;
	}

	public float getX1() {
		return x1;
	}

	public float getY1() {
		return y1;
	}

	public float getX2() {
		return x2;
	}

	public float getY2() {
		return y2;
	}
}

public class Obstacle {

	private Chunk chunks[];
	private int gridSize;
	private float chunkLength, minX, minY, maxX, maxY;
	private final String JSONFILE = "obstacles.json";
	private final float MAP_SIZE = 20f;

	public Obstacle(ModelEntity obstacles[], float chunkLength) {
		this.chunkLength = chunkLength;
		this.gridSize    = (int)(MAP_SIZE / chunkLength);
		chunks           = new Chunk[gridSize * gridSize];
		int k = 0;
		for (float xStart = -MAP_SIZE / 2; xStart < MAP_SIZE / 2 - chunkLength; xStart += chunkLength) {
			for (float yStart = -MAP_SIZE / 2; yStart < MAP_SIZE / 2 - chunkLength; yStart += chunkLength) {
				ArrayList<ModelEntity> tempObstacles = new ArrayList<ModelEntity>();
				for (int i = 0; i < obstacles.length; i++) {
					Vector3f position = obstacles[i].getPosition();
					if (position.getX() >= xStart && position.getX() < xStart + chunkLength && position.getZ() >= yStart && position.getZ() < yStart
						+ chunkLength) {
						tempObstacles.add(obstacles[i]);
					}
				}
				chunks[k++] = new Chunk(tempObstacles, xStart, yStart, xStart + chunkLength, yStart + chunkLength);
			}
		}
	}

	public Obstacle(float chunkLength) {
		this.chunkLength = chunkLength;
		this.gridSize    = (int)(MAP_SIZE / chunkLength);
		chunks           = new Chunk[gridSize * gridSize];
		int k = 0;
		for (float xStart = -MAP_SIZE / 2; xStart < MAP_SIZE / 2 - chunkLength; xStart += chunkLength) {
			for (float yStart = -MAP_SIZE / 2; yStart < MAP_SIZE / 2 - chunkLength; yStart += chunkLength) {
				chunks[k++] = new Chunk(new ArrayList<ModelEntity>(), xStart, yStart, xStart + chunkLength, yStart + chunkLength);
			}
		}
	}

	public void add(ModelEntity obstacle) {
		Vector3f position = obstacle.getPosition();
		for (int i = 0; i < chunks.length; i++) {
			if (position.getX() >= chunks[i].getX1() && position.getY() >= chunks[i].getY1() &&
				position.getX() < chunks[i].getX2() && position.getY() < chunks[i].getY2()) {
				chunks[i].obstacles.add(obstacle);
				return;
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
		boolean result = false;
		ArrayList<Chunk> BFResults = broadPhase(car);
		if (BFResults.size() != 0) {
			result = result | narrowPhase(BFResults, car);
		}
		return result;
	}

	private ArrayList<Chunk> broadPhase(Car car) {
		Vector3f carPosition = car.getPosition();
		int i = (int)(carPosition.getX() / this.chunkLength + this.gridSize / 2);
		int j = (int)(carPosition.getZ() / this.chunkLength + this.gridSize / 2);

		Chunk chunk;
		ArrayList<Chunk> list = new ArrayList<Chunk>();
		for (int m = -1; m < 2; m++) {
			for (int n = -1; n < 2; n++) {
				int index = (i + m) * gridSize + j + n;
				if (index < gridSize * gridSize && index >= 0) {
					chunk = chunks[index];
					if (chunk.obstacles.size() != 0) {
						list.add(chunk);
					}
				}
			}
		}
		return list;
	}

	private boolean narrowPhase(ArrayList<Chunk> BFResults, Car car) {
		ArrayList<ModelEntity> obstacles = new ArrayList<ModelEntity>();
		for (int i = 0; i < BFResults.size(); i++) {
			for (int j = 0; j < BFResults.get(i).obstacles.size(); j++) {
				obstacles.add(BFResults.get(i).obstacles.get(j));
			}
		}
		boolean result = false;
		for (int i = 0; i < obstacles.size(); i++) {
			ModelEntity model = obstacles.get(i);
			result = result | sepAxis(model, car);
		}
		return result;
	}

	private static boolean sepAxis(ModelEntity a, Car b) {
		Vector3f aPosition = a.getPosition();
		Vector3f bPosition = b.box.getPosition();
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

	public float[] raysCollision(Car car) {
		ArrayList<Chunk> BFResults = broadPhase(car);
		if (BFResults.size() != 0) {
			ArrayList<ModelEntity> obstacles = new ArrayList<ModelEntity>();
			for (int i = 0; i < BFResults.size(); i++) {
				for (int j = 0; j < BFResults.get(i).obstacles.size(); j++) {
					obstacles.add(BFResults.get(i).obstacles.get(j));
				}
			}

			if (minX == 0)
				calculateDimentions(obstacles.get(0));

			float frontDist = car.getRayLength(), leftDist = car.getRayLength(), rightDist = car.getRayLength();
			Vector2f frontRayCollision, leftRayCollision, rightRayCollision;
			float tFront = car.getRayLength(), tLeft = car.getRayLength(), tRight = car.getRayLength();

			for (int i = 0; i < obstacles.size(); i++) {
				frontRayCollision = rayCheck(obstacles.get(i), car.getCentroid(), car.getFrontRay());
				leftRayCollision  = rayCheck(obstacles.get(i), car.getCentroid(), car.getLeftRay());
				rightRayCollision = rayCheck(obstacles.get(i), car.getCentroid(), car.getRightRay());
				if (frontRayCollision != null)
					tFront = calcDist(frontRayCollision, car.getCentroid());
				if (leftRayCollision != null)
					tLeft = calcDist(leftRayCollision, car.getCentroid());
				if (rightRayCollision != null)
					tRight = calcDist(rightRayCollision, car.getCentroid());

				if (tFront < frontDist)
					frontDist = tFront;
				if (tLeft < leftDist)
					leftDist = tLeft;
				if (tRight < rightDist)
					rightDist = tRight;
			}
			return new float[]{frontDist, leftDist, rightDist};
		}
		return null;
	}

	private float calcDist(Vector2f a, Vector3f b) {
		return (float)Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY(), b.getZ()));
	}

	private Vector2f rayCheck(ModelEntity model, Vector3f start, Vector3f end) {
		float rotation = model.getRotation().getY();
		Vector3f position = model.getPosition();
		Vector2f position2D = new Vector2f(position.getX(), position.getZ());
		Vector2f currentVec = new Vector2f(start.getX(), start.getZ());
		Vector2f destination = new Vector2f(end.getX(), end.getZ());

		float DistX = end.getX() - start.getX();
		float DistZ = end.getZ() - start.getZ();

		BigDecimal stepX = new BigDecimal(DistX / 30);
		BigDecimal stepZ = new BigDecimal(DistZ / 30);

		Vector2f topLeft = new Vector2f(position.getX() + minX, position.getZ() + maxY);
		Vector2f bottomRight = new Vector2f(position.getX() + maxX, position.getZ() + minY);
		Vector2f topRight = new Vector2f(position.getX() + maxX, position.getZ() + maxY);
		Vector2f bottomLeft = new Vector2f(position.getX() + minX, position.getZ() + minY);

		if (rotation != 0) {
			topLeft     = topLeft.rotate(rotation, position2D);
			bottomRight = bottomRight.rotate(rotation, position2D);
			topRight    = topRight.rotate(rotation, position2D);
			bottomLeft  = bottomLeft.rotate(rotation, position2D);
		}

		while (Math.abs(currentVec.getX()) < Math.abs(destination.getX()) || Math.abs(currentVec.getY()) < Math.abs(destination.getY())) {
			currentVec.setX(currentVec.getX() + stepX.floatValue());
			currentVec.setY(currentVec.getY() + stepZ.floatValue());
			boolean res = Obstacle.isInsideRect(topLeft, bottomRight, topRight, bottomLeft, currentVec);
			if (res == true) {
				return currentVec;
			}
		}
		return null;
	}

	@SuppressWarnings ("unused")
	private static boolean isInsideRect(Vector2f topLeft, Vector2f bottomRight, Vector2f topRight, Vector2f bottomLeft, Vector2f currentVec) {
		Vector2f arr[] = {topLeft, bottomLeft, bottomRight, topRight};
		int n = 4;
		float x, y;
		if ((x = rotate(arr[0], arr[1], currentVec)) < 0 || (y = rotate(arr[0], arr[n - 1], currentVec)) > 0) {
			return false;
		}
		int p = 1;
		int r = n - 1;
		while (r - p > 1) {
			int q = (p + r) / 2;
			if (rotate(arr[0], arr[q], currentVec) < 0) {
				r = q;
			}
			else {
				p = q;
			}
		}
		return !intersect(arr[0], currentVec, arr[p], arr[r]);
	}

	private static boolean intersect(Vector2f a, Vector2f b, Vector2f c, Vector2f d) {
		return rotate(a, b, c) * rotate(a, b, d) <= 0 & rotate(c, d, a) * rotate(c, d, b) < 0;
	}

	private static float rotate(Vector2f a, Vector2f b, Vector2f c) {
		return (b.getX() - a.getX()) * (c.getY() - b.getY()) - (b.getY() - a.getY()) * (c.getX() - b.getX());
	}

	@SuppressWarnings ("unchecked")
	public void saveToJSON() {
		JSONObject data = new JSONObject();
		data.put("chunkLength", chunkLength);
		data.put("gridSize", gridSize);
		JSONArray chunks = new JSONArray();
		data.put("chunks", chunks);
		for (int i = 0; i < this.chunks.length; i++) {
			if (this.chunks[i] == null)
				break;
			JSONObject chunk = new JSONObject();
			chunks.add(chunk);

			chunk.put("x1", this.chunks[i].getX1());
			chunk.put("y1", this.chunks[i].getY1());
			chunk.put("x2", this.chunks[i].getX2());
			chunk.put("y2", this.chunks[i].getY2());
			chunk.put("index", i);

			JSONArray obstacles = new JSONArray();
			chunk.put("obstacles", obstacles);
			for (int j = 0; j < this.chunks[i].obstacles.size(); j++) {
				JSONObject obstacle = new JSONObject();
				Vector3f position = this.chunks[i].obstacles.get(j).getPosition();
				JSONArray JSONPosition = new JSONArray();

				JSONPosition.add(position.getX());
				JSONPosition.add(position.getY());
				JSONPosition.add(position.getZ());

				obstacle.put("position", JSONPosition);
				Vector3f rotation = this.chunks[i].obstacles.get(j).getRotation();
				JSONArray JSONRotation = new JSONArray();

				JSONRotation.add(rotation.getX());
				JSONRotation.add(rotation.getY());
				JSONRotation.add(rotation.getZ());

				obstacle.put("rotation", JSONRotation);
				obstacles.add(obstacle);
			}
		}
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("res/jsons/" + this.JSONFILE));
			writer.write(data.toJSONString());
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadFromJSON(TexturedModel model) {
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader("res/jsons/" + this.JSONFILE));
		}
		catch (IOException | ParseException e) {
			System.out.println("No obstacles file");
			return;
		}
		JSONObject jsonObject = (JSONObject)obj;
		this.chunkLength = ((Number)jsonObject.get("chunkLength")).floatValue();
		this.gridSize    = ((Number)jsonObject.get("gridSize")).intValue();
		this.chunks      = new Chunk[gridSize * gridSize];
		JSONArray JSONChunks = (JSONArray)jsonObject.get("chunks");
		for (int i = 0; i < JSONChunks.size(); i++) {
			JSONObject JSONChunk = (JSONObject)JSONChunks.get(i);
			ArrayList<ModelEntity> obstacles = new ArrayList<ModelEntity>();
			if (JSONChunk.get("obstacles") != "[]") {
				JSONArray JSONObstacles = (JSONArray)JSONChunk.get("obstacles");
				for (int j = 0; j < JSONObstacles.size(); j++) {
					JSONObject JSONEntity = (JSONObject)JSONObstacles.get(j);
					JSONArray position = (JSONArray)JSONEntity.get("position");
					JSONArray rotation = (JSONArray)JSONEntity.get("rotation");
					Vector3f entityPosition = new Vector3f(
						((Number)position.get(0)).floatValue(), ((Number)position.get(1)).floatValue(), ((Number)position.get(2)).floatValue());
					Vector3f entityRotation = new Vector3f(
						((Number)rotation.get(0)).floatValue(), ((Number)rotation.get(1)).floatValue(), ((Number)rotation.get(2)).floatValue());
					obstacles.add(new ModelEntity(model, entityPosition, entityRotation, new Vector3f(1f, 1f, 1f)));
				}
			}
			float x1 = ((Number)JSONChunk.get("x1")).floatValue();
			float y1 = ((Number)JSONChunk.get("y1")).floatValue();
			float x2 = ((Number)JSONChunk.get("x2")).floatValue();
			float y2 = ((Number)JSONChunk.get("y2")).floatValue();

			int index = ((Number)JSONChunk.get("index")).intValue();
			this.chunks[index] = new Chunk(obstacles, x1, y1, x2, y2);
		}
	}

	@SuppressWarnings ("unchecked")
	protected void saveToJSON(String JSONFILE) {
		JSONObject data = new JSONObject();
		data.put("chunkLength", chunkLength);
		data.put("gridSize", gridSize);
		JSONArray chunks = new JSONArray();
		data.put("chunks", chunks);
		for (int i = 0; i < this.chunks.length; i++) {
			if (this.chunks[i] == null)
				break;
			JSONObject chunk = new JSONObject();
			chunks.add(chunk);
			chunk.put("x1", this.chunks[i].getX1());
			chunk.put("y1", this.chunks[i].getY1());
			chunk.put("x2", this.chunks[i].getX2());
			chunk.put("y2", this.chunks[i].getY2());
			chunk.put("index", i);
			JSONArray obstacles = new JSONArray();
			chunk.put("obstacles", obstacles);
			for (int j = 0; j < this.chunks[i].obstacles.size(); j++) {
				JSONObject obstacle = new JSONObject();
				Vector3f position = this.chunks[i].obstacles.get(j).getPosition();
				JSONArray JSONPosition = new JSONArray();
				JSONPosition.add(position.getX());
				JSONPosition.add(position.getY());
				JSONPosition.add(position.getZ());
				obstacle.put("position", JSONPosition);
				Vector3f rotation = this.chunks[i].obstacles.get(j).getRotation();
				JSONArray JSONRotation = new JSONArray();
				JSONRotation.add(rotation.getX());
				JSONRotation.add(rotation.getY());
				JSONRotation.add(rotation.getZ());
				obstacle.put("rotation", JSONRotation);
				obstacles.add(obstacle);
			}
		}
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("res/jsons/" + JSONFILE));
			writer.write(data.toJSONString());
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}

	}

	protected void loadFromJSON(String JSONFILE, TexturedModel model) {
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader("res/jsons/" + JSONFILE));
		}
		catch (IOException | ParseException e) {
			System.out.println("No finish file");
			return;
		}
		JSONObject jsonObject = (JSONObject)obj;
		this.chunkLength = ((Number)jsonObject.get("chunkLength")).floatValue();
		this.gridSize    = ((Number)jsonObject.get("gridSize")).intValue();
		this.chunks      = new Chunk[gridSize * gridSize];
		JSONArray JSONChunks = (JSONArray)jsonObject.get("chunks");
		for (int i = 0; i < JSONChunks.size(); i++) {
			JSONObject JSONChunk = (JSONObject)JSONChunks.get(i);
			ArrayList<ModelEntity> obstacles = new ArrayList<ModelEntity>();
			if (JSONChunk.get("obstacles") != "[]") {
				JSONArray JSONObstacles = (JSONArray)JSONChunk.get("obstacles");
				for (int j = 0; j < JSONObstacles.size(); j++) {
					JSONObject JSONEntity = (JSONObject)JSONObstacles.get(j);
					JSONArray position = (JSONArray)JSONEntity.get("position");
					JSONArray rotation = (JSONArray)JSONEntity.get("rotation");
					Vector3f entityPosition = new Vector3f(
						((Number)position.get(0)).floatValue(), ((Number)position.get(1)).floatValue(), ((Number)position.get(2)).floatValue());
					Vector3f entityRotation = new Vector3f(
						((Number)rotation.get(0)).floatValue(), ((Number)rotation.get(1)).floatValue(), ((Number)rotation.get(2)).floatValue());
					obstacles.add(new ModelEntity(model, entityPosition, entityRotation, new Vector3f(1f, 1f, 1f)));
				}
			}
			float x1 = ((Number)JSONChunk.get("x1")).floatValue();
			float y1 = ((Number)JSONChunk.get("y1")).floatValue();
			float x2 = ((Number)JSONChunk.get("x2")).floatValue();
			float y2 = ((Number)JSONChunk.get("y2")).floatValue();
			int index = ((Number)JSONChunk.get("index")).intValue();
			this.chunks[index] = new Chunk(obstacles, x1, y1, x2, y2);
		}
	}

	private void calculateDimentions(ModelEntity model) {
		float[] vertices = model.getModel().getVertices();

		float x, y;
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		maxX = -Float.MAX_VALUE;
		maxY = -Float.MAX_VALUE;

		for (int i = 0; i < vertices.length / 3; i++) {
			x = vertices[i * 3];
			y = vertices[i * 3 + 2];

			if (x < minX)
				minX = x;
			else if (x > maxX)
				maxX = x;

			if (y < minY)
				minY = y;
			else if (y > maxY)
				maxY = y;
		}
	}
}