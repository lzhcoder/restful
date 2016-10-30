package cn.tm.ms.restful;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.tm.ms.restful.annotation.Ctrl;
import cn.tm.ms.restful.annotation.RES;
import cn.tm.ms.restful.annotation.Serv;
import cn.tm.ms.restful.common.ScanPackage;
import cn.tm.ms.restful.service.TestService;

public class Test {
	
	Map<String, Object> ctrlMAP=new HashMap<String, Object>();
	Map<String, Object> servMAP=new HashMap<String, Object>();
	
	public void scan(String pack) {
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
				res(entry.getValue(), entry.getValue().getClass());
			}

			//$NON-NLS-扫描所有控制层进行服务注入$
			for (Map.Entry<String,Object> entry:ctrlMAP.entrySet()) {
				res(entry.getValue(), entry.getValue().getClass());
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
	public void res(Object obj, Class<?> clazz) {
		try {
			Field[] fields=clazz.getDeclaredFields();
			for (Field field:fields) {
				RES res=field.getAnnotation(RES.class);
				if(res!=null){
					field.setAccessible(true);
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

	public static void main(String[] args) {
		try {
			Test test=new Test();
			test.scan("cn.tm.ms.restful");
			for (Map.Entry<String,Object> entry:test.servMAP.entrySet()) {
				System.out.println(entry.getKey()+"-->"+entry.getValue());
				if(entry.getValue() instanceof TestService){
					TestService ts=(TestService)entry.getValue();
					System.out.println(ts.service());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
