package cn.tm.ms.restful.common.utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Title: 资源复制
 * @Description:
 * 资源复制(深复制,浅复制暂未处理)<br>
 * @Author lry
 * @Date 2016年1月22日 上午11:58:10
 * @Version v1.0
 */
public class BeanUtils {

	/** 
     * 利用反射实现对象之间属性复制 
     * @param from 数据源
     * @param to 目标容器
     */  
    public static void copyProperties(Object from, Object to) throws Exception {
        copyPropertiesExclude(from, to, null);
    }
      
    /** 
     * 复制对象属性
     * @param from 数据源
     * @param to 目标容器
     * @param excludsArray 排除属性列表 
     * @throws Exception 
     */
    public static void copyPropertiesExclude(Object from, Object to, String[] excludsArray) throws Exception {
        List<String> excludesList = null;  
        if(excludsArray != null && excludsArray.length > 0) {  
            excludesList = Arrays.asList(excludsArray); //构造列表对象  
        }  
        Method[] fromMethods = from.getClass().getDeclaredMethods();  
        Method[] toMethods = to.getClass().getDeclaredMethods();  
        Method fromMethod = null, toMethod = null;  
        String fromMethodName = null, toMethodName = null;  
        for (int i = 0; i < fromMethods.length; i++) {  
            fromMethod = fromMethods[i];  
            fromMethodName = fromMethod.getName();  
            if (!fromMethodName.contains("get")){
            	if (!fromMethodName.startsWith("is")){//自动生成的boolean类型资源复制
            		continue;
            	}
            }
            //排除列表检测  
            if(excludesList != null && excludesList.contains(fromMethodName.substring(fromMethodName.startsWith("is")?2:3).toLowerCase())) {  
                continue;  
            }
            toMethodName = "set" + fromMethodName.substring(fromMethodName.startsWith("is")?2:3);
            toMethod = findMethodByName(toMethods, toMethodName);
            if (toMethod == null){
            	continue;  
            }
            Object value = fromMethod.invoke(from, new Object[0]);
            if(value == null){
            	continue;            	
            }
            //集合类判空处理
            if(value instanceof Collection) {
                @SuppressWarnings("rawtypes")
				Collection newValue = (Collection)value;
                if(newValue.size() <= 0){
                	continue;                	
                }
            }
            toMethod.invoke(to, new Object[] {value});
        }
    }
      
    /** 
     * 对象属性值复制，仅复制指定名称的属性值 
     * @param from 数据源
     * @param to 目标容器
     * @param includsArray 需要复制的属性
     * @throws Exception 
     */  
    public static void copyPropertiesInclude(Object from, Object to, String[] includsArray) throws Exception {
        List<String> includesList = null;
        if(includsArray != null && includsArray.length > 0) {
            includesList = Arrays.asList(includsArray); //构造列表对象
        } else {
            return;
        }
        Method[] fromMethods = from.getClass().getDeclaredMethods();
        Method[] toMethods = to.getClass().getDeclaredMethods();
        Method fromMethod = null, toMethod = null;
        String fromMethodName = null, toMethodName = null;
        for (int i = 0; i < fromMethods.length; i++) {
            fromMethod = fromMethods[i];
            fromMethodName = fromMethod.getName();
            if (!fromMethodName.contains("get")){
            	if (!fromMethodName.startsWith("is")){
            		continue;
            	}
            }
            //排除列表检测
            String str = fromMethodName.substring(fromMethodName.startsWith("is")?2:3);
            if(!includesList.contains(str.substring(0,1).toLowerCase() + str.substring(1))) {
                continue;
            }
            toMethodName = "set" + fromMethodName.substring(fromMethodName.startsWith("is")?2:3);
            toMethod = findMethodByName(toMethods, toMethodName);
            if (toMethod == null){
            	continue;
            }
            Object value = fromMethod.invoke(from, new Object[0]);  
            if(value == null){
            	continue;  
            }
            //集合类判空处理  
            if(value instanceof Collection) {  
                @SuppressWarnings("rawtypes")
				Collection newValue = (Collection)value;  
                if(newValue.size() <= 0){
                	continue;
                }
            }
            toMethod.invoke(to, new Object[] {value});
        }
    }
    
    /** 
     * 将map转换成Javabean 
     * @param obj javaBean 
     * @param data map数据 
     */ 
    @SuppressWarnings("rawtypes")
	public static Object copyMapToObj(Map data,Object obj) {
        Method[] methods = obj.getClass().getDeclaredMethods();
        for (Method method : methods) {
            try {
                if (method.getName().startsWith("set")) {
                    String field = method.getName();
                    field = field.substring(field.indexOf("set") + 3);
                    field = field.toLowerCase().charAt(0) + field.substring(1);
                    Object mapVal=data.get(field);
                    if(mapVal!=null){
                    	method.invoke(obj, new Object[]{mapVal});
                    }
                }
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
        return obj;
    }
  
    /** 
     * 从方法数组中获取指定名称的方法
     * @param methods
     * @param name
     * @return
     */
    private static Method findMethodByName(Method[] methods, String name) {
        for (int j = 0; j < methods.length; j++) {
            if (methods[j].getName().equals(name)){
            	return methods[j];
            }
        }
        return null;
    }
}