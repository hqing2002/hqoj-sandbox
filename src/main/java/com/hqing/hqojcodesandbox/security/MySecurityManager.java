package com.hqing.hqojcodesandbox.security;

import java.security.Permission;

/**
 * 安全管理器
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public class MySecurityManager extends SecurityManager {
    @Override
    public void checkPermission(Permission perm) {
        System.out.println("检测到使用权限" + perm.getActions());
    }

    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("exec权限异常: " + cmd);
    }

    @Override
    public void checkRead(String file) {
        throw new SecurityException("read权限异常: " + file);
    }

    @Override
    public void checkWrite(String file) {
        throw new SecurityException("write权限异常: " + file);
    }

    @Override
    public void checkDelete(String file) {
        throw new SecurityException("del权限异常: " + file);
    }

    @Override
    public void checkConnect(String host, int port) {
        throw new SecurityException("connect权限异常: " + host + ":" + port);
    }
}
