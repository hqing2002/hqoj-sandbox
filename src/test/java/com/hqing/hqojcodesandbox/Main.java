package com.hqing.hqojcodesandbox;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import com.hqing.hqojcodesandbox.impl.java.JavaCodeSandboxTemplate;
import com.hqing.hqojcodesandbox.utils.RegularUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * FileDescribe
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
@SpringBootTest
public class Main {
    @Test
    public void test() {
        //获取工作目录(在IDE中为项目根目录)
        String userDir = System.getProperty("user.dir");

        //这里用File.separator作为分隔符而不是直接写'\\'是因为在linux和windows上分隔符不一样, 用这个可以自动识别兼容不同系统
        //globalCodePathName: userDir/tempCode
        String globalCodePathName = userDir + File.separator + "testCodeDir";

        //把用户的代码隔离到文件夹中, 命名用UUID
        //userCodePath: userDir/tempCode/UUID/getCodeFileName
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + "test.txt";
        File hello = FileUtil.writeString("hello", userCodePath, StandardCharsets.UTF_8);
        System.out.println(hello.getAbsolutePath());
        System.out.println(hello.getParentFile().getAbsoluteFile());

    }
}
