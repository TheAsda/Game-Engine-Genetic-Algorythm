package main;

import org.lwjgl.glfw.GLFW;

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
	private static Camera camera = new Camera(new Vector3f(-1f, 0.5f, 0f), new Vector3f(0, 0, 0));
	private static Camera editorsCamera = new Camera(new Vector3f(0f, 10f, 5.5f), new Vector3f(-90, 0, 0));
	private static boolean editorsMode = false;
	private static char currentType;
	private static ModelEntity currentModel = null;
	private static Obstacle obstacles;
	private static Finish finishes;
	private static TexturedModel car;
	private static TexturedModel brick;
	private static TexturedModel finish;

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

		Car carEntity = new Car(car, new Vector3f(0, 0, 0f), new Vector3f(0f, 0f, 0f), new Vector3f(1f, 1f, 1f), 0.001f);
		ModelEntity floorEntity = new ModelEntity(floor, new Vector3f(0f, -1f, 0f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
		ModelEntity monkeyEntity = new ModelEntity(monkey, carEntity.getFrontRay(), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));

		obstacles = new Obstacle(carEntity.getRayLength());
		finishes  = new Finish(carEntity.getRayLength());

		obstacles.loadFromJSON(brick);
		finishes.loadFromJSON(finish);

		renderer.proseeEntity(carEntity);
		renderer.proseeEntity(floorEntity);
		renderer.proseeEntity(monkeyEntity);
		obstacles.render(renderer);
		finishes.render(renderer);

		int i = 0;

		while (!window.closed()) {
			if (window.isUpdating()) {
				keyActions(carEntity);

				window.update();
				renderer.update();
				carEntity.update();

				monkeyEntity.setPosition(carEntity.getFrontRay());

				if (editorsMode == false) {
					camera.update(window);
					renderer.loadCamera(camera);
				}
				else {
					renderer.loadCamera(editorsCamera);
				}

				//finishes.finishCheck(carEntity);

				shader.bind();
				shader.useMatrices();
				renderer.render();
				shader.unbind();
				window.swapBuffers();
				i++;
			}
			if (i % 5 == 0) {
				obstacles.raysCollision(carEntity);
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

	public static void keyActions(Car car) {
		if (window.isKeyPressed(GLFW.GLFW_KEY_ESCAPE))
			window.close();
		if (window.isKeyPressed(GLFW.GLFW_KEY_L))
			window.lockMouse();
		if (window.isKeyPressed(GLFW.GLFW_KEY_U))
			window.unlockMouse();
		if (window.isKeyDown(GLFW.GLFW_KEY_UP))
			car.accelerate();
		if (window.isKeyDown(GLFW.GLFW_KEY_DOWN))
			car.stop();
		if (window.isKeyDown(GLFW.GLFW_KEY_LEFT))
			car.steerLeft();
		if (window.isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
			car.steerRight();
		}
		if (window.isKeyDown(GLFW.GLFW_KEY_R)) {
			car.reset();
		}
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
		}
		if (window.isKeyPressed(GLFW.GLFW_KEY_E)) {
			editorsMode = !editorsMode;
			if (editorsMode = true)
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