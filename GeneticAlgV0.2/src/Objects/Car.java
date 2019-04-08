package objects;

import engine.maths.Vector3f;
import engine.rendering.models.ModelEntity;
import engine.rendering.models.TexturedModel;

public class Car extends ModelEntity {

	private final float drag = 0.994f, angularDrag = 0.6f, turnSpeed = 0.02f, maxAngularVelocity = turnSpeed * 1f, rayLength = 3f,
		rayAngle = (float)Math.PI / 4;
	@SuppressWarnings ("unused")
	private float power, angle, angularVelocity = 0, velocity = 0, maxVelocity, minVelocity, boxXLength, boxYLength, boxZLength;
	private Vector3f centroid, startPosition, startAngle, forwardRay, leftRay, rightRay;
	public ModelEntity box;
	public float boxVertices[];

	public Car(TexturedModel model, Vector3f position, Vector3f angle, Vector3f scale, float power) {
		super(model, position, angle, scale);

		startPosition = position;
		startAngle    = angle;

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
			}, "brick.png");
		this.box        = new ModelEntity(box, position, angle, scale);
		this.power      = power;
		this.centroid   = new Vector3f(position.getX(), position.getY() + 0.3f, position.getZ());
		this.forwardRay = centroid.add(new Vector3f(0f, 0f, rayLength));
		this.leftRay    = centroid.add(new Vector3f(-rayLength * (float)Math.cos(rayAngle), 0f, rayLength * (float)Math.sin(rayAngle)));
		this.rightRay   = centroid.add(new Vector3f(rayLength * (float)Math.cos(rayAngle), 0f, rayLength * (float)Math.sin(rayAngle)));
		this.angle      = angle.getY();
		maxVelocity     = power * 100;
		minVelocity     = power * 5;
	}

	public void update() {
		if (velocity == 0) {
			angularVelocity = 0;
		}
		if (velocity > 0.0f) {
			super.addRotation(0, (float)Math.toDegrees(angularVelocity), 0);
			box.addRotation(0, (float)Math.toDegrees(angularVelocity), 0);
			rotateRays();
			moveRays();
			super.addPosition(-velocity * (float)Math.sin(angle), 0f, velocity * (float)Math.cos(angle));
			box.addPosition(-velocity * (float)Math.sin(angle), 0, velocity * (float)Math.cos(angle));
			angle += angularVelocity;
		}
		velocity        *= drag;
		angularVelocity *= angularDrag;
	}

	private void moveRays() {
		centroid   = centroid.add(new Vector3f(-velocity * (float)Math.sin(angle), 0f, velocity * (float)Math.cos(angle)));
		forwardRay = forwardRay.add(new Vector3f(-velocity * (float)Math.sin(angle), 0f, velocity * (float)Math.cos(angle)));
		leftRay    = leftRay.add(new Vector3f(-velocity * (float)Math.sin(angle), 0f, velocity * (float)Math.cos(angle)));
		rightRay   = rightRay.add(new Vector3f(-velocity * (float)Math.sin(angle), 0f, velocity * (float)Math.cos(angle)));
	}

	private void rotateRays() {
		Vector3f newFRayPosition = new Vector3f();
		newFRayPosition.setX((float)(centroid.getX() + (forwardRay.getX() - centroid.getX()) * Math.cos(angularVelocity) - (forwardRay.getZ() - centroid
			.getZ()) * Math.sin(angularVelocity)));
		newFRayPosition.setZ((float)(centroid.getZ() + (forwardRay.getX() - centroid.getX()) * Math.sin(angularVelocity) + (forwardRay.getZ() - centroid
			.getZ()) * Math.cos(angularVelocity)));
		newFRayPosition.setY(forwardRay.getY());
		forwardRay = newFRayPosition;

		Vector3f newLRayPosition = new Vector3f();
		newLRayPosition.setX((float)(centroid.getX() + (leftRay.getX() - centroid.getX()) * Math.cos(angularVelocity) - (leftRay.getZ() - leftRay
			.getZ()) * Math.sin(angularVelocity)));
		newLRayPosition.setZ((float)(centroid.getZ() + (leftRay.getX() - centroid.getX()) * Math.sin(angularVelocity) + (leftRay.getZ() - leftRay
			.getZ()) * Math.cos(angularVelocity)));
		newLRayPosition.setY(leftRay.getY());
		leftRay = newLRayPosition;

		Vector3f newRRayPosition = new Vector3f();
		newRRayPosition.setX((float)(centroid.getX() + (rightRay.getX() - centroid.getX()) * Math.cos(angularVelocity) - (rightRay.getZ() - centroid
			.getZ()) * Math.sin(angularVelocity)));
		newRRayPosition.setZ((float)(centroid.getZ() + (rightRay.getX() - centroid.getX()) * Math.sin(angularVelocity) + (rightRay.getZ() - centroid
			.getZ()) * Math.cos(angularVelocity)));
		newRRayPosition.setY(rightRay.getY());
		rightRay = newRRayPosition;
	}

	public Vector3f getForwardRay() {
		return forwardRay;
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

	public void reset() {
		super.setPosition(startPosition);
		super.setRotation(startAngle);
		box.setPosition(startPosition);
		box.setRotation(startAngle);
		velocity        = 0f;
		angularVelocity = 0f;
		angle           = startAngle.getY();
		centroid        = startPosition;
	}
}
