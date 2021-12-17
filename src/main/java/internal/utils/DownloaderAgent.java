package main.java.internal.utils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloaderAgent {
    private static final Logger LOGGER = Logger.getLogger(DownloaderAgent.class.getName());

    private DownloaderAgent() {
    }

    private static String getFromLocal(String filename) throws IOException {
        File f = new File(filename);
        if(f.exists()) { /* show alert */
            byte[] encoded = Files.readAllBytes(Paths.get(filename));
            return new String(encoded);
        }

        return null;
    }

    public static String readJsonFromGitHub(String page, int tokenIndex, String cachePath) throws IOException {
        // String building ..
        String res = getFromLocal(cachePath + URLEncoder.encode(page, StandardCharsets.UTF_8));
        if (res != null){
            return res;
        }

        int threshold = 0;
        int count = -1;
        // We need a connection to retrieve information from page
        URL url;
        boolean exitCondition = false;
        StringBuilder builder = new StringBuilder();
        do {
            try {
                url = new URL(page);
                URLConnection conn = url.openConnection();

                conn.setRequestProperty("Authorization", JSONConfig.getEncodedTocken(tokenIndex));
                // String building ..
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                while ((inputLine = bufferedReader.readLine()) != null) {
                    builder.append(inputLine);
                    builder.append(System.lineSeparator());
                }
                bufferedReader.close();
                exitCondition = true;

            } catch (IOException | NullPointerException e) {
                if (threshold > 10) {
                    LOGGER.log(Level.SEVERE, e.getMessage());
                    System.exit(1);
                }
                tokenIndex = count++;
                if (count > 5) {
                    threshold++;
                    count = 0;
                }
            }
        } while (!exitCondition);

        if ( !builder.toString().equals("[]\n")){
            PrintWriter out = new PrintWriter("cache/" + URLEncoder.encode(page, StandardCharsets.UTF_8));
            out.println(builder);
            out.close();
        }

        return builder.toString().trim();

    }
}
