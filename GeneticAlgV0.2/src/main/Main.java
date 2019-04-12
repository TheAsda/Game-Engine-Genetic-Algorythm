package main;

import org.lwjgl.glfw.GLFW;

import NeuralNetwork.Population;
import engine.io.Loader;
import engine.io.Window;
import engine.maths.Vector3f;
import engine.rendering.Camera;
import engine.rendering.Renderer;
import engine.rendering.models.ModelEntity;
import engine.rendering.models.TexturedModel;
import engine.shaders.BasicShader;
import objects.Car;
import objects.Finish;
import objects.Obstacle;

public class Main {

	private static final int WIDTH = 1200, HEIGHT = 1000, FPS = 60;
	private static final String CAR_OBJ = "Car.obj", CAR_TEXTURE = "Car4.png", FLOOR_OBJ = "floor.obj", FLOOR_TEXTURE = "floor.png",
		BRICK_OBJ = "brick.obj", BRICK_TEXTURE = "brick.png", FINISH_OBJ = "finish.obj", FINISH_TEXTURE = "finish.png";
	private static Window window = new Window(WIDTH, HEIGHT, FPS, "Car Game");
	private static BasicShader shader = new BasicShader();
	private static Renderer renderer = new Renderer(shader, window);
	private static Camera camera = new Camera(new Vector3f(0f, 1f, 0f), new Vector3f(0, 0, 0));
	private static Camera editorsCamera = new Camera(new Vector3f(0f, 10f, 5.5f), new Vector3f(-90, 0, 0));
	private static boolean editorsMode = false;
	private static char currentType;
	private static ModelEntity currentModel = null;
	private static Obstacle obstacles;
	private static Finish finishes;
	private static TexturedModel car;
	private static TexturedModel brick;
	private static TexturedModel finish;
	private final static int population = 10;
	private static boolean renderedModels[] = new boolean[population];

	public static void main(String[] args) {
		window.create();
		window.setBackgroundColor(1.0f, 1.0f, 1.0f);
		window.lockMouse();
		shader.create();
		renderer.update();

		car    = Loader.loadModel(CAR_OBJ, CAR_TEXTURE);
		brick  = Loader.loadModel(BRICK_OBJ, BRICK_TEXTURE);
		finish = Loader.loadModel(FINISH_OBJ, FINISH_TEXTURE);

		TexturedModel floor = Loader.loadModel(FLOOR_OBJ, FLOOR_TEXTURE);
		TexturedModel monkey = Loader.loadModel("monkey.obj", "monkey.png");

		Car carEntities[] = new Car[population];
		Car carEntitiesDone[] = new Car[population];
		for (int i = 0; i < population; i++) {
			carEntities[i] = new Car(car, new Vector3f(0f, 0f, 0f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f), 0.001f);
			renderer.proseeEntity(carEntities[i]);
		}
		ModelEntity floorEntity = new ModelEntity(floor, new Vector3f(0f, -1f, 0f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
		//ModelEntity monkeyEntity = new ModelEntity(monkey, carEntity.getFrontRay(), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));

		obstacles = new Obstacle(carEntities[0].getRayLength());
		finishes  = new Finish(carEntities[0].getRayLength());

		finishes.loadFromJSON(finish);
		obstacles.loadFromJSON(brick);

		renderer.proseeEntity(floorEntity);
		//renderer.proseeEntity(monkeyEntity);
		obstacles.render(renderer);
		finishes.render(renderer);
		//renderer.proseeEntity(carEntity.box);

		int i = 0;
		int counter = 0;
		while (!window.closed()) {
			if (window.isUpdating()) {
				keyActions();

				window.update();
				renderer.update();
				for (int k = 0; k < population; k++) {
					if (carEntities[k] != null)
						carEntities[k].update();
				}

				if (editorsMode == false) {
					camera.update(window);
					renderer.loadCamera(camera);
				}
				else {
					renderer.loadCamera(editorsCamera);
				}

				shader.bind();
				shader.useMatrices();
				renderer.render();
				shader.unbind();
				window.swapBuffers();

				i++;
			}
			if (i % 5 == 0) {
				for (int k = 0; k < population; k++) {
					int check = 0;
					for (int j = 0; j < population; j++) {
						if (carEntities[j] == null) {
							check++;
						}
					}

					if (check == population) {
						carEntities     = Population.nextGeneration(carEntitiesDone, car, population);
						carEntitiesDone = new Car[population];
						for (int j = 0; j < population; j++) {
							renderer.proseeEntity(carEntities[j]);
						}
						break;
					}

					if (carEntities[k] == null)
						continue;

					carEntities[k].addScore();
					counter++;

					float result[] = obstacles.raysCollision(carEntities[k]);
					if (result == null)
						System.out.println("No obstacles around");
					else {
						//System.out.println("Front ray dist: " + result[0]);
						//System.out.println("Left ray dist: " + result[1]);
						//System.out.println("Right ray dist: " + result[2]);
						carEntities[k].think(result);
					}

					if (finishes.finishCheck(carEntities[k])) {
						System.out.println("Final");
					}
					if ((carEntities[k].getScore() > 12 * 10 &&
						carEntities[k].getCentroid().getX() == carEntities[k].getStartPosition().getX() &&
						carEntities[k].getCentroid().getZ() == carEntities[k].getStartPosition().getZ()) ||
						counter >= 12 * 20) {
						carEntities[k].setRender(false);
						carEntities[k].setScore(0);
						carEntitiesDone[k] = carEntities[k];
						carEntities[k]     = null;
						counter            = 0;
						continue;
					}

					if (obstacles.detectCollision(carEntities[k])) {
						carEntities[k].setRender(false);
						carEntitiesDone[k] = carEntities[k];
						carEntities[k]     = null;
					}
				}

				i = 1;
			}
		}
		finish.remove();
		car.remove();
		floor.remove();
		brick.remove();
		shader.remove();
		window.stop();
	}

	public static void keyActions() {
		if (window.isKeyPressed(GLFW.GLFW_KEY_ESCAPE))
			window.close();
		if (window.isKeyPressed(GLFW.GLFW_KEY_L))
			window.lockMouse();
		if (window.isKeyPressed(GLFW.GLFW_KEY_U))
			window.unlockMouse();
		/*if (window.isKeyDown(GLFW.GLFW_KEY_UP))
			car.accelerate();
		if (window.isKeyDown(GLFW.GLFW_KEY_DOWN))
			car.stop();
		if (window.isKeyDown(GLFW.GLFW_KEY_LEFT))
			car.steerLeft();
		if (window.isKeyDown(GLFW.GLFW_KEY_RIGHT))
			car.steerRight();
		if (window.isKeyDown(GLFW.GLFW_KEY_R))
			car.reset();
		if (window.isKeyPressed(GLFW.GLFW_KEY_C)) {
			float result[] = obstacles.raysCollision(car);
			if (result == null)
				System.out.println("No obstacles around");
			else {
				System.out.println("Front ray dist: " + result[0]);
				System.out.println("Left ray dist: " + result[1]);
				System.out.println("Right ray dist: " + result[2]);
			}
		}
		if (window.isKeyPressed(GLFW.GLFW_KEY_P)) {
			System.out.println(obstacles.detectCollision(car));
			System.out.println(finishes.finishCheck(car));
		}*/
		if (window.isKeyPressed(GLFW.GLFW_KEY_E)) {
			editorsMode = !editorsMode;
			if (editorsMode == true)
				window.unlockMouse();
			else
				window.lockMouse();
		}
		if (window.isKeyPressed(GLFW.GLFW_KEY_Z)) {
			obstacles.saveToJSON();
			finishes.saveToJSON();
		}
		if (window.isKeyPressed(GLFW.GLFW_KEY_X)) {
			obstacles.loadFromJSON(brick);
			finishes.loadFromJSON(finish);
			obstacles.render(renderer);
			finishes.render(renderer);
		}
		if (editorsMode == true) {
			Vector3f vec = window.getMousePosition(WIDTH, HEIGHT);
			if (window.isKeyPressed(GLFW.GLFW_KEY_B) && (currentModel == null || currentType == 'f')) {
				if (currentType == 'f')
					renderer.remove(finish);
				currentType  = 'b';
				currentModel = new ModelEntity(brick, vec, new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
				renderer.proseeEntity(currentModel);
			}
			if (window.isKeyPressed(GLFW.GLFW_KEY_F) && (currentModel == null || currentType == 'b')) {
				if (currentType == 'b')
					renderer.remove(brick);
				currentType  = 'f';
				currentModel = new ModelEntity(finish, vec, new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
				renderer.proseeEntity(currentModel);
			}
			if (window.isKeyPressed(GLFW.GLFW_KEY_KP_ADD) && currentModel != null) {
				System.out.println("plus");
				currentModel.addRotation(0, -7, 0);
			}
			if (window.isKeyPressed(GLFW.GLFW_KEY_KP_SUBTRACT) && currentModel != null) {
				System.out.println("minus");
				currentModel.addRotation(0, 7, 0);
			}
			if (currentModel != null) {
				currentModel.setPosition(vec);
				if (window.isMousePressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
					if (currentType == 'b')
						obstacles.add(currentModel);
					if (currentType == 'f')
						finishes.add(currentModel);
					currentModel = null;
					currentType  = ' ';
				}
			}
		}
	}
}