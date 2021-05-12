package com.atg.atgtools.services;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

//https://simplesolution.dev/spring-boot-sftp-file-transfer-using-jsch-java-library/

@Service
public class ATGReportDownload {

        private Logger logger = LoggerFactory.getLogger(ATGReportDownload.class);


        private String host;

        private Integer port;

        private String username;

        private String password;

        private Integer sessionTimeout;

        private Integer channelTimeout;


        public boolean uploadFile(String localFilePath, String remoteFilePath) {
            ChannelSftp channelSftp = createChannelSftp();
            try {
                channelSftp.put(localFilePath, remoteFilePath);
                return true;
            } catch(SftpException ex) {
                logger.error("Error upload file", ex);
            } finally {
                disconnectChannelSftp(channelSftp);
            }

            return false;
        }


        public boolean downloadFile(String localFilePath, String remoteFilePath) {
            ChannelSftp channelSftp = createChannelSftp();
            OutputStream outputStream;
            try {
                /*File file = new File(localFilePath);
                outputStream = new FileOutputStream(file);
                channelSftp.get(remoteFilePath, outputStream);*/
                channelSftp.get(remoteFilePath,localFilePath);
                //file.createNewFile();
                return true;
            } catch(SftpException  ex) {
                logger.error("Error download file", ex);
            } finally {
                disconnectChannelSftp(channelSftp);
            }

            return false;
        }

        private ChannelSftp createChannelSftp() {
            try {
                JSch jSch = new JSch();
                //Session session = jSch.getSession(username, host, port);
                Session session = jSch.getSession("ksunder-adm", "atg-pvtbquk-ndc-aux01.ghanp.kfplc.com", 22);
                session.setConfig("StrictHostKeyChecking", "no");
                //session.setPassword(password);
                session.setPassword("Feb@2019");
                session.connect(15000);
                Channel channel = session.openChannel("sftp");
                channel.connect(15000);
                return (ChannelSftp) channel;
            } catch(JSchException ex) {
                logger.error("Create ChannelSftp error", ex);
            }

            return null;
        }

        private void disconnectChannelSftp(ChannelSftp channelSftp) {
            try {
                if( channelSftp == null)
                    return;

                if(channelSftp.isConnected())
                    channelSftp.disconnect();

                if(channelSftp.getSession() != null)
                    channelSftp.getSession().disconnect();

            } catch(Exception ex) {
                logger.error("SFTP disconnect error", ex);
            }
        }

    public static void main(String[] args) {
        ATGReportDownload atgReportDownload = new ATGReportDownload();
        String todaysFiles = "SKUSummaryReport_*"+ LocalDate.now().toString() +".*";
        boolean isDonloaded = atgReportDownload.downloadFile(".\\","/app/ecomm/SKUReporting/"+todaysFiles);
        System.out.println(isDonloaded?"Downloaded":"Not Downloaded");

    }

}

