package webapp.controller;

import org.noear.solon.annotation.XBean;
import org.noear.solon.annotation.XMapping;

@XBean
public class HelloService {
    @XMapping("hello")
    public String hello(String name){
        return "hello world!";
    }
}
