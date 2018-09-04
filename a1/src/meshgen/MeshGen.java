class MeshGen
{
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
                }
                else if (args[i].equals("-n"))
                {
                    // parse latitude divisions from args[i+1]
                    // use Integer.parseInt() for parsing integer parameter
                } else if (args[i].equals("-m"))
                {
                    // parse v divisions
                } else if (args[i].equals("-o"))
                {
                    // output file
                }
            }
        } else {
            // User provides an input OBJ mesh file, which the program reads in.
            // The mesh is assumed to have no normals (if normals are included in the input file, they are ignored).
            // The program then generates approximate normals at each vertex as described below.
            // Writes the resulting mesh to the user-provided output file.
            OBJMesh mesh = new OBJMesh(args[1]);

            // Calculate normals

            mesh.writeOBJ(args[3]);
        }
    }
}