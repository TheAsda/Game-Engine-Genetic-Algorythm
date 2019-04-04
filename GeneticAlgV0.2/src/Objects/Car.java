package Objects;

import engine.maths.Vector3f;
import engine.rendering.Renderer;
import engine.rendering.models.ModelEntity;
import engine.rendering.models.TexturedModel;
import engine.rendering.models.UntexturedModel;

public class Car extends ModelEntity {

	private final float drag = 0.994f, angularDrag = 0.6f, turnSpeed = 0.02f, maxAngularVelocity = turnSpeed * 1f;
	private float power, angle, angularVelocity = 0, velocity = 0, maxVelocity, minVelocity, boxXLength, boxYLength, boxZLength;
	private Vector3f centroid;
	public ModelEntity box;
	public float boxVertices[];
	public Car(TexturedModel model, Vector3f position, Vector3f angle, Vector3f scale, float power) {
		super(model, position, angle, scale);

		float vertices[] = super.getModel().getVertices();
		float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE, minX = Float.MAX_VALUE, minY = Float.MAX_VALUE,
			minZ = Float.MAX_VALUE;

		for (int i = 0; i < vertices.length / 3; ++i) {
			float x = vertices[i * 3];
			float y = vertices[i * 3 + 1];
			float z = vertices[i * 3 + 2];
			if (x > maxX) {
				maxX = x;
			}
			else if (x < minX) {
				minX = x;
			}
			if (y > maxY) {
				maxY = y;
			}
			else if (y < minY) {
				minY = y;
			}

			if (z > maxZ) {
				maxZ = z;
			}
			else if (z < minZ) {
				minZ = z;
			}
		}

		boxXLength  = maxX - minX;
		boxYLength  = maxY - minY;
		boxZLength  = maxZ - minZ;

		boxVertices = new float[]{
			minX, maxY, maxZ,
			minX, minY, maxZ,
			maxX, minY, maxZ,
			maxX, maxY, maxZ,

			minX, maxY, minZ,
			minX, minY, minZ,
			maxX, minY, minZ,
			maxX, maxY, minZ
		};

		TexturedModel box = new TexturedModel(
			boxVertices, new float[]{}, new float[]{}, new int[]{
				0, 1, 2,
				0, 3, 2,
				0, 1, 4,
				1, 4, 5,
				3, 2, 7,
				2, 7, 6,
				4, 6, 5,
				4, 6, 7
			}, "brick.png"
		);
		this.box      = new ModelEntity(box, position, angle, scale);

		this.power    = power;
		this.centroid = new Vector3f(position.getX(), position.getY(), position.getZ());
		this.angle    = angle.getY();
		maxVelocity   = power * 100;
		minVelocity   = power * 5;
	}

	public void update() {
		if (velocity == 0) {
			angularVelocity = 0;
		}
		if (velocity > 0.0f) {
			super.addRotation(0, (float)Math.toDegrees(angularVelocity), 0);
			box.addRotation(0, (float)Math.toDegrees(angularVelocity), 0);
			centroid = centroid.add(new Vector3f(-velocity * (float)Math.sin(angle), 0f, velocity * (float)Math.cos(angle)));
			super.addPosition(-velocity * (float)Math.sin(angle), 0, velocity * (float)Math.cos(angle));
			box.addPosition(-velocity * (float)Math.sin(angle), 0, velocity * (float)Math.cos(angle));
			angle += angularVelocity;
		}
		velocity        *= drag;
		angularVelocity *= angularDrag;
	}

	public void accelerate() {
		if (velocity < maxVelocity)
			velocity += power;

	}

	public void stop() {
		if (velocity > 0)
			velocity -= power;
	}

	public void steerLeft() {
		if (this.angularVelocity < this.maxAngularVelocity && this.velocity > this.minVelocity)
			angularVelocity += turnSpeed;
	}

	public void steerRight() {
		if (this.angularVelocity > -this.maxAngularVelocity && this.velocity > this.minVelocity)
			angularVelocity -= turnSpeed;
	}
}
