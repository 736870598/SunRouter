package com.sunxy.router.core;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.sunxy.router.annotation.model.RouteMeta;
import com.sunxy.router.core.template.IRouteGroup;
import com.sunxy.router.core.template.IRouteRoot;
import com.sunxy.router.core.utils.ClassUtils;
import com.sunxy.router.core.utils.Consts;
import com.sunxy.router.core.utils.Utils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/3 0003.
 */
public class RouterCore {

    private static RouterCore instance;


    /**
     * 存储分组信息
     */
    Map<String, Class<? extends IRouteGroup>> routes = new HashMap<>();
    /**
     * 存储分组类信息
     */
    Map<String, IRouteGroup> IRouteGroups = new HashMap<>();
    /**
     * 存储RouteMeta信息
     */
    Map<String, RouteMeta> routeMetas = new HashMap<>();

    private RouterCore(){}

    public static RouterCore getInstance(){
        if (instance == null){
            synchronized (RouterCore.class){
                if (instance == null){
                    instance = new RouterCore();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化信息
     */
    public void init(Application application){
        try {
            loadInfo(application);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 初始化信息
     *  获取 com.sunxy.router.routes 包下所有的以 SunRouter$$Root$$ 开头的类
     */
    private void loadInfo(Application application) throws InterruptedException, IOException,
            PackageManager.NameNotFoundException, ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        // 获取 com.sunxy.router.routes 包下所有的类
        Set<String> classSet = ClassUtils.getFileNameByPackageName(application, Consts.PACKAGE_OF_GENERATE_FILE);
        if (!Utils.isEmpty(classSet)){
            for (String classPath : classSet) {
                //筛选以 SunRouter$$Root$$ 开头的类
                if (classPath.startsWith(Consts.PACKAGE_OF_GENERATE_FILE + "." + Consts.NAME_OF_ROOT)){
                    Log.v("sunxy---", classPath);
                    Object objClass = Class.forName(classPath).getConstructor().newInstance();
                    if (objClass instanceof IRouteRoot){
                        ((IRouteRoot) objClass).loadInto(routes);
                    }
                }
            }
        }
    }

    public Intent createIntent(Context context, String path){
        Class<?> classFromPath = getClassFromPath(path);
        if (classFromPath == null){
            return null;
        }
        return new Intent(context, classFromPath);
    }

    public Class<?> getClassFromPath(String path){
        RouteMeta routeMeta = loadRouteMeta(path);
        if (routeMeta == null){
            return null;
        }
        return routeMeta.getDestination();
    }

    /**
     * 根据路由地址path获取RouteMeta
     */
    public RouteMeta loadRouteMeta(String path) {
        if (Utils.isEmpty(path)){
            return null;
        }
        RouteMeta routeMeta = routeMetas.get(path);
        //如果没有拿到，说明还没有加载过该group，去加载
        if (routeMeta == null){
            //获取组名
            String groupName = getGroupName(path);
            if (Utils.isEmpty(groupName)){
                return null;
            }

            try {
                //获取IRouteGroup
                IRouteGroup iRouteGroup = loadIRouteGroup(groupName);
                if (iRouteGroup != null){
                    iRouteGroup.loadInto(routeMetas);
                    routeMeta = routeMetas.get(path);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        return routeMeta;
    }

    private IRouteGroup loadIRouteGroup(String groupName) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        IRouteGroup iRouteGroup = IRouteGroups.get(groupName);
        if (iRouteGroup == null){
            Class<? extends IRouteGroup> aClass = routes.get(groupName);
            if (aClass != null){
                iRouteGroup = aClass.getConstructor().newInstance();
                IRouteGroups.put(groupName, iRouteGroup);
            }
        }
        return iRouteGroup;
    }

    private String getGroupName(String path) {
        if (Utils.isEmpty(path)){
            return null;
        }
        if (path.startsWith("/")){
            path = path.substring(1);
        }
        return path.substring(0, path.indexOf("/"));
    }
}
