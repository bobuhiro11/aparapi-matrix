package aparapi_matrix;

import java.util.Random;
import com.amd.aparapi.Kernel;

public class MatMulEle extends Kernel
{
  private float matA[];
  private float matB[];
  private float matC[];

  private int lenA;

  @Override
  public void run()
  {
    int i = getGlobalId();

    if(i<lenA){
      matC[i] = matA[i] * matB[i];
    }
  }

  public MatMulEle(float matA[], float matB[])
  {
    this.matA = matA;
    this.matB = matB;

    lenA = matA.length;
    matC = new float [lenA];
  }

  public float[] result()
  {
    return this.matC;
  }
}
