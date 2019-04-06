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
	private static Window window = new Window(WIDTH, HEIGHT, FPS, "LWJGL");
	private static BasicShader shader = new BasicShader();
	private static Renderer renderer = new Renderer(shader, window);
	private static Camera camera = new Camera(new Vector3f(-1f, 0.5f, 0f), new Vector3f(0, 0, 0));
	private static Camera editorsCamera = new Camera(new Vector3f(0f, 10f, 5.5f), new Vector3f(-90, 0, 0));
	private static boolean editorsMode = false;
	private static char currentType;
	private static ModelEntity currentModel = null;
	private static Obstacle obstacles = new Obstacle(10f);
	private static Finish finishes = new Finish(10f);

	public static void main(String[] args) {
		window.create();
		window.setBackgroundColor(1.0f, 1.0f, 1.0f);
		window.lockMouse();
		shader.create();
		renderer.update();
		TexturedModel car = Loader.loadModel(CAR_OBJ, CAR_TEXTURE);
		TexturedModel floor = Loader.loadModel(FLOOR_OBJ, FLOOR_TEXTURE);
		TexturedModel brick = Loader.loadModel(BRICK_OBJ, BRICK_TEXTURE);
		TexturedModel finish = Loader.loadModel(BRICK_OBJ, BRICK_TEXTURE);
		Car carEntity = new Car(car, new Vector3f(0, 0, 0f), new Vector3f(0f, 0f, 0f), new Vector3f(1f, 1f, 1f), 0.001f);
		ModelEntity floorEntity = new ModelEntity(floor, new Vector3f(0f, -1f, 0f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
		/*ModelEntity finishEntity = new ModelEntity(finish, new Vector3f(5f, 0f, 10f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
		ModelEntity brickEntity1 = new ModelEntity(brick, new Vector3f(0.8f, 0f, 0f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
		ModelEntity brickEntity2 = new ModelEntity(brick, new Vector3f(-0.8f, 0f, 0f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
		ModelEntity brickEntity3 = new ModelEntity(brick, new Vector3f(0.8f, 0f, 1.2f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
		ModelEntity brickEntity4 = new ModelEntity(brick, new Vector3f(-0.8f, 0f, 1.2f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
		ModelEntity brickEntity5 = new ModelEntity(brick, new Vector3f(-0.5f, 0f, 2.35f), new Vector3f(0, -30f, 0), new Vector3f(1f, 1f, 1f));
		ModelEntity brickEntity6 = new ModelEntity(brick, new Vector3f(1.1f, 0f, 2.28f), new Vector3f(0, -30f, 0), new Vector3f(1f, 1f, 1f));
		ModelEntity brickEntity7 = new ModelEntity(brick, new Vector3f(0f, 0f, 3.5f), new Vector3f(0, -30, 0), new Vector3f(1f, 1f, 1f));
		ModelEntity brickEntity8 = new ModelEntity(brick, new Vector3f(10f, 0f, 7f), new Vector3f(0, -30, 0), new Vector3f(1f, 1f, 1f));
		Obstacle obstacles = new Obstacle(
			new ModelEntity[]{brickEntity1, brickEntity2, brickEntity3, brickEntity4, brickEntity5, brickEntity6, brickEntity7, brickEntity8}, 10f
		);
		Finish finishes = new Finish(new ModelEntity[]{finishEntity}, 10f);*/
		renderer.proseeEntity(carEntity);
		//renderer.proseeEntity(carEntity.box);
		renderer.proseeEntity(floorEntity);
		obstacles.render(renderer);
		finishes.render(renderer);

		while (!window.closed()) {
			if (window.isUpdating()) {
				keyActions(carEntity);
				window.update();
				renderer.update();
				carEntity.update();
				if (editorsMode == false) {
					camera.update(window);
					renderer.loadCamera(camera);
				}
				else {
					renderer.loadCamera(editorsCamera);
				}
				finishes.finishCheck(carEntity);
				shader.bind();
				shader.useMatrices();
				renderer.render();
				shader.unbind();
				window.swapBuffers();
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
		if (window.isKeyPressed(GLFW.GLFW_KEY_P)) {
			System.out.println(obstacles.detectCollision(car));
		}
		if (window.isKeyPressed(GLFW.GLFW_KEY_E)) {
			editorsMode = !editorsMode;
			window.unlockMouse();
		}
		if(window.isKeyPressed(GLFW.GLFW_KEY_Z)) {
			obstacles.saveToJSON();
		}
		if (editorsMode == true) {
			Vector3f vec = window.getMousePosition(WIDTH, HEIGHT);
			if (window.isKeyPressed(GLFW.GLFW_KEY_B)) {
				currentType = 'b';
				TexturedModel brick = Loader.loadModel(BRICK_OBJ, BRICK_TEXTURE);
				currentModel = new ModelEntity(brick, vec, new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
				renderer.proseeEntity(currentModel);
			}
			if (window.isKeyPressed(GLFW.GLFW_KEY_F)) {
				currentType = 'f';
				TexturedModel finish = Loader.loadModel(FINISH_OBJ, FINISH_TEXTURE);
				currentModel = new ModelEntity(finish, vec, new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
				renderer.proseeEntity(currentModel);
			}
			if (window.isKeyPressed(GLFW.GLFW_KEY_KP_ADD)) {
				System.out.println("plus");
				currentModel.addRotation(0, -5, 0);
			}
			if (window.isKeyPressed(GLFW.GLFW_KEY_KP_SUBTRACT)) {
				System.out.println("minus");
				currentModel.addRotation(0, 5, 0);
			}
			if (currentModel != null) {
				currentModel.setPosition(vec);
				if (window.isMousePressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
					if (currentType == 'b')
						obstacles.add(currentModel);
					if (currentType == 'f')
						finishes.add(currentModel);
					currentModel = null;
				}
			}
		}
	}
}