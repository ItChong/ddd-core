package com.chongstack.ddd.application;

import java.io.Serializable;

/**
 * 命令标记接口。
 * <p>
 * 命令代表一次写操作意图，属于 Application 层的入参。
 * 命令应该是不可变的，包含执行操作所需的全部数据。
 * <p>
 * 推荐使用 Java Record 来实现 Command。
 */
public interface Command extends Serializable {

}
