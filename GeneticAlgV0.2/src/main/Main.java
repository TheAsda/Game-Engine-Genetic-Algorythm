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

public class Main {

	private static final int WIDTH = 800, HEIGHT = 600, FPS = 60;
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
		TexturedModel model = Loader.loadModel("Car.obj", "Car4.png");
		TexturedModel floor = Loader.loadModel("floor.obj", "floor.png");
		TexturedModel brick = Loader.loadModel("brick.obj", "brick.png");
		ModelEntity entity = new ModelEntity(model, new Vector3f(0, 0, 1f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
		ModelEntity floorEntity = new ModelEntity(floor, new Vector3f(0f, -1f, 0f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
		ModelEntity brickEntity = new ModelEntity(brick, new Vector3f(0f, 0.0f, 0f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
		renderer.proseeEntity(entity);
		renderer.proseeEntity(floorEntity);
		renderer.proseeEntity(brickEntity);
		while (!window.closed()) {
			if (window.isUpdating()) {
				keyActions();
				window.update();
				renderer.update();
				camera.update(window);
				renderer.loadCamera(camera);
				shader.bind();
				shader.useMatrices();
				renderer.render();
				shader.unbind();
				window.swapBuffers();
			}
		}

		model.remove();
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
	}
}