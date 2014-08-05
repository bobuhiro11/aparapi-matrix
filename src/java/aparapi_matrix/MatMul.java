package aparapi_matrix;

import java.util.Random;
import com.amd.aparapi.Kernel;

public class MatMul extends Kernel
{
  private float matA[];
  private float matB[];
  private float matC[];

  private int rowA, colArowB, colB;

  @Override
  public void run()
  {
    int i = getGlobalId() / colB;
    int j = getGlobalId() % colB;
    float v = 0;
    for(int k = 0; k<colArowB; k++) {
      v += matA[i * colArowB + k] * matB[k * colB + j];
    }
    matC[i * colB + j] = v;
  }

  public MatMul(float matA[], float matB[], int rowA, int colArowB, int colB)
  {
    this.rowA     = rowA;
    this.colArowB = colArowB;
    this.colB     = colB;

    this.matA = matA;
    this.matB = matB;

    matC = new float [rowA * colB    ];

    for(int i=0; i<rowA; i++) {
      for(int j=0; j<colB; j++) {
        matC[i * colB + j] = 0;
      }
    }
  }

  public float[] result()
  {
    return this.matC;
  }
}
