package cn.tm.ms.restful.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * Properties文件载入工具类
 * 
 * @author lry
 */
public class PropLoader {
	
	private Properties properties;

	public PropLoader loader(String... resourcesPaths) {
		properties = loadProperties(resourcesPaths);
		return this;
	}

	public Properties getProperties() {
		return properties;
	}

	/**
	 * 取出Property。
	 */
	private String getValue(String key) {
		String systemProperty = System.getProperty(key);
		if (systemProperty != null) {
			return systemProperty;
		}
		return properties.getProperty(key);
	}

	/**
	 * 取出String类型的Property,如果都為Null则抛出异常.
	 */
	public String getProperty(String key) {
		String value = getValue(key);
		if (value == null) {
			throw new NoSuchElementException();
		}
		return value;
	}

	/**
	 * 取出String类型的Property.如果都為Null則返回Default值.
	 */
	public String getProperty(String key, String defaultValue) {
		String value = getValue(key);
		return value != null ? value : defaultValue;
	}

	/**
	 * 取出Integer类型的Property.如果都為Null或内容错误则抛出异常.
	 */
	public Integer getInteger(String key) {
		String value = getValue(key);
		if (value == null) {
			throw new NoSuchElementException();
		}
		return Integer.valueOf(value);
	}

	/**
	 * 取出Integer类型的Property.如果都為Null則返回Default值，如果内容错误则抛出异常
	 */
	public Integer getInteger(String key, Integer defaultValue) {
		String value = getValue(key);
		return value != null ? Integer.valueOf(value) : defaultValue;
	}
	
	/**
	 * 取出Long类型的Property.如果都為Null或内容错误则抛出异常.
	 */
	public Long getLong(String key) {
		String value = getValue(key);
		if (value == null) {
			throw new NoSuchElementException();
		}
		return Long.valueOf(value);
	}

	/**
	 * 取出Long类型的Property.如果都為Null則返回Default值，如果内容错误则抛出异常
	 */
	public Long getLong(String key, Long defaultValue) {
		String value = getValue(key);
		return value != null ? Long.valueOf(value) : defaultValue;
	}

	/**
	 * 取出Double类型的Property.如果都為Null或内容错误则抛出异常.
	 */
	public Double getDouble(String key) {
		String value = getValue(key);
		if (value == null) {
			throw new NoSuchElementException();
		}
		return Double.valueOf(value);
	}

	/**
	 * 取出Double类型的Property.如果都為Null則返回Default值，如果内容错误则抛出异常
	 */
	public Double getDouble(String key, Double defaultValue) {
		String value = getValue(key);
		return value != null ? Double.valueOf(value) : defaultValue;
	}

	/**
	 * 取出Boolean类型的Property.如果都為Null抛出异常,如果内容不是true/false则返回false.
	 */
	public Boolean getBoolean(String key) {
		String value = getValue(key);
		if (value == null) {
			throw new NoSuchElementException();
		}
		return Boolean.valueOf(value);
	}

	/**
	 * 取出Boolean类型的Propert.如果都為Null則返回Default值,如果内容不为true/false则返回false.
	 */
	public Boolean getBoolean(String key, boolean defaultValue) {
		String value = getValue(key);
		return value != null ? Boolean.valueOf(value) : defaultValue;
	}

	/**
	 * 载入多个文件, 文件路径使用Spring Resource格式.
	 */
	private Properties loadProperties(String... resourcesPaths) {
		Properties props = new Properties();
		for (String location : resourcesPaths) {
			InputStream is = null;
			try {
				is = getResourceAsInputStream(location);
				if(is==null){
					throw new RuntimeException("不能发现【"+location+"】资源文件!!");
				}else{
					//按UTF-8方式加载
					props.load(new InputStreamReader(is, "UTf-8"));					
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return props;
	}

	public InputStream getResourceAsInputStream(String filePath) {
		assert filePath != null : "filePath must not be null!";
		if (filePath.startsWith("/")) {
			return getDefaultClassLoader().getResourceAsStream(filePath.substring(1));
		} else if (filePath.startsWith("classpath:")) {
			return getResourceAsInputStream(filePath.substring("classpath:".length()));
		} else if (filePath.startsWith("classpath*:")) {
			return getResourceAsInputStream(filePath.substring("classpath*:".length()));
		}
		return getDefaultClassLoader().getResourceAsStream(filePath);
	}

	/**
	 * 获得ClassLoader对象
	 * 
	 * @return {@link ClassLoader}
	 */
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Exception ignored) {
			ignored.printStackTrace();
		}
		if (cl == null) {
			cl = PropLoader.class.getClassLoader();
			if (cl == null) {
				try {
					cl = ClassLoader.getSystemClassLoader();
				} catch (Exception ignored) {
					;
				}
			}
		}
		return cl;
	}
	
	/**
	 * 获取所有的key-value
	 * @param <K>
	 * @param <V>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <K, V> Map<K, V> getDatas(Class<K> k,Class<V> v){
		Map<K,V> map=new HashMap<K, V>();
		for (Map.Entry<Object,Object> entry:getProperties().entrySet()) {
			map.put((K)entry.getKey(), (V)entry.getValue());
		}
		return map;
	}
	
}