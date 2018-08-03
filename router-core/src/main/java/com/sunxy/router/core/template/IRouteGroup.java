package com.sunxy.router.core.template;

import com.sunxy.router.annotation.model.RouteMeta;

import java.util.Map;

/**
 * @author Lance
 * @date 2018/2/22
 */

public interface IRouteGroup {

    void loadInto(Map<String, RouteMeta> atlas);
}
