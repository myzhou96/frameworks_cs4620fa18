package meshgen;

import math.Vector3;
import java.io.IOException;

public class MeshGen
{
    private static final float FULL_DEGREE = (float)(2*Math.PI);
    private static final float RADIUS = 1.0f;

    private static int n = 32;
    private static int m = 16;
    private static String shape = "";

    /**
     * Main method. Parses the command.
     * @param args
     */
    public static void main(String[] args) {
        if (args[0].equals("-g")) {
            // generate mesh if the first option is "-g"
            OBJMesh mesh = new OBJMesh();

            for (int i=0; i < args.length; i += 2)
            {
                if (args[i].equals("-g"))
                {
                    // parse whether it is sphere or cylinder from args[i+1]
                    shape = args[i+1];
                }
                else if (args[i].equals("-n"))
                {
                    // parse latitude divisions from args[i+1]
                    // use Integer.parseInt() for parsing integer parameter
                    n = Integer.parseInt(args[i+1]);
                } else if (args[i].equals("-m"))
                {
                    // parse longitude divisions
                    m = Integer.parseInt(args[i+1]);
                } else if (args[i].equals("-o"))
                {
                    // output file
                }
            }

            if (shape.equals("sphere")) {
                // create vertices
                float yIncrement = (RADIUS*2)/m;
                float currY = 1.0f - yIncrement;
                float currDegree = 0.0f;
                float degreeIncrement = FULL_DEGREE/n;

                mesh.positions.add(new Vector3(0.0f, 1.0f, 0.0f));
                while (currY > -1.0f) {
                    double theta = Math.asin((double)(currY/RADIUS));
                    while (currDegree < FULL_DEGREE) {
                        System.out.println("degree is " + currDegree + " " + theta);
                        float x = (float) (Math.cos(theta) * Math.sin(currDegree));
                        float z = (float) (Math.cos(theta) * Math.cos(currDegree));
                        System.out.println(x + " " + currY + " " + z);
                        mesh.positions.add(new Vector3(x, currY, z));
                        currDegree += degreeIncrement;
                    }
                    currDegree = 0.0f;
                    currY -= yIncrement;
                }
                mesh.positions.add(new Vector3(0.0f, -1.0f, 0.0f));

                // create faces
                for (int i = 1; i <= n; i++) {
                    OBJFace face = new OBJFace(3, false, false);
                    face.positions[0] = 0;
                    face.positions[1] = i;
                    face.positions[2] = (i%n) + 1;
                    mesh.faces.add(face);
                }

                // separate into rectangles
                for (int i = 0; i < m-1; i++) {
                    int top = (i*n) + 1;
                    int bottom = top + n;
                    System.out.println("top and bottom are " + top + " " + bottom);
                    for (int j = 0; j < n; j++) {
                        OBJFace bottomTri = new OBJFace(3, false, false);
                        bottomTri.positions[0] = top + j;
                        bottomTri.positions[1] = bottom + j;
                        bottomTri.positions[2] = (bottom + 1 + j >= bottom + n) ? bottom : bottom + 1 + j;
                        mesh.faces.add(bottomTri);
                        OBJFace topTri = new OBJFace(3, false, false);
                        topTri.positions[0] = (bottom + 1 + j >=bottom + n) ? bottom : bottom + 1 + j;
                        topTri.positions[1] = (top + 1 + j >= top + n) ? top : top + 1 + j;
                        topTri.positions[2] = top + j;
                        mesh.faces.add(topTri);
                    }
                }

                int total = n * (m-1) + 1;
                System.out.println("total is " + total);
                for (int i = 1; i <= n; i++) {
                    OBJFace face = new OBJFace(3, false, false);
                    face.positions[0] = total;
                    face.positions[1] = total-n+(i%n)+1;
                    face.positions[2] = (total-n+(i%n) >= total) ? total-n+1 : total-n+(i%n);
                    mesh.faces.add(face);
                }
            }

            try {
                mesh.writeOBJ("test.obj");
            } catch (IOException e) {
                System.out.println("Writing failed.");
            }
        } else {
            // User provides an input OBJ mesh file, which the program reads in.
            // The mesh is assumed to have no normals (if normals are included in the input file, they are ignored).
            // The program then generates approximate normals at each vertex as described below.
            // Writes the resulting mesh to the user-provided output file.
//            OBJMesh mesh = new OBJMesh(args[1]);

            // Calculate normals

//            mesh.writeOBJ(args[3]);
        }
    }
}