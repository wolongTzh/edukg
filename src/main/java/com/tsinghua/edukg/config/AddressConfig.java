package com.tsinghua.edukg.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class AddressConfig {

    @Value("${address.server.qa}")
    private String qaAddress;

    @Value("${address.server.linking}")
    private String linkingAddress;

    @Value("${address.server.sourcePath}")
    private String sourceAddress;

    @Value("${address.file.examSource}")
    private String examSourceAddress;

    @Value("${address.file.textbook}")
    private String bookBasePath;

    @Value("${address.file.jiebaDictLinux}")
    private String jiebaDictAddressLinux;

    @Value("${address.file.jiebaDictWin}")
    private String jiebaDictAddressWin;

    @Value("${address.file.stopWords}")
    private String stopWords;

    @Value("${address.file.sourcePath}")
    private String sourcePath;

    @Value("${address.sign.split}")
    private String sign;
}
