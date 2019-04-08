package objects;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
	private float chunkLength;
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
}