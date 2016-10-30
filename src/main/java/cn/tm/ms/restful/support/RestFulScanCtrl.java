package cn.tm.ms.restful.support;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.glassfish.jersey.jackson.JacksonFeature;
//import org.glassfish.jersey.logging.LoggingFeature;
 
import org.glassfish.jersey.server.ResourceConfig;

import cn.tm.ms.restful.annotation.Ctrl;
import cn.tm.ms.restful.annotation.RES;
import cn.tm.ms.restful.annotation.Serv;
import cn.tm.ms.restful.common.ScanPackage;

public class RestFulScanCtrl extends ResourceConfig {

	/**
	 * 控制层集合
	 */
	public final Map<String, Object> ctrlMAP=new HashMap<String, Object>();
	/**
	 * 服务层集合
	 */
	public final Map<String, Object> servMAP=new HashMap<String, Object>();
	
	
	/**
	 * 扫描及初始化
	 * 
	 * @param pack
	 * @param customServMAP 自定义服务
	 */
	public RestFulScanCtrl(String pack, Map<String, Object> customServMAP) {
		setApplicationName(RestFulScanCtrl.class.getName());
		register(JacksonFeature.class);
		//register(LoggingFeature.class);

		//$NON-NLS-注入自定义服务$
		if(customServMAP!=null){
			if(!customServMAP.isEmpty()){
				servMAP.putAll(customServMAP);
			}
		}
		
		//扫描并创建实例
		scanServAndCtrl(pack);
		
		//注册服务
		for (Map.Entry<String,Object> entry:ctrlMAP.entrySet()) {
			register(entry.getValue());
		}
	}
	
	/**
	 * 扫描所有控制层和服务层
	 * 
	 * @param pack
	 */
	private void scanServAndCtrl(String pack) {
		try {
			Set<Class<?>> clazzs = ScanPackage.getClasses(pack);
			for (Class<?> clazz : clazzs) {
				Ctrl ctrl = clazz.getAnnotation(Ctrl.class);
				Serv serv = clazz.getAnnotation(Serv.class);

				if(ctrl!=null&&serv!=null){
					throw new RuntimeException("The '"+clazz.getName()+"' of ctrl and serv can not be simultaneously as one");
				}else{
					//$NON-NLS-扫描服务层$
					if (serv != null) {
						try {
							String servKEY=serv.value();
							if(servKEY==null||servKEY.length()<1){
								servKEY=clazz.getName();
							}
							
							//$NON-NLS-重复校验$
							if(servMAP.containsKey(servKEY)){
								throw new RuntimeException("The already exist of serv '"+servKEY+"'.");
							}else{
								//$NON-NLS-注入资源$
								Object servObj=clazz.newInstance();
								servMAP.put(servKEY, servObj);							
							}
						} catch (InstantiationException | IllegalAccessException e) {
							throw new RuntimeException("The class '"+clazz+"' must contain a default constructor that can be accessed by an external.");
						}
					} else {
						//$NON-NLS-扫描控制器$
						if (ctrl != null) {
							try {
								String ctrlKEY=ctrl.value();
								if(ctrlKEY==null||ctrlKEY.length()<1){
									ctrlKEY=clazz.getName();
								}
								
								//$NON-NLS-重复校验$
								if(ctrlMAP.containsKey(ctrlKEY)){
									throw new RuntimeException("The already exist of ctrl '"+ctrlKEY+"'.");
								}else{
									Object ctrlObj=clazz.newInstance();
									ctrlMAP.put(ctrlKEY, ctrlObj);
								}
							} catch (InstantiationException | IllegalAccessException e) {
								throw new RuntimeException("The class '"+clazz+"' must contain a default constructor that can be accessed by an external.");
							}
						}
					}
				}
			}
			
			//$NON-NLS-扫描所有服务层进行服务注入$
			for (Map.Entry<String,Object> entry:servMAP.entrySet()) {
				scanRes(entry.getValue(), entry.getValue().getClass());
			}

			//$NON-NLS-扫描所有控制层进行服务注入$
			for (Map.Entry<String,Object> entry:ctrlMAP.entrySet()) {
				scanRes(entry.getValue(), entry.getValue().getClass());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 服务资源注入
	 * 
	 * @param obj
	 * @param clazz
	 */
	private void scanRes(Object obj, Class<?> clazz) {
		try {
			Field[] fields=clazz.getDeclaredFields();
			for (Field field:fields) {
				RES res=field.getAnnotation(RES.class);
				if(res!=null){
					field.setAccessible(true);//强制赋值
					String resKEY=res.value();
					if(resKEY==null||resKEY.length()<1){
						resKEY=field.getType().getName();
					}
					
					Object servObj=servMAP.get(resKEY);
					if(servObj!=null){
						field.set(obj, servObj);						
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
