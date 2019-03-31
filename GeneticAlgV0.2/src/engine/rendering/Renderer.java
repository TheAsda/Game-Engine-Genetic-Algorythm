package engine.rendering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import engine.io.Window;
import engine.maths.Matrix4f;
import engine.rendering.models.ModelEntity;
import engine.rendering.models.TexturedModel;
import engine.shaders.BasicShader;

public class Renderer {

	private BasicShader shader;
	private Window window;
	private Map<TexturedModel, List<ModelEntity>> entities = new HashMap();

	public Renderer(BasicShader shader, Window window) {
		this.shader = shader;
		this.window = window;
	}

	public void update() {
		shader.loadProjectionMatrix(new Matrix4f().projection(70.0f, (float)window.getWidth() / window.getHeight(), 0.1f, 1000.0f));
	}

	public void loadCamera(Camera camera) {
		shader.loadViewMatrix(camera.getViewMatrix());
	}

	public void proseeEntity(ModelEntity entity) {
		TexturedModel model = entity.getModel();
		List<ModelEntity> entities = this.entities.get(model);
		if (entities != null) {
			entities.add(entity);
		}
		else {
			List<ModelEntity> newList = new ArrayList<>();
			newList.add(entity);
			this.entities.put(model, newList);
		}
	}

	public void render() {
		for (TexturedModel model : entities.keySet()) {
			GL30.glBindVertexArray(model.getVertexArrayID());
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
			List<ModelEntity> list = entities.get(model);
			for (ModelEntity entity : list) {
				shader.loadTransformationMatrix(entity.getTransformationMatrix());
				shader.useMatrices();
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, entity.getModel().getMaterial().getTextureID());
				GL11.glDrawElements(GL11.GL_TRIANGLES, entity.getModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);

			}
			GL20.glDisableVertexAttribArray(0);
			GL20.glDisableVertexAttribArray(1);
			GL30.glBindVertexArray(0);
		}
	}
}