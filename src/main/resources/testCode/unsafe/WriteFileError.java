package testCode.unsafe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * 向服务器写文件（植入危险程序）
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public class WriteFileError {
    public static void main(String[] args) throws IOException {
        String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator +
                "src" + File.separator +
                "main" + File.separator +
                "resources" + File.separator +
                "木马程序.bat";
        String errorProgram = "java -version 2>&1";
        Files.write(Paths.get(filePath), Collections.singletonList(errorProgram));
        System.out.println("写木马成功, 你完了");
    }
}
