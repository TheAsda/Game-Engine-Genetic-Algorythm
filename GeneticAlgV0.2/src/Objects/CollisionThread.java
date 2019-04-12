package objects;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

import engine.maths.Vector2f;
import engine.maths.Vector3f;
import engine.rendering.models.ModelEntity;

public class CollisionThread implements Callable<Vector2f> {

	private ModelEntity model;
	private Vector3f start;
	private Vector3f end;
	private Obstacle obstacles;
	private boolean isNew=true;

	public CollisionThread(ModelEntity model, Vector3f start, Vector3f end, Obstacle obstacles) {
		this.model     = model;
		this.start     = start;
		this.end       = end;
		this.obstacles = obstacles;
	}

	public CollisionThread() {
	}

	public void setData(ModelEntity model) {
		this.model = model;
	}

	@Override
	public Vector2f call() throws Exception {
		return rayCheck(model, start, end);
	}

	private Vector2f rayCheck(ModelEntity model, Vector3f start, Vector3f end) {
		float rotation = model.getRotation().getY();
		Vector3f position = model.getPosition();
		Vector2f position2D = new Vector2f(position.getX(), position.getZ());
		Vector2f currentVec = new Vector2f(start.getX(), start.getZ());

		float DistX = end.getX() - start.getX();
		float DistZ = end.getZ() - start.getZ();

		BigDecimal stepX = new BigDecimal(DistX / 30);
		BigDecimal stepZ = new BigDecimal(DistZ / 30);

		Vector2f topLeft = new Vector2f(position.getX() + obstacles.getMinX(), position.getZ() + obstacles.getMaxY());
		Vector2f bottomRight = new Vector2f(position.getX() + obstacles.getMaxX(), position.getZ() + obstacles.getMinY());
		Vector2f topRight = new Vector2f(position.getX() + obstacles.getMaxX(), position.getZ() + obstacles.getMaxY());
		Vector2f bottomLeft = new Vector2f(position.getX() + obstacles.getMinX(), position.getZ() + obstacles.getMinY());

		if (rotation != 0) {
			topLeft     = topLeft.rotate(rotation, position2D);
			bottomRight = bottomRight.rotate(rotation, position2D);
			topRight    = topRight.rotate(rotation, position2D);
			bottomLeft  = bottomLeft.rotate(rotation, position2D);
		}

		for (int i = 0; i < 30; i++) {
			currentVec.setX(currentVec.getX() + stepX.floatValue());
			currentVec.setY(currentVec.getY() + stepZ.floatValue());
			boolean res = isInsideRect(topLeft, bottomRight, topRight, bottomLeft, currentVec);
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

	public void setData(ModelEntity model, Vector3f start, Vector3f end, Obstacle obstacles) {
		this.model     = model;
		this.start     = start;
		this.end       = end;
		this.obstacles = obstacles;
	}

	
	public boolean isNew() {
		return isNew;
	}

	
	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}	
}
