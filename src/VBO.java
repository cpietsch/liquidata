/**
 * A reusable, barebones wrapper for OpenGL Vertex Buffer Objects. Only
 * handles actual vertex, normal and color buffers and assumes they can be dynamically
 * modified/updated.
 * 
 * @author Karsten Schmidt <info at postspectacular.com>
 */
import javax.media.opengl.GL;

import com.sun.opengl.util.BufferUtil;

public class VBO {

  final int STRIDE = BufferUtil.SIZEOF_FLOAT * 4;

  int numVertices;
  int[] vertID = new int[] {-1};
  int[] normID = new int[] {-1};
  int[] colorID = new int[] {-1};

  private GL gl;

  public VBO(GL gl, int num) {
    this.gl = gl;
    numVertices = num;
  }

  public void cleanup() {
    gl.glDeleteBuffers(1, vertID, 0);
    gl.glDeleteBuffers(1, normID, 0);
    gl.glDeleteBuffers(1, colorID, 0);
  }

  void initBuffer(int[] bufferID) {
    gl.glGenBuffersARB(1, bufferID, 0);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, bufferID[0]);
    gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, numVertices * STRIDE, null, 
    GL.GL_DYNAMIC_DRAW_ARB);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
  }

  public void render(int shapeID) {
    // gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
    // check if we need to use normals?
    if (normID[0] != -1) {
      gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
      gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, normID[0]);
      gl.glNormalPointer(GL.GL_FLOAT, STRIDE, 0);
    }
    // check if we need to use colors?
    if (colorID[0] != -1) {
      gl.glEnableClientState(GL.GL_COLOR_ARRAY);
      gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, colorID[0]);
      gl.glColorPointer(4, GL.GL_FLOAT, STRIDE, 0);
    }
    gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, vertID[0]);
    gl.glVertexPointer(3, GL.GL_FLOAT, STRIDE, 0);
    gl.glDrawArrays(shapeID, 0, numVertices);
    gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
    if (normID[0] != -1) {
      gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
    }
    if (colorID[0] != -1) {
      gl.glDisableClientState(GL.GL_COLOR_ARRAY);
    }
  }

  protected void updateBuffer(int id, float[] data) {
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, id);
    gl.glMapBufferARB(GL.GL_ARRAY_BUFFER_ARB, GL.GL_WRITE_ONLY)
      .asFloatBuffer().put(data);
    gl.glUnmapBufferARB(GL.GL_ARRAY_BUFFER_ARB);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
  }

  public void updateColors(float[] colors) {
    if (colorID[0] == -1) {
      initBuffer(colorID);
    }
    updateBuffer(colorID[0], colors);
  }

  public void updateNormals(float[] normals) {
    if (normID[0] == -1) {
      initBuffer(normID);
    }
    updateBuffer(normID[0], normals);
  }

  public void updateVertices(float[] vertices) {
    if (vertID[0] == -1) {
      initBuffer(vertID);
    }
    updateBuffer(vertID[0], vertices);
  }
}
