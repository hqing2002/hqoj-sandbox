package com.hqing.hqojcodesandbox;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;

import java.io.IOException;
import java.util.List;

/**
 * DockerJavaDemo
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public class DockerDemo {
    public static void main(String[] args) throws InterruptedException {
        //获取默认的Docker Client
        try (DockerClient dockerClient = DockerClientBuilder.getInstance().build()) {
            System.out.println("\n程序日志输出\n");
            //查询镜像列表
            ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
            List<Image> imageList = listImagesCmd.exec();
            System.out.println(imageList);

            //拉取镜像
            String image = "hello-world:latest";
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);

            //函数回调
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println("下载镜像" + item.getStatus());
                    super.onNext(item);
                }
            };
            //exec执行, awaitCompletion()阻塞等待异步请求完成
            pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
            System.out.println("下载完成");

            //创建容器
            CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
            CreateContainerResponse createContainerResponse = containerCmd.exec();
            System.out.println(createContainerResponse);
            //获取创建出的容器id
            String containerId = createContainerResponse.getId();

            //查看容器状态
            ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();
            List<Container> containerList = listContainersCmd.withShowAll(true).exec();
            for (Container container : containerList) {
                System.out.println(container);
            }

            //启动容器
            dockerClient.startContainerCmd(containerId).exec();

            //查看容器输出日志
            ResultCallback.Adapter<Frame> logContainResultCallback = new ResultCallback.Adapter<Frame>() {
                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (streamType == StreamType.STDOUT) {
                        System.out.println(new String(frame.getPayload()));
                    } else if (streamType == StreamType.STDERR) {
                        System.err.println(new String(frame.getPayload()));
                    } else {
                        System.out.println("其他类型: " + new String(frame.getPayload()));
                    }
                }
            };

            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTailAll()
                    .exec(logContainResultCallback)
                    .awaitCompletion();

            //删除容器
            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
            System.out.println("\n程序日志结束\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
