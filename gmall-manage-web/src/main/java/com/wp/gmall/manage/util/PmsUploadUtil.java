/**
 * FileName: PmsUploadUtil
 * Author: WP
 * Date: 2020/2/10 10:32
 * Description:
 * History:
 **/
package com.wp.gmall.manage.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class PmsUploadUtil {

    public static String uploadImage(MultipartFile multipartFile) {
        String imgUrl = "http://192.168.253.131";
        //读取配置文件
        String file = PmsUploadUtil.class.getResource("/tracker.conf").getFile();
        try {
            ClientGlobal.init(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = null;
        try {
            trackerServer = trackerClient.getTrackerServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StorageClient storageClient = new StorageClient(trackerServer, null);

        try {
//            String orginalFilename = "D:\\BaiduNetdiskDownload\\a.jpg";
            byte[] bytes = multipartFile.getBytes();
            //获得原始文件名  eg: a.jpg
            String originalFilename = multipartFile.getOriginalFilename();
            //截取最后一个 “.” ,获得文件后缀名
            int lastIndexOf = originalFilename.lastIndexOf(".");
            String extName = originalFilename.substring(lastIndexOf+1);
            String[] upload_file = storageClient.upload_file(bytes, extName, null);
            for (int i = 0; i < upload_file.length; i++) {
                imgUrl += "/" + upload_file[i];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imgUrl;
    }
}
