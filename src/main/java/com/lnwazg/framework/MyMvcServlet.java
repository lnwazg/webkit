package com.lnwazg.framework;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.lang3.tuple.MutablePair;

import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.jdbc.impl.ext.MysqlJdbcSupport;
import com.lnwazg.dbkit.utils.DbKit;
import com.lnwazg.kit.controllerpattern.AbstractControllerManager;
import com.lnwazg.kit.controllerpattern.ControllerKit;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.map.Maps;
import com.lnwazg.kit.reflect.ClassKit;

/**
 * 我的极简的MVC控制器
 * @author nan.li
 * @version 2017年3月31日
 */
public class MyMvcServlet extends HttpServlet
{
    
    private static final long serialVersionUID = 325172485440541289L;
    
    /**
     * 默认的controller搜索包路径
     */
    private static final String DEFAULT_CONTROLLER_SEARCH_PACKAGENAME = "com.lnwazg.controller";
    
    /**
     * 默认的DAO包名
     */
    private static final String DEFAULT_DAO_PACKAGE_NAME = "com.lnwazg.dao";
    
    /**
     * 默认的Service包名
     */
    private static final String DEFAULT_SERVICE_PACKAGE_NAME = "com.lnwazg.service";
    
    /**
     * controller的管理器
     */
    private AbstractControllerManager controllerManager;
    
    @Override
    public void init(ServletConfig config)
        throws ServletException
    {
        super.init(config);
        
        Logs.TIMESTAMP_LOG_SWITCH = true;
        Logs.FILE_LOG_SWITCH = true;
        Logs.i("MyMvcServlet init...");
        
        //准备数据库连接
        DataSource datasource = DbKit.getDataSource("mysql.properties");
        MyJdbc jdbc = new MysqlJdbcSupport(datasource);
        
        //初始化DAO层
        DbKit.packageSearchAndInitDao(DEFAULT_DAO_PACKAGE_NAME, jdbc);
        
        //初始化service层
        DbKit.packageSearchAndInitService(DEFAULT_SERVICE_PACKAGE_NAME, jdbc);
        
        //针对这个上下文路径进行编码，并分发到具体的servlet
        controllerManager = new AbstractControllerManager(DEFAULT_CONTROLLER_SEARCH_PACKAGENAME)
        {
            @Override
            public void fillParamsCustom(Object object, Map<String, Object> paramMap)
            {
                //每次都用单例，但是走的是线程本地对象
                ThreadLocal<HttpServletRequest> request = ClassKit.getFieldValue(object, "request");
                ThreadLocal<HttpServletResponse> response = ClassKit.getFieldValue(object, "response");
                ThreadLocal<HttpServletRequest> req = ClassKit.getFieldValue(object, "req");
                ThreadLocal<HttpServletResponse> resp = ClassKit.getFieldValue(object, "resp");
                
                request.set((HttpServletRequest)paramMap.get("request"));
                response.set((HttpServletResponse)paramMap.get("response"));
                req.set((HttpServletRequest)paramMap.get("req"));
                resp.set((HttpServletResponse)paramMap.get("resp"));
                
                //                ClassKit.setField(object, "request", paramMap.get("request"));
                //                ClassKit.setField(object, "response", paramMap.get("response"));
                //                ClassKit.setField(object, "req", paramMap.get("req"));
                //                ClassKit.setField(object, "resp", paramMap.get("resp"));
            }
        };
    }
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        //        //    /webtest
        //        System.out.println("path: " + req.getContextPath());
        //        //    /index/abc.do
        //        System.out.println("path2: " + req.getServletPath());
        
        String path = req.getServletPath();
        //        Logs.d("servletPath=" + path);
        path = path.substring(0, path.indexOf("."));
        path = ControllerKit.fixPath(path);
        //        Logs.d("path=" + path);
        
        //path:  /message/sendMsg
        //params:   
        if (ControllerKit.matchPath(path))
        {
            MutablePair<String, String> pair = ControllerKit.resolvePath(path);
            String classShortName = pair.getLeft();//     news
            String methodName = pair.getRight();//        readNews
            //            Logs.d("classShortName=" + classShortName);
            //            Logs.d("methodName=" + methodName);
            //然后调用对应的方法即可。注入参数map
            controllerManager.invoke(classShortName, methodName, Maps.asMap("request", req, "response", resp, "req", req, "resp", resp));
        }
        else
        {
            Logs.e(String.format("Controller path: 【%s】  格式不正确，无法调用相关的Controller！", path));
        }
    }
}
