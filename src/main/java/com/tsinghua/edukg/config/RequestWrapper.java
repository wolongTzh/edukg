package com.tsinghua.edukg.config;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
public class RequestWrapper extends HttpServletRequestWrapper {
    private byte[] body;

    public RequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        body = this.toByteArray(request.getInputStream());
    }

    private byte[] toByteArray(ServletInputStream inputStream) throws IOException {
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        byte[] buffer=new byte[1024*4];
        int n=0;
        while((n= inputStream.read(buffer))!=-1){
            out.write(buffer,0,n);
        }
        return out.toByteArray();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais=new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return bais.read();
            }
        };
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] data){
        body=data;
    }
}