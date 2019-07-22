package com.hyf.rpc.controller;

import com.alibaba.fastjson.JSON;
import com.hyf.rpc.service.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Howinfun
 * @desc
 * @date 2019/7/15
 */
@RestController()
@RequestMapping("/hello")
public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);
    @Autowired
    private HelloService helloService;

    @GetMapping("/sayHello")
    public String sayHello(@RequestParam String name){
        return  helloService.sayHello(name);
    }

    @PostMapping("/postParams")
    public String postParams(HttpServletRequest request){
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String result = JSON.toJSONString(name+password);
        return result;
    }

    @PostMapping("/postMultipart")
    public String postMultipart(HttpServletRequest request,@RequestParam("file") MultipartFile file){
        if (!file.isEmpty()){
            String savePath = "D:\\test\\"+file.getOriginalFilename();
            try {
                file.transferTo(new File(savePath));
            } catch (IOException e) {
                logger.error(e.getMessage());
                return "上传失败";
            }
        }
        return "上传成功";
    }

    @PostMapping("postFile")
    public String postFile(@RequestBody byte[] data){
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream("D:\\3.jpg");
            outputStream.write(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "success";
    }

    public static void main(String[] args) {
        System.out.println();
    }

}
