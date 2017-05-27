package JsonUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

/**
 * Created by Eddie on 2017/3/30.
 */
public class JsonCopier {
    public static void copyJsonFromURL(String url, String filePath, String fileName) {
        URL u;
        InputStream source;
        File dest = new File(filePath, fileName);
        try {
            if(!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            u = new URL(url);
            source = u.openStream();
            Files.copy(source, dest.toPath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {

        }
    }
}
