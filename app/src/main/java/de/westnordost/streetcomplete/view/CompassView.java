package de.westnordost.streetcomplete.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.AttributeSet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CompassView extends GLSurfaceView
{
	private float rotation, tilt;

	public CompassView(Context ctx){
		super(ctx);
		init();
	}

	public CompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setEGLContextClientVersion(1);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		setZOrderMediaOverlay(true);

		setRenderer(new CompassRenderer());
	}

	public void setOrientation(float rotation, float tilt)
	{
		this.rotation = rotation;
		this.tilt = tilt;
	}

	private class CompassRenderer implements Renderer
	{
		private ByteBuffer indexBuffer;
		private FloatBuffer vertexBuffer;

		private byte indices[] = { 0,1,2,3 };
		private float vertices[] = { 0,1, 0.7f,-1, 0,-0.7f, -0.7f,-1 };

		public CompassRenderer()
		{
			ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
			vbb.order(ByteOrder.nativeOrder());
			vertexBuffer = vbb.asFloatBuffer();
			vertexBuffer.put(vertices);
			vertexBuffer.position(0);

			indexBuffer = ByteBuffer.allocateDirect(indices.length);
			indexBuffer.put(indices);
			indexBuffer.position(0);
		}

		@Override public void onSurfaceCreated(GL10 gl, EGLConfig config)
		{
			gl.glClearColor(1, 1, 1, 0);
		}

		@Override public void onSurfaceChanged(GL10 gl, int width, int height)
		{
			gl.glViewport(0, 0, width, height);
			float ratio = (float) width / height;
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);
		}

		@Override public void onDrawFrame(GL10 gl)
		{
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
			GLU.gluLookAt(gl, 0,0,-5, 0,0,0, 0,1,0);

			gl.glScalef(1.25f, 1.25f,1.25f);
			gl.glRotatef(tilt * 180 / (float) Math.PI, 1, 0, 0);
			gl.glRotatef(rotation * 180 / (float) Math.PI, 0, 0, 1);

			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

			gl.glColor4f(0.8f,0.2f,0.2f,1);
			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);
			gl.glDrawElements(GL10.GL_TRIANGLE_FAN, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);
		}
	}
}
