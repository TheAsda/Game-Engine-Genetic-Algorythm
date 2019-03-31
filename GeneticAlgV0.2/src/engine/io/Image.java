package engine.io;

import java.nio.ByteBuffer;

public class Image {
    public ByteBuffer getImage() {
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return heigh;
    }

    private ByteBuffer image;
    private int width, heigh;

    Image(int width, int heigh, ByteBuffer image) {
        this.image = image;
        this.heigh = heigh;
        this.width = width;
    }
}