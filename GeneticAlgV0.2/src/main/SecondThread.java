package main;

import java.util.ArrayList;

import engine.io.Window;
import main.Main.Mutation;
import objects.Car;
import objects.CollisionThread;
import objects.Obstacle;

public class SecondThread implements Runnable {
	
	private static Thread                     thread;
	private static double                     time             = (double)System.nanoTime() / (double)1000000000;
	private Window                            window;
	private Car                               carEntities[];
	private int                               population;
	private Obstacle                          obstacles;
	private static ArrayList<CollisionThread> collisionThreads = new ArrayList<CollisionThread>(3);
	private Mutation                          mutate;
	
	SecondThread(Window window, Car carEntities[], int population, Obstacle obstacles, Mutation mutate) {
		
		for (int i = 0; i < 3; i++)
			collisionThreads.add(new CollisionThread());
		
		this.window      = window;
		this.carEntities = carEntities;
		this.population  = population;
		this.obstacles   = obstacles;
		this.mutate      = mutate;
		
		thread = new Thread(this);
		thread.start();
	}
	
	@Override
	public void run() {
		
		int counter = 0;
		
		while (!window.closed()) {
			if (window.isUpdating(1) && mutate.isMutating() == false) {
				for (int k = 0; k < population; k++) {
					if (carEntities[k].isRender() == true) {
						carEntities[k].update();
						
						float result[] = obstacles.raysCollision(carEntities[k], collisionThreads);
						
						if (result == null)
							System.out.println("No obstacles around");
						else {
							//System.out.println("Front ray dist: " + result[0]);
							//System.out.println("Left ray dist: " + result[1]);
							//System.out.println("Right ray dist: " + result[2]);
							carEntities[k].think(result);
						}
						if (counter % 3 == 0) {
							
							carEntities[k].calcDistance();
							
							if ((double)System.nanoTime() / (double)1000000000 - time > 12) {
								carEntities[k].setRender(false);
							}
							
							if (obstacles.detectCollision(carEntities[k])) {
								System.out.println("Dead");
								carEntities[k].setRender(false);
							}
						}
					}
				}
				counter++;
			}
		}
	}
	
	public void resetTime() {
		
		time = (double)System.nanoTime() / (double)1000000000;
	}
}
