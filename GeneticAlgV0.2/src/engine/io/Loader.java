package engine.io;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import engine.io.obj.ModelData;
import engine.io.obj.OBJLoader;
import engine.rendering.models.TexturedModel;

public class Loader {

	public static Image loadImage(String path) {
		ByteBuffer image;
		int width, heigh;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer comp = stack.mallocInt(1);
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);

			image = STBImage.stbi_load(path, w, h, comp, 4);
			if (image == null) {
				System.err.println("Couldn't load " + path);
			}
			width = w.get();
			heigh = h.get();
		}
		return new Image(width, heigh, image);
	}

	public static TexturedModel loadModel(String objPath,String texturePath) {
		ModelData data=OBJLoader.loadOBJ(objPath);
		return new TexturedModel(data.getVertices(),data.getTextureCoords(),data.getNormals(),data.getIndices(),texturePath);
	}
}
