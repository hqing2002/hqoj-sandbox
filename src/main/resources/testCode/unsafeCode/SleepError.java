/**
 * 无限睡眠（阻塞程序执行）
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        long oneHour = 60 * 60 * 1000;
        Thread.sleep(oneHour);
        System.out.println("睡够了");
    }
}
