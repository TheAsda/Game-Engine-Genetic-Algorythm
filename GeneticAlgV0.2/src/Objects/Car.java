package objects;

import NeuralNetwork.NeuralNetwork;
import engine.maths.Vector3f;
import engine.rendering.models.ModelEntity;
import engine.rendering.models.TexturedModel;

public class Car extends ModelEntity {
	
	private final float   drag  = 0.994f, angularDrag = 0.6f, turnSpeed = 0.02f, maxAngularVelocity = turnSpeed * 1f,
				rayLength = 2f,
				rayAngle = (float)Math.PI / 4;
	private float         power, angle, angularVelocity = 0, velocity = 0, maxVelocity, minVelocity;
	private Vector3f      centroid, startPosition, startAngle, frontRay, leftRay, rightRay;
	public ModelEntity    box;
	public float          boxVertices[], boxDimentions[];
	private NeuralNetwork brain;
	private int           score = 0;
	private float         distance;
	
	public Car(TexturedModel model, Vector3f position, Vector3f angle, float power) {
		
		super(model, position, angle, new Vector3f(1, 1, 1));
		
		brain = new NeuralNetwork(3, 4, 2);
		
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
		
		boxDimentions = new float[]{minX, minZ, maxX, maxZ};
		
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
		this.box      = new ModelEntity(box, position, angle, new Vector3f(1, 1, 1));
		this.power    = power;
		this.centroid = new Vector3f(position.getX(), position.getY() + 0.3f, position.getZ());
		this.frontRay = centroid.add(new Vector3f(0f, 0f, rayLength));
		this.leftRay  = centroid.add(new Vector3f(-rayLength * (float)Math.cos(rayAngle), 0f, rayLength * (float)Math.sin(rayAngle)));
		this.rightRay = centroid.add(new Vector3f(rayLength * (float)Math.cos(rayAngle), 0f, rayLength * (float)Math.sin(rayAngle)));
		this.angle    = angle.getY();
		maxVelocity   = power * 100;
		minVelocity   = power * 5;
	}
	
	public Car(Car car, NeuralNetwork brain) {
		
		super(car.getModel(), car.getStartPosition(), car.getStartAngle(), new Vector3f(1, 1, 1));
		
		this.brain = brain;
		
		startPosition = car.getStartPosition();
		startAngle    = car.getStartAngle();
		
		boxDimentions = car.getBoxDimentions();
		
		boxVertices = car.getBoxVertices();
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
		this.box = new ModelEntity(box, startPosition, startAngle, new Vector3f(1, 1, 1));
		
		power = car.power;
		
		centroid = startPosition.add(new Vector3f(0, 0.3f, 0));
		frontRay = centroid.add(new Vector3f(0f, 0f, rayLength));
		leftRay  = centroid.add(new Vector3f(-rayLength * (float)Math.cos(rayAngle), 0f, rayLength * (float)Math.sin(rayAngle)));
		rightRay = centroid.add(new Vector3f(rayLength * (float)Math.cos(rayAngle), 0f, rayLength * (float)Math.sin(rayAngle)));
		
		angle       = startAngle.getY();
		maxVelocity = power * 100;
		minVelocity = power * 5;
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
	
	public void think(float[] inputs) {
		
		for (int i = 0; i < inputs.length; i++)
			inputs[i] = inputs[i] / rayLength * 2 - 1;
		float outputs[] = brain.feedforward(inputs);
		
		float accel = outputs[0];
		float steer = outputs[1];
		
		if (accel > 0)
			accelerate(accel);
		else
			stop(-accel);
		
		if (steer > 0)
			steerLeft(steer);
		else
			steerRight(-steer);
		
	}
	
	public void addScore() {
		
		score++;
	}
	
	public int getScore() {
		
		return score;
	}
	
	public void setScore(int score) {
		
		this.score = score;
	}
	
	public NeuralNetwork getBrain() {
		
		return brain;
	}
	
	public Vector3f getStartPosition() {
		
		return startPosition;
	}
	
	private void moveRays() {
		
		centroid = centroid.add(new Vector3f(-velocity * (float)Math.sin(angle), 0f, velocity * (float)Math.cos(angle)));
		frontRay = frontRay.add(new Vector3f(-velocity * (float)Math.sin(angle), 0f, velocity * (float)Math.cos(angle)));
		leftRay  = leftRay.add(new Vector3f(-velocity * (float)Math.sin(angle), 0f, velocity * (float)Math.cos(angle)));
		rightRay = rightRay.add(new Vector3f(-velocity * (float)Math.sin(angle), 0f, velocity * (float)Math.cos(angle)));
	}
	
	private void rotateRays() {
		
		Vector3f newFRayPosition = new Vector3f();
		newFRayPosition.setX((float)(centroid.getX() + (frontRay.getX() - centroid.getX()) * Math.cos(angularVelocity) - (frontRay.getZ() - centroid
					.getZ()) * Math.sin(angularVelocity)));
		newFRayPosition.setZ((float)(centroid.getZ() + (frontRay.getX() - centroid.getX()) * Math.sin(angularVelocity) + (frontRay.getZ() - centroid
					.getZ()) * Math.cos(angularVelocity)));
		newFRayPosition.setY(frontRay.getY());
		frontRay = newFRayPosition;
		
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
	
	public NeuralNetwork mutate(float mutationRate) {
		
		return this.brain.mutate(mutationRate);
	}
	
	public Vector3f getFrontRay() {
		
		return frontRay;
	}
	
	public float[] getBoxDimentions() {
		
		return boxDimentions;
	}
	
	public float[] getBoxVertices() {
		
		return boxVertices;
	}
	
	public Vector3f getLeftRay() {
		
		return leftRay;
	}
	
	public ModelEntity getBox() {
		
		return box;
	}
	
	public Vector3f getRightRay() {
		
		return rightRay;
	}
	
	public float getRayLength() {
		
		return rayLength;
	}
	
	public Vector3f getCentroid() {
		
		return centroid;
	}
	
	public float getAngle() {
		
		return angle;
	}
	
	public Vector3f getStartAngle() {
		
		return startAngle;
	}
	
	public void accelerate(float mul) {
		
		if (velocity < maxVelocity)
			velocity += power * mul;
	}
	
	public void stop(float mul) {
		
		if (velocity > 0)
			velocity -= power * mul;
	}
	
	public void steerLeft(float mul) {
		
		if (this.angularVelocity < this.maxAngularVelocity && this.velocity > this.minVelocity)
			angularVelocity += turnSpeed * mul;
	}
	
	public void steerRight(float mul) {
		
		if (this.angularVelocity > -this.maxAngularVelocity && this.velocity > this.minVelocity)
			angularVelocity -= turnSpeed * mul;
	}
	
	public void calcDistance() {
		
		distance = (float)Math.sqrt(Math.pow(centroid.getX() - startPosition.getX(), 2) + Math.pow(centroid.getZ() - startPosition.getZ(), 2));
	}
	
	public float getDistance() {
		
		return distance;
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
		this.frontRay   = centroid.add(new Vector3f(0f, 0f, rayLength));
		this.leftRay    = centroid.add(new Vector3f(-rayLength * (float)Math.cos(rayAngle), 0f, rayLength * (float)Math.sin(rayAngle)));
		this.rightRay   = centroid.add(new Vector3f(rayLength * (float)Math.cos(rayAngle), 0f, rayLength * (float)Math.sin(rayAngle)));
	}
}
