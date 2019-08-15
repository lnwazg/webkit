package com.lnwazg.framework;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.lnwazg.kit.common.model.FrontObj;
import com.lnwazg.kit.gson.GsonKit;
import com.lnwazg.kit.io.StreamUtils;
import com.lnwazg.kit.mime.MimeMappingMap;

/**
 * 控制器基类
 * @author nan.li
 * @version 2017年3月31日
 */
public class BasicController
{
    protected ThreadLocal<HttpServletRequest> request;
    
    protected ThreadLocal<HttpServletResponse> response;
    
    protected ThreadLocal<HttpServletRequest> req;
    
    protected ThreadLocal<HttpServletResponse> resp;
    
    public String getParam(String key)
    {
        return req.get().getParameter(key);
    }
    
    /**
     * JSON形式的响应
     * @author lnwazg@126.com
     * @param json
     */
    public void okJson(String json)
    {
        resp.get().setHeader("Content-Type", "application/json;charset=utf-8");
        try
        {
            response.get().getWriter().write(json);
            response.get().getWriter().flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public void okJsonDefaultSuccess()
    {
        okJson(new FrontObj().success());
    }
    
    public void okBytes(byte[] bytes, String extension)
    {
        String contentType = "application/octet-stream";//默认是bin类型的输出MIME
        if (StringUtils.isNotEmpty(extension))
        {
            contentType = String.format("%s;charset=utf-8", MimeMappingMap.mimeMap.get(extension.toLowerCase()));
        }
        resp.get().setHeader("Content-Type", contentType);
        OutputStream oStream = null;
        try
        {
            oStream = response.get().getOutputStream();
            IOUtils.write(bytes, oStream);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            StreamUtils.close(oStream);
        }
    }
    
    /**
     * 将对象转换成JSON形式的响应
     * @author lnwazg@126.com
     * @param obj
     */
    public void okJson(Object obj)
    {
        okJson(GsonKit.gson.toJson(obj));
    }
    
    public void addHeaderPre(String key, String value)
    {
        resp.get().addHeader(key, value);
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
