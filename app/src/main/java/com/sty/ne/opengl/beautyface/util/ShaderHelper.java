package com.sty.ne.opengl.beautyface.util;

import android.util.Log;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FALSE;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;

/**
 * 着色器工具栏
 */
public class ShaderHelper {
    private static final String TAG = ShaderHelper.class.getSimpleName();
    private static final boolean DEBUG = true;

    /**
     * 加载并编译着色器代码
     * @param type 着色器类型（GL_VERTEX_SHADER, GL_FRAGMENT_SHADER）
     * @param shaderCode 着色器代码
     * @return 着色器ID（返回0表示失败）
     */
    private static int compileShader(int type, String shaderCode) {
        //创建着色器
        final int shaderObjectId = glCreateShader(type);
        if(shaderObjectId == 0) {
            if(DEBUG) {
                Log.w(TAG, "创建着色器失败");
                return 0;
            }
        }
        //上传（加载）着色器代码
        glShaderSource(shaderObjectId, shaderCode);
        //编译着色器代码
        glCompileShader(shaderObjectId);
        //获取编译状态
        int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);
        if(DEBUG) {
            Log.v(TAG, "编译着色器代码：\n");
            Log.i(TAG, shaderCode);
        }
        //判断编译状态
        if(compileStatus[0] == GL_FALSE) {
            if(DEBUG) {
                Log.w(TAG, "着色器编译失败：\n");
                Log.i(TAG, shaderCode);
            }
            //如果失败，删除着色器对象
            glDeleteShader(shaderObjectId);
            return 0;
        }
        return shaderObjectId;
    }

    /**
     * 加载并编译顶点着色器
     * @param shaderCode 顶点着色器代码
     * @return 顶点着色器ID（返回0表示失败）
     */
    public static int compileVertexShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    /**
     * 加载并编译片元着色器
     * @param shaderCode 片元着色器代码
     * @return 片元着色器ID（返回0表示失败）
     */
    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    /**
     * 将顶点着色器和片元着色器一起链接到OpenGL程序中
     * @param vertexShaderId  顶点着色器ID
     * @param fragmentShaderId  片元着色器ID
     * @return 程序ID（返回0表示链接失败）
     */
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        //创建一个新的OpenGL程序对象
        final int programObjectId = glCreateProgram();
        if(programObjectId == 0) {
            if(DEBUG) {
                Log.e(TAG, "创建程序失败");
            }
            return 0;
        }
        //Attach 顶点着色器
        glAttachShader(programObjectId, vertexShaderId);
        //Attach 片元着色器
        glAttachShader(programObjectId, fragmentShaderId);
        //将两个着色器一起链接到OpenGL程序
        glLinkProgram(programObjectId);
        //获取链接状态
        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);
        //判断链接状态
        if(linkStatus[0] == GL_FALSE) {
            if(DEBUG) {
                Log.w(TAG, "链接程序失败：\n");
                Log.i(TAG, glGetProgramInfoLog(programObjectId));
            }
            //如果失败，删除程序对象
            glDeleteProgram(programObjectId);
            return 0;
        }
        return programObjectId;
    }

    /**
     * 验证程序（开发过程中可以调用）
     * @param programObjectId
     * @return true：验证成功  false：验证失败
     */
    public static boolean validateProgram(int programObjectId) {
        glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);
        if(DEBUG) {
            Log.d(TAG, "程序验证状态：" + validateStatus[0] + "\n程序日志信息："
                    + glGetProgramInfoLog(programObjectId));
        }
        return validateStatus[0] != 0;
    }
}
