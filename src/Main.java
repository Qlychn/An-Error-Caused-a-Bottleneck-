import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBEasyFont;

import java.nio.ByteBuffer;

public class Main {
    public static void main(String[] args) {
        GLFW.glfwInit();
        long win = GLFW.glfwCreateWindow(300, 500, "2D", 0, 0);
        GLFW.glfwMakeContextCurrent(win);
        GL.createCapabilities();

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glOrtho(0, 300, 500, 0, -1, 1);


        while (!GLFW.glfwWindowShouldClose(win)) { //main loop
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            // Draw "Hi"
            ByteBuffer mesh = BufferUtils.createByteBuffer(2 * 270);
            int quads = STBEasyFont.stb_easy_font_print(0, 0, "Hi", null, mesh);

            GL11.glPushMatrix();
            GL11.glTranslatef(140, 240, 0); // Position
            GL11.glScalef(3, 3, 1);         // Size

            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glVertexPointer(2, GL11.GL_FLOAT, 16, mesh);
            GL11.glDrawArrays(GL11.GL_QUADS, 0, quads * 4);
            GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

            GL11.glPopMatrix();

            // Display frame & fetch events
            GLFW.glfwSwapBuffers(win);
            GLFW.glfwPollEvents();
        }
    }
}