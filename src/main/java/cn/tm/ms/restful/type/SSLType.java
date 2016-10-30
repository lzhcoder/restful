package cn.tm.ms.restful.type;

public enum SSLType {
	
	/**
	 * 简单认证:不需要导入证书
	 */
	SIMPLE,
	
	/**
	 * 复杂认证:需要双边都导入证书
	 */
	COMPLEX;
	
}
