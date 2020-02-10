/**
 * FileName: GmallWebTest
 * Author: WP
 * Date: 2020/2/9 18:53
 * Description:
 * History:
 **/
package com.wp.gmall.manage;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallWebTest {

    @Test
    public void test() throws Exception{
        String file = this.getClass().getResource("/tracker.conf").getFile();
        ClientGlobal.init(file);
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getTrackerServer();
        StorageClient storageClient = new StorageClient(trackerServer,null);
        String orginalFilename="D:\\BaiduNetdiskDownload\\a.jpg";
        String[] upload_file = storageClient.upload_file(orginalFilename, "jpg", null);
        String url = "http://192.168.253.131";
        for (int i = 0; i < upload_file.length; i++) {
            url += "/"+upload_file[i];
        }
        System.out.println(url);
    }
}
