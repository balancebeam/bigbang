package example.dao;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class BellService {

    @Resource
    private Bell bell;

    public void f(){
        int c= bell.count();
        System.out.println(c);
    }
}
