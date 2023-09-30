package dtk.oceansurvival.utility;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import static java.nio.file.Files.copy;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileSystemLib {

    public static void copyFilesFromStream(JavaPlugin streamSource, String baseStreamDir, String baseCopyDir, List<String> filesPaths){
        try {
            File outFile;
            File fileDir;
            InputStream inStream;
            streamSource.getLogger().info("copying files form jar");
            for (String filePath: filesPaths) {
                outFile = new File(baseCopyDir + "/" + filePath);
                fileDir = new File(outFile.getParent());
                inStream = streamSource.getResource(baseStreamDir + "/" + filePath);
                //create the dir if it dose not exist
                if(!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                if (inStream != null) {
                    copy(inStream, outFile.toPath(), REPLACE_EXISTING);

                    streamSource.getLogger().info("Source: " + inStream.toString() + " Dest: " + outFile.getPath());
                }
                else{
                    streamSource.getLogger().info("Source is not a valid path.");
                    streamSource.getLogger().info("Source: " + baseStreamDir + "/" + filePath + " Dest: " + outFile.getPath());
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
