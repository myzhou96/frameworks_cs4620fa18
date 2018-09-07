package meshgen;

import math.Vector2;
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
                Vector3 topVector = new Vector3(0.0f, 1.0f, 0.0f);
                mesh.positions.add(topVector);
                mesh.normals.add(topVector);

                // top uvs
                float thetaIncrement = (float)(Math.PI/m);
                float theta = (float)(Math.PI/2.0f);
                float phiIncrement = FULL_DEGREE/n;
                float phi = 0.0f;
                while (phi <= FULL_DEGREE) {
                    float v = (float)(theta/Math.PI) + 0.5f;
                    float u = phi/FULL_DEGREE;
                    mesh.uvs.add(new Vector2(u, v));
                    phi += phiIncrement;
                }

                System.out.println("look1 " + mesh.uvs.size());

                theta -= thetaIncrement;
                phi = 0.0f;

                for (int i = 0; i < m-1; i++) {
                    System.out.println("theta is " + theta);
                    for (int j = 0; j < n; j++) {
                        float x = (float)(Math.cos(theta) * Math.sin(phi));
                        float y = (float)(Math.sin(theta));
                        float z = (float)(Math.cos(theta) * Math.cos(phi));
                        Vector3 vec = new Vector3(x, y, z);
                        mesh.positions.add(vec);
                        mesh.normals.add(vec); // same as positions

                        // create uvs
                        float v = theta/(float) Math.PI + 0.5f;
                        float u = phi/FULL_DEGREE;
                        mesh.uvs.add(new Vector2(u, v));

                        phi += phiIncrement;
                    }
                    phi = 0.0f;
                    theta -= thetaIncrement;
                }

                System.out.println("look2 " + mesh.uvs.size());

                Vector3 bottomVector = new Vector3(0.0f, -1.0f, 0.0f);
                mesh.positions.add(bottomVector);
                mesh.normals.add(bottomVector);

                // bottom uvs
                theta = (float) -Math.PI/2.0f;
                while (phi <= FULL_DEGREE) {
                    float v = theta/(float) Math.PI + 0.5f;
                    float u = phi/FULL_DEGREE;
                    mesh.uvs.add(new Vector2(u, v));
                    phi += phiIncrement;
                }

                System.out.println("look3 " + mesh.uvs.size());

                // edge uvs
                phi = FULL_DEGREE;
                theta = (float) Math.PI/2.0f;
                for (int i = 0; i < m; i++) {
                    float v = theta/(float) Math.PI + 0.5f;
                    float u = phi/FULL_DEGREE;
                    mesh.uvs.add(new Vector2(u, v));
                    theta -= thetaIncrement;
                }
                //mesh.uvs.add(new Vector2(1.0f, 0.0f));

                System.out.println("look4 " + mesh.uvs.size());

                int bottomIndex = (m-1)*(n)+n;
                int edgeIndex = bottomIndex + n;

                System.out.println("look5 " + bottomIndex + " " + edgeIndex);

                // create top faces
                for (int i = 1; i <= n; i++) {
                    OBJFace face = new OBJFace(3, true, true);
                    face.positions[0] = 0;
                    face.positions[1] = i;
                    face.positions[2] = (i%n) + 1;

                    face.normals = face.positions;

                    face.uvs[0] = i-1;
                    face.uvs[1] = i-1+n;
                    face.uvs[2] = i+n;

                    if (i == n) {
                        face.uvs[0] = edgeIndex;
                        face.uvs[2] = edgeIndex + 1;
                    }

                    mesh.faces.add(face);
                }

                // separate into rectangles
                for (int i = 0; i < m-2; i++) {
                    int top = (i*n) + 1;
                    int bottom = top + n;
                    System.out.println("top and bottom are " + top + " " + bottom);
                    for (int j = 0; j < n; j++) {
                        OBJFace bottomTri = new OBJFace(3, true, true);
                        bottomTri.positions[0] = top + j;
                        bottomTri.positions[1] = bottom + j;
                        bottomTri.positions[2] = bottom + ((1+j)%n);

                        bottomTri.normals = bottomTri.positions;

                        bottomTri.uvs[0] = top + j + n - 1;
                        bottomTri.uvs[1] = bottom + j + n - 1;
                        bottomTri.uvs[2] = bottom + ((1+j)%n) + n - 1;

                        OBJFace topTri = new OBJFace(3, true, true);
                        topTri.positions[0] = bottom + ((1+j)%n);
                        topTri.positions[1] = top + ((1+j)%n);
                        topTri.positions[2] = top + j;

                        topTri.normals = topTri.positions;

                        topTri.uvs[0] = bottom + ((1+j)%n) + n - 1;
                        topTri.uvs[1] = top + 1 + j + n - 1;
                        topTri.uvs[2] = top + j + n - 1;

                        if (j == n-1) {
                            bottomTri.uvs[2] = edgeIndex + i + 2;
                            topTri.uvs[0] = edgeIndex + i + 2;
                            topTri.uvs[1] = edgeIndex + i + 1;
                        }

                        mesh.faces.add(bottomTri);
                        mesh.faces.add(topTri);
                    }
                }

                // create bottom faces
                int total = n * (m-1) + 1;
                System.out.println("total is " + total);
                for (int i = 0; i < n; i++) {
                    OBJFace face = new OBJFace(3, true, true);
                    face.positions[0] = total;
                    face.positions[1] = total-n+((i+1)%n);
                    face.positions[2] = total-n+i;

                    face.normals = face.positions;

                    face.uvs[0] = bottomIndex+i;
                    face.uvs[1] = bottomIndex-n+i+1;
                    face.uvs[2] = bottomIndex-n+i;

                    if (i == n-1) {
                        face.uvs[0] = edgeIndex + m - 1;
                        face.uvs[1] = edgeIndex + m - 2;
                    }

                    mesh.faces.add(face);
                }

                System.out.println("this is " + mesh.uvs.get(edgeIndex + m - 2));
                System.out.println("this is " + mesh.uvs.get(edgeIndex + m - 1));
            }

            System.out.println("is mesh valid " + mesh.isValid(true));

            try {
                OBJMesh rightMesh = new OBJMesh("data/sphere-reference.obj");
                System.out.println("is mesh compare" + OBJMesh.compare(mesh, rightMesh, true, 0.01f));
            } catch (Exception e) {
                System.out.println("INCORRECT MESH-MAKING");
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