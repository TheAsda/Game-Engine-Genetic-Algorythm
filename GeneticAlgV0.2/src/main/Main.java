package main;

import javax.print.attribute.SetOfIntegerSyntax;

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
	
	public static class Mutation {
		
		private boolean value = false;
		
		public boolean isMutating() {
			
			return value;
		}
		
		public void setValue(boolean value) {
			
			this.value = value;
		}
	}
	
	private static final int     WIDTH         = 1200, HEIGHT = 1000, FPS = 60;
	private static final String  CAR_OBJ       = "Car.obj", CAR_TEXTURE = "Car4.png", FLOOR_OBJ = "floor.obj", FLOOR_TEXTURE = "floor.png",
				BRICK_OBJ = "brick.obj", BRICK_TEXTURE = "brick.png", FINISH_OBJ = "finish.obj", FINISH_TEXTURE = "finish.png";
	private static Window        window        = new Window(WIDTH, HEIGHT, FPS, "Car Game");
	private static BasicShader   shader        = new BasicShader();
	private static Renderer      renderer      = new Renderer(shader, window);
	private static Camera        camera        = new Camera(new Vector3f(0f, 1f, 0f), new Vector3f(0, 0, 0));
	private static Camera        editorsCamera = new Camera(new Vector3f(0f, 10f, 5.5f), new Vector3f(-90, 0, 0));
	private static boolean       editorsMode   = false;
	private static char          currentType;
	private static ModelEntity   currentModel  = null;
	private static Obstacle      obstacles;
	private static Finish        finishes;
	private static TexturedModel car;
	private static TexturedModel brick;
	private static TexturedModel finish;
	private final static int     population    = 5;
	
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
		
		Car carEntities[] = new Car[population];
		for (int i = 0; i < population; i++) {
			carEntities[i] = new Car(car, new Vector3f(0f, 0f, 0f), new Vector3f(0, 0, 0), 0.001f);
			renderer.proseeEntity(carEntities[i]);
		}
		ModelEntity floorEntity = new ModelEntity(floor, new Vector3f(0f, -1f, 0f), new Vector3f(0, 0, 0), new Vector3f(1f, 1f, 1f));
		
		obstacles = new Obstacle(carEntities[0].getRayLength());
		finishes  = new Finish(carEntities[0].getRayLength());
		
		finishes.loadFromJSON(finish);
		obstacles.loadFromJSON(brick);
		
		renderer.proseeEntity(floorEntity);
		obstacles.render(renderer);
		finishes.render(renderer);
		
		Mutation mutate = new Mutation();
		
		SecondThread thread = new SecondThread(window, carEntities, population, obstacles, mutate);
		
		while (!window.closed()) {
			if (window.isUpdating(0) && mutate.isMutating() == false) {
				keyActions();
				
				int check = 0;
				for (int j = 0; j < population; j++) {
					if (carEntities[j].isRender() == false) {
						check++;
					}
				}
				
				if (check == population) {
					
					mutate.setValue(true);
					
					for (int j = 0; j < population; j++) {
						carEntities[j].calcDistance();
					}
					
					carEntities = Population.nextGeneration(carEntities);
					
					for (int j = 0; j < population; j++) {
						renderer.proseeEntity(carEntities[j]);
					}
					
					thread.resetTime();
					
					mutate.setValue(false);
					
					continue;
				}
				
				window.update();
				renderer.update(); 
				
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