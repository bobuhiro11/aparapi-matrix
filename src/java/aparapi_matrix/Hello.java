package aparapi_matrix;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

public class Hello {

  public static String hello(String s){
    return "java interop success, and your argment is [" + s + "]";
  }

  public static float[] sum() {

    final int size = 512;

    final float[] a = new float[size];
    final float[] b = new float[size];

    for (int i = 0; i < size; i++) {
      a[i] = (float)(Math.random()*100);
      b[i] = (float)(Math.random()*100);
    }

    final float[] sum = new float[size];

    Kernel kernel = new Kernel(){
      @Override public void run() {
        int gid = getGlobalId();
        sum[gid] = a[gid] + b[gid];
      }
    };

    kernel.execute(Range.create(512));

    //for (int i = 0; i < size; i++) {
    //  System.out.printf("%6.2f + %6.2f = %8.2f\n", a[i], b[i], sum[i]);
    //}

    kernel.dispose();
    return sum;
  }

}
