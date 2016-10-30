package cn.tm.ms.restful.service;

import cn.tm.ms.restful.annotation.RES;
import cn.tm.ms.restful.annotation.Serv;

@Serv
public class TestService {

	@RES
	public TestService testService;
	
	@RES
	public TestService testService1;
	
	public TestService service() {
		return testService;
	}
	
}
