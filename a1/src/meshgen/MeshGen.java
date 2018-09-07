package meshgen;

import math.Vector2;
import math.Vector3;
import meshgen.OBJMesh.OBJFileFormatException;

import java.io.IOException;

public class MeshGen
{
    private static final float FULL_DEGREE = (float)(2*Math.PI);
    private static final float RADIUS = 1.0f;

    private static int n;
    private static int m;
    private static String shape = "";
    private static String inputFile = "";
    private static String outputFile = "";

    public static OBJMesh generateCylinder(){
        int firstBottomIndex = n + 1;
        double degreeInc = 360.0/n;
        OBJMesh mesh = new OBJMesh();
        int lastTopPanel = -1;
        int lastBottomPanel = -1;
        
        //values for center of top is at index 0
        Vector3 topCap = new Vector3(0.0f, 1.0f, 0.0f);
        mesh.positions.add(topCap);
        mesh.normals.add(new Vector3(0.0f, 1.0f, 0.0f).normalize()); 
        mesh.uvs.add(new Vector2(0.75f, 0.75f));
        
        //Adding position, normal, and UV
        double degree = 0;
        float uTexture = (float) (0.75 * n);
        System.out.println("uTexture: " + uTexture);
        while(degree < 360){
            double radian = degree * (Math.PI / 180);
            Vector3 position = new Vector3((float)Math.cos(radian), 1.0f, (float)Math.sin(radian));
            mesh.positions.add((position));
            mesh.normals.add(position.clone().sub(topCap).normalize());
            mesh.uvs.add(new Vector2(uTexture/n, 0.5f)); //Top row of the label
            degree += degreeInc;
            if(uTexture == 1){
                lastTopPanel = mesh.uvs.size();
                uTexture = n+1;
            }
            uTexture--;
        }
//        mesh.uvs.add(new Vector2(uTexture/n, 0.5f));
        System.out.println("size of uvs: " + mesh.uvs.size());
        
        //values for center of bottom is at index 33
        Vector3 bottomCap = new Vector3(0.0f, -1.0f, 0.0f);
        mesh.positions.add(bottomCap);
        mesh.normals.add(new Vector3(0.0f, -1.0f, 0.0f).normalize());
        mesh.uvs.add(new Vector2(0.25f, 0.75f));
        System.out.println(uTexture);
        degree = 0;
        uTexture = (float) (0.75 * n);
        while(degree < 360){
            double radian = degree * (Math.PI / 180);
            Vector3 position = new Vector3((float)Math.cos(radian), -1.0f, (float)Math.sin(radian));
            mesh.positions.add((position));
            mesh.normals.add(position.clone().sub(bottomCap).normalize());
            mesh.uvs.add(new Vector2(uTexture/n, 0.0f));
            degree += degreeInc;
            if(uTexture == 1){
                lastBottomPanel = mesh.uvs.size();
                uTexture = n+1;
            }
            uTexture--;
        }

        //Texture coordinates for the top and bottom
        int topUv = mesh.uvs.size();
        for(float i = 0; i < 360; i += degreeInc){
            double radian = (360-i) * (Math.PI / 180.0f);
            mesh.uvs.add(new Vector2((float)Math.cos(radian)*0.25f + 0.75f, (float)Math.sin(radian)*0.25f + 0.75f));
        }
        int bottomUv = mesh.uvs.size();
        for(float i = 0; i < 360; i += degreeInc){
            double radian = i * (Math.PI / 180);
            mesh.uvs.add(new Vector2((float)Math.cos(radian)*0.25f + 0.25f, (float)Math.sin(radian)*0.25f + 0.75f));
        }
        mesh.uvs.add(new Vector2(0.0f, 0.5f));
        mesh.uvs.add(new Vector2(0.0f, 0.0f));
        
        
        //Make cap faces
        System.out.println("first bottom index: " + firstBottomIndex); //where the bottom center is
        System.out.println("topUv: " + topUv);
        System.out.println("bottomUv: " + bottomUv);
        System.out.println("size: " + (mesh.uvs.size()-2));
        int i = 1;
        while(i <= n){
            
            OBJFace triangleTop = new OBJFace(3, true, true);
            OBJFace triangleBottom = new OBJFace(3, true, true);

            triangleTop.positions[0] = 0;
            triangleTop.positions[2] = i;
            triangleBottom.positions[0] = firstBottomIndex;
            triangleBottom.positions[1] = i + firstBottomIndex;
            if(i == n){
                triangleTop.positions[1] = 1;
                triangleBottom.positions[2] = firstBottomIndex + 1;
            }
            else {
                triangleTop.positions[1] = i + 1;
                triangleBottom.positions[2] = i + firstBottomIndex + 1;
            }
            
            triangleTop.normals[0] = 0;
            triangleTop.normals[1] = 0;
            triangleTop.normals[2] = 0;
            triangleBottom.normals[0] = firstBottomIndex;
            triangleBottom.normals[1] = firstBottomIndex;
            triangleBottom.normals[2] = firstBottomIndex;
            
            triangleTop.uvs[0] = 0;     
            triangleBottom.uvs[0] = n + 1;
            triangleBottom.uvs[1] = bottomUv + i - 1;
            if(i == n){
                triangleTop.uvs[1] = topUv;
                triangleTop.uvs[2] = bottomUv - 1;
                triangleBottom.uvs[2] = bottomUv;
                
            }
            else {
                triangleTop.uvs[1] = i + topUv; 
                triangleTop.uvs[2] = i + topUv - 1;
                triangleBottom.uvs[2] = bottomUv + i;
                
            }
            
            mesh.faces.add(triangleTop);
            mesh.faces.add(triangleBottom);
            i++;
        }
        
        //Make side faces
        //Adds in triangles on the side with the point facing up
        i = 1;
        
        while (i <= n){
            OBJFace triangle = new OBJFace(3, true, true);  
            if(i == n){
                triangle.positions[0] = n;
                triangle.positions[2] = n * 2 + 1;
                triangle.positions[1] = firstBottomIndex + 1;
                triangle.normals[0] = n;
                triangle.normals[2] = n * 2 + 1;
                triangle.normals[1] = firstBottomIndex + 1;
                triangle.uvs[0] = i;
                triangle.uvs[1] = firstBottomIndex + 1;
                triangle.uvs[2] = firstBottomIndex + n;
            }
            else{
                triangle.positions[0] = i;
                triangle.positions[1] = i + firstBottomIndex + 1;
                triangle.positions[2] = i + firstBottomIndex; 
                triangle.normals[0] = i;
                triangle.normals[1] = i + firstBottomIndex + 1;
                triangle.normals[2] = i + firstBottomIndex;
                triangle.uvs[0] = i;//firstBottomIndex-i;
                if(i == .75 * n){
                    triangle.uvs[1] = mesh.uvs.size()-1; 
                }
                else {
                    triangle.uvs[1] = i + firstBottomIndex + 1; //n*2+2-i;  
                }
                triangle.uvs[2] = i + firstBottomIndex;     
            }

            mesh.faces.add(triangle);
            i++;
        }
        System.out.println(i);
        
        //Adds in triangles on the side with the point facing down
        i += 2;
        while (i <= n * 2 + 1){
            if(i == n * 2 + 1){
                OBJFace triangle2 = new OBJFace(3, true, true);
                triangle2.positions[0] = firstBottomIndex + 1;
                triangle2.positions[1] = n;
                triangle2.positions[2] = 1;             
                triangle2.normals[0] = firstBottomIndex + 1;
                triangle2.normals[1] = n;
                triangle2.normals[2] = 1;           
                triangle2.uvs[0] = firstBottomIndex + 1;
                triangle2.uvs[1] = n;
                triangle2.uvs[2] = 1;   
                mesh.faces.add(triangle2);
            }
            OBJFace triangle = new OBJFace(3, true, true);
            triangle.positions[0] = i;
            triangle.positions[1] = i - (firstBottomIndex + 1);
            triangle.positions[2] = i - firstBottomIndex;
            triangle.normals[0] = i;
            triangle.normals[1] = i - (firstBottomIndex + 1);
            triangle.normals[2] = i - firstBottomIndex;
            if(i == lastBottomPanel){
                triangle.uvs[0] = mesh.uvs.size()-1; 
                triangle.uvs[1] = lastTopPanel-1;
                triangle.uvs[2] = mesh.uvs.size()-2;
            }
            else {
                triangle.uvs[0] = i;
                triangle.uvs[1] = i - (firstBottomIndex + 1);
                triangle.uvs[2] = i - firstBottomIndex;
            }
            mesh.faces.add(triangle);
            
            i++;
        }
        
        try {
            mesh.writeOBJ(outputFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return mesh;
        
    }

    public static OBJMesh generateSphere(){
        OBJMesh mesh = new OBJMesh();
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
   
        try {
            mesh.writeOBJ(outputFile);
        } catch (IOException e) {
            System.out.println("Writing failed.");
        }

        return mesh;
    }

    public static void genNormals(){
        try {
            OBJMesh mesh = new OBJMesh(inputFile);
            System.out.println(mesh.normals.size());
            for(Vector3 position: mesh.positions){
                mesh.normals.add(new Vector3(0, 0, 0));
            }
            for(OBJFace triangle: mesh.faces){
                if(!triangle.hasNormals()){
                    triangle.normals = new int[3];
                    triangle.normals = triangle.positions;
                }
                Vector3 pos0 = mesh.getPosition(triangle, 0);
                Vector3 pos1 = mesh.getPosition(triangle, 1);
                Vector3 pos2 = mesh.getPosition(triangle, 2);
                Vector3 U = pos1.clone().sub(pos0);
                Vector3 V = pos2.clone().sub(pos0);
                Vector3 cross = U.clone().cross(V);
                for(int i = 0; i < triangle.positions.length; i++){
                    Vector3 normal = mesh.getNormal(triangle, i);
                    normal.add(cross.normalize());
                }
            }
            for(Vector3 normal: mesh.normals){
                normal.normalize();
            }
            try {
                mesh.writeOBJ(outputFile);
                System.out.println("HERE");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }           
                  
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Main method. Parses the command.
     * @param args
     */
    public static void main(String[] args) {
        if (args[0].equals("-g")) {
            // generate mesh if the first option is "-g"
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
                generateSphere();
            } else if (shape.equals("cylinder")){
                generateCylinder();
            } 
        }
        else {
            for (int i=0; i < args.length; i += 2){
                if (args[i].equals("-i"))
                {
                    // parse whether it is sphere or cylinder from args[i+1]
                    inputFile = args[i+1];
                }
                else if (args[i].equals("-o"))
                {
                    // parse latitude divisions from args[i+1]
                    // use Integer.parseInt() for parsing integer parameter
                    outputFile = args[i+1];
                } 
            }
            genNormals();
        }
    }
}