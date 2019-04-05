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
import objects.Obstacle;

public class Main {

	private static final int WIDTH = 1200, HEIGHT = 1000, FPS = 60;
	private static Window window = new Window(WIDTH, HEIGHT, FPS, "LWJGL");
	private static BasicShader shader = new BasicShader();
	private static Renderer renderer = new Renderer(shader, window);
	private static Camera camera = new Camera(new Vector3f(-1f, 0.5f, 0f), new Vector3f(0, 0, 0));

	public static void main(String[] args) {
		window.create();
		window.setBackgroundColor(1.0f, 1.0f, 1.0f);
		window.lockMouse();
		shader.create();
		renderer.update();
		TexturedModel car = Loader.loadModel("Car.obj", "Car4.png");
		TexturedModel floor = Loader.loadModel("floor.obj", "floor.png");
		TexturedModel brick = Loader.loadModel("brick.obj", "brick.png");
		Car carEntity = new Car(car, new Vector3f(0, 0, 0f), new Vector3f(0f, 0f, 0f), new Vector3f(1f, 1f, 1f), 0.001f);
		ModelEntity floorEntity = new ModelEntity(floor, new Vector3f(0f, -1f, 0f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
		ModelEntity brickEntity1 = new ModelEntity(brick, new Vector3f(1f, 0f, 3f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
		ModelEntity brickEntity2 = new ModelEntity(brick, new Vector3f(5f, 0f, 10f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
		ModelEntity brickEntity3 = new ModelEntity(brick, new Vector3f(10f, 0f, 7f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
		Obstacle obstacles = new Obstacle(new ModelEntity[]{brickEntity1, brickEntity2, brickEntity3}, 10f);
		renderer.proseeEntity(carEntity);
		renderer.proseeEntity(carEntity.box);
		renderer.proseeEntity(floorEntity);
		obstacles.render(renderer);
		while (!window.closed()) {
			if (window.isUpdating()) {
				keyActions(carEntity, obstacles);
				window.update();
				renderer.update();
				camera.update(window);
				carEntity.update();
				renderer.loadCamera(camera);
				shader.bind();
				shader.useMatrices();
				renderer.render();
				shader.unbind();
				window.swapBuffers();
			}
		}

		car.remove();
		floor.remove();
		brick.remove();
		shader.remove();
		window.stop();
	}

	public static void keyActions(Car car, Obstacle obstacles) {
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
	}
}