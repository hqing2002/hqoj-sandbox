package testCode.unsafe;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 运行其他程序（比如危险木马）
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public class RunFileError {
    public static void main(String[] args) throws IOException, InterruptedException {
        String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator +
                "src" + File.separator +
                "main" + File.separator +
                "resources" + File.separator +
                "木马程序.bat";
        Process process = Runtime.getRuntime().exec(filePath);
        process.waitFor();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String outPutLine;
        while ((outPutLine = bufferedReader.readLine()) != null) {
            System.out.println(outPutLine);
        }
        System.out.println("异常程序执行成功");
    }
}
